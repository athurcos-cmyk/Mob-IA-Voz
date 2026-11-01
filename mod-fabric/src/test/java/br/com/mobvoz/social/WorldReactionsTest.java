package br.com.mobvoz.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.social.AmbientScenes.Situation;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** As reacoes ao mundo (familias ev_*) sao fixas e precisam de VARIACAO: muitas falas diferentes por acontecimento e por tipo de criatura. */
class WorldReactionsTest {
    private static final Set<String> VILLAGER = Set.of("calm", "villagers", "villager", "villager:farmer");
    private static final Set<String> GOLEM = Set.of("calm", "villagers", "golem");
    private static final Set<String> COW = Set.of("calm", "animals", "farm", "herd", "cow");
    private static final Set<String> WOLF = Set.of("calm", "animals", "pet", "wolf");
    private static final Set<String> CHICKEN = Set.of("calm", "animals", "farm", "herd", "chicken");

    /** Todas as falas diferentes que {@code kind} diz ao reagir a {@code event} (so a abertura: e a fala de uma criatura sozinha). */
    private static Set<String> openings(String event, Set<String> kind, String thing, String... extraTags) {
        Set<String> tags = new HashSet<>(Set.of("day", "clear", event));
        tags.addAll(Set.of(extraTags));
        Random rnd = new Random(5);
        Set<String> out = new HashSet<>();
        for (int n = 0; n < 1500; n++) {
            var s = AmbientScenes.pick(new Situation(tags, kind, kind, "", "", "", "", thing), rnd, k -> false, id -> id.startsWith("ev_"));
            s.ifPresent(sc -> out.add(sc.lines().get(0).text()));
        }
        return out;
    }

    @Test
    void everyWorldEventHasManyDifferentReactions() {
        for (String ev : new String[] {"ev:boom", "ev:thunder", "ev:wake", "ev:revive", "ev:fall"}) {
            assertTrue(openings(ev, VILLAGER, "").size() >= 4, ev + " aldeao");
            assertTrue(openings(ev, COW, "").size() >= 3, ev + " vaca");
        }
        for (String ev : new String[] {"ev:boom", "ev:thunder"}) {
            assertTrue(openings(ev, GOLEM, "").size() >= 4, ev + " golem");
        }
        for (String ev : new String[] {"ev:thanks", "ev:raiders"}) {
            assertTrue(openings(ev, VILLAGER, "").size() >= 4, ev + " aldeao");
            assertTrue(openings(ev, GOLEM, "").size() >= 3, ev + " golem");
            assertTrue(openings(ev, COW, "").isEmpty(), ev + ": vaca nao reage a isso");
        }
        assertTrue(openings("ev:hit", VILLAGER, "").size() >= 8);
        assertTrue(openings("ev:hit", COW, "").size() >= 10, "vaca: falas de especie + de bicho");
        assertTrue(openings("ev:hit", WOLF, "").size() >= 10);
        assertTrue(openings("ev:hit", GOLEM, "").size() >= 4);
        for (String ev : new String[] {"ev:gift_like", "ev:gift_meh", "ev:gift_dislike"}) {
            assertTrue(openings(ev, VILLAGER, "cenoura").size() >= 4, ev + " aldeao");
            assertTrue(openings(ev, COW, "cenoura").size() >= 4, ev + " vaca");
            assertTrue(openings(ev, GOLEM, "papoula").size() >= 3, ev + " golem");
        }
        assertTrue(openings("ev:danger", VILLAGER, "zumbi").size() >= 6);
        assertTrue(openings("ev:danger", WOLF, "zumbi").size() >= 8);
        assertTrue(openings("ev:danger", GOLEM, "zumbi").size() >= 8);
        assertTrue(openings("ev:egg", CHICKEN, "").size() >= 5);
        assertTrue(openings("ev:honest", VILLAGER, "3 esmeraldas").size() >= 4);
    }

    @Test
    void greetingsDependOnWhatTheCreatureReallyRemembers() {
        Set<String> gift = openings("ev:greet", VILLAGER, "", "f0:GIFT");
        Set<String> hit = openings("ev:greet", VILLAGER, "", "f0:DAMAGED_BY_PLAYER");
        assertTrue(gift.stream().anyMatch(l -> l.contains("presente")), "quem ganhou presente lembra dele: " + gift);
        assertTrue(hit.stream().anyMatch(l -> l.contains("pancada") || l.contains("fez") || l.contains("mãos")), "quem apanhou lembra: " + hit);
        assertFalse(openings("ev:greet", VILLAGER, "").stream().anyMatch(l -> l.contains("presente") || l.contains("pancada")),
                "sem fato verificado a criatura nao inventa lembranca");
    }

    @Test
    void theGolemGreetsLikeAGuardOfTheVillage() {
        Set<String> plain = openings("ev:greet", GOLEM, "");
        assertTrue(plain.size() >= 8, plain.toString());
        assertTrue(plain.stream().filter(l -> l.contains("vila")).count() >= 5, "o golem fala da vila: " + plain);
        Set<String> gift = openings("ev:greet", GOLEM, "", "f0:GIFT");
        assertTrue(gift.stream().anyMatch(l -> l.contains("presente")), gift.toString());
        Set<String> hit = openings("ev:greet", GOLEM, "", "f0:DAMAGED_BY_PLAYER");
        assertTrue(hit.stream().anyMatch(l -> l.contains("pancada") || l.contains("bateu")), hit.toString());
    }

    @Test
    void theThingIsSpokenAndNothingEverRendersEmptyOrOpen() {
        Set<String> all = openings("ev:danger", VILLAGER, "aranha");
        assertTrue(all.stream().anyMatch(l -> l.contains("aranha") || l.contains("Aranha")), all.toString());
        for (String l : all) {
            assertFalse(l.contains("{") || l.contains("}") || l.isBlank(), l);
        }
        assertTrue(openings("ev:boom", VILLAGER, "").stream().noneMatch(String::isBlank));
    }

    @Test
    void reactionFamiliesNeverHappenWithoutTheirEvent() {
        Random rnd = new Random(2);
        Set<String> tags = Set.of("day", "clear", "k0", "n0", "holds", "armor", "hurt", "hungry");
        for (int n = 0; n < 1500; n++) {
            var s = AmbientScenes.pick(new Situation(tags, VILLAGER, VILLAGER, "Bia", "", "uma espada", "uma armadura de ferro", ""), rnd, k -> false)
                    .orElseThrow();
            assertFalse(s.family().startsWith("ev_"), "reacao a algo que nao aconteceu: " + s.family());
        }
    }

    @Test
    void cooldownIsMeasuredInGameTicks() {
        assertTrue(WorldReactions.due(null, 100, 300));
        assertFalse(WorldReactions.due(0L, 100, 300));
        assertTrue(WorldReactions.due(0L, 300, 300));
        assertEquals(true, WorldReactions.due(50L, 1000, 0));
    }
}
