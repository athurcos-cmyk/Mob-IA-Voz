package br.com.mobvoz.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.trade.Negotiation.Stack;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TradeUnitsTest {
    private static final TradeCatalog.Recipe SKULL = TradeCatalog.byId("wither_skeleton_wither_skeleton_skull_v2").orElseThrow();

    /** O jogador pergunta: "o que eu daria em troca?" -> a resposta e em ITENS, nunca em pontos. */
    @Test
    void priceIsSpokenInItemsNotPoints() {
        var one = SKULL.paymentExamples(1);
        assertTrue(one.contains("12 barras de ouro") && one.contains("3 diamantes") && one.contains("6 esmeraldas"), one.toString());
        var three = SKULL.paymentExamples(3);
        assertTrue(three.contains("9 diamantes"), three.toString());
        assertTrue(three.stream().noneMatch(s -> s.contains("picareta") || s.contains("espada")), "exemplos so de itens simples");
        String c = SKULL.conditions(3);
        assertFalse(c.toLowerCase().contains("ponto"), c);
        assertTrue(c.contains("no máximo 3 unidades por negócio") && !c.contains("dias de jogo"), c);
        assertTrue(c.contains("3 cabeças de esqueleto wither"));
    }

    /** 3 caveiras e UM negocio de 3 unidades: preco total proporcional; a sobra volta. */
    @Test
    void threeSkullsCostThreeTimesAndExcessReturns() {
        var ws = Profiles.of("minecraft:wither_skeleton");
        assertEquals(180.0, SKULL.totalPrice(3), 1e-9);
        var sel = Negotiation.select(ws, Map.of(), List.of(new Stack(1, "minecraft:gold_ingot", 40, 1.0)), SKULL.totalPrice(3));
        assertTrue(sel.enough());
        assertEquals(36, sel.takes().get(0).count(), "36 barras de ouro = 180");
        assertEquals(4, sel.leftovers().get(0).count(), "as 4 que passaram voltam");
        assertFalse(Negotiation.select(ws, Map.of(), List.of(new Stack(1, "minecraft:gold_ingot", 35, 1.0)), SKULL.totalPrice(3)).enough());
    }

    /** Sem cooldown nem janela: so o teto por negocio; concluir ou reembolsar so encerra a transacao. */
    @Test
    void unitsPerDealHaveACapButNoCooldown() {
        MobTrades t = MobTrades.EMPTY.open(SKULL.id(), "p", 5000, 3);
        assertEquals(3, t.offerUnits());
        t = t.reserve("tx", SKULL.id(), "p", 100, Map.of("minecraft:gold_ingot", 36)).completed();
        assertFalse(t.hasTx());
        assertTrue(t.isDone("tx"));
        // logo em seguida pode abrir outro negocio igual, sem espera
        assertTrue(t.open(SKULL.id(), "p", 5100, 3).hasOffer());
        MobTrades r = MobTrades.EMPTY.reserve("tx2", SKULL.id(), "p", 0, Map.of("minecraft:gold_ingot", 12), 1).refunded();
        assertTrue(r.isDone("tx2"));
    }

    @Test
    void missingIsSpokenInItems() {
        var farmer = Profiles.of("minecraft:villager#farmer");
        assertEquals("o equivalente a 2 fenos", Trades.missingText(farmer, 9.0));
    }
}
