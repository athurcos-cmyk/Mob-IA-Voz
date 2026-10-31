package br.com.mobvoz.social;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Orcamento das conversas mob-mob (fase 9, PURO): evita spam e carga. Regras: jogador falando tem prioridade; uma cena por
 * regiao por vez com cooldown; cooldown por criatura; teto global de cenas por minuto (combate pesado nao vira festa).
 */
public final class ChatterScheduler {
    public record Config(long regionCooldownTicks, long mobCooldownTicks, int maxScenesPerMinute, int maxTurns) {
        public static final Config DEFAULT = new Config(1200, 2400, 3, 3);
    }

    private final Config cfg;
    private final Map<String, Long> regionNext = new HashMap<>();
    private final Map<UUID, Long> mobNext = new HashMap<>();
    private final ArrayDeque<Long> recent = new ArrayDeque<>();
    private long scenes;

    public ChatterScheduler(Config cfg) {
        this.cfg = cfg;
    }

    public Config config() {
        return cfg;
    }

    public long scenesApproved() {
        return scenes;
    }

    /** Regiao = celula de 32x32 blocos por dimensao. */
    public static String region(String dimension, double x, double z) {
        return dimension + ":" + ((int) Math.floor(x) >> 5) + ":" + ((int) Math.floor(z) >> 5);
    }

    /** Aprova (e reserva) uma cena entre dois mobs, ou nega. */
    public synchronized boolean tryAcquire(String region, UUID a, UUID b, long now, boolean playerConversationActive) {
        if (playerConversationActive) {
            return false; // o jogador tem prioridade sobre fala ambiente
        }
        if (now < regionNext.getOrDefault(region, 0L) || now < mobNext.getOrDefault(a, 0L) || now < mobNext.getOrDefault(b, 0L)) {
            return false;
        }
        while (!recent.isEmpty() && now - recent.peekFirst() >= 1200) {
            recent.pollFirst();
        }
        if (recent.size() >= cfg.maxScenesPerMinute()) {
            return false;
        }
        recent.addLast(now);
        regionNext.put(region, now + cfg.regionCooldownTicks());
        mobNext.put(a, now + cfg.mobCooldownTicks());
        mobNext.put(b, now + cfg.mobCooldownTicks());
        scenes++;
        return true;
    }

    /** Limpeza de entradas vencidas (chamar de vez em quando). */
    public synchronized void prune(long now) {
        regionNext.values().removeIf(t -> t <= now);
        mobNext.values().removeIf(t -> t <= now);
    }
}
