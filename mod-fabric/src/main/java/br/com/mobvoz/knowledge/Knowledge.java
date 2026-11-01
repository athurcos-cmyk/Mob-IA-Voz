package br.com.mobvoz.knowledge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/**
 * Conhecimento COMPARTILHADO do mundo (fase 11, imutavel/puro): memoria coletiva por faccao (ex.: endermen lembram de quem
 * atacou um deles) e o registro historico verificado que o Ender Dragon pode "narrar". So eventos que o servidor
 * comprovou; nada inventado pelo LLM entra aqui.
 */
public record Knowledge(Map<String, List<Event>> factions, List<Event> history) {
    public static final int FACTION_CAP = 16;
    public static final int HISTORY_CAP = 64;
    private static final int DEDUPE_TICKS = 40;
    public static final Knowledge EMPTY = new Knowledge(Map.of(), List.of());

    public record Event(String type, UUID player, String detail, long tick) {
        static final Codec<Event> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("type").forGetter(Event::type),
                UUIDUtil.CODEC.fieldOf("player").forGetter(Event::player),
                Codec.STRING.fieldOf("detail").forGetter(Event::detail),
                Codec.LONG.fieldOf("tick").forGetter(Event::tick)).apply(i, Event::new));
    }

    public static final Codec<Knowledge> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Codec.STRING, Event.CODEC.listOf()).fieldOf("factions").forGetter(Knowledge::factions),
            Event.CODEC.listOf().fieldOf("history").forGetter(Knowledge::history)).apply(i, Knowledge::new));

    private static boolean dup(List<Event> l, Event e) {
        for (Event o : l) {
            if (o.type().equals(e.type()) && o.player().equals(e.player()) && o.detail().equals(e.detail())
                    && Math.abs(e.tick() - o.tick()) < DEDUPE_TICKS) {
                return true;
            }
        }
        return false;
    }

    public Knowledge withFaction(String faction, Event e) {
        List<Event> cur = new ArrayList<>(factions.getOrDefault(faction, List.of()));
        if (dup(cur, e)) {
            return this;
        }
        cur.add(e);
        while (cur.size() > FACTION_CAP) {
            cur.remove(0);
        }
        Map<String, List<Event>> nf = new HashMap<>(factions);
        nf.put(faction, List.copyOf(cur));
        return new Knowledge(Map.copyOf(nf), history);
    }

    public Knowledge withHistory(Event e) {
        if (dup(history, e)) {
            return this;
        }
        List<Event> nh = new ArrayList<>(history);
        nh.add(e);
        while (nh.size() > HISTORY_CAP) {
            nh.remove(0);
        }
        return new Knowledge(factions, List.copyOf(nh));
    }

    /** Cada ataque pesa 0.15 e vai sendo esquecido: pela metade a cada 30 min de jogo (36000 ticks). */
    static final double FORGIVE_HALF_LIFE_TICKS = 36000;

    /** Quanto a faccao desconfia deste jogador por ataques a seus membros (0 .. 0.6), contando o perdao do tempo. */
    public float trustPenalty(String faction, UUID player, long now) {
        double sum = factions.getOrDefault(faction, List.of()).stream()
                .filter(e -> e.type().equals("ATTACKED_MEMBER") && e.player().equals(player))
                .mapToDouble(e -> Math.pow(0.5, Math.max(0, now - e.tick()) / FORGIVE_HALF_LIFE_TICKS)).sum();
        return (float) Math.min(0.6, 0.15 * sum);
    }

    /** Sem a hora: conta todos (testes e usos antigos). */
    public float trustPenalty(String faction, UUID player) {
        return trustPenalty(faction, player, 0);
    }

    public List<String> factionLines(String faction, UUID speaker, long now) {
        List<String> out = new ArrayList<>();
        long mine = factions.getOrDefault(faction, List.of()).stream().filter(e -> e.type().equals("ATTACKED_MEMBER") && e.player().equals(speaker)).count();
        boolean attackedShown = false;
        for (Event e : factions.getOrDefault(faction, List.of())) {
            if (e.type().equals("ATTACKED_MEMBER")) {
                if (e.player().equals(speaker)) {
                    if (!attackedShown) { // um resumo so, proporcional: um golpe pode ter sido sem querer
                        attackedShown = true;
                        out.add("Todos os da sua espécie sabem: o jogador que fala com você bateu em um de vocês " + (mine == 1
                                ? "uma vez (" + age(now - e.tick()) + "); pode ter sido sem querer, e vocês não o odeiam por isso."
                                : mine + " vezes (a última " + age(now - factions.get(faction).stream().filter(x -> x.type().equals("ATTACKED_MEMBER") && x.player().equals(speaker)).mapToLong(Event::tick).max().orElse(e.tick())) + ")."));
                    }
                } else {
                    out.add("Todos os da sua espécie sabem: outro jogador atacou um de vocês (" + age(now - e.tick()) + ").");
                }
            } else if (e.type().equals("HONEST_RETURN")) {
                String who = e.player().equals(speaker) ? "o jogador que fala com você" : "outro jogador";
                out.add("Todos os da sua espécie sabem: " + who + " devolveu a um de vocês, sem ser pedido, o que não era dele (" + age(now - e.tick()) + ").");
            }
        }
        return out;
    }

    /** Historico verificado do mundo (para criaturas que o conhecem, como o Ender Dragon). Nada alem do que esta no registro. */
    public List<String> historyLines(UUID speaker, long now) {
        List<String> out = new ArrayList<>();
        for (Event e : history) {
            String who = e.player().equals(speaker) ? "o jogador que fala com você" : "outro jogador";
            String when = age(now - e.tick());
            String cap = who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1);
            switch (e.type()) {
                case "PLAYER_DEATH" -> out.add("Registro do mundo: " + who + " morreu (" + when + ").");
                case "BOSS_DEFEATED" -> out.add("Registro do mundo: " + who + " derrotou " + e.detail() + " (" + when + ").");
                case "HONEST_RETURN" -> out.add("Registro do mundo: " + who + " devolveu honestamente, sem ser pedido, o que não era dele a " + e.detail() + " (" + when + ").");
                case "ENTERED_DIMENSION" -> out.add("Registro do mundo: " + who + " entrou em " + e.detail() + " (" + when + ").");
                default -> { }
            }
        }
        return out;
    }

    static String age(long ticks) {
        long min = ticks / 1200;
        return min < 1 ? "há instantes" : min < 120 ? "há cerca de " + min + " min de jogo" : "há cerca de " + min / 60 + " h de jogo";
    }
}
