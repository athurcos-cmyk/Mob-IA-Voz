package br.com.mobvoz.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VoicesTest {
    @Test
    void eachCreatureHasAStableVoiceNumberAndTheSpecies() {
        UUID id = UUID.randomUUID();
        Voices a = Voices.forProfile("minecraft:cow", id);
        assertEquals(a, Voices.forProfile("minecraft:cow", id), "a mesma criatura, a mesma voz");
        assertEquals("minecraft:cow", a.species());
        assertTrue(a.bucket() >= 0 && a.bucket() < Voices.BUCKETS);
    }

    @Test
    void manyCreaturesSpreadOverTheVoiceVariants() {
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 400; i++) {
            seen.add(Voices.forProfile("minecraft:villager#farmer", UUID.randomUUID()).bucket());
        }
        assertTrue(seen.size() >= 50, "400 aldeoes usam quase todas as 64 variantes: " + seen.size());
    }

    @Test
    void emotionIsCarriedWithoutChangingTheCreature() {
        Voices v = Voices.forProfile("minecraft:pig", UUID.randomUUID());
        Voices e = v.withEmotion("alegre");
        assertEquals("alegre", e.emotion());
        assertEquals(v.bucket(), e.bucket());
        assertEquals("", v.withEmotion(null).emotion());
    }
}
