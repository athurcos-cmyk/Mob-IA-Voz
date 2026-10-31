package br.com.mobvoz.config;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Metricas numericas (docs/06): STT_MS, LLM_MS, TTS_MS, pipeline total, sessoes e falas ambiente. Nenhum texto/audio de
 * jogador entra aqui — so numeros — entao e seguro registrar em log e mostrar em /mobvoz status.
 */
public final class Metrics {
    public static final AtomicLong sttMs = new AtomicLong(-1);
    public static final AtomicLong llmMs = new AtomicLong(-1);
    public static final AtomicLong ttsMs = new AtomicLong(-1);
    public static final AtomicLong pipelineMs = new AtomicLong(-1);
    public static final AtomicLong conversations = new AtomicLong();
    public static final AtomicLong failures = new AtomicLong();

    private Metrics() {
    }

    public static String summary(int activeSessions, int npcLines) {
        return String.format("STT_MS=%d LLM_MS=%d TTS_MS=%d PIPELINE_MS=%d ACTIVE_SESSIONS=%d CONVERSAS=%d FALHAS=%d NPC_FALAS=%d",
                sttMs.get(), llmMs.get(), ttsMs.get(), pipelineMs.get(), activeSessions, conversations.get(), failures.get(), npcLines);
    }
}
