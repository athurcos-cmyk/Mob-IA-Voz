package br.com.mobvoz.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.social.AmbientScenes.Family;
import br.com.mobvoz.social.AmbientScenes.Situation;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AmbientScenesTest {
    private static Situation sit(Set<String> tags, Set<String> a, Set<String> b, String nameA, String nameB) {
        return new Situation(tags, a, b, nameA, nameB, "uma espada de diamante");
    }

    private static final Set<String> ANY = Set.of("calm", "villagers", "animals", "monsters", "farm", "cow", "pig", "sheep", "chicken", "pet", "wolf", "cat", "horse", "golem",
            "villager", "villager:farmer", "villager:librarian", "villager:toolsmith", "villager:cleric", "villager:shepherd");

    @Test
    void contentIsLargeAndClean() {
        List<Family> fams = AmbientScenes.families();
        int combos = fams.stream().mapToInt(Family::combos).sum();
        assertTrue(fams.size() >= 45, "familias: " + fams.size());
        assertTrue(combos >= 1300, "combinacoes: " + combos);
        Random rnd = new Random(7);
        for (Family f : fams) { // renderiza TODAS as combinacoes: sem espaco aberto, sem travessao, sem fala longa
            int kMax = f.c().isEmpty() ? 1 : f.c().size();
            for (int i = 0; i < f.a().size(); i++) {
                for (int j = 0; j < AmbientScenes.bPool(f, i).size(); j++) {
                    for (int k = 0; k < kMax; k++) {
                        var s = AmbientScenes.render(f, i, j, f.c().isEmpty() ? -1 : k, "x", sit(Set.of(), ANY, ANY, "Arthur", ""), rnd);
                        for (var l : s.lines()) {
                            assertFalse(l.text().contains("{") || l.text().contains("}"), f.id() + ": " + l.text());
                            assertFalse(l.text().contains("—") || l.text().contains("–"), f.id() + ": " + l.text());
                            assertTrue(l.text().length() <= 130, f.id() + ": " + l.text());
                        }
                    }
                }
            }
        }
    }

    @Test
    void everyFamilyCanHappenAndOnlyWhenItsConditionsHold() {
        for (Family f : AmbientScenes.families()) {
            Set<String> tags = new HashSet<>();
            for (String n : f.need()) {
                if (!n.startsWith("!")) {
                    tags.add(n);
                }
            }
            Set<String> a = new HashSet<>(Set.of("calm"));
            a.add(f.whoA());
            Set<String> b = new HashSet<>(Set.of("calm"));
            b.add(f.whoB());
            assertTrue(AmbientScenes.eligible(f, sit(tags, a, b, "", "")), f.id() + " nunca acontece");
            if (!f.need().isEmpty()) {
                for (String n : f.need()) { // tirar qualquer condicao positiva (ou ligar uma negada) impede a familia
                    Set<String> broken = new HashSet<>(tags);
                    if (n.startsWith("!")) {
                        broken.add(n.substring(1));
                    } else {
                        broken.remove(n);
                    }
                    assertFalse(AmbientScenes.eligible(f, sit(broken, a, b, "", "")), f.id() + " passa sem cumprir " + n);
                }
            }
            assertFalse(AmbientScenes.eligible(f, sit(tags, Set.of("zzz"), b, "", "")), f.id() + " ignora a classe");
        }
    }

    @Test
    void neverMentionsWhatIsNotTrueNow() {
        Random rnd = new Random(1);
        Set<String> tags = Set.of("day", "clear"); // sem jogador ferido/com fome/segurando nada, ninguem o conhece
        for (int n = 0; n < 400; n++) {
            var s = AmbientScenes.pick(sit(tags, ANY, ANY, "", ""), rnd, k -> false).orElseThrow();
            assertFalse(s.family().startsWith("jogador_") && !s.family().equals("jogador_passando"), "assunto sobre o jogador sem base: " + s.family());
            assertFalse(s.family().equals("noite") || s.family().equals("chuva"), s.family());
            assertEquals("", s.name());
        }
    }

    @Test
    void nameIsOnlyUsedByWhoKnowsItAndThenBothHearIt() {
        Random rnd = new Random(3);
        Set<String> tags = Set.of("day", "clear", "k0", "n0");
        boolean sawName = false;
        for (int n = 0; n < 300; n++) {
            var s = AmbientScenes.pick(sit(tags, ANY, ANY, "Arthur", ""), rnd, k -> false).orElseThrow();
            boolean says = s.lines().stream().anyMatch(l -> l.text().contains("Arthur"));
            assertEquals(says, s.name().equals("Arthur"), s.lines().toString());
            sawName |= says;
        }
        assertTrue(sawName, "com o nome sabido, alguma cena deveria usa-lo");
        var none = AmbientScenes.pick(sit(Set.of("day", "clear", "k0"), ANY, ANY, "", ""), new Random(5), k -> false).orElseThrow();
        assertEquals("", none.name());
        Random rnd2 = new Random(9);
        for (int n = 0; n < 200; n++) { // quem nao sabe o nome diz "o humano", nunca um nome inventado
            var s = AmbientScenes.pick(sit(Set.of("day", "clear", "k0"), ANY, ANY, "", ""), rnd2, k -> false).orElseThrow();
            assertFalse(s.lines().stream().anyMatch(l -> l.text().contains("essa pessoa?") && s.family().equals("jogador_nome_conta")));
        }
    }

    @Test
    void sceneAboutWhatThePlayerHoldsNamesTheItem() {
        Set<String> tags = Set.of("day", "clear", "holds");
        boolean saw = false;
        Random rnd = new Random(2);
        for (int n = 0; n < 500 && !saw; n++) {
            var s = AmbientScenes.pick(sit(tags, ANY, ANY, "", ""), rnd, k -> false).orElseThrow();
            saw = s.family().equals("jogador_item") && s.lines().get(0).text().contains("uma espada de diamante");
        }
        assertTrue(saw);
    }

    @Test
    void repeatsAreRareInALongSession() {
        Random rnd = new Random(11);
        Set<String> tags = Set.of("day", "clear", "k0", "k1");
        Set<String> a = Set.of("calm", "villagers", "villager", "villager:farmer");
        Set<String> b = Set.of("calm", "villagers", "villager");
        ArrayDeque<String> recent = new ArrayDeque<>();
        Set<String> seen = new LinkedHashSet<>();
        int repeatsInWindow = 0;
        int total = 300;
        for (int n = 0; n < total; n++) {
            var s = AmbientScenes.pick(sit(tags, a, b, "", ""), rnd, recent::contains).orElseThrow();
            if (recent.contains(s.key())) {
                repeatsInWindow++;
            }
            seen.add(s.key());
            recent.addLast(s.key());
            if (recent.size() > 80) {
                recent.pollFirst();
            }
        }
        assertEquals(0, repeatsInWindow, "nenhuma cena repete dentro das ultimas 80");
        assertTrue(seen.size() > 150, "cenas diferentes em 300 sorteios: " + seen.size());
    }
}
