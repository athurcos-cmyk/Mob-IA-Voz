package br.com.mobvoz.social;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.profile.Mentions;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.social.AmbientScenes.Situation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.AABB;

/**
 * Reacoes ao MUNDO, sem IA e sem custo: um creeper explode perto, cai um raio, o jogador acorda, volta de uma morte, mata um monstro
 * perto da vila, cai de muito alto, bate numa criatura (o "Ai!"), da um presente... e os avisos de perigo, cumprimentos e ovos.
 * Cada acontecimento vira uma tag {@code ev:...} e a fala sai das familias {@code ev_*} de {@link AmbientScenes} (muitas variacoes por
 * acontecimento, com historico para nao repetir). Uma criatura fala sozinha ou, se houver outra perto, as duas conversam.
 */
public final class WorldReactions {
    private static final Random RND = new Random();
    private static final int RECENT = 120;
    private static final ArrayDeque<String> recent = new ArrayDeque<>();
    private static final Map<String, Long> last = new ConcurrentHashMap<>();
    private static final AtomicInteger fired = new AtomicInteger();
    private static final List<Pending> pending = new ArrayList<>();

    private record Pending(long due, Runnable run) {
    }

    private WorldReactions() {
    }

    /** Quantas reacoes comecaram nesta sessao (testes e diagnostico). */
    public static int fired() {
        return fired.get();
    }

    public static void reset() {
        recent.clear();
        last.clear();
        pending.clear();
    }

    /** Puro: passou o tempo de espera desde a ultima vez? */
    static boolean due(Long lastTick, long now, long cooldown) {
        return lastTick == null || now - lastTick >= cooldown;
    }

    /** Uma criatura especifica reage sozinha (quem apanhou, quem recebeu o presente, o aliado que avisa...). */
    public static boolean speakAs(ServerPlayer p, Mob speaker, String event, String thing, long cooldown) {
        return play(p, speaker, null, event, thing, "m:" + speaker.getUUID() + ":" + event, cooldown);
    }

    /**
     * A criatura puxa assunto com o jogador (familias ev_puxa_*; ver {@link Initiative}). A abertura vai para o historico da conversa deles, e a
     * resposta do jogador e tratada com esse contexto. Cada criatura espera 20 min para abrir outra vez.
     */
    static boolean opener(ServerPlayer p, Mob speaker) {
        return play(p, speaker, null, "ev:puxa", "", "puxa:" + speaker.getUUID(), 24000);
    }

