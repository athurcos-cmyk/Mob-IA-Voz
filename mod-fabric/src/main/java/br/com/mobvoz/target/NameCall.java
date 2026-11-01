package br.com.mobvoz.target;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Chamar uma criatura PELO NOME, sem mirar nela ("Mimosa, vem aqui!", "Me segue, Rex!"). PURO: decide se a fala se dirige a um nome.
 * O nome tem de aparecer inteiro nas primeiras palavras da fala ou nas ultimas (vocativo); falar DE alguem no meio da frase
 * ("ontem eu vi a Mimosa na colina") nao e chamar. O servidor revalida tudo (distancia, nome, texto).
 */
public final class NameCall {
    /** Distancia maxima (blocos) para chamar pelo nome. */
    public static final double RANGE = 32.0;
    /** Marca "fala sem alvo na mira, mas ha criatura com nome por perto": a fala so segue se chamar alguem pelo nome. */
    public static final UUID NAMED_SENTINEL = new UUID(0L, 0L);

    private static final int LEADING_WORDS = 4;
    private static final int TRAILING_WORDS = 3;

    public record Candidate(UUID id, String name, double distance) {
    }

    private NameCall() {
    }

    static List<String> words(String s) {
        String n = Normalizer.normalize(s == null ? "" : s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        List<String> out = new ArrayList<>();
        for (String w : n.split("[^a-z0-9]+")) {
            if (!w.isEmpty()) {
                out.add(w);
            }
        }
        return out;
    }

    /** A fala se dirige a este nome? */
    public static boolean addresses(String text, String name) {
        List<String> t = words(text);
        List<String> n = words(name);
        if (n.isEmpty() || t.size() < n.size()) {
            return false;
        }
        for (int i = 0; i + n.size() <= t.size(); i++) {
            if (t.subList(i, i + n.size()).equals(n)) {
                boolean leading = i < LEADING_WORDS;
                boolean trailing = i + n.size() > t.size() - TRAILING_WORDS;
                return leading || trailing;
            }
        }
        return false;
    }

    /** Dentre as criaturas com nome ao alcance, a que a fala chama (nome mais longo primeiro; empate = a mais perto). */
    public static Optional<Candidate> pick(String text, List<Candidate> named) {
        return named.stream()
                .filter(c -> c.distance() <= RANGE && addresses(text, c.name()))
                .min(Comparator.comparingInt((Candidate c) -> -words(c.name()).size()).thenComparingDouble(Candidate::distance));
    }
}
