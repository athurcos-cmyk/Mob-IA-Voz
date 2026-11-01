package br.com.mobvoz.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TrustBarTest {
    @Test
    void progressIsWarmthOverWhatTheSpeciesNeeds() {
        assertEquals(1.0, TrustBar.progress(0.0, 0.0), "vaca: limite 0, ja segue");
        assertEquals(0.5, TrustBar.progress(0.1, 0.2), 1e-9);
        assertEquals(1.0, TrustBar.progress(0.5, 0.2), "passou do limite");
        assertEquals(0.0, TrustBar.progress(-0.3, 0.2), "calor negativo = barra vazia");
    }
}
