package br.com.mobvoz.server;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Quem autorizou voz neste servidor (em memoria: reconectar exige reenviar; nunca persiste consentimento por conta propria). */
public final class ConsentRegistry {
    private static final Set<UUID> consented = ConcurrentHashMap.newKeySet();

    private ConsentRegistry() {
    }

    public static void set(UUID player, boolean on) {
        if (on) {
            consented.add(player);
        } else {
            consented.remove(player);
        }
    }

    public static boolean has(UUID player) {
        return consented.contains(player);
    }

    public static void clear() {
        consented.clear();
    }
}
