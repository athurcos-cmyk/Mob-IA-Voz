package br.com.mobvoz.social;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.BossAdapter;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.server.TrustBar;
import br.com.mobvoz.trade.Favors;
import br.com.mobvoz.trade.Trades;
import br.com.mobvoz.trade.Values;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Presente. Dois jeitos de dar, com o mesmo resultado:
 * 1) SHIFT + botao direito com o item na mao (a criatura ganha 1 item na hora; ver MemoryEvents);
 * 2) JOGAR o item no chao olhando para a criatura (ate 7 blocos, mira em cone): ela vem pegar 1 e reage. Vale para qualquer item, gostando ou nao.
 * Itens jogados para PAGAR (oferta de troca ou favor aberto com ela, ou com alguem perto do item) nao sao presente: ficam para a troca.
 */
public final class Gifts {
    private static final double AIM_RANGE = 7.0;
    private static final double CONE_COS = Math.cos(Math.toRadians(18));
    private static final long WAIT_TICKS = 140;
    private static final double REACH = 1.8;
    private static final double REACH_STATIONARY = 3.5;
    private static final int SETTLE_TICKS = 15;

    private record Pending(UUID mob, UUID player, long until) {
    }

    private static final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    private Gifts() {
    }

    /** Aplica o presente: memoria (confianca), mensagem do jogador e fala curta da criatura. Quem chama tira o item de onde estava. */
    public static void give(Mob mob, ServerPlayer sp, ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        long tick = sp.level().getGameTime();
        var prof = Profiles.of(Profiles.keyOf(mob));
        MemoryStore.update(mob, m -> MemoryRules.gift(m, sp.getUUID(), id, 1, tick, prof.likeFactor(id), prof.reaction(id)));
        String reaction = prof.reaction(id);
        sp.displayClientMessage(Component.literal("Você deu 1x " + stack.getHoverName().getString() + ": " + reaction + ". ").append(TrustBar.text(mob, sp.getUUID())), true);
        String thing = Values.known(id) ? Values.name(id, false) : "isso";
        WorldReactions.speakAs(sp, mob, reaction.equals("gostou") ? "ev:gift_like" : reaction.equals("indiferente") ? "ev:gift_meh" : "ev:gift_dislike", thing, 0); // fala fixa, sem IA
    }

    public static int pendingCount() {
        return pending.size();
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ItemEntity it && it.tickCount < 5 && it.getOwner() instanceof ServerPlayer p) {
                onThrown(it, p);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (pending.isEmpty() || server.getTickCount() % 5 != 0) {
                return;
            }
            for (var e : List.copyOf(pending.entrySet())) {
                tickOne(server, e.getKey(), e.getValue());
            }
        });
    }

    /** O jogador largou um item: se estava olhando para uma criatura (em cone), ela e a candidata a receber de presente. */
    static void onThrown(ItemEntity item, ServerPlayer p) {
        if (!ServerConfig.current().groundGifts || p.isSpectator()) {
            return;
        }
        Vec3 eye = p.getEyePosition();
        Vec3 dir = p.getViewVector(1.0F);
        Mob best = null;
        double bestCos = CONE_COS;
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(AIM_RANGE), x -> x.isAlive() && !x.isSpectator() && !x.isInvisible())) {
            Vec3 to = m.getBoundingBox().getCenter().subtract(eye);
            double len = to.length();
            if (len < 0.5 || len > AIM_RANGE) {
                continue;
            }
            double c = dir.dot(to) / len;
            if (c >= bestCos && p.hasLineOfSight(m)) {
                best = m;
                bestCos = c;
            }
        }
        if (best != null && !tradingNear(item, p)) {
            pending.put(item.getUUID(), new Pending(best.getUUID(), p.getUUID(), p.level().getGameTime() + WAIT_TICKS));
            Attention.hold(best, p, 100); // fica por perto e virada para o jogador enquanto o presente chega (nao vai embora passeando)
        }
    }

    /** Alguem perto do item tem oferta de troca ou favor aberto com este jogador? Entao o item e pagamento, nao presente. */
    private static boolean tradingNear(ItemEntity item, ServerPlayer p) {
        String pid = p.getUUID().toString();
        for (Mob m : item.level().getEntitiesOfClass(Mob.class, item.getBoundingBox().inflate(9.0), x -> x.isAlive())) {
            var t = Trades.get(m);
            var f = Favors.get(m);
            if (t.active() && (t.offerPlayer().equals(pid) || t.txPlayer().equals(pid)) || f.active() && f.player().equals(pid)) {
                return true;
            }
        }
        return false;
    }

    private static void tickOne(net.minecraft.server.MinecraftServer server, UUID itemId, Pending pd) {
        ItemEntity item = null;
        Mob mob = null;
        for (ServerLevel l : server.getAllLevels()) {
            if (item == null && l.getEntity(itemId) instanceof ItemEntity i) {
                item = i;
            }
            if (mob == null && l.getEntity(pd.mob()) instanceof Mob m) {
                mob = m;
            }
        }
        ServerPlayer p = server.getPlayerList().getPlayer(pd.player());
        if (item == null || !item.isAlive() || mob == null || !mob.isAlive() || p == null || item.level().getGameTime() >= pd.until() || mob.level() != item.level()
                || tradingNear(item, p)) {
            pending.remove(itemId);
            return;
        }
        double d = mob.distanceTo(item);
        boolean stationary = mob.isNoAi() || !Orders.canWalkFollow(mob);
        if (item.tickCount >= SETTLE_TICKS && (d <= REACH || stationary && d <= REACH_STATIONARY)) {
            pending.remove(itemId);
            ItemStack one = item.getItem().copyWithCount(1);
            if (item.getItem().getCount() <= 1) {
                item.discard();
            } else {
                item.getItem().shrink(1);
            }
            give(mob, p, one);
        } else if (!stationary && d <= 9.0 && item.tickCount >= 8 && !(mob instanceof net.minecraft.world.entity.monster.Enemy)) {
            BossAdapter.moveToPos(mob, item.getX(), item.getY(), item.getZ()); // vem pegar
        }
    }
}
