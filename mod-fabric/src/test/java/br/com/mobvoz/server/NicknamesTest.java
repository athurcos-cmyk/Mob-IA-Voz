package br.com.mobvoz.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.mobvoz.server.Nicknames.Verdict;
import org.junit.jupiter.api.Test;

class NicknamesTest {
    @Test
    void trustedOrNeutralCreaturesAcceptAndDistrustfulOnesRefuse() {
        assertEquals(Verdict.ACEITO, Nicknames.decide("minecraft:cow", "", "Mimosa", 0.0, 0.0));
        assertEquals(Verdict.ACEITO, Nicknames.decide("minecraft:zombie", "", "Boom", 0.3, 0.1));
        assertEquals(Verdict.RECUSADO, Nicknames.decide("minecraft:cow", "", "Mimosa", -0.2, 0.0), "desconfia");
        assertEquals(Verdict.RECUSADO, Nicknames.decide("minecraft:pig", "", "Toicinho", 0.0, 0.8), "com medo");
    }

    @Test
    void bossesAndRepeatsAreNotRenamed() {
        assertEquals(Verdict.IMPOSSIVEL, Nicknames.decide("minecraft:wither", "", "Rex", 0.9, 0.0));
        assertEquals(Verdict.IMPOSSIVEL, Nicknames.decide("minecraft:ender_dragon", "", "Rex", 0.9, 0.0));
        assertEquals(Verdict.JA_ATIVO, Nicknames.decide("minecraft:wolf", "Rex", "Rex", 0.5, 0.0));
    }
}
