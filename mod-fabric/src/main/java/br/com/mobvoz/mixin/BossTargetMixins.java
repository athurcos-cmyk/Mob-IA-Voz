package br.com.mobvoz.mixin;

import br.com.mobvoz.orders.Orders;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Warden e Wither aliados so atacam quem e hostil ao dono ({@link Orders#holdsFireAt}). O Warden escolhe alvos pela raiva
 * ({@code canTargetEntity} filtra o que entra na lista de raiva); o Wither e os outros pelo {@code canAttack} (mira, cabecas laterais e o
 * seletor de alvos). Um mixin por origem; ambos so olham para esses dois chefes e saem na primeira linha para os demais.
 */
public final class BossTargetMixins {
    private BossTargetMixins() {
    }

    @Mixin(Warden.class)
    public abstract static class Warden_ {
        @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
        private void mobvoz$allyHoldsFire(Entity target, CallbackInfoReturnable<Boolean> cir) {
            if (Orders.holdsFireAt((Mob) (Object) this, target)) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * O comportamento de escavar do Warden REMOVE a criatura do mundo quando termina ou e interrompido ({@code stop}). Se o pacto chega com ele ja
     * escavando, ele sumiria com o aliado dentro; sob pacto o Warden fica de pe.
     */
    @Mixin(net.minecraft.world.entity.ai.behavior.warden.Digging.class)
    public abstract static class Digging_ {
        @Inject(method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)V", at = @At("HEAD"), cancellable = true)
        private void mobvoz$allyDoesNotDigAway(net.minecraft.server.level.ServerLevel level, Warden warden, long time,
                                               org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
            if (Orders.underPact(warden)) {
                warden.setPose(net.minecraft.world.entity.Pose.STANDING);
                ci.cancel();
            }
        }
    }

    @Mixin(LivingEntity.class)
    public abstract static class Living_ {
        @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
        private void mobvoz$allyBossHoldsFire(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
            if ((Object) this instanceof Mob m && Orders.holdsFireAt(m, target)) {
                cir.setReturnValue(false);
            }
        }
    }
}
