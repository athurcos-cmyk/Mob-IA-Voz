package br.com.mobvoz.memory;

import br.com.mobvoz.profile.Profiles;
import java.util.UUID;

/**
 * Tracos estaveis: tendencia do perfil da especie/profissao + variacao por individuo (+-1). Aqui so credulidade
 * (em quem a criatura acredita); a fase 8 deixa os demais tracos no perfil.
 */
public final class Traits {
    public static final int LOW = 0;
    public static final int MEDIUM = 1;
    public static final int HIGH = 2;

    private Traits() {
    }

    public static int credulity(String profileKey, UUID identity) {
        int shift = switch ((int) (identity.getLeastSignificantBits() & 3)) {
            case 0 -> -1;
            case 3 -> 1;
            default -> 0;
        };
        return Math.max(LOW, Math.min(HIGH, Profiles.of(profileKey).credulity() + shift));
    }
}
