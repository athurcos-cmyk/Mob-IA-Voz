package br.com.mobvoz.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PricingTest {
    /** Para TODA receita do catalogo, qualquer confianca, humor ou dia: nunca abaixo do valor da saida, nunca fora de 75%..125% do base. */
    @Test
    void priceNeverCreatesValueAndStaysInsideTheBand() {
        var rnd = new java.util.Random(42);
        for (var r : TradeCatalog.ALL) {
            double floor = r.outputCount() * Values.value(r.outputItem());
            for (int i = 0; i < 60; i++) {
                double warmth = rnd.nextDouble() * 3 - 1.5; // inclui valores fora da faixa: o codigo limita
                var q = Pricing.quote(r, warmth, new UUID(rnd.nextLong(), rnd.nextLong()), rnd.nextInt(500));
                assertTrue(q.unitPrice() + 1e-9 >= floor, r.id() + ": " + q.unitPrice() + " < valor " + floor);
                assertTrue(q.unitPrice() <= r.price() * 1.25 + 0.1 + 1e-9, r.id() + ": " + q.unitPrice() + " acima da faixa");
                assertTrue(q.unitPrice() + 1e-9 >= r.price() * 0.75 - 0.1 || q.unitPrice() <= floor + 0.1, r.id() + ": " + q.unitPrice() + " abaixo da faixa");
            }
        }
    }

    @Test
    void sameCreatureAndDayAlwaysQuoteTheSameAndTrustNeverRaisesThePrice() {
        var r = TradeCatalog.byId("villager_farmer_emerald_v2").orElseThrow();
        var mob = UUID.randomUUID();
        assertEquals(Pricing.quote(r, 0.2, mob, 7), Pricing.quote(r, 0.2, mob, 7), "insistir no mesmo dia nao muda o preco");
        double last = Double.MAX_VALUE;
        for (double w = -1; w <= 1.0001; w += 0.25) {
            double p = Pricing.quote(r, w, mob, 7).unitPrice();
            assertTrue(p <= last + 1e-9, "mais confianca nunca encarece (" + w + ")");
            last = p;
        }
        // criaturas diferentes tem humores diferentes (nem todo mundo cobra igual)
        var prices = new java.util.HashSet<Double>();
        for (int i = 0; i < 30; i++) {
            prices.add(Pricing.quote(r, 0, UUID.randomUUID(), 7).unitPrice());
        }
        assertTrue(prices.size() > 3, "o humor varia entre criaturas: " + prices);
    }

    @Test
    void haggleLowersPartiallyWithLimitsAndNeverBelowTheFloor() {
        var r = TradeCatalog.byId("villager_farmer_emerald_v2").orElseThrow(); // preco-base 13, valor 10
        int start = 130;
        assertEquals(start, Pricing.haggle(r, start, 0, 2, 1.0), "sem argumento nao baixa");
        int weak = Pricing.haggle(r, start, 1, 1, 0);
        int strong = Pricing.haggle(r, start, 2, 1, 0);
        assertTrue(weak < start && strong < weak, weak + " / " + strong);
        assertTrue(Pricing.haggle(r, start, 2, 2, 1.0) < strong, "credulidade e confianca ajudam");
        int p = start;
        for (int i = 0; i < 20; i++) {
            p = Pricing.haggle(r, p, 2, 2, 1.0);
        }
        assertTrue(p / 10.0 >= 10.0 && p / 10.0 >= 13 * 0.70 - 0.1, "piso: " + p);
        // receita sem folga (margem 1,0, como a caveira) nao admite desconto abaixo do valor
        var skull = TradeCatalog.byId("wither_skeleton_wither_skeleton_skull_v2").orElseThrow();
        assertEquals(600, Pricing.haggle(skull, 600, 2, 2, 1.0), "caveira ja esta no valor: sem desconto");
    }

    @Test
    void lockedPriceSurvivesSaveAndOldSavesStillLoad() {
        var t = MobTrades.EMPTY.open("x", "p", 100, 2, 117);
        assertEquals(11.7, t.unitPrice(13.0), 1e-9);
        assertEquals(11.7, t.open("x", "p", 200, 3, 150).unitPrice(13.0), 1e-9, "reabrir a mesma oferta mantem o preco travado");
        assertEquals(15.0, t.open("y", "p", 200, 1, 150).unitPrice(13.0), 1e-9, "outra oferta recalcula");
        assertEquals(13.0, MobTrades.EMPTY.unitPrice(13.0), 1e-9, "sem trava: preco da receita");
        var json = MobTrades.CODEC.encodeStart(JsonOps.INSTANCE, t).getOrThrow();
        assertEquals(t, MobTrades.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        var old = json.getAsJsonObject().deepCopy();
        old.remove("offerPrice"); // save antigo, sem o campo
        assertEquals(0, MobTrades.CODEC.parse(JsonOps.INSTANCE, old).getOrThrow().offerPrice());
        assertEquals(0, t.reserve("tx", "x", "p", 1, java.util.Map.of("minecraft:wheat", 1)).offerPrice(), "reservar encerra o preco da oferta");
    }
}
