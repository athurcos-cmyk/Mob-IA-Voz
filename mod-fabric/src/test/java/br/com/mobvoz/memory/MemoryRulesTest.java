package br.com.mobvoz.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.ai.SidecarClient.Proposal;
import br.com.mobvoz.memory.MobMemory.Stance;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MemoryRulesTest {
    private final UUID me = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    private static Proposal claim(String text, String stance) {
        return new Proposal("REGISTER_CLAIM", Map.of("text", text, "stance", stance));
    }

    /** T10/T33: salvar e recarregar preserva identidade, fatos, alegacoes e relacao (codec = formato do NBT). */
    @Test
    void saveAndReloadKeepsEverything() {
        MobMemory m = MobMemory.create(UUID.randomUUID());
        m = MemoryRules.gift(m, me, "minecraft:wheat", 1, 100);
        m = MemoryRules.applyProposals(m, me, List.of(claim("sou seu pai", "SUSPECTS")), 200);
        JsonElement saved = MobMemory.CODEC.encodeStart(JsonOps.INSTANCE, m).getOrThrow();
        MobMemory loaded = MobMemory.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();
        assertEquals(m, loaded);
        assertEquals(m.identityId(), loaded.identityId());
        assertTrue(MemoryRules.factLines(loaded, me, 300).stream().anyMatch(l -> l.contains("minecraft:wheat")));
    }

    /** T35/T36: uma mentira vira ALEGACAO com crenca; nenhum fato e criado e a relacao nao muda. */
    @Test
    void lieBecomesClaimNeverFact() {
        MobMemory m = MemoryRules.firstMeeting(MobMemory.create(UUID.randomUUID()), me, 0);
        MobMemory.Relationship before = m.relationOf(me);
        List<MobMemory.Fact> factsBefore = m.facts();
        m = MemoryRules.applyProposals(m, me, List.of(claim("eu sou o rei das esmeraldas", "BELIEVES")), 10);
        assertEquals(factsBefore, m.facts());
        assertEquals(before, m.relationOf(me));
        assertEquals(1, m.claims().size());
        assertEquals(Stance.BELIEVES, m.claims().get(0).stance());
        assertTrue(MemoryRules.claimLines(m, me).get(0).contains("afirmou"));
        assertFalse(MemoryRules.factLines(m, me, 20).stream().anyMatch(l -> l.contains("rei")));
    }

    /** T41: descobrir a mentira muda so a crenca; a alegacao anterior continua registrada. */
    @Test
    void disprovingChangesBeliefOnly() {
        MobMemory m = MemoryRules.applyProposals(MobMemory.create(UUID.randomUUID()), me, List.of(claim("sou seu pai", "BELIEVES")), 0);
        m = MemoryRules.applyProposals(m, me, List.of(new Proposal("UPDATE_BELIEF", Map.of("claim_index", 0.0, "stance", "DISPROVED"))), 5);
        assertEquals(1, m.claims().size());
        assertEquals(Stance.DISPROVED, m.claims().get(0).stance());
    }

    /** T42: duas pessoas alegando a mesma coisa = duas alegacoes; um jogador nao mexe na crenca da alegacao do outro. */
    @Test
    void conflictingClaimsAreSeparate() {
        MobMemory m = MobMemory.create(UUID.randomUUID());
        m = MemoryRules.applyProposals(m, me, List.of(claim("sou seu pai", "BELIEVES")), 0);
        m = MemoryRules.applyProposals(m, other, List.of(claim("sou seu pai", "DOUBTS")), 1);
        assertEquals(2, m.claims().size());
        m = MemoryRules.applyProposals(m, other, List.of(new Proposal("UPDATE_BELIEF", Map.of("claim_index", 0.0, "stance", "DISPROVED"))), 2);
        assertEquals(Stance.BELIEVES, m.claims().get(0).stance()); // alegacao de "me" intocada por "other"
    }

    @Test
    void unknownProposalsAndGarbageDoNothing() {
        MobMemory m = MobMemory.create(UUID.randomUUID());
        MobMemory out = MemoryRules.applyProposals(m, me, List.of(
                new Proposal("FOLLOW_PLAYER", Map.of()), new Proposal("minecraft:give", Map.of("item", "diamond")),
                new Proposal("REGISTER_CLAIM", Map.of("text", "x")), new Proposal("REGISTER_CLAIM", Map.of("text", 42)),
                new Proposal("UPDATE_BELIEF", Map.of("claim_index", 99.0, "stance", "BELIEVES"))), 0);
        assertEquals(m, out);
    }

    @Test
    void claimTextIsSanitizedAndCapped() {
        MobMemory m = MemoryRules.applyProposals(MobMemory.create(UUID.randomUUID()), me,
                List.of(claim("linha1\n\tlinha2 " + "a".repeat(400), "UNKNOWN")), 0);
        String t = m.claims().get(0).text();
        assertTrue(t.length() <= MobMemory.MAX_CLAIM_TEXT);
        assertFalse(t.contains("\n"));
    }

    @Test
    void capsAreEnforcedAndTotalsKept() {
        MobMemory m = MobMemory.create(UUID.randomUUID());
        for (int i = 0; i < 30; i++) {
            m = MemoryRules.gift(m, me, "minecraft:wheat", 1, i * 100L);
        }
        assertEquals(MobMemory.MAX_FACTS, m.facts().size());
        assertEquals(30, m.counts().get("GIFT"));
        for (int i = 0; i < 20; i++) {
            m = MemoryRules.applyProposals(m, me, List.of(claim("alegacao numero " + i, "UNKNOWN")), i);
        }
        assertEquals(MobMemory.MAX_CLAIMS, m.claims().size());
        assertTrue(MemoryRules.factLines(m, me, 99999).stream().anyMatch(l -> l.contains("30 vezes")));
    }

    @Test
    void damageBurstCountsOnceAndHurtsRelation() {
        MobMemory m = MemoryRules.damagedByPlayer(MobMemory.create(UUID.randomUUID()), me, 100);
        m = MemoryRules.damagedByPlayer(m, me, 105);
        assertEquals(1, m.counts().get("DAMAGED_BY_PLAYER"));
        assertTrue(m.relationOf(me).trust() < 0 && m.relationOf(me).fear() > 0);
    }

    @Test
    void giftImprovesRelation() {
        MobMemory m = MemoryRules.gift(MobMemory.create(UUID.randomUUID()), me, "minecraft:wheat", 1, 0);
        assertTrue(m.relationOf(me).affinity() > 0 && m.relationOf(me).trust() > 0);
        assertEquals(0f, m.relationOf(other).trust());
    }
}
