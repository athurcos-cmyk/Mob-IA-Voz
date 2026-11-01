package br.com.mobvoz.mixin;

import br.com.mobvoz.trade.Trades;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quem negocia com o jogador (oferta aberta ou troca em andamento) nunca passa a mirar nele, nem por um tick: o servidor ja limpava a mira a cada 10 ticks,
 * mas um hostil (aranha, zumbi) podia voltar a mirar entre uma limpeza e outra. Aqui a mira e barrada na origem.
 */
@Mixin(Mob.class)
public abstract class NegotiationTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void mobvoz$noTargetWhileNegotiating(LivingEntity target, CallbackInfo ci) {
        if (target instanceof ServerPlayer p && Trades.negotiatingWith((Mob) (Object) this, p, p.level().getGameTime())) {
            ci.cancel();
        }
    }
}
