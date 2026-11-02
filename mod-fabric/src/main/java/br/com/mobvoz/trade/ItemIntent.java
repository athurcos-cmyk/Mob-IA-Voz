package br.com.mobvoz.trade;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * O jogador raramente diz o nome exato do item: "uma armadura muito boa", "uma espada", "botas boas", "a melhor picareta", "uma comida". Aqui o servidor ESCOLHE um item
 * concreto a partir do que ele disse: a CATEGORIA (armadura, espada, picareta...), o MATERIAL se ele citou (ferro, diamante...) e, se nao citou, a QUALIDADE pelo jeito de
 * falar (simples = couro/pedra, boa = ferro, muito boa = diamante, a melhor = netherite; sem adjetivo = ferro). Puro e testavel; o resultado ainda passa pelo dicionario
 * (Values.known), entao nunca sai um item que o jogo nao conhece.
 */
final class ItemIntent {
    private ItemIntent() {
    }

    enum Tier { LOW, MID, HIGH, MAX }

    /** categoria -> palavras (sem acento, minusculas, singular e plural). */
    private static final Map<String, Set<String>> CATEGORY = Map.ofEntries(
            Map.entry("chestplate", Set.of("armadura", "armaduras", "peitoral", "peitorais", "couraca", "couracas", "colete", "coletes")),
            Map.entry("helmet", Set.of("capacete", "capacetes", "elmo", "elmos")),
            Map.entry("leggings", Set.of("calca", "calcas", "perneira", "perneiras")),
            Map.entry("boots", Set.of("bota", "botas")),
            Map.entry("sword", Set.of("espada", "espadas", "arma", "armas", "lamina")),
            Map.entry("axe", Set.of("machado", "machados", "machadinha")),
            Map.entry("pickaxe", Set.of("picareta", "picaretas", "picao", "ferramenta", "ferramentas")),
            Map.entry("shovel", Set.of("pa", "pas", "pazinha")),
            Map.entry("hoe", Set.of("enxada", "enxadas")),
            Map.entry("food", Set.of("comida", "comidas", "refeicao", "lanche")),
            Map.entry("meat", Set.of("carne", "carnes")),
            Map.entry("wings", Set.of("asas", "asa")));

    private static final Map<String, String> MATERIAL = Map.ofEntries(
            Map.entry("ferro", "iron"), Map.entry("diamante", "diamond"), Map.entry("diamantes", "diamond"), Map.entry("ouro", "golden"), Map.entry("dourada", "golden"),
            Map.entry("dourado", "golden"), Map.entry("couro", "leather"), Map.entry("malha", "chainmail"), Map.entry("cota", "chainmail"), Map.entry("netherite", "netherite"),
            Map.entry("netherita", "netherite"), Map.entry("pedra", "stone"), Map.entry("madeira", "wooden"));

    private static final Set<String> MAX_WORDS = Set.of("melhor", "melhores", "suprema", "supremo", "lendaria", "lendario", "definitiva", "definitivo", "absurda", "absurdo", "insana", "insano");
    private static final Set<String> HIGH_WORDS = Set.of("otima", "otimo", "excelente", "forte", "poderosa", "poderoso", "top", "rara", "raro", "epica", "epico", "incrivel", "irada", "irado");
    private static final Set<String> GOOD_WORDS = Set.of("boa", "bom", "boas", "bons", "decente", "decentes", "resistente", "resistentes", "legal", "legais", "razoavel", "boazinha", "boazinho");
    private static final Set<String> LOW_WORDS = Set.of("simples", "basica", "basico", "fraca", "fraco", "barata", "barato", "qualquer", "ruim", "velha", "velho", "usada", "usado");
    private static final Set<String> INTENSIFIERS = Set.of("muito", "super", "mega", "ultra", "extremamente", "demais", "bastante");

    /** Palavras que reconhece como categoria, material ou qualidade (para corrigir erros de fala so entre palavras do jogo). */
    static Set<String> vocabulary() {
        java.util.HashSet<String> v = new java.util.HashSet<>();
        CATEGORY.values().forEach(v::addAll);
        v.addAll(MATERIAL.keySet());
        v.addAll(MAX_WORDS);
        v.addAll(HIGH_WORDS);
        v.addAll(GOOD_WORDS);
        v.addAll(LOW_WORDS);
        v.addAll(INTENSIFIERS);
        return v;
    }

    /** @param normalized frase ja normalizada (minuscula, sem acento, so letras e numeros). */
    static Optional<String> resolve(String normalized) {
        String[] t = normalized.split(" ");
        String category = null;
        String material = null;
        for (String w : t) {
            for (var e : CATEGORY.entrySet()) {
                if (category == null && e.getValue().contains(w)) {
                    category = e.getKey();
                }
            }
            if (material == null && MATERIAL.containsKey(w)) {
                material = MATERIAL.get(w);
            }
        }
        if (category == null) {
            return Optional.empty();
        }
        Tier tier = tierOf(t);
        String id = switch (category) {
            case "wings" -> "elytra";
            case "meat" -> List.of(t).contains("assada") || List.of(t).contains("cozida") || List.of(t).contains("frita") ? "cooked_beef" : "beef";
            case "food" -> switch (tier) {
                case LOW -> "bread";
                case MID -> "cooked_beef";
                case HIGH -> "golden_apple";
                case MAX -> "enchanted_golden_apple";
            };
            case "chestplate", "helmet", "leggings", "boots" -> armor(material, tier) + "_" + category;
            case "sword", "axe", "pickaxe", "shovel", "hoe" -> tool(material, tier) + "_" + category;
            default -> "";
        };
        if (!Values.known("minecraft:" + id) && !id.isEmpty()) { // combinacao que nao existe (ex.: enxada de cota de malha): cai no ferro
            id = id.replaceFirst("^[a-z]+_", "iron_");
        }
        return id.isEmpty() || !Values.known("minecraft:" + id) ? Optional.empty() : Optional.of("minecraft:" + id);
    }

    static Tier tierOf(String[] t) {
        Tier best = null;
        for (int i = 0; i < t.length; i++) {
            String w = t[i];
            if (MAX_WORDS.contains(w) || i > 0 && t[i - 1].equals("mais") && (HIGH_WORDS.contains(w) || w.equals("resistente") || w.equals("boa") || w.equals("bom"))) {
                return Tier.MAX; // "a melhor", "a mais forte"
            }
            if (HIGH_WORDS.contains(w)) {
                best = Tier.HIGH;
            } else if (GOOD_WORDS.contains(w)) {
                if (best == null) {
                    best = i > 0 && (INTENSIFIERS.contains(t[i - 1])) ? Tier.HIGH : Tier.MID; // "muito boa" = diamante; "boa" ou "bem boa" = ferro
                }
            } else if (LOW_WORDS.contains(w) && best == null) {
                best = Tier.LOW;
            }
        }
        return best == null ? Tier.MID : best;
    }

    private static String armor(String material, Tier tier) {
        if (material != null && !material.equals("wooden") && !material.equals("stone")) {
            return material;
        }
        return switch (tier) {
            case LOW -> "leather";
            case MID -> "iron";
            case HIGH -> "diamond";
            case MAX -> "netherite";
        };
    }

    private static String tool(String material, Tier tier) {
        if (material != null && !material.equals("leather") && !material.equals("chainmail")) {
            return material;
        }
        return switch (tier) {
            case LOW -> "stone";
            case MID -> "iron";
            case HIGH -> "diamond";
            case MAX -> "netherite";
        };
    }
}
