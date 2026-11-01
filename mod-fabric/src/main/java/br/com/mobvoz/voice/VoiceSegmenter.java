package br.com.mobvoz.voice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * VAD por energia + segmentacao em enunciados (fase 2). PCM 16-bit mono, frames de 20 ms (48 kHz no SVC).
 * Logica pura: o tempo entra por parametro. Um enunciado = um alvo, travado quando a fala comeca.
 * O audio nunca e persistido: so e entregue ao {@code sink} e descartado.
 * Com a tecla de apertar-para-falar (push-to-talk) apertada, uma pausa NAO fecha o enunciado: ele so fecha pouco depois de soltar a tecla.
 */
public final class VoiceSegmenter {
    public static final int FRAME_MS = 20;

    /** @param target alvo travado no inicio da fala (um alvo por enunciado). */
    public record Utterance(short[] pcm, long startMs, long durationMs, UUID target) {
    }

    private final Consumer<Utterance> sink;
    private final double rmsThreshold;
    private final int hangoverMs;
    private final int minVoicedMs;
    private final int maxMs;
    private final BooleanSupplier keyHeld;
    /** Depois de soltar a tecla, espera so este tempo (cobre o ultimo frame ainda em transito) antes de fechar. */
    private static final int RELEASE_GRACE_MS = 250;
    private static final int START_FRAMES = 3;
    private static final int PRE_ROLL_FRAMES = 6;

    private final ArrayDeque<short[]> preRoll = new ArrayDeque<>();
    private List<short[]> current = new ArrayList<>();
    private boolean active;
    private int voicedRun;
    private long startMs;
    private long lastVoicedMs;
    private long lastHeldMs = Long.MIN_VALUE;
    private long voicedMs;
    private UUID target;

    public VoiceSegmenter(Consumer<Utterance> sink, double rmsThreshold, int hangoverMs, int minVoicedMs, int maxMs) {
        this(sink, rmsThreshold, hangoverMs, minVoicedMs, maxMs, () -> false);
    }

    /** @param keyHeld a tecla de apertar-para-falar esta apertada agora? (sempre falso para ativacao por voz) */
    public VoiceSegmenter(Consumer<Utterance> sink, double rmsThreshold, int hangoverMs, int minVoicedMs, int maxMs, BooleanSupplier keyHeld) {
        this.keyHeld = keyHeld;
        this.sink = sink;
        this.rmsThreshold = rmsThreshold;
        this.hangoverMs = hangoverMs;
        this.minVoicedMs = minVoicedMs;
        this.maxMs = maxMs;
    }

    public static VoiceSegmenter withDefaults(Consumer<Utterance> sink) {
        return withDefaults(sink, () -> false);
    }

    public static VoiceSegmenter withDefaults(Consumer<Utterance> sink, BooleanSupplier keyHeld) {
        return new VoiceSegmenter(sink, 600, 700, 300, 30_000, keyHeld);
    }

    /** @param currentTarget alvo da mira agora (null = ninguem na mira: a fala e ignorada). */
    public synchronized void onFrame(short[] frame, long nowMs, UUID currentTarget) {
        closeIfSilent(nowMs);
        boolean voiced = rms(frame) >= rmsThreshold;
        if (active) {
            current.add(frame);
            if (keyHeld.getAsBoolean()) {
                lastHeldMs = nowMs;
            }
            if (voiced) {
                lastVoicedMs = nowMs;
                voicedMs += FRAME_MS;
            }
            if (nowMs - startMs >= maxMs) {
                close();
            }
            return;
        }
        preRoll.addLast(frame);
        if (preRoll.size() > PRE_ROLL_FRAMES) {
            preRoll.removeFirst();
        }
        voicedRun = voiced ? voicedRun + 1 : 0;
        if (voicedRun >= START_FRAMES && currentTarget != null) {
            active = true;
            target = currentTarget;
            current = new ArrayList<>(preRoll);
            startMs = nowMs - (long) preRoll.size() * FRAME_MS;
            lastVoicedMs = nowMs;
            lastHeldMs = Long.MIN_VALUE;
            voicedMs = (long) voicedRun * FRAME_MS;
            preRoll.clear();
        }
    }

    /** Chamar periodicamente (tick): o SVC deixa de enviar frames quando o jogador cala. */
    public synchronized void poll(long nowMs) {
        closeIfSilent(nowMs);
    }

    public synchronized boolean isListening() {
        return active;
    }

    /** Descarta qualquer audio em andamento (ex.: consentimento revogado). Nada e entregue ao sink. */
    public synchronized void reset() {
        active = false;
        current = new ArrayList<>();
        voicedRun = 0;
        preRoll.clear();
        target = null;
    }

    private void closeIfSilent(long nowMs) {
        if (!active) {
            return;
        }
        boolean held = keyHeld.getAsBoolean();
        if (held) {
            lastHeldMs = nowMs;
        }
        if (nowMs - startMs >= maxMs) {
            close();
        } else if (held) {
            return; // com a tecla apertada a pausa e do jogador pensando, nao o fim da fala
        } else if (lastHeldMs >= lastVoicedMs ? nowMs - lastHeldMs >= RELEASE_GRACE_MS : nowMs - lastVoicedMs >= hangoverMs) {
            close(); // soltou a tecla: fecha logo; sem tecla (ativacao por voz): fecha apos o silencio
        }
    }

    private void close() {
        List<short[]> frames = current;
        Utterance out = null;
        if (voicedMs >= minVoicedMs) {
            int n = 0;
            for (short[] f : frames) {
                n += f.length;
            }
            short[] pcm = new short[n];
            int off = 0;
            for (short[] f : frames) {
                System.arraycopy(f, 0, pcm, off, f.length);
                off += f.length;
            }
            out = new Utterance(pcm, startMs, (long) frames.size() * FRAME_MS, target);
        }
        active = false;
        current = new ArrayList<>();
        voicedRun = 0;
        preRoll.clear();
        target = null;
        if (out != null) {
            sink.accept(out);
        }
    }

    static double rms(short[] frame) {
        if (frame.length == 0) {
            return 0;
        }
        double sum = 0;
        for (short s : frame) {
            sum += (double) s * s;
        }
        return Math.sqrt(sum / frame.length);
    }
}
