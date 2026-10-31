package br.com.mobvoz.profile;

import br.com.mobvoz.trade.Values;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Outra criatura citada na fala do jogador ("o que voce acha das vacas?"): devolve fatos de lore sobre ela para a criatura que
 * responde ter o que opinar sem inventar. Casamento por palavra inteira, nome mais longo primeiro ("esqueleto wither" antes de "wither").
 */
public final class Mentions {
    private static final Map<String, String> ID_BY_NAME = new TreeMap<>((a, b) -> a.length() != b.length() ? b.length() - a.length() : a.compareTo(b));

    static {
        try (InputStream in = Mentions.class.getResourceAsStream("/mobvoz/names.json")) {
            JsonObject names = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("names");
            for (var e : names.entrySet()) {
                e.getValue().getAsJsonArray().forEach(n -> ID_BY_NAME.put(n.getAsString(), e.getKey()));
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("names.json invalido", e);
        }
    }

    private Mentions() {
    }

    /** Nome pt-BR curto da criatura (rotulo do perfil, ou o menor nome de names.json); null se desconhecida. */
    public static String labelOf(String typeId) {
        if (Profiles.has(typeId)) {
            return Profiles.of(typeId).label();
        }
        return ID_BY_NAME.entrySet().stream().filter(e -> e.getValue().equals(typeId)).map(Map.Entry::getKey)
                .min(java.util.Comparator.comparingInt(String::length)).orElse(null);
    }

    /** Ids de criaturas citadas (no maximo 2), exceto a propria. */
    public static List<String> mentioned(String selfType, String text) {
        String rest = " " + Values.norm(text) + " ";
        List<String> found = new ArrayList<>();
        for (var e : ID_BY_NAME.entrySet()) {
            String needle = " " + e.getKey() + " ";
            if (rest.contains(needle)) {
                rest = rest.replace(needle, "  ");
                if (!e.getValue().equals(selfType) && !found.contains(e.getValue()) && found.size() < 2) {
                    found.add(e.getValue());
                }
            }
        }
        return found;
    }

    /** Linhas de fato (ja em pt-BR) sobre as criaturas citadas. */
    public static List<String> facts(String selfType, String text) {
        List<String> out = new ArrayList<>();
        for (String id : mentioned(selfType, text)) {
            var lore = Lore.of(id);
            if (lore == null) {
                continue;
            }
            String nome = Profiles.has(id) ? Profiles.of(id).label() : ID_BY_NAME.entrySet().stream().filter(e -> e.getValue().equals(id))
                    .map(Map.Entry::getKey).min(java.util.Comparator.comparingInt(String::length)).orElse(id);
            String diet = Profiles.has(id) ? " Dieta: " + Profiles.of(id).diet() + "." : "";
            out.add("O jogador citou " + nome + ". O que você sabe dela(e): " + lore.origem() + " " + lore.motivacao() + " Teme: " + lore.medos() + diet
                    + " Dê a SUA opinião pessoal sobre ela, pela sua natureza.");
        }
        return out;
    }
}
