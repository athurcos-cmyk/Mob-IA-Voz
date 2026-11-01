package br.com.mobvoz.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Quem nao pode acompanhar, ficar ou lutar recusa com um BOM motivo do proprio mundo, nunca com a desculpa tecnica de antes. */
class RefusalsTest {
    private static final MobMemory.Relationship FRIEND = new MobMemory.Relationship(1f, 1f, 0f);

    private static String refusal(String type, String action) {
        Subject s = new Subject(type, false, false, false);
        var d = OrderPolicy.decide(action, s, FRIEND, false, MobOrders.NONE, UUID.randomUUID().toString(), false, 0);
        assertEquals(OrderPolicy.Verdict.UNSUPPORTED, d.verdict(), type + " " + action);
        return d.reason();
    }

    @Test
    void noGroupUsesTheOldTechnicalExcuse() {
        for (var e : Refusals.groups().entrySet()) {
            assertTrue(e.getValue().size() >= 1, e.getKey());
            for (String r : e.getValue()) {
                assertFalse(r.toLowerCase().contains("adaptador") || r.toLowerCase().contains("ainda sem"), r);
                assertTrue(r.length() > 30 && !r.endsWith("."), "motivo claro e sem ponto final: " + r);
            }
        }
    }

    @Test
    void eachWorldGivesItsOwnReason() {
        Random rnd = new Random(1);
        for (String t : List.of("minecraft:dolphin", "minecraft:cod", "minecraft:squid", "minecraft:axolotl")) {
            String r = Refusals.cannotFollow(t, rnd).toLowerCase();
            assertTrue(r.contains("água") || r.contains("mar") || r.contains("agua"), t + ": " + r);
        }
        for (String t : List.of("minecraft:bee", "minecraft:bat", "minecraft:allay", "minecraft:parrot")) {
            String r = Refusals.cannotFollow(t, rnd).toLowerCase();
            assertTrue(r.contains("voa") || r.contains("voo") || r.contains("ar") || r.contains("ninho"), t + ": " + r);
        }
        assertTrue(Refusals.cannotFollow("minecraft:villager#farmer", rnd).contains("vila"), "aldeao: trabalho e vila");
        String ghast = Refusals.cannotFollow("minecraft:ghast", rnd).toLowerCase();
        assertTrue(ghast.contains("voa") || ghast.contains("voo") || ghast.contains("ar"), "ghast voa: " + ghast);
        assertTrue(Refusals.cannotFollow("minecraft:warden", rnd).contains("escuro"));
    }

    @Test
    void reasonsVaryAndAreSorteadas() {
        Set<String> seen = new HashSet<>();
        Random rnd = new Random(3);
        for (int i = 0; i < 60; i++) {
            seen.add(Refusals.cannotFollow("minecraft:dolphin", rnd));
        }
        assertTrue(seen.size() >= 3, "varios motivos diferentes: " + seen.size());
    }

    @Test
    void thePolicyRefusesHonestlyWithTheWorldReason() {
        String dolphin = refusal("minecraft:dolphin", "FOLLOW_PLAYER").toLowerCase();
        assertTrue(dolphin.contains("água") || dolphin.contains("mar") || dolphin.contains("agua"), dolphin);
        assertFalse(dolphin.contains("adaptador"));
        String bee = refusal("minecraft:bee", "STAY_AT").toLowerCase();
        assertFalse(bee.contains("adaptador"), bee);
        String fish = refusal("minecraft:cod", "DEFEND_PLAYER");
        assertTrue(Refusals.groups().get("fight").contains(fish), "motivo de bicho pacifico, nao a desculpa tecnica: " + fish);
    }
}
