package br.com.mobvoz.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.profile.Lore;
import br.com.mobvoz.profile.Lore.Threat;
import br.com.mobvoz.social.Reactions.Reaction;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReactionsTest {
    /** O exemplo do jogador: dizer ao porco que vai vira-lo comida -> ele foge; ao zumbi -> ele avanca; ao Warden -> nada. */
    @Test
    void threatMakesPreyFleePredatorsFightAncientsIgnore() {
        assertEquals(Reaction.FLEE, Reactions.decide(Lore.of("minecraft:pig").ameaca(), "THREAT", true, false, false));
        assertEquals(Reaction.FIGHT, Reactions.decide(Lore.of("minecraft:zombie").ameaca(), "THREAT", true, false, false));
        assertEquals(Reaction.UNMOVED, Reactions.decide(Lore.of("minecraft:warden").ameaca(), "THREAT", true, false, true));
        assertEquals(Reaction.UNMOVED, Reactions.decide(Lore.of("minecraft:ender_dragon").ameaca(), "THREAT", true, false, true));
    }

    @Test
    void cannotMoveMeansNarrativeOnlyNeverFakePhysics() {
        assertEquals(Reaction.FROZEN, Reactions.decide(Threat.FLEE, "THREAT", false, false, false));
        assertEquals(Reaction.FROZEN, Reactions.decide(Threat.FIGHT, "THREAT", false, false, false));
    }

    /** Aliado/dono/trégua: palavras duras nao fazem a criatura fugir ou atacar quem a protege. */
    @Test
    void bondsProtectThePlayerFromReactions() {
        assertEquals(Reaction.UNMOVED, Reactions.decide(Threat.FIGHT, "THREAT", true, true, false));
        assertEquals(Reaction.UNMOVED, Reactions.decide(Threat.FLEE, "THREAT", true, true, false));
    }

    @Test
    void comfortCalmsAndNormalTalkDoesNothing() {
        assertEquals(Reaction.CALMED, Reactions.decide(Threat.FLEE, "COMFORT", true, false, false));
        assertEquals(Reaction.NONE, Reactions.decide(Threat.FLEE, "NONE", true, false, false));
        assertEquals(Reaction.NONE, Reactions.decide(Threat.FIGHT, "", true, false, false));
    }

    /** Ameaca por palavras: so medo/confianca e um fato de "ameacou" (nao de "feriu"); consolo desfaz parte disso. */
    @Test
    void wordsChangeFearNotWounds() {
        UUID p = UUID.randomUUID();
        MobMemory m = MemoryRules.threatened(MobMemory.create(UUID.randomUUID()), p, 100);
        assertTrue(m.relationOf(p).fear() > 0.2f && m.relationOf(p).trust() < 0f);
        assertEquals(0, m.counts().getOrDefault("DAMAGED_BY_PLAYER", 0));
        assertTrue(MemoryRules.factLines(m, p, 200).stream().anyMatch(l -> l.contains("ameaçou com palavras") && l.contains("não foi ferido")));
        MobMemory calmed = MemoryRules.comforted(m, p, 300);
        assertTrue(calmed.relationOf(p).fear() < m.relationOf(p).fear());
    }

    /** Consciencia: cada criatura sabe quem e. Garantias de coerencia com a pesquisa (wiki). */
    @Test
    void loreKnowsWhatMakesEachCreatureWhoItIs() {
        assertTrue(Lore.count() >= 60);
        var warden = Lore.of("minecraft:warden");
        assertTrue(warden.sentidos().contains("cego") && warden.sentidos().contains("vibra"), "o Warden e cego e percebe vibracoes");
        assertTrue(warden.sentidos().toLowerCase().contains("lã") || warden.sentidos().contains("abafa"));
        assertTrue(Lore.of("minecraft:creeper").origem().contains("porco") && Lore.of("minecraft:creeper").origem().contains("lendas"),
                "origem do creeper e lenda (modelo de porco), nao fato");
        assertTrue(Lore.of("minecraft:creeper").medos().contains("Gatos"));
        assertTrue(Lore.of("minecraft:enderman").motivacao().contains("encaram") || Lore.of("minecraft:enderman").medos().contains("encara"));
        assertTrue(Lore.of("minecraft:enderman").medos().contains("Água"));
        assertTrue(Lore.of("minecraft:zombified_piglin").origem().contains("piglin"));
        assertTrue(Lore.of("minecraft:phantom").origem().contains("dormir"), "phantom: insonia");
        assertTrue(Lore.of("minecraft:wither_skeleton").origem().contains("fortalezas"));
        assertTrue(Lore.of("minecraft:blaze").origem().contains("lendas"), "origem do blaze e teoria: marcada como lenda");
        assertTrue(Lore.of("minecraft:pig").reacao().contains("comida"));
        // todo mob com perfil de conversa tem consciencia propria (ou cai na derivada, nunca fica sem)
        for (String id : List.of("cow", "pig", "chicken", "sheep", "creeper", "zombie", "skeleton", "enderman", "wither_skeleton", "villager")) {
            assertTrue(Lore.has("minecraft:" + id), "sem lore: " + id);
        }
    }
}
