package br.com.mobvoz.social;

import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Lore;
import br.com.mobvoz.profile.Lore.Threat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * Reacao NATURAL a como o jogador fala (ameaca/consolo): o porco que ouve "vou te transformar em comida" foge; o zumbi avanca;
 * o Warden nao liga. Quem decide e a natureza da criatura (lore.json) + o servidor; o LLM so reconheceu o tom. Duracoes curtas.
 */
public final class Reactions {
    public enum Reaction { FLEE, FIGHT, UNMOVED, FROZEN, CALMED, NONE }

    private static final long FLEE_TICKS = 200;
    private static final long CHASE_TICKS = 200;

    private record Chase(UUID player, long until, boolean attack) {
    }

    private static final Map<UUID, Chase> active = new ConcurrentHashMap<>();

    private Reactions() {
    }

    /** PURA. @param social "THREAT" | "COMFORT" | outro; @param protectedByBond aliado/dono/trégua: nao foge nem ataca quem o protege. */
    public static Reaction decide(Threat nature, String social, boolean canMove, boolean protectedByBond, boolean boss) {
        if ("COMFORT".equals(social)) {
            return Reaction.CALMED;
        }
        if (!"THREAT".equals(social)) {
            return Reaction.NONE;
        }
        if (boss || protectedByBond || nature == Threat.UNMOVED) {
            return Reaction.UNMOVED;
        }
        if (!canMove) {
            return Reaction.FROZEN; // assustado ou furioso, mas sem adaptador/liberdade de movimento: so narrativa
        }
        return nature == Threat.FLEE ? Reaction.FLEE : Reaction.FIGHT;
    }

    /** A reacao decidida e o resultado que vai para a fala. */
    public record Outcome(Reaction reaction, Map<String, String> result) {
    }

    /** Server thread: aplica a reacao (fisica + memoria) e devolve o resultado para a fala. */
    public static Map<String, String> react(Mob mob, ServerPlayer player, String social) {
        Outcome o = reactOutcome(mob, player, social);
        return o == null ? null : o.result();
    }

    /** Ela se acalma (cedeu ao pedido sob ameaca): para de fugir ou de perseguir. */
    public static void calm(Mob mob) {
        active.remove(mob.getUUID());
        if (mob.getTarget() instanceof ServerPlayer) {
            mob.setTarget(null);
        }
    }

    public static Outcome reactOutcome(Mob mob, ServerPlayer player, String social) {
        if (!"THREAT".equals(social) && !"COMFORT".equals(social)) {
            return null;
        }
        var lore = Lore.forMob(mob);
        long now = player.level().getGameTime();
        MobOrders o = Orders.get(mob);
        String pid = player.getUUID().toString();
        boolean bond = o.owner().equals(pid) && (o.mode() != MobOrders.Mode.NONE || o.defend()) || (o.truceFor().equals(pid) && now < o.truceUntil());
        boolean canMove = mob instanceof PathfinderMob && !mob.isNoAi() && Orders.canWalkFollow(mob);
        Reaction r = decide(lore.ameaca(), social, canMove, bond, Orders.subjectOf(mob).boss());
        UUID id = player.getUUID();
        switch (r) {
            case FLEE -> active.put(mob.getUUID(), new Chase(id, now + FLEE_TICKS, false));
            case FIGHT -> {
                Parley.end(mob); // ameacada de verdade, ela decide lutar: a conversa acabou
                active.put(mob.getUUID(), new Chase(id, now + CHASE_TICKS, true));
                mob.setTarget(player);
            }
            case CALMED -> active.remove(mob.getUUID());
            default -> { }
        }
        if (social.equals("THREAT")) {
            MemoryStore.update(mob, m -> MemoryRules.threatened(m, id, now));
        } else {
            MemoryStore.update(mob, m -> MemoryRules.comforted(m, id, now));
        }
        String res = switch (r) {
            case FLEE -> "FOGE";
            case FIGHT -> "ENFURECE";
            case UNMOVED -> "IMPASSIVEL";
            case FROZEN -> "CONGELA";
            case CALMED -> "ACALMA";
            case NONE -> "NADA";
        };
        String why = switch (r) {
            case FLEE, FIGHT, UNMOVED -> lore.reacao();
            case FROZEN -> "queria reagir (" + lore.reacao() + "), mas não consegue se mover";
            case CALMED -> "o jogador a acalmou; ela relaxa";
            case NONE -> "";
        };
        return new Outcome(r, Map.of("pedido", "REACAO", "resultado", res, "motivo", why));
    }

    public static boolean isActive(UUID mob) {
        return active.containsKey(mob);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (active.isEmpty() || server.getTickCount() % 5 != 0) {
                return;
            }
            for (var e : List.copyOf(active.entrySet())) {
                Mob mob = null;
                for (ServerLevel l : server.getAllLevels()) {
                    if (l.getEntity(e.getKey()) instanceof Mob m) {
                        mob = m;
                        break;
                    }
                }
                ServerPlayer p = server.getPlayerList().getPlayer(e.getValue().player());
                if (mob == null || !mob.isAlive() || p == null || mob.level().getGameTime() >= e.getValue().until() || p.level() != mob.level()) {
                    if (mob == null || !mob.isAlive() || p == null || mob.level().getGameTime() >= e.getValue().until()) {
                        active.remove(e.getKey());
                    }
                    continue;
                }
                if (e.getValue().attack()) {
                    if (mob.getTarget() != p) {
                        mob.setTarget(p);
                    }
                } else if (mob instanceof PathfinderMob pm && mob.distanceTo(p) < 18) {
                    Vec3 away = LandRandomPos.getPosAway(pm, 14, 7, p.position());
                    if (away != null) {
                        mob.getNavigation().moveTo(away.x, away.y, away.z, 1.5);
                    }
                }
            }
        });
    }
}
