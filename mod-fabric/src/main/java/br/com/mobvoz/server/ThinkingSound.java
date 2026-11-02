package br.com.mobvoz.server;

import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.config.ServerConfig;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Mob;

/**
 * Disfarca a espera: se a resposta ainda nao comecou a tocar {@link #DELAY_MS} depois que a criatura recebeu a fala, ela solta um som curto
 * do jeito da especie ("Muuu?", "Hmm, peraí...") enquanto "pensa". Falas fixas e neutras (nao dizem nada sobre o assunto), geradas pelo mesmo TTS
 * e guardadas no cache de audio (a repeticao e instantanea). Se a resposta chegar antes, nada e dito; se chegar durante o som, espera ele acabar.
 */
public final class ThinkingSound {
    static final long DELAY_MS = 900;
    private static final long MAX_WAIT_S = 2;
    private static final Random RND = new Random();
    private static final Map<UUID, String> LAST = new ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicInteger PLAYED = new java.util.concurrent.atomic.AtomicInteger();

    /** Quantos sons de "pensando" tocaram nesta sessao (testes e diagnostico). */
    public static int played() {
        return PLAYED.get();
    }

    private static final Map<String, List<String>> POOLS = Map.ofEntries(
            Map.entry("cow", List.of("Muuu?", "Hmm, muuu...", "Deixa eu ruminar isso...", "Muu... peraí.")),
            Map.entry("mooshroom", List.of("Muuu?", "Hmm, muuu...", "Deixa eu ruminar isso...")),
            Map.entry("pig", List.of("Oinc?", "Hmm, oinc...", "Peraí, estou farejando uma resposta...", "Oinc, deixa eu pensar.")),
            Map.entry("sheep", List.of("Méé?", "Hmm, méé...", "Deixa eu pensar um pouquinho...", "Beee... peraí.")),
            Map.entry("chicken", List.of("Có?", "Có có... peraí.", "Hmm, deixa eu ciscar essa ideia...", "Có, um instante.")),
            Map.entry("horse", List.of("Prrr?", "Hmm... prrr.", "Deixa eu pensar, hein.")),
            Map.entry("donkey", List.of("Iá?", "Hmm... iá.", "Deixa eu pensar, hein.")),
            Map.entry("wolf", List.of("Auu?", "Hmm... grr.", "Deixa eu farejar isso...", "Au, peraí.")),
            Map.entry("cat", List.of("Miau?", "Hmm... miau.", "Deixa eu pensar... miau.", "Mrrr, peraí.")),
            Map.entry("villager", List.of("Hmm...", "Hum, deixa eu ver.", "Ah, peraí...", "Hmmmm, um instante.", "Deixa eu pensar um pouco.")),
            Map.entry("iron_golem", List.of("Hm. Um instante.", "Processando...", "Hmm. Analisando.")),
            Map.entry("zombie", List.of("Uuurgh...", "Hmm... uuurgh.", "Peraaaí...")),
            Map.entry("skeleton", List.of("Hmm... clac clac.", "Um instante...", "Hmm, deixa eu pensar...")),
            Map.entry("creeper", List.of("Peraí...", "Hmm... deixa eu ver.", "Um instante...")),
            Map.entry("enderman", List.of("Hmm... vwoop.", "Um instante...", "Hmm...")),
            Map.entry("warden", List.of("...", "Hmm.", "Um instante.")));
    private static final List<String> DEFAULT = List.of("Hmm...", "Ah, peraí...", "Deixa eu pensar...", "Hum...", "Um instante...");

    private ThinkingSound() {
    }

    /** Puro: o som de "pensando" desta especie, sem repetir o ultimo dito por esta criatura. */
    static String pick(String entityPath, String last, Random rnd) {
        List<String> pool = POOLS.getOrDefault(entityPath, DEFAULT);
        String s;
        int guard = 0;
        do {
            s = pool.get(rnd.nextInt(pool.size()));
        } while (s.equals(last) && pool.size() > 1 && ++guard < 8);
        return s;
    }

    /** Controle de um "pensando": {@link #beforeAnswer} roda a fala de resposta depois do som, se ele estiver tocando. Usar so na thread do servidor. */
    public static final class Handle {
        private boolean answered;
        private boolean playing;
        private Runnable waiting;

        /** A resposta ficou pronta: cancela um som ainda nao dito e, se ele esta tocando agora, espera acabar antes de {@code speakAnswer}. */
        public void beforeAnswer(Runnable speakAnswer) {
            answered = true;
            if (!playing) {
                speakAnswer.run();
            } else {
                waiting = speakAnswer; // o fim do som a dispara (ou o limite de espera)
            }
        }

        private void soundEnded() {
            playing = false;
            Runnable r = waiting;
            waiting = null;
            if (r != null) {
                r.run();
            }
        }
    }

    /** Sem som de "pensando" (desligado, sem voz ou sem sidecar): a resposta toca na hora. */
    public static final Handle NONE = new Handle();

    /**
     * Comeca a contar. O som ja e sintetizado agora (frase curta: rapido, e fica em cache); toca se, depois de {@link #DELAY_MS}, a resposta ainda nao veio.
     * Chamar na thread do servidor.
     */
    public static Handle begin(MinecraftServer server, Speaker speaker, SidecarClient sidecar, Mob mob, Voices voice, String entityPath) {
        if (speaker == null || !ServerConfig.current().thinkingSounds) {
            return NONE;
        }
        UUID id = mob.getUUID();
        String text = pick(entityPath, LAST.get(id), RND);
        LAST.put(id, text);
        Handle h = new Handle();
        CompletableFuture<Optional<short[]>> pcm = sidecar.synthesizeCached(text, voice.voice(), voice.pitch(), voice.speed());
        CompletableFuture<Void> delay = CompletableFuture.runAsync(() -> { }, CompletableFuture.delayedExecutor(DELAY_MS, TimeUnit.MILLISECONDS));
        delay.thenCombine(pcm, (d, o) -> o).whenComplete((o, err) -> server.execute(() -> {
            if (err != null || o == null || o.isEmpty() || h.answered || !mob.isAlive() || mob.isRemoved()) {
                return;
            }
            h.playing = true;
            PLAYED.incrementAndGet();
            speaker.speak(mob, o.get(), () -> server.execute(h::soundEnded));
            // rede de seguranca: se o fim do som nunca vier, a resposta nao fica presa
            CompletableFuture.runAsync(() -> { }, CompletableFuture.delayedExecutor(MAX_WAIT_S, TimeUnit.SECONDS)).thenRun(() -> server.execute(h::soundEnded));
        }));
        return h;
    }

    public static void reset() {
        LAST.clear();
    }
}
