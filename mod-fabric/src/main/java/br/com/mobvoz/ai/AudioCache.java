package br.com.mobvoz.ai;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Cache de AUDIO das falas fixas (reacoes ao mundo, conversas de ambiente, falas de troca): a mesma frase com a mesma voz nao e
 * sintetizada de novo. LRU com teto em bytes (as falas repetem muito; as geradas pela IA nunca passam por aqui). Thread-safe.
 */
public final class AudioCache {
    /** Passo do tom no cache: a variacao individual (+-7%) vira ~8 degraus de 2%, imperceptiveis, e as falas passam a se repetir de verdade. */
    public static final double PITCH_STEP = 0.02;

    private final long maxBytes;
    private final LinkedHashMap<String, short[]> map = new LinkedHashMap<>(64, 0.75f, true);
    private long bytes;
    private long hits;
    private long misses;

    public AudioCache(long maxBytes) {
        this.maxBytes = maxBytes;
    }

    public static double quantizePitch(double pitch) {
        return Math.round(pitch / PITCH_STEP) * PITCH_STEP;
    }

    public static String key(String text, String voice, double pitch, double speed, String engine) {
        return String.format(Locale.ROOT, "%s|%s|%.2f|%.2f|%s", text, voice, quantizePitch(pitch), speed, engine);
    }

    public synchronized short[] get(String key) {
        short[] v = map.get(key);
        if (v == null) {
            misses++;
        } else {
            hits++;
        }
        return v;
    }

    public synchronized void put(String key, short[] pcm) {
        long size = (long) pcm.length * 2;
        if (size > maxBytes) {
            return;
        }
        short[] old = map.put(key, pcm);
        bytes += size - (old == null ? 0 : (long) old.length * 2);
        for (Iterator<Map.Entry<String, short[]>> it = map.entrySet().iterator(); bytes > maxBytes && it.hasNext(); ) {
            var e = it.next(); // o menos usado recentemente
            bytes -= (long) e.getValue().length * 2;
            it.remove();
        }
    }

    public synchronized long hits() {
        return hits;
    }

    public synchronized long misses() {
        return misses;
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized long bytes() {
        return bytes;
    }
}
