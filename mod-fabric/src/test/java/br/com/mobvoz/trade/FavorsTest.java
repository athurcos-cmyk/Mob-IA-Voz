package br.com.mobvoz.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.OrderPolicy.Decision;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import br.com.mobvoz.orders.OrderPolicy.Verdict;
import br.com.mobvoz.profile.Profiles;
import org.junit.jupiter.api.Test;

class FavorsTest {
    private static final Profiles.Profile SKELETON = Profiles.of("minecraft:skeleton");

    /** O esqueleto gosta de ossos e flechas (gosto) e aceita penas, silex e gravetos (troca): tudo isso paga. Nada que ele odeia. */
    @Test
    void payItemsAreLikesAndWantsWithoutDislikes() {
        var pay = Favors.payItems(SKELETON);
        assertTrue(pay.containsAll(java.util.List.of("minecraft:bone", "minecraft:arrow", "minecraft:feather")), pay.toString());
        assertFalse(pay.contains("minecraft:water_bucket"), "o que ele odeia nunca paga");
        assertEquals("minecraft:bone", pay.get(0), "o item mais valioso vem primeiro");
    }

    /** O preco e dito em itens e nunca passa de 16 do item mais valioso que ela aceita. */
    @Test
    void priceIsInItemsAndCapped() {
        double price = Favors.priceFor(SKELETON, 0.6);
        assertEquals(8.0, price, 1e-9, "16 ossos de 0,5");
        String ex = Favors.examples(SKELETON, price);
        assertTrue(ex.startsWith("16 ossos"), ex);
        assertFalse(ex.toLowerCase().contains("ponto"), ex);
        assertEquals(3.0, Favors.priceFor(SKELETON, 0.05), 1e-9, "pouca falta de confianca = o preco minimo");
        assertTrue(Favors.examples(SKELETON, 3.0).startsWith("6 ossos"));
    }

    /** Quem nao tem gosto nenhum no perfil aceita moedas, para todo mob poder ser pago. */
    @Test
    void creatureWithoutTastesAcceptsCoins() {
        var pay = Favors.payItems(Profiles.of("minecraft:nao_existe_este_mob"));
        assertTrue(pay.contains("minecraft:emerald") && pay.contains("minecraft:diamond"), pay.toString());
    }

    @Test
    void onlyTrustRefusalsCanBePaid() {
        assertTrue(Favors.trustRefusal(new Decision("FOLLOW_PLAYER", Verdict.REFUSED, "ainda nao confia o bastante em quem pede")));
        assertTrue(Favors.trustRefusal(new Decision("DEFEND_PLAYER", Verdict.REFUSED, "nao confia o bastante para lutar por quem pede")));
        assertFalse(Favors.trustRefusal(new Decision("FOLLOW_PLAYER", Verdict.REFUSED, "ja obedece a outro jogador")));
        assertFalse(Favors.trustRefusal(new Decision("FOLLOW_PLAYER", Verdict.REFUSED, "quem pede ja tem aliados demais com ele")));
        assertFalse(Favors.trustRefusal(new Decision("FOLLOW_PLAYER", Verdict.UNSUPPORTED, "nao confia")));
    }

    /** O limiar usado para cobrar e o mesmo que a decisao usa: pagar o que falta cobre exatamente a recusa. */
    @Test
    void trustNeededMatchesTheDecision() {
        Subject sk = new Subject("minecraft:skeleton", true, true);
        double need = OrderPolicy.trustNeeded("FOLLOW_PLAYER", sk);
        assertTrue(need > 0);
        assertTrue(OrderPolicy.trustNeeded("DEFEND_PLAYER", sk) > need && OrderPolicy.trustNeeded("STAY_AT", sk) < need);
    }
}
