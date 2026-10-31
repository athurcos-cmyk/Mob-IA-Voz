package br.com.mobvoz.server;

import br.com.mobvoz.profile.Profiles;
import java.util.UUID;

/** Voz por perfil (Piper pt_BR, modelos CC0) + variacao individual estavel de tom. */
public record Voices(String voice, double pitch, double speed) {
    public static Voices forProfile(String profileKey, UUID identity) {
        Profiles.Profile p = Profiles.of(profileKey);
        return new Voices(p.voice(), Profiles.individualPitch(p, identity), p.speed());
    }
}
