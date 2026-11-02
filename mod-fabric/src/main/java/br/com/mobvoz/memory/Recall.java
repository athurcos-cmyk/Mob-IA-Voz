package br.com.mobvoz.memory;

import br.com.mobvoz.memory.MobMemory.Fact;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Lembrancas: de vez em quando a criatura recebe UM momento marcante que viveu com o jogador (presente, troca, consolo, devolucao honesta, pancada, quebra de pacto)
 * como sugestao de assunto, para comentar de passagem ("lembra daquela cenoura?"). Para nao virar repeticao: so uma vez a cada 5 minutos por criatura, o mesmo momento
 * so volta depois de 30 minutos, sorteio de 30% por fala, e nunca um momento de menos de 1 minuto atras (esse a conversa ja trata). Os fatos continuam sendo so os verificados.
 */
public final class Recall {
    static final long MOB_COOLDOWN = 5 * 60 * 20;
    static final long FACT_COOLDOWN = 30 * 60 * 20;
    static final long MIN_AGE = 60 * 20;
    static final double CHANCE = 0.3;
    private static final Set<String> NOTABLE = Set.of("GIFT", "TRADE", "COMFORTED", "HONEST_RETURN", "DAMAGED_BY_PLAYER", "PACT_BROKEN", "TAMED");

    private static final Map<UUID, Long> LAST_BY_MOB = new HashMap<>();
    private static final Map<String, Long> LAST_BY_FACT = new HashMap<>();

    private Recall() {
    }

    /** A linha de lembranca para esta fala, ou vazio. Registra o uso (a criatura "gastou" essa lembranca). */
    public static synchronized Optional<String> pick(UUID mob, MobMemory mem, UUID speaker, long now, Random rnd) {
        Long lastMob = LAST_BY_MOB.get(mob);
        if (lastMob != null && now - lastMob < MOB_COOLDOWN && now >= lastMob) {
            return Optional.empty();
        }
        if (rnd.nextDouble() >= CHANCE) {
            return Optional.empty();
        }
        Fact best = null;
        String bestKey = "";
        for (Fact f : mem.facts()) {
            String key = mob + ":" + f.type() + ":" + f.tick();
            Long used = LAST_BY_FACT.get(key);
            boolean fresh = used == null || now < used || now - used >= FACT_COOLDOWN;
            if (f.player().equals(speaker) && NOTABLE.contains(f.type()) && now - f.tick() >= MIN_AGE && fresh && (best == null || rnd.nextBoolean())) {
                best = f;
                bestKey = key;
            }
        }
        if (best == null) {
            return Optional.empty();
        }
        String line = MemoryRules.factLine(best, speaker, now);
        if (line == null) {
            return Optional.empty();
        }
        LAST_BY_MOB.put(mob, now);
        LAST_BY_FACT.put(bestKey, now);
        return Optional.of("LEMBRANÇA (opcional): " + line
                + " Só comente isso se tiver ligação REAL com o que o jogador acabou de dizer ou perguntar; se o assunto for outro, não toque nesse momento. Se comentar, uma frase curta, de passagem. Nunca insista nem force.");
    }

    /** Testes. */
    public static synchronized void reset() {
        LAST_BY_MOB.clear();
        LAST_BY_FACT.clear();
    }
}
