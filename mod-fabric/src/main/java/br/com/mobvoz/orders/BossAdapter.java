package br.com.mobvoz.orders;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.phys.Vec3;

/**
 * Fase 12: adaptadores de movimento/combate dos chefes (thread do servidor). Warden (IA por "brain") e Wither (voador) acompanham;
 * o Dragao so existe no Fim e nao tem navegacao (so luta e trégua). Cada chefe tem um jeito proprio de mirar e de baixar a guarda.
 */
public final class BossAdapter {
    private static final float FOLLOW_SPEED = 1.2f;

    private BossAdapter() {
    }

    public static boolean isBoss(Mob m) {
        return m instanceof WitherBoss || m instanceof EnderDragon || m instanceof Warden || m instanceof ElderGuardian;
    }

    /** Chefes com adaptador de movimento (seguir/ficar): o Warden (brain) e o Wither (voo). */
    public static boolean canFollow(Mob m) {
        return m instanceof Warden || m instanceof WitherBoss;
    }

    public static void moveToEntity(Mob m, LivingEntity target) {
        if (m instanceof Warden w) {
            w.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, FOLLOW_SPEED, 2));
        } else {
            m.getNavigation().moveTo(target, FOLLOW_SPEED);
        }
    }

    public static void moveToPos(Mob m, double x, double y, double z) {
        if (m instanceof Warden w) {
            w.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new Vec3(x, y, z), 1.0f, 1));
        } else {
            m.getNavigation().moveTo(x, y, z, 1.0);
        }
    }

    public static void stopMoving(Mob m) {
        if (m instanceof Warden w) {
            w.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
        m.getNavigation().stop();
    }

    /** Manda o chefe atacar {@code target} do jeito dele (Mob#setTarget nao serve ao Warden nem ao Dragao). */
    public static void attack(Mob m, LivingEntity target) {
        if (m instanceof Warden w) {
            w.increaseAngerAt(target, 150, false);
            w.setAttackTarget(target);
        } else if (m instanceof EnderDragon d) {
            d.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
            DragonChargePlayerPhase charge = d.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER);
            charge.setTarget(target.position());
        } else {
            m.setTarget(target);
        }
    }

    /** Sem alvo e sem DIG_COOLDOWN o Warden escava e some (setDigCooldown so renova um cooldown ja existente); sob contrato ele fica. */
    public static void keepAboveGround(Warden w) {
        w.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, WardenAi.DIGGING_COOLDOWN);
    }

    /** O chefe deixa de mirar {@code p} (trégua): apaga raiva/alvo/investida. Idempotente. */
    public static void pacify(Mob m, ServerPlayer p) {
        if (m instanceof Warden w) {
            keepAboveGround(w);
            w.clearAnger(p);
            if (w.getTarget() == p) {
                w.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
        } else if (m instanceof EnderDragon d) {
            EnderDragonPhase<?> cur = d.getPhaseManager().getCurrentPhase().getPhase();
            if (cur == EnderDragonPhase.CHARGING_PLAYER || cur == EnderDragonPhase.STRAFE_PLAYER) {
                d.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            }
        } else if (m instanceof WitherBoss w) {
            for (int i = 0; i < 3; i++) {
                if (w.getAlternativeTarget(i) == p.getId()) {
                    w.setAlternativeTarget(i, 0);
                }
            }
            if (w.getTarget() == p) {
                w.setTarget(null);
            }
        } else if (m.getTarget() == p) {
            m.setTarget(null);
        }
    }
}
