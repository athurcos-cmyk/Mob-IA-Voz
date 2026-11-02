package br.com.mobvoz.server;

import br.com.mobvoz.ai.SidecarClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Mob;

/**
 * Voz por frase: o TTS gera a fala inteira antes de tocar, entao uma resposta de 3 frases so comecava depois de gerar as 3. Aqui a fala e
 * dividida em frases: a 1a toca assim que fica pronta e as outras sao geradas ENQUANTO ela toca (uma por vez, como o servico de voz exige).
 * O som continua saindo do mob, em ordem; legenda e registro continuam sendo o texto inteiro.
 */
public final class SentenceVoice {
    /** Abaixo disto nao divide: uma frase curta so tem a perder (pausa entre pedacos). */
    static final int MIN_TOTAL = 60;
    /** Cada pedaco tem pelo menos isto (a 1a frase precisa durar o bastante para a 2a ficar pronta enquanto ela toca). */
    static final int MIN_CHUNK = 28;
    static final int MAX_PARTS = 4;
    private static final long PART_TIMEOUT_S = 30;

    /** Um futuro por pedaco, em ordem; a 1a fica pronta primeiro. */
    public record Voiced(List<CompletableFuture<Optional<short[]>>> parts) {
        public CompletableFuture<Optional<short[]>> first() {
            return parts.get(0);
        }

        /** Fala ja pronta (reserva, cache): um pedaco so. */
        public static Voiced of(short[] pcm) {
            return new Voiced(List.of(CompletableFuture.completedFuture(Optional.of(pcm))));
        }
    }

    private SentenceVoice() {
    }

    /**
     * Divide em pedacos de fala: quebra depois de . ! ? (nunca em reticencias "..."), junta frases curtas ate cada pedaco ter {@link #MIN_CHUNK}
     * caracteres, no maximo {@link #MAX_PARTS} pedacos (o resto vai no ultimo) e um final muito curto vai junto do anterior.
     */
    public static List<String> split(String text) {
        String t = text == null ? "" : text.strip().replaceAll("\\s+", " ");
        if (t.length() < MIN_TOTAL) {
            return List.of(t);
        }
        List<String> sentences = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            boolean end = c == '!' || c == '?' || (c == '.' && !(i > 0 && t.charAt(i - 1) == '.') && !(i + 1 < t.length() && t.charAt(i + 1) == '.'));
            if (end && i + 1 < t.length() && t.charAt(i + 1) == ' ') {
                sentences.add(t.substring(start, i + 1));
                start = i + 2;
            }
        }
        if (start < t.length()) {
            sentences.add(t.substring(start));
        }
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String s : sentences) {
            if (cur.length() > 0) {
                cur.append(' ');
            }
            cur.append(s);
            if (cur.length() >= MIN_CHUNK && parts.size() < MAX_PARTS - 1) {
                parts.add(cur.toString());
                cur.setLength(0);
            }
        }
        if (cur.length() > 0) {
            if (parts.isEmpty() || (cur.length() >= 14 && parts.size() < MAX_PARTS)) {
                parts.add(cur.toString());
            } else {
                parts.set(parts.size() - 1, parts.get(parts.size() - 1) + " " + cur);
            }
        }
        return parts.size() <= 1 ? List.of(t) : parts;
    }

    /** Sintetiza em cadeia: o pedaco N+1 so comeca quando o N termina (o TTS e um so), e todos comecam a contar ja. */
    public static Voiced synthesize(SidecarClient sidecar, String text, Voices v) {
        return chain(split(text), p -> sidecar.synthesize(p, v.voice(), v.pitch(), v.speed(), v.species(), v.bucket(), v.emotion()));
    }

    static Voiced chain(List<String> parts, Function<String, CompletableFuture<Optional<short[]>>> synth) {
        List<CompletableFuture<Optional<short[]>>> out = new ArrayList<>();
        CompletableFuture<?> prev = CompletableFuture.completedFuture(null);
        for (String p : parts) {
            CompletableFuture<Optional<short[]>> f = prev.handle((a, e) -> (Void) null).thenCompose(x -> synth.apply(p));
            out.add(f);
            prev = f;
        }
        return new Voiced(out);
    }

    /** Toca os pedacos em ordem (pula um que falhou); {@code onDone} roda ao fim, ou se o mob morrer no meio. Chamar na thread do servidor. */
    public static void play(Speaker sp, MinecraftServer server, Mob mob, Voiced voiced, Runnable onDone) {
        next(sp, server, mob, voiced.parts(), 0, onDone);
    }

    private static void next(Speaker sp, MinecraftServer server, Mob mob, List<CompletableFuture<Optional<short[]>>> parts, int i, Runnable onDone) {
        if (i >= parts.size() || !mob.isAlive() || mob.isRemoved()) {
            onDone.run();
            return;
        }
        parts.get(i).orTimeout(PART_TIMEOUT_S, TimeUnit.SECONDS).whenComplete((o, err) -> server.execute(() -> {
            if (err != null || o == null || o.isEmpty() || !mob.isAlive() || mob.isRemoved()) {
                next(sp, server, mob, parts, i + 1, onDone); // este pedaco falhou: segue para o proximo
                return;
            }
            sp.speak(mob, o.get(), () -> server.execute(() -> next(sp, server, mob, parts, i + 1, onDone)));
        }));
    }
}
