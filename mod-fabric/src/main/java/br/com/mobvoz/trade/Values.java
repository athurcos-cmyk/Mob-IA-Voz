package br.com.mobvoz.trade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Valor de troca e nomes pt-BR dos itens (values.json). O LLM so propoe NOMES; aqui o servidor os resolve para ids reais. */
public final class Values {
    private static final Map<String, Double> VALUE = new HashMap<>();
    private static final Map<String, List<String>> NAMES = new HashMap<>();
    private static final Map<String, String> BY_NAME = new HashMap<>();
    /** Nome sem "de/do/da": "cabeca wither" acha "cabeca de wither". Chave que serve a 2 itens diferentes e descartada (ambigua). */
    private static final Map<String, String> LOOSE = new HashMap<>();
    private static final java.util.Set<String> LOOSE_AMBIGUOUS = new java.util.HashSet<>();

    static {
        try (InputStream in = Values.class.getResourceAsStream("/mobvoz/values.json")) {
            JsonObject items = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("items");
            for (var e : items.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                VALUE.put(e.getKey(), o.get("value").getAsDouble());
                List<String> pt = new java.util.ArrayList<>();
                o.getAsJsonArray("pt").forEach(x -> pt.add(x.getAsString()));
                NAMES.put(e.getKey(), pt);
                pt.forEach(n -> {
                    BY_NAME.put(norm(n), e.getKey());
                    String lk = loose(norm(n));
                    String prev = LOOSE.putIfAbsent(lk, e.getKey());
                    if (prev != null && !prev.equals(e.getKey())) {
                        LOOSE_AMBIGUOUS.add(lk);
                    }
                });
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("values.json invalido", e);
        }
    }

    private Values() {
    }

    static String loose(String normalized) {
        return normalized.replaceAll("\\b(de|do|da|dos|das)\\b", " ").replaceAll("\\s+", " ").trim();
    }

    public static String norm(String s) {
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static final java.util.Set<String> FLOWERS = java.util.Set.of("dandelion", "poppy", "blue_orchid", "allium", "azure_bluet",
            "red_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley", "sunflower");

    public static boolean isFlower(String id) {
        return id.startsWith("minecraft:") && FLOWERS.contains(id.substring(10));
    }

    /** O jogador pediu "uma flor" sem dizer qual? */
    public static boolean isGenericFlower(String spoken) {
        String n = norm(spoken).replaceFirst("^(uma|umas|uns|um|\\d+) ", "");
        return n.equals("flor") || n.equals("flores");
    }

    public static java.util.Set<String> ids() {
        return java.util.Collections.unmodifiableSet(VALUE.keySet());
    }

    public static boolean known(String id) {
        return VALUE.containsKey(id);
    }

    /** Valor unitario; itens fora da tabela valem 0 (a criatura nao os considera moeda). */
    public static double value(String id) {
        return VALUE.getOrDefault(id, 0.0);
    }

    /**
     * Nome falado -> id real ("picareta de diamante" -> minecraft:diamond_pickaxe). Entende o jeito de falar do jogador: nome exato e apelidos; sem "de"; pedido vago
     * ("uma armadura muito boa", "uma espada", "botas") em que o servidor ESCOLHE o item pela categoria, material e qualidade ({@link ItemIntent}); qualificativos soltos
     * ("armadura de ferro boa"); e erros de fala ("espada de dimante"). Vazio se o servidor nao conhece.
     */
    public static Optional<String> lookup(String spoken) {
        return lookup(norm(spoken), true);
    }

    private static Optional<String> lookup(String n, boolean allowFuzzy) {
        Optional<String> direct = direct(n);
        if (direct.isPresent()) {
            return direct;
        }
        if (allowFuzzy) { // "espada de dimante": corrige as palavras desconhecidas antes de decidir
            Optional<String> fixed = fuzzy(n.split(" "));
            if (fixed.isPresent()) {
                return fixed;
            }
        }
        Optional<String> intent = ItemIntent.resolve(n.replaceFirst("^(o|a|os|as|um|uma|uns|umas|\\d+) ", ""));
        if (intent.isPresent()) {
            return intent;
        }
        // "armadura de ferro boa": tira os qualificativos do fim e tenta de novo
        String[] words = n.replaceFirst("^(o|a|os|as|um|uma|uns|umas|\\d+) ", "").split(" ");
        int end = words.length;
        while (end > 1 && QUALIFIERS.contains(words[end - 1])) {
            end--;
        }
        if (end < words.length) {
            Optional<String> r = lookup(String.join(" ", java.util.Arrays.copyOf(words, end)), allowFuzzy);
            if (r.isPresent()) {
                return r;
            }
        }
        return allowFuzzy ? fuzzy(words) : Optional.empty();
    }

    /** Nome exato, com artigo/quantidade na frente, ou sem "de/do/da" (quando so um item tem esse nome). */
    private static Optional<String> direct(String n) {
        if (BY_NAME.containsKey(n)) {
            return Optional.of(BY_NAME.get(n));
        }
        String stripped = n.replaceFirst("^(o|a|os|as|um|uma|uns|umas|\\d+) ", "");
        String exact = BY_NAME.get(stripped);
        if (exact != null) {
            return Optional.of(exact);
        }
        String lk = loose(stripped);
        return LOOSE_AMBIGUOUS.contains(lk) ? Optional.empty() : Optional.ofNullable(LOOSE.get(lk));
    }

    private static final java.util.Set<String> QUALIFIERS = java.util.Set.of("boa", "bom", "boas", "bons", "melhor", "melhores", "nova", "novo", "novas", "novos",
            "legal", "legais", "qualquer", "completa", "completo", "inteira", "inteiro", "bonita", "bonito", "resistente", "forte", "rara", "raro", "decente", "top",
            "pra", "para", "mim", "favor", "porfavor", "muito", "bem", "bastante", "super", "mega", "demais", "excelente", "otima", "otimo", "simples");

    private static volatile java.util.Set<String> vocabulary;

    /** Palavras que existem nos nomes dos itens e no vocabulario de categorias: o chao para corrigir erro de fala. */
    private static java.util.Set<String> vocabulary() {
        java.util.Set<String> v = vocabulary;
        if (v == null) {
            v = new java.util.HashSet<>(ItemIntent.vocabulary());
            for (String name : BY_NAME.keySet()) {
                for (String w : name.split(" ")) {
                    if (w.length() >= 4) {
                        v.add(w);
                    }
                }
            }
            vocabulary = v;
        }
        return v;
    }

    /** Erros de reconhecimento de voz ("dimante", "composteria"): troca cada palavra desconhecida pela UNICA palavra conhecida a 1 letra (2 se longa); empate = nao adivinha. */
    private static Optional<String> fuzzy(String[] words) {
        java.util.Set<String> vocab = vocabulary();
        boolean changed = false;
        String[] fixed = words.clone();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.length() < 5 || vocab.contains(w) || w.matches("\\d+")) {
                continue;
            }
            int max = w.length() >= 8 ? 2 : 1;
            String best = null;
            int bestD = max + 1;
            boolean tie = false;
            for (String c : vocab) {
                if (Math.abs(c.length() - w.length()) > max) {
                    continue;
                }
                int d = distance(w, c, max);
                if (d < bestD) {
                    best = c;
                    bestD = d;
                    tie = false;
                } else if (d == bestD && d <= max) {
                    tie = true;
                }
            }
            if (best != null && !tie && bestD <= max) {
                fixed[i] = best;
                changed = true;
            }
        }
        return changed ? lookup(String.join(" ", fixed), false) : Optional.empty();
    }

