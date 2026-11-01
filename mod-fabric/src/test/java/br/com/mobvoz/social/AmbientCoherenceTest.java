package br.com.mobvoz.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.social.AmbientScenes.Family;
import br.com.mobvoz.social.AmbientScenes.Situation;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Bateria de COERENCIA das conversas ambiente: conversa so entre criaturas que combinam (aldeao com aldeao, animal com animal, monstro com a
 * mesma especie) e nenhuma fala fora do mundo de quem a diz (vaca nao fala em "trocar", aldeao nao muga, zumbi nao e simpatico).
 * Tambem exporta amostras por tipo de par para o juiz de IA (ai-service/tests/battery_ambient.py).
 */
class AmbientCoherenceTest {
    private static Set<String> cow() {
        return Set.of("calm", "animals", "herd", "farm", "cow");
    }

    private static Set<String> pig() {
        return Set.of("calm", "animals", "herd", "farm", "pig");
    }

    private static Set<String> wolf() {
        return Set.of("calm", "animals", "pet", "wolf");
    }

    private static Set<String> cat() {
        return Set.of("calm", "animals", "pet", "cat");
    }

    private static Set<String> farmer() {
        return Set.of("calm", "villagers", "villager", "villager:farmer");
    }

    private static Set<String> librarian() {
        return Set.of("calm", "villagers", "villager", "villager:librarian");
    }

    private static Set<String> golem() {
        return Set.of("calm", "villagers", "golem");
    }

    private static Set<String> mon(String type) {
        return Set.of("monsters", type);
    }

    @Test
    void onlyCreaturesThatMakeSenseTogetherTalk() {
        assertTrue(Ambient.canChat(cow(), cow(), true));
        assertTrue(Ambient.canChat(cow(), pig(), false), "animais com animais");
        assertTrue(Ambient.canChat(wolf(), wolf(), true));
        assertFalse(Ambient.canChat(wolf(), cat(), false), "predadores de especies diferentes nao tem assunto");
        assertFalse(Ambient.canChat(cat(), cow(), false));
        assertTrue(Ambient.canChat(farmer(), librarian(), false), "aldeoes entre si");
        assertTrue(Ambient.canChat(farmer(), golem(), false), "o golem faz parte da vila");
        assertTrue(Ambient.canChat(mon("zombie"), mon("zombie"), true));
        assertFalse(Ambient.canChat(cow(), farmer(), false), "aldeao nao conversa com vaca");
        assertFalse(Ambient.canChat(cat(), farmer(), false));
        assertFalse(Ambient.canChat(mon("zombie"), farmer(), false), "zumbi e aldeao nao tem conversa agradavel");
        assertTrue(Ambient.canChat(mon("zombie"), mon("skeleton"), false), "monstros conversam entre si, de especies diferentes");
        assertTrue(Ambient.canChat(mon("creeper"), mon("zombie"), false));
        assertFalse(Ambient.canChat(mon("skeleton"), farmer(), false), "monstro nunca com aldeao");
        assertFalse(Ambient.canChat(mon("zombie"), cow(), false));
    }

    /** Palavras que quebram o mundo de quem fala. */
    private static final List<String> NOT_FOR_ANIMALS = List.of("troca", "trocar", "negóci", "esmeralda", "estoque", "banca", "vila", "aldeão", "aldeões",
            "golem", " cama ", " porta ", "plantação", "horta", "colheita", "armeiro", "cliente", "livro", "forja", "poção", "caldeirão", "pão", "fofoca");
    private static final List<String> NOT_FOR_VILLAGERS = List.of("muuu", "có ", "mééé", "oink", "capim", "pasto", "ciscar", "focinho",
            "grrr", "ssss", "sss...");
    private static final List<String> NOT_FOR_MONSTERS = List.of("prazer", "simpátic", "amigo", "obrigad", "bom dia", "boa noite", "por favor", "gente boa",
            "bonito", "lindo", "gentil", "honest", "troca", "vila", "horta", "colheita", "muuu", "có ");

