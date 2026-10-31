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

    static {
        try (InputStream in = Values.class.getResourceAsStream("/mobvoz/values.json")) {
            JsonObject items = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("items");
            for (var e : items.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                VALUE.put(e.getKey(), o.get("value").getAsDouble());
                List<String> pt = new java.util.ArrayList<>();
                o.getAsJsonArray("pt").forEach(x -> pt.add(x.getAsString()));
                NAMES.put(e.getKey(), pt);
                pt.forEach(n -> BY_NAME.put(norm(n), e.getKey()));
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("values.json invalido", e);
        }
    }

    private Values() {
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

    /** Nome falado -> id real ("picareta de diamante" -> minecraft:diamond_pickaxe). Vazio se o servidor nao conhece. */
    public static Optional<String> lookup(String spoken) {
        String n = norm(spoken);
        if (BY_NAME.containsKey(n)) {
            return Optional.of(BY_NAME.get(n));
        }
        // plural/artigos simples: "as cenouras", "3 ferros"
        String stripped = n.replaceFirst("^(o|a|os|as|um|uma|uns|umas|\\d+) ", "");
        return Optional.ofNullable(BY_NAME.get(stripped));
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
