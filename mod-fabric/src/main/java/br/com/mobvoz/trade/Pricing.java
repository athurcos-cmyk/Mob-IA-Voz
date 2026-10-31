package br.com.mobvoz.trade;

import br.com.mobvoz.trade.TradeCatalog.Recipe;
import java.util.Random;
import java.util.UUID;

/**
 * Preco variavel e DETERMINISTA de uma oferta (puro, testavel). Parte do preco-base da receita (valor da saida x margem) e aplica tres
 * ajustes limitados: confianca no jogador, humor da criatura (fixo por criatura+receita+dia de jogo, entao insistir nao muda nada) e
 * barganha por argumento. Invariantes: nunca abaixo do valor do que ela entrega (sem criar valor), nunca abaixo de 70% nem acima de
 * 125% do preco-base. O preco e travado na oferta; este codigo so calcula.
 */
public final class Pricing {
    static final double MOOD = 0.10;
    static final double RELATION = 0.15;
    static final double MIN_MULT = 0.75;
    static final double MAX_MULT = 1.25;
    static final double HAGGLE_FLOOR_MULT = 0.70;

    private Pricing() {
    }

    /** @param why frase-fato (em pt-BR) para a criatura explicar o preco; vazia se o preco e o normal. */
    public record Quote(double unitPrice, String why) {
    }

    static double valueFloor(Recipe r) {
        return r.outputCount() * Values.value(r.outputItem());
    }

    static double roundUpTenths(double p) {
        return Math.ceil(p * 10 - 1e-9) / 10.0;
    }

    /** @param warmth -1..1 (quanto a criatura gosta do jogador); @param day dia de jogo (tick / 24000). */
    public static Quote quote(Recipe r, double warmth, UUID mob, long day) {
        double mood = (new Random(mob.getMostSignificantBits() * 31 + mob.getLeastSignificantBits() * 17 + r.id().hashCode() * 131L + day * 7919L)
                .nextDouble() * 2 - 1) * MOOD;
        double w = Math.max(-1, Math.min(1, warmth));
        double rel = w >= 0 ? -RELATION * w : RELATION * -w;
        double m = Math.max(MIN_MULT, Math.min(MAX_MULT, 1 + mood + rel));
        double price = roundUpTenths(Math.max(valueFloor(r), Math.min(r.price() * MAX_MULT, r.price() * m)));
        String why = "";
        if (rel < -0.04) {
            why = "você confia no jogador e fez um preço de amigo";
        } else if (rel > 0.04) {
            why = "você desconfia um pouco do jogador e cobra mais";
        } else if (mood < -0.04) {
            why = "hoje você está de bom humor e cobra um pouco menos";
        } else if (mood > 0.04) {
            why = "hoje você está mais exigente e cobra um pouco mais";
        }
        return new Quote(price, price == r.price() ? "" : why);
    }

    /**
     * Barganha com um argumento. {@code strength} 0..2 vem do analisador (so opina); o efeito e limitado aqui pela credulidade e pela
     * confianca. Devolve o novo preco por unidade em decimos (igual ao atual se nao houve efeito).
     */
    public static int haggle(Recipe r, int currentTenths, int strength, int credulity, double trust) {
        if (strength <= 0) {
            return currentTenths;
        }
        double cut = (strength >= 2 ? 0.12 : 0.06) * (0.6 + 0.2 * Math.max(0, Math.min(2, credulity))) + 0.03 * Math.max(0, Math.min(1, trust));
        double lowest = Math.max(valueFloor(r), r.price() * HAGGLE_FLOOR_MULT);
        double now = currentTenths / 10.0;
        double next = roundUpTenths(Math.max(lowest, now - r.price() * cut));
        return (int) Math.round(Math.min(now, next) * 10);
    }

    /** Menor preco por unidade que esta receita admite numa barganha. */
    public static double lowest(Recipe r) {
        return roundUpTenths(Math.max(valueFloor(r), r.price() * HAGGLE_FLOOR_MULT));
    }
}
