package br.com.mobvoz.memory;

import br.com.mobvoz.memory.MobMemory.Claim;
import br.com.mobvoz.memory.MobMemory.Fact;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** O que uma criatura sabe do jogador, em linhas legiveis (comando {@code /mobvoz memoria}). PURO: so formata o que o servidor guardou. */
public final class MemoryReport {
    /** Tudo o que o relatorio precisa. {@code villageGrudge}: 0..1 da desconfianca coletiva da aldeia (0 = nenhuma ou nao se aplica). */
    public record View(String name, String species, String playerName, MobMemory mem, UUID player, long nowTick, double warmth, double need,
                       String difficulty, double villageGrudge) {
    }

    private MemoryReport() {
    }

    public static List<String> lines(View v) {
        List<String> out = new ArrayList<>();
        String who = v.name().isEmpty() || v.name().equalsIgnoreCase(v.species()) ? v.species() : v.name() + " (" + v.species() + ")";
        out.add("§e=== Memória de " + who + " ===§r");
        var rel = v.mem().relationOf(v.player());
        boolean known = v.mem().knows(v.player());
        out.add("Conhece você: " + (known ? "sim" : "não") + (v.playerName().isEmpty() ? (known ? " (ainda não sabe o seu nome)" : "") : "; chama você de " + v.playerName()));
        out.add("Confiança " + pct(rel.trust()) + " | afinidade " + pct(rel.affinity()) + " | medo " + Math.round(rel.fear() * 100) + "  (de -100 a 100; medo de 0 a 100)");
        double p = v.need() <= 0 ? 1.0 : Math.max(0, Math.min(1, v.warmth() / v.need()));
        out.add("Aceita te seguir: " + Math.round(p * 100) + "%" + (v.need() <= 0 ? " (basta não desconfiar)" : String.format(" (precisa de %.2f de calor; dificuldade %s)", v.need(), v.difficulty())));
        if (v.villageGrudge() > 0.005) {
            out.add("Fama na aldeia: desconfia de você " + Math.round(v.villageGrudge() / 0.6 * 100) + "% do máximo (perde força com o tempo)");
        }
        List<Fact> mine = v.mem().facts().stream().filter(f -> f.player().equals(v.player())).toList();
        if (!mine.isEmpty()) {
            out.add("§7Fatos que ela lembra (verificados pelo jogo):§r");
            for (Fact f : mine) {
                out.add(" - " + label(f) + " " + MemoryRules.ago(v.nowTick() - f.tick()));
            }
        }
        List<Claim> claims = v.mem().claims().stream().filter(c -> c.player().equals(v.player())).toList();
        if (!claims.isEmpty()) {
            out.add("§7O que você afirmou, e no que ela acredita (nada disso é fato):§r");
            for (Claim c : claims) {
                out.add(" - \"" + c.text() + "\" → " + stance(c.stance()));
            }
        }
        if (!known && mine.isEmpty() && claims.isEmpty()) {
            out.add("Ela ainda não tem nenhuma lembrança de você.");
        }
        return out;
    }

    static String label(Fact f) {
        String d = f.detail().isEmpty() ? "" : " (" + f.detail() + ")";
        return switch (f.type()) {
            case "FIRST_MET" -> "Primeiro encontro";
            case "GIFT" -> "Você deu de presente" + d;
            case "TRADE" -> "Troca justa" + d;
            case "TAMED" -> "Aceitou ser sua";
            case "HONEST_RETURN" -> "Você devolveu, sem ser pedido, o que não era seu" + d;
            case "PACT_BROKEN" -> "Você quebrou um pacto de paz";
            case "THREAT_SPOKEN" -> "Você a ameaçou com palavras";
            case "COMFORTED" -> "Você a acalmou";
            case "DAMAGED_BY_PLAYER" -> "Você a machucou";
            case "CONVERSION" -> "Ela se transformou" + d;
            default -> f.type().toLowerCase().replace('_', ' ');
        };
    }

    static String stance(MobMemory.Stance s) {
        return switch (s) {
            case BELIEVES -> "acredita";
            case SUSPECTS -> "desconfia, mas está curiosa";
            case DOUBTS -> "duvida";
            case DISPROVED -> "rejeita";
            case UNKNOWN -> "ainda não decidiu";
        };
    }

    private static String pct(float v) {
        return Math.round(v * 100) + "";
    }
}
