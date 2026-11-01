package br.com.mobvoz.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AudioCacheTest {
    @Test
    void individualPitchesCollapseIntoFewStepsSoFixedLinesActuallyRepeat() {
        java.util.Set<String> keys = new java.util.HashSet<>();
        for (int h = 0; h < 256; h++) {
            double pitch = 0.88 * (0.93 + 0.14 * (h / 255.0)); // a variacao individual de Profiles.individualPitch
            keys.add(AudioCache.key("Que susto!", "pt_BR-faber-medium", pitch, 0.95, "auto"));
        }
        assertTrue(keys.size() <= 9, "degraus: " + keys.size());
        assertTrue(keys.size() >= 3, "ainda ha variedade de tom entre individuos");
        assertEquals(0.90, AudioCache.quantizePitch(0.8999), 1e-9);
    }

    @Test
    void hitsAndMissesAreCounted() {
        AudioCache c = new AudioCache(1_000_000);
        String k = AudioCache.key("Oi", "v", 1.0, 1.0, "auto");
        assertNull(c.get(k));
        c.put(k, new short[100]);
        assertNotNull(c.get(k));
        assertEquals(1, c.hits());
        assertEquals(1, c.misses());
    }

    @Test
    void evictsTheLeastRecentlyUsedWhenOverTheByteBudget() {
        AudioCache c = new AudioCache(1000); // 500 amostras de 2 bytes
        c.put("a", new short[200]);
        c.put("b", new short[200]);
        c.get("a"); // "a" fica mais recente que "b"
        c.put("c", new short[200]); // estoura: sai o menos usado ("b")
        assertNotNull(c.get("a"));
        assertNull(c.get("b"));
        assertNotNull(c.get("c"));
        assertTrue(c.bytes() <= 1000, "bytes " + c.bytes());
        c.put("enorme", new short[10_000]); // maior que o teto: nem entra
        assertNull(c.get("enorme"));
    }

    @Test
    void replacingAnEntryDoesNotLeakBytes() {
        AudioCache c = new AudioCache(10_000);
        for (int i = 0; i < 50; i++) {
            c.put("k", new short[100]);
        }
        assertEquals(200, c.bytes());
        assertEquals(1, c.size());
    }
}
