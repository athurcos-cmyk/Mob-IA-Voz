package br.com.mobvoz.trade;

import br.com.mobvoz.profile.Profiles;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Catalogo FECHADO de trocas (trades.json): cada criatura/profissao tem varias receitas e so da o que combina com a sua natureza
 * (as saidas sao exatamente {@code profile.drops}). O LLM nao inventa receitas nem itens. O preco e interno (pontos);
 * o JOGADOR so ouve itens e porcentagens, nunca "pontos".
 */
public final class TradeCatalog {
    /**
     * @param price preco POR UNIDADE (pontos internos); @param maxUnits maximo de unidades num so negocio;
     *              @param flavor fala opcional na entrega.
     */
    public record Recipe(String id, String profile, double price, String outputItem, int outputCount, String flavor,
                         long offerTicks, int maxUnits) {
        public double totalPrice(int units) {
            return price * units;
        }

        /** Opcoes de pagamento em ITENS para o preco total (ex.: "12 barras de ouro", "6 esmeraldas", "3 diamantes"). */
        public List<String> paymentExamples(int units) {
            return paymentExamples(units, price);
        }

        public List<String> paymentExamples(int units, double unitPrice) {
            Profiles.Profile p = Profiles.of(profile);
            double total = unitPrice * units;
            java.util.LinkedHashSet<String> acc = new java.util.LinkedHashSet<>(p.wants());
            currencyFor(outputItem).stream().filter(c -> !p.dislikes().contains(c)).forEach(acc::add); // moedas que todo mundo aceita
            List<String> ids = acc.stream().filter(i -> Values.value(i) > 0 && !i.endsWith("_pickaxe") && !i.endsWith("_sword"))
                    .sorted((a, b) -> Double.compare(Values.value(a), Values.value(b))).toList();
            List<String> out = new ArrayList<>();
            for (String i : ids) { // itens "normais": quantidade entre 2 e 16 (nem 1 item so, nem pilhas absurdas)
                int n = (int) Math.ceil(total / Values.value(i) - 1e-9);
                if (n >= 2 && n <= 16 && out.size() < 3) {
                    out.add(n + " " + Values.name(i, true));
                }
            }
            for (String i : ids) { // oficio de itens baratos (ex.: trigo): cai para o melhor item disponivel
                int n = (int) Math.ceil(total / Values.value(i) - 1e-9);
                if (out.isEmpty() && n >= 1 && n <= 64) {
                    out.add(n + " " + Values.name(i, n != 1));
                }
            }
            if (out.isEmpty()) { // sem gosto fixo (troca improvisada): o que ela aprovar na hora, medido em esmeraldas
                int n = (int) Math.ceil(total / Values.value("minecraft:emerald") - 1e-9);
                out.add("o valor de " + n + " " + Values.name("minecraft:emerald", n != 1) + " em coisas que ela aprove");
            }
            return out;
        }

        /** Condicoes verificadas (o LLM so repete): produto, preco em ITENS, o que mais aceita e o limite por negocio. */
        public String conditions(int units) {
            return conditions(units, price);
        }

        public String conditions(int units, double unitPrice) {
            Profiles.Profile p = Profiles.of(profile);
            java.util.LinkedHashSet<String> accepted = new java.util.LinkedHashSet<>(p.wants());
            currencyFor(outputItem).stream().filter(c -> !p.dislikes().contains(c)).forEach(accepted::add);
            String accepts = accepted.stream().sorted((a, b) -> {
                int c = Double.compare(Values.value(b), Values.value(a));
                return c != 0 ? c : a.compareTo(b);
            }).limit(6).map(i -> Values.name(i, true)).collect(Collectors.joining(", "));
            return "por " + (outputCount * units) + " " + Values.name(outputItem, outputCount * units != 1)
                    + " ela quer, jogado no chão perto dela, o equivalente a: " + String.join(", ou ", paymentExamples(units, unitPrice))
                    + " (pode combinar itens; ela costuma aceitar: " + (accepts.isEmpty() ? "o que ela aprovar na hora" : accepts)
                    + "; qualquer outra coisa ela avalia na hora e pode ser convencida com um bom motivo; o que ela não quiser fica com você e a sobra é devolvida). "
                    + "Limite: no máximo " + maxUnits + " unidades por negócio";
        }
    }

    public static final List<Recipe> ALL;

