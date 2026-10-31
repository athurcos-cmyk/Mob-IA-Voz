package br.com.mobvoz.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class MobTradesTest {
    private static final String P = "player-a";

    @Test
    void offerReserveCompleteIsOneWayAndIdempotent() {
        MobTrades t = MobTrades.EMPTY.open("pig_apple_v2", P, 2400);
        assertTrue(t.hasOffer());
        t = t.reserve("tx-1", "pig_apple_v2", P, 100, java.util.Map.of("minecraft:carrot", 3));
        assertFalse(t.hasOffer(), "a oferta vira transacao");
        assertTrue(t.hasTx());
        t = t.completed();
        assertFalse(t.hasTx());
        assertTrue(t.isDone("tx-1"));
    }

    /** T15: apos reiniciar no meio (RESERVADO persistido), uma transacao ja encerrada nao paga duas vezes. */
    @Test
    void reservedStateSurvivesSaveAndAFinishedIdIsNeverPaidTwice() {
        MobTrades t = MobTrades.EMPTY.reserve("tx-9", "cow_leather_v2", P, 50, java.util.Map.of("minecraft:wheat", 2));
        var json = MobTrades.CODEC.encodeStart(JsonOps.INSTANCE, t).getOrThrow();
        MobTrades loaded = MobTrades.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(t, loaded);
        assertTrue(loaded.hasTx() && !loaded.isDone("tx-9"));
        MobTrades paid = loaded.completed();
        assertTrue(paid.isDone("tx-9"));
        MobTrades again = paid.reserve("tx-9", "cow_leather_v2", P, 90, java.util.Map.of("minecraft:wheat", 2)); // replay do mesmo id
        assertTrue(again.isDone("tx-9"), "o id continua marcado: o ticker so limpa, nao paga");
    }

    @Test
    void refundClosesTheTransactionToo() {
        MobTrades t = MobTrades.EMPTY.reserve("tx-2", "pig_apple_v2", P, 10, java.util.Map.of("minecraft:carrot", 3)).refunded();
        assertTrue(t.isDone("tx-2"));
    }

    /** Sem cooldown: dois negocios seguidos fecham normalmente (nao ha janela nem teto por prazo). */
    @Test
    void backToBackDealsNeedNoWait() {
        MobTrades t = MobTrades.EMPTY;
        for (int i = 0; i < 10; i++) {
            t = t.reserve("tx" + i, "pig_apple_v2", P, 100 + i, java.util.Map.of("minecraft:carrot", 3)).completed();
        }
        assertTrue(t.isDone("tx9") && !t.hasTx());
    }

    @Test
    void doneListIsBounded() {
        MobTrades t = MobTrades.EMPTY;
        for (int i = 0; i < 100; i++) {
            t = t.reserve("tx" + i, "r", P, i, java.util.Map.of("minecraft:carrot", 1)).refunded();
        }
        assertEquals(MobTrades.MAX_DONE, t.done().size());
        assertTrue(t.isDone("tx99") && !t.isDone("tx0"));
    }

    @Test
    void catalogIsClosedAndSane() {
        assertTrue(TradeCatalog.forProfile("minecraft:pig").isPresent());
        assertTrue(TradeCatalog.forProfile("minecraft:wither_skeleton").isPresent(), "nao ha receita rara/desligada");
        assertTrue(TradeCatalog.ALL.stream().noneMatch(r -> r.outputItem().contains("diamond") || r.outputItem().contains("netherite")));
        // nada absurdo para o jogador: pilha por negocio cabe numa pilha e o preco de 1 unidade cabe em <= 32 do item aceito mais valioso
        for (var r : TradeCatalog.ALL) {
            assertTrue(r.outputCount() * r.maxUnits() <= 64, r.id() + " entrega mais que uma pilha por negocio");
            var p = br.com.mobvoz.profile.Profiles.of(r.profile());
            double best = p.wants().stream().mapToDouble(Values::value).max().orElse(0);
            assertTrue(Math.ceil(r.price() / best - 1e-9) <= 32, r.id() + " exige itens demais para 1 unidade");
        }
    }

    /** O livro-razao guarda a persuasao da oferta atual e a descarta ao fechar. */
    @Test
    void persuasionLivesAndDiesWithTheOffer() {
        MobTrades t = MobTrades.EMPTY.open("wither_skeleton_wither_skeleton_skull_v2", P, 5000).withPersuasion("minecraft:iron_ingot", 480);
        assertEquals(480, t.persuadedMap().get("minecraft:iron_ingot"));
        assertEquals(1, t.persuasions());
        assertEquals(480, t.open("wither_skeleton_wither_skeleton_skull_v2", P, 6000).persuadedMap().get("minecraft:iron_ingot"), "mesma oferta: mantem");
        assertTrue(t.open("wither_skeleton_wither_skeleton_skull_v2", "outro", 6000).persuadedMap().isEmpty(), "outro jogador: recomeca");
        assertTrue(t.closeOffer().persuadedMap().isEmpty());
        var json = MobTrades.CODEC.encodeStart(JsonOps.INSTANCE, t).getOrThrow();
        assertEquals(t, MobTrades.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }
}
