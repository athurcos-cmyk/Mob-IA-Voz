package br.com.mobvoz.trade;

import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.social.Reactions;
import br.com.mobvoz.social.Reactions.Reaction;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Mob;

/**
 * "Me da isso, senao eu te mato": o jogador ameaca para conseguir um item que a criatura teria. Quem decide e o servidor, pela natureza dela:
 * a medrosa cede; a valente cede se estiver em desvantagem (ferida, ou o jogador com espada/machado ou armadura forte); a impassivel e os
 * chefes nao cedem. Ceder e de graca, mas custa a confianca (a ameaca ja a marcou) e so vale ate certo valor: ninguem entrega um elytra de medo.
 */
public final class Extortion {
    /** Acima disto ninguem cede de medo. */
    static final double MAX_VALUE = 100.0;

    private Extortion() {
    }

    /** O jogador esta em condicoes de intimidar um valente? Mob ferido a menos da metade, espada/machado na mao ou armadura de ferro ou melhor. */
    static boolean outmatched(Mob mob, ServerPlayer p) {
        var held = p.getMainHandItem();
        return mob.getHealth() <= mob.getMaxHealth() * 0.5 || held.is(ItemTags.SWORDS) || held.is(ItemTags.AXES) || p.getArmorValue() >= 10;
    }

    /** Decide e, se ela ceder, larga o item para o jogador. Devolve o resultado para a fala (pedido EXTORSAO). */
    public static Map<String, String> demand(Mob mob, ServerPlayer player, String item, int count, String giveVerdict, Reaction reaction) {
        String name = Values.name(item, count != 1);
        String key = Profiles.keyOf(mob);
        if (Orders.subjectOf(mob).boss() || reaction == Reaction.UNMOVED || reaction == Reaction.NONE || reaction == Reaction.CALMED) {
            return result("RECUSOU", "a ameaça não te abala e você não entrega nada por medo");
        }
        boolean has = TradeCatalog.forItem(key, item).isPresent() || giveVerdict.equals("YES") || giveVerdict.equals("MAYBE");
        if (!has) {
            return result("SEM_O_ITEM", "você não tem " + name + ", nem ameaçada teria como entregar");
        }
        if (Values.value(item) > MAX_VALUE) {
            return result("RECUSOU", name + " vale demais: nem com medo você entrega isso");
        }
        boolean cedes = reaction == Reaction.FLEE || reaction == Reaction.FROZEN || reaction == Reaction.FIGHT && outmatched(mob, player);
        if (!cedes) {
            return result("RECUSOU", "a ameaça te enfurece e você prefere lutar a ceder");
        }
        int n = Math.max(1, Math.min(Math.max(1, count), TradeCatalog.dynUnits(item)));
        if (mob.level() instanceof ServerLevel level) {
            Trades.spawn(level, item, n, mob.position().add(0, 0.6, 0), mob, player.getUUID());
        }
        Reactions.calm(mob);
        String given = n + " " + Values.name(item, n != 1);
        return result("ENTREGOU", "com medo da ameaça você largou " + given + " no chão para o jogador, sem cobrar nada; fale assustada ou a contragosto, "
                + "e conte em UMA frase curta de onde veio esse item (um causo seu, plausível para a sua espécie); você não confia mais nele depois disso");
    }

    private static Map<String, String> result(String res, String why) {
        return Map.of("pedido", "EXTORSAO", "resultado", res, "motivo", why);
    }
}
