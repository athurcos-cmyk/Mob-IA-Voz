package br.com.mobvoz.target;

import java.util.UUID;

/**
 * Folga na mira: quem esta no centro da mira vira o alvo; se a mira escapar (a criatura anda, o jogador mexe o mouse), o alvo anterior fica por
 * {@link #KEEP_TICKS} se ainda estiver perto e visivel. Assim o jogador nao precisa seguir o bicho com o mouse enquanto fala. Puro e testavel.
 */
public final class StickyTarget {
    public static final long KEEP_TICKS = 100; // 5 s
    private UUID current;
    private long lastAimedTick;

    /**
     * @param aimed alvo da mira agora (ou null); @param stillValid o alvo anterior ainda esta perto, vivo e visivel?
     * @return o alvo a usar neste tick
     */
    public UUID choose(UUID aimed, long tick, boolean stillValid) {
        if (aimed != null) {
            current = aimed;
            lastAimedTick = tick;
            return aimed;
        }
        if (current != null && stillValid && tick - lastAimedTick <= KEEP_TICKS) {
            return current;
        }
        current = null;
        return null;
    }

    public UUID current() {
        return current;
    }

    public void clear() {
        current = null;
    }
}