    /** Quem estiver perto do ponto reage (uma criatura, ou duas que conversem). */
    static boolean around(ServerPlayer p, ServerLevel level, double x, double y, double z, double radius, Predicate<Mob> who, String event, String thing,
                          long cooldown) {
        String key = "p:" + p.getUUID() + ":" + event;
        if (!due(last.get(key), level.getGameTime(), cooldown)) {
            return false;
        }
        List<Mob> near = new ArrayList<>(level.getEntitiesOfClass(Mob.class, new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius),
                m -> m.isAlive() && !(m instanceof Enemy) && p.distanceTo(m) <= 24 && !ConversationService.isBusy(m.getUUID()) && who.test(m)));
        if (near.isEmpty()) {
            return false;
        }
        Collections.shuffle(near, RND);
        Mob a = near.get(0);
        Mob b = null;
        for (Mob m : near) {
            if (m != a && a.distanceTo(m) <= 6 && Ambient.canChat(Ambient.classesOf(a), Ambient.classesOf(m), a.getType() == m.getType())) {
                b = m;
                break;
            }
        }
        return play(p, a, b, event, thing, key, cooldown);
    }

    private static boolean play(ServerPlayer p, Mob a, Mob b, String event, String thing, String key, long cooldown) {
        if (!ServerConfig.current().naturalEvents || ConversationService.activeSessions() > 0 || !(a.level() instanceof ServerLevel level)
                || !a.isAlive() || ConversationService.isBusy(a.getUUID())) {
            return false;
        }
        long now = level.getGameTime();
        if (!due(last.get(key), now, cooldown)) {
            return false;
        }
        Situation base = Ambient.situation(level, p, a, b == null ? a : b);
        Set<String> tags = new HashSet<>(base.tags());
        tags.add(event);
        var pick = AmbientScenes.pick(new Situation(tags, base.classesA(), base.classesB(), base.nameA(), base.nameB(), base.item(), base.armor(), thing == null ? "" : thing),
                RND, recent::contains, id -> id.startsWith("ev_"));
        if (pick.isEmpty()) {
            return false;
        }
        var scene = pick.get();
        last.put(key, now);
        recent.addLast(scene.key());
        while (recent.size() > RECENT) {
            recent.pollFirst();
        }
        List<Mob> actors = b == null ? List.of(a, a) : List.of(a, b);
        if (!scene.name().isEmpty()) { // o nome foi dito em voz alta: quem estava perto o aprende
            for (Mob m : actors) {
                if (!MemoryStore.get(m).nameOf(p.getUUID()).equals(scene.name())) {
                    MemoryStore.update(m, mm -> mm.withName(p.getUUID(), scene.name()));
                }
            }
        }
        fired.incrementAndGet();
        if (event.equals("ev:puxa")) { // a criatura abriu a conversa: a fala entra no historico, para a resposta do jogador ter contexto
            ConversationService.rememberOpening(a, p, scene.lines().get(0).text());
        }
        Ambient.play(actors, p, b == null ? scene.lines().subList(0, 1) : scene.lines(), 0, true);
        return true;
    }

    private static void later(long due, Runnable run) {
        pending.add(new Pending(due, run));
    }

    private static ServerPlayer nearestPlayer(Entity e, double range) {
        return e.level().getNearestPlayer(e, range) instanceof ServerPlayer sp ? sp : null;
    }

    private static boolean villageFolk(Mob m) {
        return m instanceof AbstractVillager || m instanceof IronGolem;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long tick = server.getTickCount();
            for (var it = pending.iterator(); it.hasNext(); ) {
                var pd = it.next();
                if (tick >= pd.due()) {
                    it.remove();
                    pd.run().run();
                }
            }
            if (tick % 40 == 0) { // saqueadores perto de gente da vila
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    if (p.level() instanceof ServerLevel level) {
                        var raiders = level.getEntitiesOfClass(Raider.class, p.getBoundingBox().inflate(28), r -> r.isAlive() && !(r instanceof net.minecraft.world.entity.monster.Witch)); // bruxa sozinha nao e saque
                        if (!raiders.isEmpty()) {
                            Raider r = raiders.get(0);
                            around(p, level, r.getX(), r.getY(), r.getZ(), 24, WorldReactions::villageFolk, "ev:raiders", Mentions.labelOf(typeId(r)), 1200);
                        }
                    }
                }
            }
        });

        // creeper que explodeu (removido "descartado" a menos de 24 blocos de um jogador: longe disso nem existe despawn)
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Creeper c && c.getRemovalReason() == Entity.RemovalReason.DISCARDED) {
                ServerPlayer p = nearestPlayer(c, 24);
                if (p != null) {
                    around(p, level, c.getX(), c.getY(), c.getZ(), 20, m -> true, "ev:boom", "", 300);
                }
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof LightningBolt bolt) {
                ServerPlayer p = nearestPlayer(bolt, 40);
                if (p != null) {
                    around(p, level, bolt.getX(), bolt.getY(), bolt.getZ(), 24, m -> true, "ev:thunder", "", 300);
                }
            }
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, pos) -> {
            if (entity instanceof ServerPlayer p && p.level() instanceof ServerLevel level && level.getDayTime() % 24000 < 2500) {
                around(p, level, p.getX(), p.getY(), p.getZ(), 16, m -> true, "ev:wake", "", 2400);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive && newPlayer.level() instanceof ServerLevel level) {
                var pid = newPlayer.getUUID();
                later(level.getServer().getTickCount() + 60, () -> around(newPlayer, level, newPlayer.getX(), newPlayer.getY(), newPlayer.getZ(), 16,
                        m -> MemoryStore.get(m).knows(pid), "ev:revive", "", 1200));
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof Enemy && source.getEntity() instanceof ServerPlayer killer && entity.level() instanceof ServerLevel level) {
                around(killer, level, entity.getX(), entity.getY(), entity.getZ(), 20, WorldReactions::villageFolk, "ev:thanks", Mentions.labelOf(typeId(entity)), 400);
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (taken <= 0 || !entity.isAlive()) {
                return;
            }
            if (entity instanceof ServerPlayer p && taken >= 6 && source.is(DamageTypeTags.IS_FALL) && p.level() instanceof ServerLevel level) {
                around(p, level, p.getX(), p.getY(), p.getZ(), 12, m -> true, "ev:fall", "", 400);
            } else if (entity instanceof Mob m && !(m instanceof Enemy) && source.getEntity() instanceof ServerPlayer p && RND.nextDouble() < 0.7) {
                speakAs(p, m, "ev:hit", "", 160); // o "Ai!" de quem apanhou
            }
        });
    }

    private static String typeId(Entity e) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
    }
}
