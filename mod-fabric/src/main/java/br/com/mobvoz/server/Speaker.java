package br.com.mobvoz.server;

import net.minecraft.world.entity.Mob;

/** Saida de voz espacial. Implementada com o Simple Voice Chat; abstrata para o servidor nao depender dele. */
public interface Speaker {
    /**
     * Fala o PCM (int16, 48 kHz, mono) a partir da posicao do mob, seguindo-o. {@code onDone} roda quando termina
     * ou e interrompido (mob morto/descarregado, parada do servidor), em qualquer thread.
     */
    void speak(Mob mob, short[] pcm, Runnable onDone);

    /** Interrompe a fala em andamento deste mob, se houver. */
    void cancel(Mob mob);
}
