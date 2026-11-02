package br.com.mobvoz.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.config.ServerConfig;
import org.junit.jupiter.api.Test;

class AmbientLevelTest {
    private static Ambient.Level of(String lvl) {
        ServerConfig c = new ServerConfig();
        c.ambientLevel = lvl;
        return Ambient.levelOf(c);
    }

    /** O padrao e "poucas": o dono achou as conversas de ambiente demais com varios bichos juntos. */
    @Test
    void defaultIsFewAndEachLevelIsRarerThanTheNext() {
        assertEquals("poucas", new ServerConfig().ambientLevel);
        var poucas = of("poucas");
        var normal = of("normal");
        var muitas = of("muitas");
        assertTrue(poucas.playerGap() > normal.playerGap() && normal.playerGap() > muitas.playerGap());
        assertTrue(poucas.regionCooldown() > normal.regionCooldown() && normal.regionCooldown() > muitas.regionCooldown());
        assertTrue(poucas.chance() < normal.chance() && normal.chance() < muitas.chance());
        assertTrue(poucas.playerGap() >= 1800, "no minimo ~90 s entre duas conversas perto do mesmo jogador");
        assertEquals(poucas, of("lixo"), "valor invalido cai no padrao");
    }

    @Test
    void levelNames() {
        for (String ok : new String[] {"nenhuma", "poucas", "normal", "muitas", "MUITAS"}) {
            assertTrue(ServerConfig.isAmbientLevel(ok), ok);
        }
        assertFalse(ServerConfig.isAmbientLevel("demais"));
        assertFalse(new ServerConfig().thinkingFillers, "o 'deixa eu pensar' vem desligado");
    }
}
