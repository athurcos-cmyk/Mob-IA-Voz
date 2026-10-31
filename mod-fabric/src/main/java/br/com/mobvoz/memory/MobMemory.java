package br.com.mobvoz.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.core.UUIDUtil;

/**
 * Memoria persistente de UMA criatura (docs/12 e 04): identidade estavel, fatos VERIFICADOS (so o servidor cria),
 * alegacoes dos jogadores com a CRENCA da criatura, e relacao por jogador. Imutavel; cada mudanca devolve uma copia.
 * Fatos e alegacoes ficam em listas separadas de proposito: uma alegacao nunca vira fato.
 */
public record MobMemory(UUID identityId, List<Fact> facts, Map<String, Integer> counts, List<Claim> claims,
                        Map<UUID, Relationship> relations, Map<UUID, String> names) {
    public static final int MAX_FACTS = 12;
    public static final int MAX_CLAIMS = 8;
    public static final int MAX_CLAIM_TEXT = 140;

    public enum Stance { UNKNOWN, SUSPECTS, BELIEVES, DOUBTS, DISPROVED;
        static final Codec<Stance> CODEC = Codec.STRING.xmap(Stance::parse, Enum::name);

        public static Stance parse(String s) {
            try {
                return valueOf(s.toUpperCase(Locale.ROOT));
            } catch (RuntimeException e) {
                return UNKNOWN;
            }
        }
    }

    /** Evento verificado pelo servidor (nunca criado a partir de texto do LLM). */
    public record Fact(String type, UUID player, String detail, long tick) {
        static final Codec<Fact> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("type").forGetter(Fact::type),
                UUIDUtil.CODEC.fieldOf("player").forGetter(Fact::player),
                Codec.STRING.fieldOf("detail").forGetter(Fact::detail),
                Codec.LONG.fieldOf("tick").forGetter(Fact::tick)).apply(i, Fact::new));
    }

    /** O que um jogador DISSE + o que a criatura pensa disso. Nao e fato. */
    public record Claim(UUID id, UUID player, String text, long tick, Stance stance) {
        static final Codec<Claim> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Claim::id),
                UUIDUtil.CODEC.fieldOf("player").forGetter(Claim::player),
                Codec.STRING.fieldOf("text").forGetter(Claim::text),
                Codec.LONG.fieldOf("tick").forGetter(Claim::tick),
                Stance.CODEC.fieldOf("stance").forGetter(Claim::stance)).apply(i, Claim::new));
    }

    /** trust/affinity em [-1,1]; fear em [0,1]. */
    public record Relationship(float trust, float affinity, float fear) {
        public static final Relationship NEUTRAL = new Relationship(0, 0, 0);
        static final Codec<Relationship> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.fieldOf("trust").forGetter(Relationship::trust),
                Codec.FLOAT.fieldOf("affinity").forGetter(Relationship::affinity),
                Codec.FLOAT.fieldOf("fear").forGetter(Relationship::fear)).apply(i, Relationship::new));

        public Relationship {
            trust = Math.max(-1f, Math.min(1f, trust));
            affinity = Math.max(-1f, Math.min(1f, affinity));
            fear = Math.max(0f, Math.min(1f, fear));
        }

        public Relationship plus(float dTrust, float dAffinity, float dFear) {
            return new Relationship(trust + dTrust, affinity + dAffinity, fear + dFear);
        }
    }

    /** Sem nomes (memorias antigas e testes): a criatura ainda nao sabe o nome de ninguem. */
    public MobMemory(UUID identityId, List<Fact> facts, Map<String, Integer> counts, List<Claim> claims, Map<UUID, Relationship> relations) {
        this(identityId, facts, counts, claims, relations, Map.of());
    }

    public static final Codec<MobMemory> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("identity").forGetter(MobMemory::identityId),
            Fact.CODEC.listOf().fieldOf("facts").forGetter(MobMemory::facts),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("counts").forGetter(MobMemory::counts),
            Claim.CODEC.listOf().fieldOf("claims").forGetter(MobMemory::claims),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Relationship.CODEC).fieldOf("relations").forGetter(MobMemory::relations),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING).optionalFieldOf("names", Map.of()).forGetter(MobMemory::names))
            .apply(i, MobMemory::new));

    public static MobMemory create(UUID identityId) {
        return new MobMemory(identityId, List.of(), Map.of(), List.of(), Map.of());
    }

    public Relationship relationOf(UUID player) {
        return relations.getOrDefault(player, Relationship.NEUTRAL);
    }

    /** Nome que ESTA criatura aprendeu do jogador (cada criatura sabe, ou nao, por conta propria); vazio = so "Humano". */
    public String nameOf(UUID player) {
        return names.getOrDefault(player, "");
    }

    public MobMemory withName(UUID player, String name) {
        Map<UUID, String> nn = new HashMap<>(names);
        nn.put(player, name);
        return new MobMemory(identityId, facts, counts, claims, relations, Map.copyOf(nn));
    }

    public boolean knows(UUID player) {
        return relations.containsKey(player);
    }

    /** Acrescenta um fato; so os MAX_FACTS mais recentes ficam detalhados, os totais ficam em {@code counts}. */
    public MobMemory withFact(Fact f) {
        List<Fact> nf = new ArrayList<>(facts);
        nf.add(f);
        while (nf.size() > MAX_FACTS) {
            nf.remove(0);
        }
        Map<String, Integer> nc = new HashMap<>(counts);
        nc.merge(f.type(), 1, Integer::sum);
        return new MobMemory(identityId, List.copyOf(nf), Map.copyOf(nc), claims, relations, names);
    }

    public MobMemory withRelation(UUID player, UnaryOperator<Relationship> change) {
        Map<UUID, Relationship> nr = new HashMap<>(relations);
        nr.put(player, change.apply(relationOf(player)));
        return new MobMemory(identityId, facts, counts, claims, Map.copyOf(nr), names);
    }

    /** Registra uma alegacao (dedupe por jogador+texto); ao lotar, descarta a mais "morta" primeiro. */
    public MobMemory withClaim(Claim c) {
        String norm = c.text().toLowerCase(Locale.ROOT);
        for (Claim old : claims) {
            if (old.player().equals(c.player()) && old.text().toLowerCase(Locale.ROOT).equals(norm)) {
                return withStance(claims.indexOf(old), c.stance());
            }
        }
        List<Claim> nc = new ArrayList<>(claims);
        nc.add(c);
        while (nc.size() > MAX_CLAIMS) {
            int drop = indexOfStance(nc, Stance.DISPROVED);
            if (drop < 0) {
                drop = indexOfStance(nc, Stance.UNKNOWN);
            }
            nc.remove(Math.max(drop, 0));
        }
        return new MobMemory(identityId, facts, counts, List.copyOf(nc), relations, names);
    }

    public MobMemory withStance(int index, Stance s) {
        if (index < 0 || index >= claims.size()) {
            return this;
        }
        List<Claim> nc = new ArrayList<>(claims);
        Claim c = nc.get(index);
        nc.set(index, new Claim(c.id(), c.player(), c.text(), c.tick(), s));
        return new MobMemory(identityId, facts, counts, List.copyOf(nc), relations, names);
    }

    private static int indexOfStance(List<Claim> list, Stance s) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).stance() == s) {
                return i;
            }
        }
        return -1;
    }
}
