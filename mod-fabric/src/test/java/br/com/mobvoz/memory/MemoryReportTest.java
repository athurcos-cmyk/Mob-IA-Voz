package br.com.mobvoz.memory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.memory.MobMemory.Stance;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MemoryReportTest {
    private final UUID me = UUID.randomUUID();

    private static String all(List<String> l) {
        return String.join("\n", l);
    }

    @Test
    void aCreatureThatNeverMetThePlayerSaysSo() {
        var v = new MemoryReport.View("", "vaca", "", MobMemory.create(UUID.randomUUID()), me, 0, 0, 0, "normal", 0);
        String t = all(MemoryReport.lines(v));
        assertTrue(t.contains("Conhece você: não") && t.contains("nenhuma lembrança"), t);
    }

    @Test
    void showsNameTrustFactsAndWhatTheCreatureThinksOfClaims() {
        MobMemory m = MemoryRules.firstMeeting(MobMemory.create(UUID.randomUUID()), me, 0).withName(me, "Arthur");
        m = MemoryRules.gift(m, me, "minecraft:carrot", 1, 100, 1.0, "gostou");
        m = m.withClaim(new MobMemory.Claim(UUID.randomUUID(), me, "Eu sou o rei da vila", 200, Stance.DOUBTS));
        var v = new MemoryReport.View("Mimosa", "vaca", "Arthur", m, me, 1200, 0.1, 0.2, "normal", 0);
        String t = all(MemoryReport.lines(v));
        assertTrue(t.contains("Mimosa (vaca)"), t);
        assertTrue(t.contains("chama você de Arthur"), t);
        assertTrue(t.contains("Aceita te seguir: 50%") && t.contains("dificuldade normal"), t);
        assertTrue(t.contains("Primeiro encontro") && t.contains("Você deu de presente"), t);
        assertTrue(t.contains("\"Eu sou o rei da vila\" → duvida"), t);
        assertTrue(t.contains("nada disso é fato"), "alegacao nunca e apresentada como fato: " + t);
    }

    @Test
    void showsOnlyWhatConcernsThisPlayerAndTheVillageGrudge() {
        UUID other = UUID.randomUUID();
        MobMemory m = MemoryRules.gift(MobMemory.create(UUID.randomUUID()), other, "minecraft:wheat", 1, 5, 1.0, "gostou");
        var v = new MemoryReport.View("", "aldeão", "", m, me, 100, -0.1, 0.1, "dificil", 0.3);
        String t = all(MemoryReport.lines(v));
        assertFalse(t.contains("presente"), "fato de OUTRO jogador nao aparece: " + t);
        assertTrue(t.contains("Fama na aldeia"), t);
        assertTrue(t.contains("Aceita te seguir: 0%"), "calor negativo = 0%: " + t);
    }

    @Test
    void creaturesThatFollowAnywhereAreDescribedWithoutANeed() {
        var v = new MemoryReport.View("", "vaca", "", MobMemory.create(UUID.randomUUID()), me, 0, 0.0, 0.0, "facil", 0);
        assertTrue(all(MemoryReport.lines(v)).contains("Aceita te seguir: 100%"));
    }
}