    private static Set<String> kinds(String who) {
        return switch (who) {
            case "calm" -> Set.of("animals", "villagers");
            case "villagers", "villager", "golem" -> Set.of("villagers");
            case "animals", "farm", "cow", "pig", "sheep", "chicken", "pet", "wolf", "cat", "horse" -> Set.of("animals");
            default -> who.startsWith("villager:") ? Set.of("villagers") : Set.of("monsters");
        };
    }

    @Test
    void everyFamilyOnlyUsesWordsThatFitWhoSpeaksIt() {
        StringBuilder bad = new StringBuilder();
        for (Family f : AmbientScenes.families()) {
            Set<String> audience = new HashSet<>(kinds(f.whoA()));
            audience.retainAll(kinds(f.whoB()));
            assertFalse(audience.isEmpty(), f.id() + ": quem abre e quem responde nunca poderiam conversar");
            if (f.id().equals("jogador_troca") || f.id().equals("ev_volta_troca")) {
                continue; // fato verificado: ate a vaca realmente troca com o jogador
            }
            List<String> all = new java.util.ArrayList<>(f.a());
            all.addAll(f.b());
            all.addAll(f.c());
            for (String line : all) {
                String t = " " + line.toLowerCase(Locale.ROOT) + " ";
                for (String[] g : new String[][] {{"animals"}, {"villagers"}, {"monsters"}}) {
                    if (!audience.contains(g[0])) {
                        continue;
                    }
                    List<String> forbidden = g[0].equals("animals") ? NOT_FOR_ANIMALS : g[0].equals("villagers") ? NOT_FOR_VILLAGERS : NOT_FOR_MONSTERS;
                    for (String w : forbidden) {
                        if (t.contains(w)) {
                            bad.append("\n  ").append(f.id()).append(" (").append(g[0]).append("): \"").append(line).append("\" tem \"").append(w.strip()).append('"');
                        }
                    }
                }
            }
        }
        assertEquals("", bad.toString(), "falas fora do mundo de quem as diz");
    }

    @Test
    void creaturesWantWhatTheyLikeAndNoticeArmor() {
        Random rnd = new Random(5);
        Set<String> tags = Set.of("day", "clear", "holds", "want0");
        boolean wantsIt = false;
        for (int n = 0; n < 400; n++) {
            var s = AmbientScenes.pick(new Situation(tags, cow(), pig(), "", "", "trigo", ""), rnd, k -> false).orElseThrow();
            wantsIt |= s.family().equals("bicho_quer_comida") && s.lines().get(0).text().toLowerCase(Locale.ROOT).contains("trigo");
            assertFalse(s.family().equals("jogador_item") || s.family().equals("bicho_item"), "se ela gosta do item, nao e 'nao e de comer': " + s.family());
        }
        assertTrue(wantsIt, "a vaca deveria querer o trigo");
        boolean armor = false;
        for (int n = 0; n < 400 && !armor; n++) {
            var s = AmbientScenes.pick(new Situation(Set.of("day", "clear", "armor"), farmer(), librarian(), "", "", "", "uma armadura de ferro"), rnd, k -> false).orElseThrow();
            armor = s.family().equals("vila_armadura") && s.lines().stream().anyMatch(l -> l.text().contains("armadura de ferro"));
        }
        assertTrue(armor, "aldeoes deveriam comentar a armadura");
    }

