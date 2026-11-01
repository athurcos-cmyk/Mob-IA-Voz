package br.com.mobvoz.memory;

import br.com.mobvoz.profile.Profiles;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** Eventos do mundo que viram FATOS VERIFICADOS na memoria (presente e agressao). */
public final class MemoryEvents {
    private MemoryEvents() {
    }

    public static void register() {
        // Presente: SHIFT + botao direito com um item na mao principal. Consome 1 (nada e criado), fora do criativo.
        // Vanilla preservado onde SHIFT+clique ja tem funcao (pets, cavalos). Aldeoes aceitam presente com SHIFT (sem SHIFT: comercio normal).
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || !(entity instanceof Mob mob)
                    || mob instanceof TamableAnimal || mob instanceof AbstractHorse
                    || player.getItemInHand(hand).isEmpty() || player.isSpectator()) {
                return InteractionResult.PASS;
            }
            if (player instanceof ServerPlayer sp) {
                var stack = sp.getItemInHand(hand);
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                long tick = sp.level().getGameTime();
                var prof = Profiles.of(Profiles.keyOf(mob));
                MemoryStore.update(mob, m -> MemoryRules.gift(m, sp.getUUID(), id, 1, tick, prof.likeFactor(id), prof.reaction(id)));
                if (!sp.isCreative()) {
                    stack.shrink(1);
                }
                String reaction = prof.reaction(id);
                sp.displayClientMessage(Component.literal("Você deu 1x " + stack.getHoverName().getString() + ": " + reaction + ". ")
                        .append(br.com.mobvoz.server.TrustBar.text(mob, sp.getUUID())), true);
                String thing = br.com.mobvoz.trade.Values.known(id) ? br.com.mobvoz.trade.Values.name(id, false) : "isso";
                br.com.mobvoz.social.WorldReactions.speakAs(sp, mob, reaction.equals("gostou") ? "ev:gift_like" : reaction.equals("indiferente") ? "ev:gift_meh"
                        : "ev:gift_dislike", thing, 0); // a criatura fala do presente (fixa, sem IA)
            }
            return InteractionResult.SUCCESS;
        });

        // Zumbi-aldeao curado (e outras conversoes): a nova entidade herda identidade, fatos, alegacoes e ordens, com um fato de
        // conversao. Sem esse evento (ex.: outra entidade qualquer) nada e herdado.
        ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, conversionParams) -> {
            MobMemory old = previous.getAttached(MemoryStore.TYPE);
            if (old != null) {
                String from = BuiltInRegistries.ENTITY_TYPE.getKey(previous.getType()).toString();
                String to = BuiltInRegistries.ENTITY_TYPE.getKey(converted.getType()).toString();
                converted.setAttached(MemoryStore.TYPE, old.withFact(new MobMemory.Fact("CONVERSION", new java.util.UUID(0, 0),
                        from + " -> " + to, previous.level().getGameTime())));
            }
            var orders = previous.getAttached(br.com.mobvoz.orders.MobOrders.TYPE);
            if (orders != null) {
                br.com.mobvoz.orders.Orders.set(converted, orders);
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (damageTaken > 0 && entity instanceof Mob mob && source.getEntity() instanceof ServerPlayer sp) {
                long tick = sp.level().getGameTime();
                MemoryStore.update(mob, m -> MemoryRules.damagedByPlayer(m, sp.getUUID(), tick));
            }
        });
    }
}
