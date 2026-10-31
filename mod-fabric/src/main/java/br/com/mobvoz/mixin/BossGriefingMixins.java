package br.com.mobvoz.mixin;

import br.com.mobvoz.orders.Orders;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fase 12: chefe sob contrato (aliado ou em tregua) nao destroi blocos ({@link Orders#restrictsGriefing}). Um mixin por origem de destruicao. */
public final class BossGriefingMixins {
    private BossGriefingMixins() {
    }

    /** O Wither quebra blocos em volta quando ferido (contagem {@code destroyBlocksTick}); zerada a cada tick sob contrato. */
    @Mixin(WitherBoss.class)
    public abstract static class Wither {
        @Shadow
        private int destroyBlocksTick;

        @Inject(method = "customServerAiStep", at = @At("HEAD"))
        private void mobvoz$noBlockBreaking(CallbackInfo ci) {
            if (Orders.restrictsGriefing((Mob) (Object) this)) {
                this.destroyBlocksTick = 0;
            }
        }
    }

    /** A explosao da caveira do Wither passa a nao ferir blocos quando disparada por um Wither sob contrato (o dano a criaturas continua). */
    @Mixin(WitherSkull.class)
    public abstract static class Skull {
        @ModifyArg(method = "onHit", at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"), index = 6)
        private Level.ExplosionInteraction mobvoz$noBlockDamage(Level.ExplosionInteraction original) {
            return ((WitherSkull) (Object) this).getOwner() instanceof Mob m && Orders.restrictsGriefing(m) ? Level.ExplosionInteraction.NONE : original;
        }
    }

    /** O Dragao quebra blocos ao atravessar paredes ({@code checkWalls}); sob contrato ele passa sem destruir. */
    @Mixin(EnderDragon.class)
    public abstract static class Dragon {
        @Inject(method = "checkWalls", at = @At("HEAD"), cancellable = true)
        private void mobvoz$noWallBreaking(net.minecraft.server.level.ServerLevel level, AABB box, CallbackInfoReturnable<Boolean> cir) {
            if (Orders.restrictsGriefing((Mob) (Object) this)) {
                cir.setReturnValue(false);
            }
        }
    }
}