    /** Amostras por tipo de par, para o juiz de IA: build/ambient-samples.json. */
    @Test
    void exportSamplesForTheAiJudge() throws IOException {
        Object[][] pairs = {
                {"vaca + vaca, dia, o jogador segura trigo (ela gosta)", cow(), cow(), Set.of("day", "clear", "holds", "want0"), "trigo", ""},
                {"vaca + porco, dia, o jogador segura uma espada (nao e comida)", cow(), pig(), Set.of("day", "clear", "holds"), "espada de diamante", ""},
                {"porco + galinha, noite", pig(), Set.of("calm", "animals", "herd", "farm", "chicken"), Set.of("night"), "", ""},
                {"vaca + porco, chuva, jogador ferido e com fome", cow(), pig(), Set.of("rain", "hurt", "hungry"), "", ""},
                {"lobo + lobo, dia, jogador com armadura de diamante", wolf(), wolf(), Set.of("day", "clear", "armor"), "", "uma armadura de diamante"},
                {"gato + gato, dia, o jogador segura peixe (o gato gosta)", cat(), cat(), Set.of("day", "clear", "holds", "want0"), "peixe cru", ""},
                {"vaca + vaca, as duas ja conhecem o jogador e uma sabe o nome", cow(), cow(), Set.of("day", "clear", "k0", "k1", "n0"), "", ""},
                {"aldeão fazendeiro + aldeão bibliotecário, dia", farmer(), librarian(), Set.of("day", "clear"), "", ""},
                {"aldeão + aldeão, noite, o jogador segura esmeraldas", farmer(), librarian(), Set.of("night", "holds"), "esmeraldas", ""},
                {"aldeão + aldeão, dia, jogador com armadura de ferro", farmer(), librarian(), Set.of("day", "clear", "armor"), "", "uma armadura de ferro"},
                {"aldeão + golem, dia, o jogador segura uma papoula (o golem gosta)", farmer(), golem(), Set.of("day", "clear", "holds", "want0"), "papoula", ""},
                {"aldeão + aldeão, chuva, um deles tem fato de troca com o jogador", farmer(), librarian(), Set.of("rain", "k0", "f0:TRADE"), "", ""},
                {"zumbi + zumbi, noite", mon("zombie"), mon("zombie"), Set.of("night"), "", ""},
                {"zumbi + esqueleto, noite", mon("zombie"), mon("skeleton"), Set.of("night"), "", ""},
                {"creeper + zumbi, dia", mon("creeper"), mon("zombie"), Set.of("day", "clear"), "", ""},
                {"aranha + zumbi, noite, jogador segura espada", mon("spider"), mon("zombie"), Set.of("night", "holds"), "espada de ferro", ""},
                {"esqueleto + creeper, chuva", mon("skeleton"), mon("creeper"), Set.of("rain"), "", ""},
                {"enderman + bruxa, noite", mon("enderman"), mon("witch"), Set.of("night"), "", ""},
                {"zumbi + zumbi, jogador com armadura de ferro", mon("zombie"), mon("zombie"), Set.of("night", "armor"), "", "uma armadura de ferro"},
                {"esqueleto + esqueleto, jogador com armadura de diamante", mon("skeleton"), mon("skeleton"), Set.of("night", "armor"), "", "uma armadura de diamante"},
                {"aranha + aranha, noite", mon("spider"), mon("spider"), Set.of("night"), "", ""},
                {"creeper + creeper, dia", mon("creeper"), mon("creeper"), Set.of("day", "clear"), "", ""},
                {"enderman + enderman, noite", mon("enderman"), mon("enderman"), Set.of("night"), "", ""},
                {"bruxa + bruxa, chuva", mon("witch"), mon("witch"), Set.of("rain"), "", ""},
                {"warden + warden, noite", mon("warden"), mon("warden"), Set.of("night"), "", ""},
        };
        JsonArray out = new JsonArray();
        Random rnd = new Random(2026);
        for (Object[] p : pairs) {
            @SuppressWarnings("unchecked")
            Situation sit = new Situation((Set<String>) p[3], (Set<String>) p[1], (Set<String>) p[2], (((Set<String>) p[3]).contains("n0") ? "Bia" : ""), "",
                    (String) p[4], (String) p[5]);
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 40 && seen.size() < 10; i++) {
                var scene = AmbientScenes.pick(sit, rnd, seen::contains);
                if (scene.isEmpty()) {
                    break;
                }
                if (!seen.add(scene.get().key())) {
                    continue;
                }
                JsonObject o = new JsonObject();
                o.addProperty("par", (String) p[0]);
                o.addProperty("familia", scene.get().family());
                JsonArray lines = new JsonArray();
                scene.get().lines().forEach(l -> lines.add(l.speaker() + ": " + l.text()));
                o.add("falas", lines);
                out.add(o);
            }
        }
        assertTrue(out.size() >= pairs.length * 3, "amostras geradas: " + out.size());
        Path file = Path.of("build", "ambient-samples.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(out), StandardCharsets.UTF_8);
        assertEquals(pairs.length, out.asList().stream().map(e -> e.getAsJsonObject().get("par").getAsString()).distinct().count(), "todo par tem amostras");
    }
}
