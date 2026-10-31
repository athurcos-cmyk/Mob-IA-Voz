package br.com.mobvoz.social;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Conversa em andamento = ninguem ataca ninguem. Enquanto o jogador fala com uma criatura (e por {@link #GRACE_TICKS} depois da
 * ultima fala), ela nao o mira nem o fere, mesmo hostil e sem alianca. Acaba quando o jogador a fere ou a ameaca e ela decide lutar.
 */
public final class Parley {
    public static final long GRACE_TICKS = 600;
    private static final Map<UUID, Entry> active = new ConcurrentHashMap<>();

    private record Entry(UUID player, long until) {
    }

    private Parley() {
    }

    /** Server thread: o jogador falou com (ou foi respondido por) esta criatura. */
    public static void touch(Mob mob, ServerPlayer player) {
        active.put(mob.getUUID(), new Entry(player.getUUID(), mob.level().getGameTime() + GRACE_TICKS));
    }

    public static void end(Mob mob) {
        active.remove(mob.getUUID());
    }

    public static boolean protects(Mob mob, ServerPlayer player, long now) {
        Entry e = active.get(mob.getUUID());
        return e != null && e.player().equals(player.getUUID()) && now < e.until();
    }

    public static int activeCount() {
        return active.size();
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (active.isEmpty() || server.getTickCount() % 5 != 0) {
                return;
            }
            long now = server.overworld().getGameTime();
            for (var e : List.copyOf(active.entrySet())) {
                if (now >= e.getValue().until()) {
                    active.remove(e.getKey());
                    continue;
                }
                for (ServerLevel level : server.getAllLevels()) {
                    if (level.getEntity(e.getKey()) instanceof Mob m && m.isAlive()) {
                        if (m.getTarget() instanceof ServerPlayer tp && tp.getUUID().equals(e.getValue().player())) {
                            m.setTarget(null);
                            if (m instanceof NeutralMob n) {
                                n.stopBeingAngry();
                            }
                            if (m instanceof Creeper c) {
                                c.setSwellDir(-1);
                            }
                        }
                        break;
                    }
                }
            }
        });
    }
}
