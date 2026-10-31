package br.com.mobvoz.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatterSchedulerTest {
    private final ChatterScheduler s = new ChatterScheduler(ChatterScheduler.Config.DEFAULT);
    private static UUID id(int n) {
        return new UUID(0, n);
    }

    @Test
    void firstSceneOkThenSameRegionBlockedUntilCooldown() {
        assertTrue(s.tryAcquire("ow:0:0", id(1), id(2), 0, false));
        assertFalse(s.tryAcquire("ow:0:0", id(3), id(4), 100, false), "regiao ocupada");
        assertTrue(s.tryAcquire("ow:0:0", id(3), id(4), 1300, false), "cooldown da regiao passou");
    }

    @Test
    void mobCooldownBlocksSameCreatures() {
        assertTrue(s.tryAcquire("a", id(1), id(2), 0, false));
        assertFalse(s.tryAcquire("b", id(1), id(9), 1300, false), "mob 1 ainda em cooldown (2400)");
        assertTrue(s.tryAcquire("b", id(1), id(9), 2500, false));
    }

    /** Prioridade do jogador: conversa de jogador ativa derruba fala ambiente. */
    @Test
    void playerConversationHasPriority() {
        assertFalse(s.tryAcquire("a", id(1), id(2), 0, true));
        assertEquals(0, s.scenesApproved());
    }

    /** T20: combate pesado com 30 mobs em varias regioes -> no maximo 3 cenas por minuto. */
    @Test
    void heavyCombatIsCappedPerMinute() {
        int approved = 0;
        for (int i = 0; i < 30; i++) {
            if (s.tryAcquire("r" + i, id(i * 2), id(i * 2 + 1), i * 10L, false)) {
                approved++;
            }
        }
        assertEquals(3, approved);
        assertTrue(s.tryAcquire("novo", id(100), id(101), 1300, false), "o teto e por janela deslizante de 1 minuto");
    }

    @Test
    void regionKeyGroupsNearbyBlocks() {
        assertEquals(ChatterScheduler.region("ow", 1, 1), ChatterScheduler.region("ow", 30, 30));
        assertFalse(ChatterScheduler.region("ow", 1, 1).equals(ChatterScheduler.region("ow", 40, 1)));
        assertFalse(ChatterScheduler.region("ow", 1, 1).equals(ChatterScheduler.region("nether", 1, 1)));
    }
}