    static {
        List<Recipe> l = new ArrayList<>();
        try (InputStream in = TradeCatalog.class.getResourceAsStream("/mobvoz/trades.json")) {
            var arr = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class).getAsJsonArray("recipes");
            arr.forEach(e -> {
                JsonObject o = e.getAsJsonObject();
                l.add(new Recipe(o.get("id").getAsString(), o.get("profile").getAsString(), o.get("price").getAsDouble(),
                        o.get("item").getAsString(), o.get("count").getAsInt(), o.has("flavor") ? o.get("flavor").getAsString() : "",
                        o.get("offer").getAsLong(), o.has("units") ? o.get("units").getAsInt() : 1));
            });
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("trades.json invalido", e);
        }
        ALL = List.copyOf(l);
    }

    private TradeCatalog() {
    }

    /** Nome em pt-BR para a fala. */
    public static String nameOf(String itemId, boolean plural) {
        return Values.name(itemId, plural);
    }

    /** Tudo o que a criatura/profissao pode dar (a 1a receita e a padrao). */
    public static List<Recipe> recipesFor(String profileKey) {
        List<Recipe> own = ALL.stream().filter(r -> r.profile().equals(profileKey)).toList();
        if (own.isEmpty() && profileKey.contains("#")) { // profissao sem receita propria (aldeao sem oficio, bobo...): cai nas da especie
            String base = profileKey.substring(0, profileKey.indexOf('#'));
            return ALL.stream().filter(r -> r.profile().equals(base)).toList();
        }
        return own;
    }

    /** Receita padrao da criatura/profissao ("quer trocar?" sem pedir nada). */
    public static Optional<Recipe> forProfile(String profileKey) {
        return recipesFor(profileKey).stream().findFirst();
    }

    /** Receita que entrega exatamente {@code itemId} (o que o jogador pediu). */
    public static Optional<Recipe> forItem(String profileKey, String itemId) {
        return recipesFor(profileKey).stream().filter(r -> r.outputItem().equals(itemId)).findFirst();
    }

    public static Optional<Recipe> byId(String id) {
        if (id.startsWith(DYN)) {
            return parseDynamic(id);
        }
        return ALL.stream().filter(r -> r.id().equals(id)).findFirst();
    }

    // ---- receitas IMPROVISADAS: item fora do catalogo que a IA julgou plausivel (ou que a criatura achou por sorte) ----
    // O id carrega tudo ("dyn|perfil|item|preco em decimos|unidades"), entao sobrevive a salvar/sair sem nenhum estado extra.
    private static final String DYN = "dyn|";
    public static final double DYN_MAX_VALUE = 400.0; // qualquer item do jogo (ate netherite e elitro) pode ser improvisado; o preco acompanha a raridade
    public static final int DYN_MAX_UNITS = 16;

    /** Quantas unidades cabem num negocio improvisado: item barato em quantidade (farinha de osso), caro de a um. */
    public static int dynUnits(String item) {
        double v = Values.value(item);
        return v < 2 ? DYN_MAX_UNITS : v < 10 ? 8 : v < 60 ? 4 : 1;
    }

    /** Moedas que qualquer criatura aceita (a menos que odeie), exceto o que e do mesmo material do que ela da: ninguem paga ferro para ganhar ferro. */
    public static final List<String> CURRENCY = List.of("minecraft:emerald", "minecraft:diamond", "minecraft:gold_ingot", "minecraft:iron_ingot");
    private static final java.util.Set<String> MATERIALS = java.util.Set.of("iron", "golden", "gold", "diamond", "netherite", "emerald", "copper", "leather", "chainmail");

    /** Material do item pela 1a palavra do id ("iron_chestplate" -> "iron"); "" se nao for de material. */
    static String materialOf(String itemId) {
        String id = itemId.substring(itemId.indexOf(':') + 1);
        String first = id.contains("_") ? id.substring(0, id.indexOf('_')) : id;
        return MATERIALS.contains(first) ? (first.equals("golden") ? "gold" : first) : "";
    }

    public static List<String> currencyFor(String outputItem) {
        String mat = materialOf(outputItem);
        return CURRENCY.stream().filter(c -> mat.isEmpty() || !materialOf(c).equals(mat)).toList();
    }

    /** Receita improvisada: preco por unidade = valor x margem (meio em meio), sempre >= valor da saida (sem criar valor). */
    public static Optional<Recipe> dynamic(String profile, String item, double margin, int units) {
        double value = Values.value(item);
        if (value <= 0 || value > DYN_MAX_VALUE || margin < 1.0 || units < 1 || units > DYN_MAX_UNITS) {
            return Optional.empty();
        }
        double price = Math.ceil(value * margin * 2 - 1e-9) / 2;
        return parseDynamic(DYN + profile + "|" + item + "|" + Math.round(price * 10) + "|" + units);
    }

    private static Optional<Recipe> parseDynamic(String id) {
        String[] f = id.split("\\|");
        try {
            if (f.length != 5) {
                return Optional.empty();
            }
            double price = Integer.parseInt(f[3]) / 10.0;
            int units = Integer.parseInt(f[4]);
            if (!Values.known(f[2]) || price + 1e-9 < Values.value(f[2]) || units < 1 || units > DYN_MAX_UNITS || Values.value(f[2]) > DYN_MAX_VALUE) {
                return Optional.empty(); // id adulterado/invalido: nunca cria valor
            }
            return Optional.of(new Recipe(id, f[1], price, f[2], 1, "", 2400, units));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
