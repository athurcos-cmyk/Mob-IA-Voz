package br.com.mobvoz.client.voice;

import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.net.UtterancePayload;
import br.com.mobvoz.voice.VoiceSegmenter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrada de voz do cliente: frames PCM do Simple Voice Chat -> consentimento + VAD -> enunciado ->
 * STT no sidecar local (assincrono) -> pacote ao servidor com o texto e o UNICO alvo travado na fala.
 * O audio so existe em memoria ate a transcricao; nunca vai ao disco nem aos logs (AGENTS.md #7).
 */
public final class VoiceInput {
    public static final Logger LOGGER = LoggerFactory.getLogger("mobvoz.voice");
    /** Estado local alem dos estados do servidor (StatusPayload). */
    public static final int UNDERSTANDING = 10;
    public static final int NOT_UNDERSTOOD = 11;
    public static final int NO_SERVER = 12;

    private static final SidecarClient SIDECAR = SidecarClient.local();
    private static volatile UUID target;
    private static final AtomicInteger detected = new AtomicInteger();
    private static final AtomicInteger sent = new AtomicInteger();
    private static final VoiceSegmenter SEGMENTER = VoiceSegmenter.withDefaults(VoiceInput::onUtterance);

    private static volatile int status = StatusPayload.IDLE;
    private static volatile long statusSinceMs;
    /** Frames de audio de entidade recebidos do SVC por entidade (diagnostico/teste). */
    private static final Map<UUID, AtomicInteger> receivedFrames = new ConcurrentHashMap<>();

    private VoiceInput() {
    }

    public static void setTarget(UUID id) {
        target = id;
    }

    /** Chamado na thread do microfone do SVC. Sem consentimento retorna antes de tocar no audio. */
    public static void onFrame(short[] pcm) {
        if (!VoiceConsent.granted()) {
            return;
        }
        // sem ninguem na mira, mas com criatura de apelido por perto: grava assim mesmo (so segue se a fala CHAMAR alguem pelo nome)
        UUID t = target != null ? target : (MobVozClient.namedNearby().isEmpty() ? null : br.com.mobvoz.target.NameCall.NAMED_SENTINEL);
        SEGMENTER.onFrame(pcm.clone(), System.currentTimeMillis(), t);
    }

    /** Chamado no tick do cliente. */
    public static void tick() {
        if (VoiceConsent.granted()) {
            SEGMENTER.poll(System.currentTimeMillis());
        }
        // Estados transitorios nao ficam presos na tela.
        int s = status;
        if (s == UNDERSTANDING && System.currentTimeMillis() - statusSinceMs > 60_000) {
            status = StatusPayload.IDLE; // seguranca: o servidor nunca respondeu
        }
        if ((s == StatusPayload.ERROR || s == StatusPayload.BUSY || s == NOT_UNDERSTOOD || s == NO_SERVER)
                && System.currentTimeMillis() - statusSinceMs > 3000) {
            status = StatusPayload.IDLE;
        }
    }

    public static void reset() {
        SEGMENTER.reset();
    }

    public static boolean listening() {
        return SEGMENTER.isListening();
    }

    public static int detectedCount() {
        return detected.get();
    }

    public static int sentCount() {
        return sent.get();
    }

    public static int status() {
        return status;
    }

    public static void setStatus(int s) {
        status = s;
        statusSinceMs = System.currentTimeMillis();
    }

    public static void onEntityAudio(UUID entity, short[] frame) {
        receivedFrames.computeIfAbsent(entity, k -> new AtomicInteger()).incrementAndGet();
    }

    public static int receivedFrames(UUID entity) {
        AtomicInteger n = receivedFrames.get(entity);
        return n == null ? 0 : n.get();
    }

    private static void onUtterance(VoiceSegmenter.Utterance u) {
        int n = detected.incrementAndGet();
        LOGGER.info("fala #{} detectada: {} ms, alvo {}", n, u.durationMs(), u.target());
        Minecraft mc = Minecraft.getInstance();
        if (!ClientPlayNetworking.canSend(UtterancePayload.TYPE)) {
            setStatus(NO_SERVER); // servidor sem o mod: nada e transcrito nem enviado
            return;
        }
        setStatus(UNDERSTANDING);
        ByteBuffer buf = ByteBuffer.allocate(u.pcm().length * 2).order(ByteOrder.LITTLE_ENDIAN);
        buf.asShortBuffer().put(u.pcm());
        long t0 = System.nanoTime();
        SIDECAR.transcribe(buf.array()).thenAccept(text -> mc.execute(() -> {
            br.com.mobvoz.config.Metrics.sttMs.set((System.nanoTime() - t0) / 1_000_000);
            if (text.isEmpty()) {
                setStatus(NOT_UNDERSTOOD);
                return;
            }
            var call = br.com.mobvoz.target.NameCall.pick(text.get(), MobVozClient.namedNearby());
            if (call.isEmpty() && br.com.mobvoz.target.NameCall.NAMED_SENTINEL.equals(u.target())) {
                setStatus(StatusPayload.IDLE); // falou perto de um bicho com apelido, sem chamar ninguem: descarta
                return;
            }
            sent.incrementAndGet();
            ClientPlayNetworking.send(call.isPresent() ? new UtterancePayload(call.get().id(), text.get(), false, true)
                    : new UtterancePayload(u.target(), text.get(), false));
        }));
    }
}
