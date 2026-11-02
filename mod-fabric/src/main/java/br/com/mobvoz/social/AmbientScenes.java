package br.com.mobvoz.social;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversas AMBIENTE entre criaturas, SEM IA (ambient.json, gerado por tools/gen/genambient.py). PURO: escolhe uma familia de assunto cujas
 * condicoes sao verdadeiras agora, sorteia abertura/resposta/fecho e preenche os espacos. A fala so afirma o que as condicoes garantem
 * (hora, clima, estado do jogador, o que cada criatura realmente sabe dele) e nada aqui muda o estado do jogo.
 */
public final class AmbientScenes {
    /** {@code bg}: reacoes genericas ("Pois e."), usadas so depois de uma AFIRMACAO; uma pergunta so recebe resposta de {@code b}. */
    public record Family(String id, String whoA, String whoB, List<String> need, int weight, boolean aboutPlayer, List<String> a, List<String> b,
                         List<String> bg, List<String> c, boolean solo, List<Script> scripts) {
        /** {@code solo}: reacao de UMA criatura (so a abertura {@code a}; sem resposta nem fecho). */
        public int combos() {
            return !scripts.isEmpty() ? scripts.size() : solo ? a.size() : a.size() * (b.size() + bg.size()) * Math.max(1, c.size());
        }
    }

    /**
     * Cena ESCRITA INTEIRA (abertura, resposta e fecho que combinam entre si), com condicoes proprias alem das da familia ({@code need}: hora, clima, bioma...).
     * Familias com {@code scripts} nunca misturam falas de cenas diferentes (o sorteio independente de a/b/c deixava respostas que nao respondem).
     */
    public record Script(List<String> need, String a, String b, String c) {
    }

    static boolean needs(List<String> need, Set<String> tags) {
        for (String n : need) {
            boolean neg = n.startsWith("!");
            if (tags.contains(neg ? n.substring(1) : n) == neg) {
                return false;
            }
        }
        return true;
    }

    /** Respostas possiveis a abertura {@code i}: pergunta -> so as especificas; afirmacao -> tambem as genericas. */
    static List<String> bPool(Family f, int i) {
        if (f.bg().isEmpty() || f.a().get(i).strip().endsWith("?")) {
            return f.b();
        }
        List<String> all = new ArrayList<>(f.b());
        all.addAll(f.bg());
        return all;
    }

    /** Quem fala (0 = abre, 1 = responde) e o que diz. */
    public record Line(int speaker, String text) {
    }

    /** {@code name}: o nome do jogador dito em voz alta na cena (vazio = ninguem o disse); as duas criaturas o aprendem ao ouvi-lo. */
    public record Scene(String family, String key, List<Line> lines, String name, boolean aboutPlayer) {
    }

    /**
     * O momento: {@code tags} (condicoes verdadeiras agora) e as classes de cada criatura. {@code nameA}/{@code nameB}: nome do jogador que
     * cada uma ja sabe ("" = nao sabe). {@code item}: nome do que o jogador segura ("" = nada conhecido); {@code armor}: o que ele veste
     * ("uma armadura de ferro"; "" = nada). {@code thing}: o "{coisa}" de uma reacao a um acontecimento (o inimigo que se aproxima, o que foi devolvido...).
     */
    public record Situation(Set<String> tags, Set<String> classesA, Set<String> classesB, String nameA, String nameB, String item, String armor, String thing) {
        public Situation(Set<String> tags, Set<String> classesA, Set<String> classesB, String nameA, String nameB, String item, String armor) {
            this(tags, classesA, classesB, nameA, nameB, item, armor, "");
        }

        public Situation(Set<String> tags, Set<String> classesA, Set<String> classesB, String nameA, String nameB, String item) {
            this(tags, classesA, classesB, nameA, nameB, item, "", "");
        }
    }

    private static final Pattern SLOT = Pattern.compile("\\{([^}]*)\\}");
    private static final List<Family> FAMILIES = new ArrayList<>();
    private static final Map<String, List<String>> POOLS = new HashMap<>();