    /** Distancia de edicao (inserir, apagar, trocar, trocar duas letras vizinhas), cortada em {@code limit + 1}. */
    static int distance(String a, String b, int limit) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }
        return d[a.length()][b.length()] > limit ? limit + 1 : d[a.length()][b.length()];
    }

    /** Nome legivel em pt-BR para a fala. */
    public static String name(String id, boolean plural) {
        List<String> n = NAMES.get(id);
        if (n == null) {
            return id.substring(id.indexOf(':') + 1).replace('_', ' ');
        }
        String single = n.get(0);
        if (!plural) {
            return single;
        }
        if (n.size() > 1 && n.get(1).equals(single)) {
            return single; // substantivo de massa ("areia", "carvão"): sem plural
        }
        String auto = pluralize(single);
        String firstPl = auto.contains(" ") ? auto.substring(0, auto.indexOf(' ')) : auto;
        // prefere o plural cadastrado que comeca pelo plural da 1a palavra ("carnes podres"); senao o calculado
        return n.stream().filter(x -> !x.equals(single) && norm(x).startsWith(norm(firstPl))).findFirst().orElse(auto);
    }

    /** Plural pt-BR do NOME (so a 1a palavra varia em "barra de ouro" -> "barras de ouro"). */
    static String pluralize(String s) {
        int sp = s.indexOf(' ');
        String w = sp < 0 ? s : s.substring(0, sp);
        String rest = sp < 0 ? "" : s.substring(sp);
        String p;
        if (w.endsWith("s")) {
            p = w;
        } else if (w.endsWith("ão")) {
            p = w.equals("pão") ? w.substring(0, w.length() - 2) + "ães" : w.substring(0, w.length() - 2) + "ões";
        } else if (w.endsWith("r") || w.endsWith("z")) {
            p = w + "es";
        } else if (w.endsWith("m")) {
            p = w.substring(0, w.length() - 1) + "ns";
        } else if (w.endsWith("l")) {
            p = w.substring(0, w.length() - 1) + "is";
        } else {
            p = w + "s";
        }
        return p + rest;
    }
}
