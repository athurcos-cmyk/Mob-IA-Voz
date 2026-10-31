package br.com.mobvoz.voice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VoiceSegmenterTest {
    private static final int FRAME = 960;
    private final List<VoiceSegmenter.Utterance> out = new ArrayList<>();
    private final VoiceSegmenter seg = VoiceSegmenter.withDefaults(out::add);
    private long now;

    private short[] frame(int amp) {
        short[] f = new short[FRAME];
        for (int i = 0; i < FRAME; i++) {
            f[i] = (short) (amp * Math.sin(i * 0.2));
        }
        return f;
    }

    private void play(int ms, int amp, UUID target) {
        for (int t = 0; t < ms; t += VoiceSegmenter.FRAME_MS) {
            seg.onFrame(frame(amp), now, target);
            now += VoiceSegmenter.FRAME_MS;
        }
    }

    /** T05: 10 frases curtas com pausas -> exatamente 10 enunciados, silencio ignorado. */
    @Test
    void tenPhrasesGiveTenUtterances() {
        UUID cow = UUID.randomUUID();
        for (int i = 0; i < 10; i++) {
            play(1000, 8000, cow);
            play(1500, 0, cow); // pausa
        }
        assertEquals(10, out.size());
        assertTrue(out.stream().allMatch(u -> u.target().equals(cow) && u.durationMs() >= 1000));
    }

    @Test
    void silenceAndTinyNoiseAreIgnored() {
        play(5000, 0, UUID.randomUUID());
        play(3000, 100, UUID.randomUUID()); // ruido abaixo do limiar
        seg.poll(now + 5000);
        assertEquals(0, out.size());
    }

    @Test
    void clickShorterThanMinimumIsDropped() {
        play(100, 8000, UUID.randomUUID());
        play(1000, 0, UUID.randomUUID());
        assertEquals(0, out.size());
    }

    /** Sem alvo na mira a fala nao vira enunciado. */
    @Test
    void noTargetNoUtterance() {
        play(1000, 8000, null);
        play(1500, 0, null);
        assertEquals(0, out.size());
    }

    /** Um alvo por enunciado: mudar a mira no meio nao troca o alvo. */
    @Test
    void targetIsLockedAtStart() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        play(500, 8000, a);
        play(500, 8000, b);
        play(1000, 0, b);
        assertEquals(1, out.size());
        assertEquals(a, out.get(0).target());
    }

    /** O SVC para de mandar frames quando o jogador cala: o poll do tick fecha o enunciado. */
    @Test
    void pollClosesWhenFramesStop() {
        play(1000, 8000, UUID.randomUUID());
        assertTrue(seg.isListening());
        seg.poll(now + 800);
        assertEquals(1, out.size());
        assertTrue(!seg.isListening());
    }

    @Test
    void maxDurationCutsLongSpeech() {
        play(20_000, 8000, UUID.randomUUID());
        assertTrue(out.size() >= 1);
        assertTrue(out.get(0).durationMs() <= 15_100);
    }
}
