package br.com.mobvoz.trade;

import br.com.mobvoz.profile.Profiles.Profile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Negociacao PURA e deterministica (o LLM so descreve o que o jogador disse; quem decide e esta classe):
 * quanto cada item interessa a criatura, se um argumento a convence (com limites) e quais itens, em que quantidade, fecham o
 * negocio sem levar nada alem do pedido.
 */
public final class Negotiation {
    public static final double MAX_PERSUADED_INTEREST = 0.8;
    public static final int MAX_PERSUASIONS = 3;
    private static final double EPS = 1e-6;

    private Negotiation() {
    }

    /** 0..1. Itens que ela aceita = 1. Os que ela ODEIA = 0 sempre. Os demais = quanto foi convencida (0 se nunca). */
    public static double interest(Profile p, String item, Map<String, Integer> persuadedMilli) {
        if (p.dislikes().contains(item)) {
            return 0;
        }
        if (p.wants().contains(item)) {
            return 1.0;
        }
        return Math.min(MAX_PERSUADED_INTEREST, persuadedMilli.getOrDefault(item, 0) / 1000.0);
    }

    public enum Verdict { CONVINCED, PARTIAL, NO_ARGUMENT, AVERSION, FATIGUE, ALREADY_INTERESTED }

    public record Persuasion(Verdict verdict, int newMilli) {
    }

    /**
     * @param credulity 0..2 (do perfil + individuo); @param trust -1..1; @param strength 0 sem argumento, 1 plausivel, 2 convincente
     *                  (quem pontua e o analisador do LLM; o EFEITO e limitado aqui); @param tries tentativas ja feitas nesta oferta.
     */
    public static Persuasion persuade(Profile p, String item, int credulity, double trust, int strength, int currentMilli, int tries) {
        if (p.wants().contains(item)) {
            return new Persuasion(Verdict.ALREADY_INTERESTED, 1000);
        }
        if (p.dislikes().contains(item)) {
            return new Persuasion(Verdict.AVERSION, 0); // aversao nao se convence por conversa
        }
        if (tries >= MAX_PERSUASIONS) {
            return new Persuasion(Verdict.FATIGUE, currentMilli);
        }
        if (strength <= 0) {
            return new Persuasion(Verdict.NO_ARGUMENT, currentMilli);
        }
        double base = strength >= 2 ? 0.55 : 0.30;
        double gain = base * (0.6 + 0.2 * Math.max(0, Math.min(2, credulity))) + 0.15 * Math.max(0, trust);
        int now = (int) Math.round(Math.min(MAX_PERSUADED_INTEREST, currentMilli / 1000.0 + gain) * 1000);
        return new Persuasion(now >= 400 ? Verdict.CONVINCED : Verdict.PARTIAL, now);
    }

    /** Uma pilha de itens do jogador no chao. {@code ref} identifica a entidade de quem chama. */
    public record Stack(int ref, String item, int count, double dist) {
    }

    public record Take(int ref, String item, int count) {
    }

    /** @param value valor total tomado; @param enough fechou? ; @param leftovers pilhas INTERESSANTES que sobraram (para devolver). */
    public record Selection(List<Take> takes, double value, boolean enough, List<Take> leftovers, double available) {
    }

    /** Tudo-ou-nada: so toma o MINIMO de unidades (mais perto primeiro) para atingir {@code price}; o resto e devolvido. */
    public static Selection select(Profile p, Map<String, Integer> persuadedMilli, List<Stack> stacks, double price) {
        List<Stack> sorted = new ArrayList<>(stacks);
        sorted.sort((a, b) -> Double.compare(a.dist(), b.dist()));
        double available = 0;
        for (Stack s : sorted) {
            available += unit(p, s.item(), persuadedMilli) * s.count();
        }
        if (available + EPS < price) {
            return new Selection(List.of(), 0, false, List.of(), available);
        }
        List<Take> takes = new ArrayList<>();
        List<Take> left = new ArrayList<>();
        double acc = 0;
        for (Stack s : sorted) {
            double u = unit(p, s.item(), persuadedMilli);
            if (u <= 0) {
                continue;
            }
            int n = 0;
            if (acc + EPS < price) {
                n = Math.min(s.count(), (int) Math.ceil((price - acc - EPS) / u));
                acc += n * u;
                takes.add(new Take(s.ref(), s.item(), n));
            }
            if (s.count() - n > 0) {
                left.add(new Take(s.ref(), s.item(), s.count() - n));
            }
        }
        return new Selection(takes, acc, true, left, available);
    }

    static double unit(Profile p, String item, Map<String, Integer> persuadedMilli) {
        return Values.value(item) * interest(p, item, persuadedMilli);
    }

    /** Valor aceito de uma lista de itens oferecidos (por exemplo, falados) sob o interesse atual. */
    public static double acceptedValue(Profile p, Map<String, Integer> persuadedMilli, Map<String, Integer> items) {
        double v = 0;
        for (var e : items.entrySet()) {
            v += unit(p, e.getKey(), persuadedMilli) * e.getValue();
        }
        return v;
    }
}
