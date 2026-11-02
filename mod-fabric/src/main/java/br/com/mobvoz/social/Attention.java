package br.com.mobvoz.social;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.Orders;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Atencao: a criatura com quem o jogador esta falando (ou prestes a falar) fica PARADA, virada para ele e chega perto (ate {@link #CLOSE} blocos), em vez de
 * seguir a vida e sair do alcance no meio da fala. Vale enquanto o cliente avisa que ela esta na mira, e por mais alguns segundos depois de cada fala dela.
 * Nao mexe em quem ja tem ordem (segue, guarda, defende), em quem luta, foge ou e hostil, nem em quem vive na agua ou no ar.
 */
public final class Attention {
    /** Quanto tempo a atencao dura sem novo aviso (o cliente reenvia a cada segundo enquanto a mira esta nela). */
    public static final long HOLD_TICKS = 70;
    /** Depois de falar com ela, continua parada o tempo da resposta e mais um pouco. */
    public static final long TALK_TICKS = 160;
    /** Distancia a partir da qual ela se aproxima, e ate onde se aproxima. */
    static final double FAR = 4.5;
    static final double CLOSE = 2.8;
    static final double MAX_RANGE = 9.0;

    private record Entry(UUID player, long until) {
    }

    private static final Map<UUID, Entry> active = new ConcurrentHashMap<>();

    private Attention() {
    }

    /** Quem o jogador acabou de mirar (o cliente avisa): a fala dentro desta janela vale para ela mesmo se o mouse ja saiu de cima. */
    private record Aimed(UUID mob, long until) {
    }

    private static final Map<UUID, Aimed> aimed = new ConcurrentHashMap<>();
    /** Folga depois do ultimo aviso do cliente (que reenvia a cada segundo enquanto a estrela esta nela, ate 5 s depois de a mira sair). */
    static final long AIM_GRACE_TICKS = 40;

    /** O cliente avisou que a criatura esta na mira (ou na folga da mira). Revalida alcance e linha de visao; vale tambem para quem nao ficaria parado (peixe, morcego, hostil). */
    public static void hold(ServerPlayer player, UUID mobId) {
        if (!(player.level() instanceof ServerLevel level) || !(level.getEntity(mobId) instanceof Mob mob) || !mob.isAlive()) {
            return;
        }
        if (mob.distanceTo(player) <= MAX_RANGE && player.hasLineOfSight(mob)) {
            aimed.put(player.getUUID(), new Aimed(mobId, level.getGameTime() + AIM_GRACE_TICKS));
        }
        if (ServerConfig.current().conversationAttention) {
            hold(mob, player, HOLD_TICKS);
        }
    }

    /** O jogador mirou nesta criatura ha pouco (dentro da folga)? Complementa a conferencia de mira exata do servidor. */
    public static boolean wasAimed(ServerPlayer player, Mob mob) {
        Aimed a = aimed.get(player.getUUID());
        return a != null && a.mob().equals(mob.getUUID()) && mob.level().getGameTime() < a.until() && mob.distanceTo(player) <= MAX_RANGE && player.hasLineOfSight(mob);
    }

    public static void hold(Mob mob, ServerPlayer player, long ticks) {
        if (!ServerConfig.current().conversationAttention || !eligible(mob) || mob.distanceTo(player) > MAX_RANGE || mob.level() != player.level()) {
            return;
        }
        long until = mob.level().getGameTime() + ticks;
        active.merge(mob.getUUID(), new Entry(player.getUUID(), until), (old, nw) -> nw.until() >= old.until() ? nw : old);
    }

    public static boolean isHolding(UUID mob) {
        return active.containsKey(mob);
    }

    /** Quem pode ser segurado: pacifico, andando, sem ordem, sem luta nem fuga. Aquaticos, voadores e hostis nao "ficam parados" para conversar. */
    static boolean eligible(Mob mob) {
        if (mob.isNoAi() || !(mob instanceof PathfinderMob) || !Orders.canWalkFollow(mob) || mob.isPassenger() || mob.isVehicle() || mob.isLeashed()) {
            return false;
        }
        MobOrders o = Orders.get(mob);
        if (o.mode() != MobOrders.Mode.NONE || o.defend() || Reactions.isActive(mob.getUUID())) {
            return false;
        }
        return mob.getTarget() == null && !(mob instanceof Enemy);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (active.isEmpty() || server.getTickCount() % 2 != 0) {
                return;
            }
            long now = server.overworld().getGameTime();
            for (var e : List.copyOf(active.entrySet())) {
                Mob mob = null;
                for (ServerLevel l : server.getAllLevels()) {
                    if (l.getEntity(e.getKey()) instanceof Mob m) {
                        mob = m;
                        break;
                    }
                }
                ServerPlayer p = server.getPlayerList().getPlayer(e.getValue().player());
                if (mob == null || !mob.isAlive() || p == null || now >= e.getValue().until() || p.level() != mob.level() || !eligible(mob) || mob.distanceTo(p) > MAX_RANGE + 3) {
                    active.remove(e.getKey());
                    continue;
                }
                face(mob, p);
            }
        });
    }

    private static void face(Mob mob, ServerPlayer p) {
        mob.getLookControl().setLookAt(p, 40f, 40f);
        double d = mob.distanceTo(p);
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET); // quem anda pelo cerebro (aldeao, cabra...) larga o destino
        if (d > CLOSE && (d > FAR || mob.getNavigation().isInProgress())) {
            mob.getNavigation().moveTo(p, 0.9); // chega perto, devagar
            return;
        }
        mob.getNavigation().stop();
    }
}
