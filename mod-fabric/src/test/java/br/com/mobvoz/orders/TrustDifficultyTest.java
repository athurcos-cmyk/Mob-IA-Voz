package br.com.mobvoz.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import br.com.mobvoz.profile.Profiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Dificuldade da confianca: facil/normal/dificil mudam so quanto de confianca as criaturas EXIGEM; chefes e vacas (limite 0) nao mudam. */
class TrustDifficultyTest {
    private static final Subject PIG = new Subject("minecraft:pig", true, false);
    private static final Subject COW = new Subject("minecraft:cow", true, false);
    private static final Subject ZOMBIE = new Subject("minecraft:zombie", true, true);

    @AfterEach
    void back() {
        ServerConfig.set(new ServerConfig());
    }

    private static void level(String l) {
        ServerConfig c = new ServerConfig();
        c.trustDifficulty = l;
        ServerConfig.set(c.normalized());
    }

    @Test
    void easyAsksForLessAndHardForMoreThanNormal() {
        double base = Profiles.of("minecraft:pig").follow();
        assertTrue(base > 0);
        level("normal");
        assertEquals(base, OrderPolicy.followThreshold(PIG), 1e-9);
        level("facil");
        assertEquals(base * 0.6, OrderPolicy.followThreshold(PIG), 1e-9);
        level("dificil");
        assertEquals(base * 1.4, OrderPolicy.followThreshold(PIG), 1e-9);
        level("facil");
        assertEquals(0.6 * 0.6, OrderPolicy.followThreshold(ZOMBIE), 1e-9, "hostis (minimo 0,6) tambem");
    }

    @Test
    void creaturesThatAlwaysFollowStayThatWay() {
        for (String l : new String[] {"facil", "normal", "dificil"}) {
            level(l);
            assertEquals(0.0, OrderPolicy.followThreshold(COW), 1e-9, l);
        }
    }

    @Test
    void bossesIgnoreTheDifficulty() {
        Subject wither = new Subject("minecraft:wither", false, true, true, true);
        level("facil");
        double easy = OrderPolicy.followThreshold(wither);
        level("dificil");
        assertEquals(easy, OrderPolicy.followThreshold(wither), 1e-9);
        assertTrue(easy >= OrderPolicy.BOSS_MIN_TRUST);
    }

    @Test
    void invalidValuesFallBackToNormalAndTheConfigFileKeepsTheChoice() {
        assertEquals("normal", ServerConfig.parse("{\"trustDifficulty\":\"impossivel\"}").trustDifficulty);
        assertEquals("normal", ServerConfig.parse("{}").trustDifficulty);
        assertEquals("facil", ServerConfig.parse("{\"trustDifficulty\":\"FACIL\"}").trustDifficulty);
        assertFalse(ServerConfig.isDifficulty("medio"));
        assertTrue(ServerConfig.isDifficulty("dificil"));
    }
}
