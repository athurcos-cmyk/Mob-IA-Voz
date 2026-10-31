package br.com.mobvoz.mixin;

import br.com.mobvoz.orders.Orders;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** PORTAL_HOLD: criatura com ordem de nao entrar em portais nao os atravessa (nem por empurrao, nem seguindo o dono). */
@Mixin(Entity.class)
public abstract class EntityPortalMixin {
    @Inject(method = "canUsePortal", at = @At("HEAD"), cancellable = true)
    private void mobvoz$portalHold(boolean allowPassengers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Mob mob && Orders.portalHeld(mob)) {
            cir.setReturnValue(false);
        }
    }
}
