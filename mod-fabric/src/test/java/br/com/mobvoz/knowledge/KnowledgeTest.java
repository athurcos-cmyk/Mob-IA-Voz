package br.com.mobvoz.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.knowledge.Knowledge.Event;
import com.mojang.serialization.JsonOps;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KnowledgeTest {
    private final UUID me = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    /** T24: um enderman atacado faz OUTRO enderman desconfiar do jogador; cresce com ataques, com teto. */
    @Test
    void factionRemembersAttackersAndCapsThePenalty() {
        Knowledge k = Knowledge.EMPTY;
        assertEquals(0f, k.trustPenalty("enderman", me));
        for (int i = 0; i < 20; i++) {
            k = k.withFaction("enderman", new Event("ATTACKED_MEMBER", me, "", i * 100L));
        }
        assertEquals(0.6f, k.trustPenalty("enderman", me), 1e-6, "teto de 0.6");
        assertEquals(0f, k.trustPenalty("enderman", other), "so quem atacou");
        assertEquals(0f, k.trustPenalty("aldeia", me), "outra faccao nao sabe");
        assertTrue(k.factionLines("enderman", me, 5000).get(0).contains("o jogador que fala com você"));
        assertEquals(Knowledge.FACTION_CAP, k.factions().get("enderman").size());
    }

    @Test
    void burstOfHitsIsOneEvent() {
        Knowledge k = Knowledge.EMPTY.withFaction("enderman", new Event("ATTACKED_MEMBER", me, "", 100))
                .withFaction("enderman", new Event("ATTACKED_MEMBER", me, "", 110));
        assertEquals(1, k.factions().get("enderman").size());
    }

    /** T25: o dragao so conhece o que esta no registro; nada alem disso aparece. */
    @Test
    void dragonHistoryContainsOnlyLoggedEvents() {
        assertTrue(Knowledge.EMPTY.historyLines(me, 100).isEmpty(), "sem registro, o dragao nao sabe nada");
        Knowledge k = Knowledge.EMPTY
                .withHistory(new Event("ENTERED_DIMENSION", me, "o Fim", 1000))
                .withHistory(new Event("BOSS_DEFEATED", me, "o Wither", 2000))
                .withHistory(new Event("PLAYER_DEATH", other, "", 3000));
        var lines = k.historyLines(me, 40000);
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).contains("entrou em o Fim") && lines.get(1).contains("derrotou o Wither"));
        assertTrue(lines.get(2).contains("outro jogador"), "fato de outro jogador e atribuido a 'outro jogador'");
        assertFalse(lines.stream().anyMatch(l -> l.contains("Ender Dragon")), "nada inventado");
    }

    /** T33: salvar/carregar nao duplica nem perde eventos; historico tem teto. */
    @Test
    void roundTripAndCaps() {
        Knowledge k = Knowledge.EMPTY;
        for (int i = 0; i < 100; i++) {
            k = k.withHistory(new Event("PLAYER_DEATH", me, "", i * 100L));
        }
        assertEquals(Knowledge.HISTORY_CAP, k.history().size());
        var json = Knowledge.CODEC.encodeStart(JsonOps.INSTANCE, k).getOrThrow();
        assertEquals(k, Knowledge.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }
}
