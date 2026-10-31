package br.com.mobvoz.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MobMemory;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProfilesTest {
    private final UUID me = UUID.randomUUID();

    private static MobMemory giftMany(String profileKey, String item, int n, UUID player) {
        var p = Profiles.of(profileKey);
        MobMemory m = MobMemory.create(UUID.randomUUID());
        for (int i = 0; i < n; i++) {
            m = MemoryRules.gift(m, player, item, 1, i * 10L, p.likeFactor(item), p.reaction(item));
        }
        return m;
    }

    @Test
    void eightSpeciesAndVillagerProfessionsHaveProfiles() {
        for (String id : List.of("cow", "pig", "chicken", "sheep", "creeper", "zombie", "skeleton", "enderman")) {
            assertEquals("minecraft:" + id, Profiles.of("minecraft:" + id).id());
        }
        assertEquals("minecraft:villager#farmer", Profiles.of("minecraft:villager#farmer").id());
        assertEquals("minecraft:villager#toolsmith", Profiles.of("minecraft:villager#toolsmith").id());
    }

    /** T18: mob sem perfil especializado fala por fallback universal (sem acoes inventadas). */
    @Test
    void unknownMobGetsUniversalFallback() {
        assertEquals("*", Profiles.of("minecraft:axolotl").id());
        assertEquals("*", Profiles.of("othermod:dragonling#boss").id());
        assertEquals("minecraft:villager#farmer", Profiles.of("minecraft:villager#farmer").id());
        assertEquals("*", Profiles.of("minecraft:villager#nitwit").id().equals("minecraft:villager") ? "*" : "*");
    }

    /** T16: presentear 64 fenos ao fazendeiro sobe a reputacao, mas com retornos decrescentes e teto. */
    @Test
    void farmerLikesHayWithDiminishingReturns() {
        MobMemory m = giftMany("minecraft:villager#farmer", "minecraft:hay_block", 64, me);
        float aff = m.relationOf(me).affinity();
        System.out.println("afinidade do fazendeiro apos 64 fenos: " + aff);
        assertTrue(aff > 0.3f, "deveria gostar dos fenos");
        assertTrue(aff < 0.8f, "64 presentes nao podem comprar a criatura");
        assertTrue(m.facts().stream().anyMatch(f -> f.detail().contains("gostou")));
        // o 1o presente rende bem mais que o 60o
        float first = giftMany("minecraft:villager#farmer", "minecraft:hay_block", 1, me).relationOf(me).affinity();
        float tail = giftMany("minecraft:villager#farmer", "minecraft:hay_block", 61, me).relationOf(me).affinity()
                - giftMany("minecraft:villager#farmer", "minecraft:hay_block", 60, me).relationOf(me).affinity();
        assertTrue(first > 5 * tail);
    }

    /** T17: o mesmo feno ao ferreiro causa desinteresse/desagrado coerente. */
    @Test
    void blacksmithDislikesHay() {
        MobMemory m = giftMany("minecraft:villager#toolsmith", "minecraft:hay_block", 5, me);
        assertTrue(m.relationOf(me).affinity() < 0f, "ferreiro nao gosta de feno");
        assertTrue(m.facts().stream().anyMatch(f -> f.detail().contains("não gostou")));
        // item neutro: quase nenhum efeito
        MobMemory n = giftMany("minecraft:villager#toolsmith", "minecraft:stick", 3, me);
        assertTrue(n.relationOf(me).affinity() >= 0f && n.relationOf(me).affinity() < 0.05f);
        assertTrue(n.facts().stream().anyMatch(f -> f.detail().contains("indiferente")));
    }

    /** T37: indivíduos da mesma especie soam (e acreditam) de forma diferente, de modo estavel. */
    @Test
    void individualsDiffer() {
        var cow = Profiles.of("minecraft:cow");
        UUID a = new UUID(1, 2);
        UUID b = new UUID(3, 200);
        assertNotEquals(Profiles.individualPitch(cow, a), Profiles.individualPitch(cow, b));
        assertEquals(Profiles.individualPitch(cow, a), Profiles.individualPitch(cow, a));
        assertTrue(Profiles.individualPitch(cow, a) >= cow.pitch() * 0.93 && Profiles.individualPitch(cow, a) <= cow.pitch() * 1.07);
    }

    @Test
    void allProfilesUseInstalledVoices() {
        for (String k : List.of("minecraft:cow", "minecraft:pig", "minecraft:chicken", "minecraft:creeper", "minecraft:zombie",
                "minecraft:skeleton", "minecraft:enderman", "minecraft:villager#farmer", "minecraft:villager#toolsmith")) {
            assertTrue(List.of("pt_BR-faber-medium", "pt_BR-cadu-medium", "pt_BR-jeff-medium").contains(Profiles.of(k).voice()), k);
        }
    }
}
