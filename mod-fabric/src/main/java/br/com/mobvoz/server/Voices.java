package br.com.mobvoz.server;

import br.com.mobvoz.profile.Profiles;
import java.util.UUID;

/**
 * Voz da criatura: a do perfil (Piper pt_BR) + tom individual estavel, mais a ESPECIE e o INDIVIDUO (0 a 63) para o sintetizador montar uma mistura propria de
 * vozes por especie e uma variacao por individuo (duas vacas nao soam iguais), e a EMOCAO da fala, que muda um pouco tom e velocidade.
 */
public record Voices(String voice, double pitch, double speed, String species, int bucket, String emotion) {
    public static final int BUCKETS = 64;

    public Voices(String voice, double pitch, double speed) {
        this(voice, pitch, speed, "", 0, "");
    }

    public static Voices forProfile(String profileKey, UUID identity) {
        Profiles.Profile p = Profiles.of(profileKey);
        int hash = (int) ((identity.getMostSignificantBits() ^ identity.getLeastSignificantBits()) ^ (identity.getLeastSignificantBits() >>> 32));
        return new Voices(p.voice(), Profiles.individualPitch(p, identity), p.speed(), profileKey, Math.floorMod(hash, BUCKETS), "");
    }

    /** A mesma voz, com a emocao desta fala (a IA devolve neutro, alegre, triste, irritado...). */
    public Voices withEmotion(String e) {
        return new Voices(voice, pitch, speed, species, bucket, e == null ? "" : e);
    }
}