    static {
        try (InputStream in = AmbientScenes.class.getResourceAsStream("/mobvoz/ambient.json")) {
            JsonObject root = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            for (var e : root.getAsJsonObject("pools").entrySet()) {
                POOLS.put(e.getKey(), strings(e.getValue().getAsJsonArray()));
            }
            for (JsonElement el : root.getAsJsonArray("families")) {
                JsonObject o = el.getAsJsonObject();
                JsonArray who = o.getAsJsonArray("who");
                FAMILIES.add(new Family(o.get("id").getAsString(), who.get(0).getAsString(), who.get(1).getAsString(), strings(o.getAsJsonArray("need")),
                        o.get("weight").getAsInt(), o.has("player") && o.get("player").getAsBoolean(), strings(o.getAsJsonArray("a")),
                        strings(o.getAsJsonArray("b")), o.has("bg") ? strings(o.getAsJsonArray("bg")) : List.of(), strings(o.getAsJsonArray("c")),
                        o.has("solo") && o.get("solo").getAsBoolean(), scripts(o)));
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("ambient.json invalido", e);
        }
    }

    private AmbientScenes() {
    }

    private static List<Script> scripts(JsonObject fam) {
        List<Script> out = new ArrayList<>();
        if (fam.has("scenes")) {
            for (JsonElement el : fam.getAsJsonArray("scenes")) {
                JsonObject o = el.getAsJsonObject();
                out.add(new Script(o.has("need") ? strings(o.getAsJsonArray("need")) : List.of(), o.get("a").getAsString(), o.get("b").getAsString(),
                        o.has("c") ? o.get("c").getAsString() : ""));
            }
        }
        return List.copyOf(out);
    }

    private static List<String> strings(JsonArray arr) {
        List<String> out = new ArrayList<>();
        arr.forEach(x -> out.add(x.getAsString()));
        return List.copyOf(out);
    }

    public static List<Family> families() {
        return List.copyOf(FAMILIES);
    }

    public static Map<String, List<String>> pools() {
        return Map.copyOf(POOLS);
    }

    static boolean eligible(Family f, Situation s) {
        if (!s.classesA().contains(f.whoA()) || !s.classesB().contains(f.whoB())) {
            return false;
        }
        return needs(f.need(), s.tags()) && (f.scripts().isEmpty() || f.scripts().stream().anyMatch(sc -> needs(sc.need(), s.tags())));
    }

    /** Escolhe uma cena para este momento, evitando as ja usadas ({@code recentlyUsed} recebe a chave da cena); vazio = nenhum assunto cabe. */
    public static Optional<Scene> pick(Situation s, Random rnd, Predicate<String> recentlyUsed) {
        return pick(s, rnd, recentlyUsed, id -> true);
    }

    /** {@code familyFilter}: so as familias aceitas (testes). */
    public static Optional<Scene> pick(Situation s, Random rnd, Predicate<String> recentlyUsed, Predicate<String> familyFilter) {
        List<Family> ok = new ArrayList<>();
        int total = 0;
        for (Family f : FAMILIES) {
            if (familyFilter.test(f.id()) && eligible(f, s)) {
                ok.add(f);
                total += f.weight();
            }
        }
        if (ok.isEmpty()) {
            return Optional.empty();
        }
        Family fam = ok.get(0);
        int i = 0;
        int j = 0;
        int k = 0;
        int script = -1;
        String key = "";
        for (int attempt = 0; attempt < 24; attempt++) { // sorteia de novo (ate a familia) para fugir do que acabou de ser dito
            int r = rnd.nextInt(total);
            for (Family f : ok) {
                if (r < f.weight()) {
                    fam = f;
                    break;
                }
                r -= f.weight();
            }
            if (!fam.scripts().isEmpty()) { // cena escrita inteira: sorteia so entre as que cabem neste momento
                List<Integer> fit = new ArrayList<>();
                for (int n = 0; n < fam.scripts().size(); n++) {
                    if (needs(fam.scripts().get(n).need(), s.tags())) {
                        fit.add(n);
                    }
                }
                script = fit.get(rnd.nextInt(fit.size()));
                i = j = k = 0;
                key = fam.id() + ":s" + script;
            } else {
                script = -1;
                i = rnd.nextInt(fam.a().size());
                j = fam.solo() ? 0 : rnd.nextInt(bPool(fam, i).size());
                k = fam.c().isEmpty() ? -1 : rnd.nextInt(fam.c().size());
                key = fam.id() + ":" + i + ":" + j + ":" + k;
            }
            if (!recentlyUsed.test(key)) {
                break;
            }
        }
        return Optional.of(script >= 0 ? render(fam, fam.scripts().get(script), key, s, rnd) : render(fam, i, j, k, key, s, rnd));
    }

    static Scene render(Family f, Script sc, String key, Situation s, Random rnd) {
        boolean closing = !sc.c().isEmpty();
        return render(f, closing ? new int[] {0, 1, 0} : new int[] {0, 1}, closing ? new String[] {sc.a(), sc.b(), sc.c()} : new String[] {sc.a(), sc.b()}, key, s, rnd);
    }

    static Scene render(Family f, int i, int j, int k, String key, Situation s, Random rnd) {
        int[] speakers = f.solo() ? new int[] {0} : k >= 0 ? new int[] {0, 1, 0} : new int[] {0, 1};
        String[] texts = f.solo() ? new String[] {f.a().get(i)} : k >= 0 ? new String[] {f.a().get(i), bPool(f, i).get(j), f.c().get(k)}
                : new String[] {f.a().get(i), bPool(f, i).get(j)};
        return render(f, speakers, texts, key, s, rnd);
    }

    private static Scene render(Family f, int[] speakers, String[] texts, String key, Situation s, Random rnd) {
        Map<String, String> chosen = new HashMap<>();
        List<Line> lines = new ArrayList<>();
        String heard = ""; // nome que ja foi dito em voz alta nesta cena
        for (int n = 0; n < texts.length; n++) {
            String own = speakers[n] == 0 ? s.nameA() : s.nameB();
            String name = !own.isEmpty() ? own : heard;
            boolean usedName = false;
            Matcher m = SLOT.matcher(texts[n]);
            StringBuilder out = new StringBuilder();
            while (m.find()) {
                String slot = m.group(1);
                String val;
                switch (slot) {
                    case "jogador" -> {
                        usedName |= !name.isEmpty();
                        val = name.isEmpty() ? "o humano" : name;
                    }
                    case "Jogador" -> {
                        usedName |= !name.isEmpty();
                        val = name.isEmpty() ? "O humano" : name;
                    }
                    case "nome", "Nome" -> {
                        usedName |= !name.isEmpty();
                        val = name.isEmpty() ? "essa pessoa" : name;
                    }
                    case "item" -> val = s.item();
                    case "Item" -> val = s.item().isEmpty() ? "" : s.item().substring(0, 1).toUpperCase() + s.item().substring(1);
                    case "armadura" -> val = s.armor();
                    case "coisa" -> val = s.thing();
                    case "Coisa" -> val = s.thing().isEmpty() ? "" : s.thing().substring(0, 1).toUpperCase() + s.thing().substring(1);
                    default -> {
                        String pool = slot.startsWith("@") ? slot.substring(1) : slot;
                        List<String> options = POOLS.getOrDefault(pool, List.of(pool));
                        val = chosen.computeIfAbsent(pool, p -> options.get(rnd.nextInt(options.size())));
                    }
                }
                m.appendReplacement(out, Matcher.quoteReplacement(val));
            }
            m.appendTail(out);
            if (usedName) {
                heard = name;
            }
            lines.add(new Line(speakers[n], out.toString().replaceAll("\\bde o humano", "do humano").replaceAll("\\bem o humano", "no humano").replace(" a o humano", " ao humano").replaceAll("\\bpor o humano", "pelo humano")));
        }
        return new Scene(f.id(), key, List.copyOf(lines), heard, f.aboutPlayer());
    }
}
