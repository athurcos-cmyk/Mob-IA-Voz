package br.com.mobvoz.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.memory.MobMemory.Relationship;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.OrderPolicy.Rules;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import br.com.mobvoz.orders.OrderPolicy.Verdict;
import br.com.mobvoz.trade.TradeCatalog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GovernanceTest {
    private static final Relationship MAX = new Relationship(1, 1, 0);
    private static final Subject WARDEN = new Subject("minecraft:warden", false, true, true, true);

    @AfterEach
    void reset() {
        ServerConfig.set(new ServerConfig());
    }

    /** T26/T27: contratos com chefes ficam desligados por padrao; ligados, so o que o adaptador do chefe sabe fazer e so com confianca alta. */
    @Test
    void bossContractsAreOffByDefaultAndNeverFakeSuccess() {
        for (String a : new String[] {"FOLLOW_PLAYER", "DEFEND_PLAYER", "CEASE_HOSTILITY", "STAY_AT"}) {
            var off = OrderPolicy.decide(a, WARDEN, MAX, true, MobOrders.NONE, "p", false, 0, new Rules(false, 2, 0));
            assertEquals(Verdict.REFUSED, off.verdict());
        }
        Rules on = new Rules(true, 2, 0);
        // sem adaptador de movimento (como o Dragao): seguir/ficar sao impossiveis, nunca sucesso falso
        for (String a : new String[] {"FOLLOW_PLAYER", "STAY_AT"}) {
            assertEquals(Verdict.UNSUPPORTED, OrderPolicy.decide(a, WARDEN, MAX, true, MobOrders.NONE, "p", false, 0, on).verdict());
        }
        // lutar e baixar a guarda: aceitos so com confianca de chefe (pacto raro), recusados com confianca comum
        Relationship fair = new Relationship(0.4f, 0.4f, 0);
        for (String a : new String[] {"DEFEND_PLAYER", "CEASE_HOSTILITY"}) {
            assertTrue(OrderPolicy.decide(a, WARDEN, MAX, true, MobOrders.NONE, "p", false, 0, on).accepted(), a);
            assertEquals(Verdict.REFUSED, OrderPolicy.decide(a, WARDEN, fair, false, MobOrders.NONE, "p", false, 0, on).verdict(), a);
        }
        // com adaptador (Warden/Wither de verdade): seguir tambem exige confianca de chefe
        Subject withAdapter = new Subject("minecraft:warden", true, true, true, true);
        assertTrue(OrderPolicy.decide("FOLLOW_PLAYER", withAdapter, MAX, false, MobOrders.NONE, "p", false, 0, on).accepted());
        assertEquals(Verdict.REFUSED, OrderPolicy.decide("FOLLOW_PLAYER", withAdapter, new Relationship(0.6f, 0.6f, 0), false, MobOrders.NONE, "p", false, 0, on).verdict());
    }

    @Test
    void followerLimitPerPlayer() {
        Subject cow = new Subject("minecraft:cow", true, false);
        assertEquals(Verdict.REFUSED, OrderPolicy.decide("FOLLOW_PLAYER", cow, MAX, false, MobOrders.NONE, "p", false, 0, new Rules(false, 2, 2)).verdict());
        assertEquals(Verdict.ACCEPTED, OrderPolicy.decide("FOLLOW_PLAYER", cow, MAX, false, MobOrders.NONE, "p", false, 0, new Rules(false, 2, 1)).verdict());
        // quem ja e seguidor deste jogador nao e barrado pelo proprio limite
        assertEquals(Verdict.ALREADY, OrderPolicy.decide("FOLLOW_PLAYER", cow, MAX, false, MobOrders.NONE.follow("p", 0), "p", false, 0,
                new Rules(false, 2, 2)).verdict());
    }

    /** Nao ha mais receita rara nem opt-in: a caveira e uma receita comum; um server.json antigo com "rareRecipes" ainda carrega. */
    @Test
    void thereAreNoRareRecipesAnymore() {
        assertTrue(TradeCatalog.forProfile("minecraft:wither_skeleton").isPresent());
        ServerConfig.set(ServerConfig.parse("{\"rareRecipes\":{\"wither_skeleton_gold_skull_v1\":true}}"));
        var r = TradeCatalog.forItem("minecraft:wither_skeleton", "minecraft:wither_skeleton_skull").orElseThrow();
        assertEquals(3, r.maxUnits());
        assertEquals(1, r.outputCount());
    }

    @Test
    void configDefaultsAreConservativeAndBrokenJsonIsSafe() {
        ServerConfig d = new ServerConfig();
        assertFalse(d.bossContractsEnabled);
        assertTrue(d.requireVoiceConsent);
        assertEquals(0, ServerConfig.parse("{\"maxActiveFollowersPerPlayer\":-5}").maxActiveFollowersPerPlayer);
        assertEquals(32, ServerConfig.parse("{\"maxActiveFollowersPerPlayer\":999}").maxActiveFollowersPerPlayer);
        assertFalse(ServerConfig.parse("{\"preset\":\"low\"}").chatterEnabled);
        assertTrue(ServerConfig.parse("").requireVoiceConsent, "arquivo vazio = padroes");
        assertEquals("auto", d.voiceEngine);
        assertEquals("piper", ServerConfig.parse("{\"voiceEngine\":\"Piper\"}").voiceEngine);
        assertEquals("auto", ServerConfig.parse("{\"voiceEngine\":\"xyz\"}").voiceEngine, "valor invalido = auto");
    }
}
