package br.com.mobvoz.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecallTest {
    private final UUID mob = UUID.randomUUID();
    private final UUID me = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();
    /** sorteio que sempre cai dentro dos 30% */
    private final Random lucky = new Random() {
        @Override
        public double nextDouble() {
            return 0.0;
        }
    };

    @BeforeEach
    void clean() {
        Recall.reset();
    }

    private MobMemory withGift(long tick) {
        return MemoryRules.gift(MobMemory.create(UUID.randomUUID()), me, "minecraft:wheat", 1, tick);
    }

    @Test
    void remembersOneMomentAndAsksToMentionItLightly() {
        Optional<String> r = Recall.pick(mob, withGift(0), me, 5_000, lucky);
        assertTrue(r.isPresent());
        assertTrue(r.get().startsWith("LEMBRANÇA (opcional)") && r.get().contains("minecraft:wheat") && r.get().contains("Nunca insista"), r.get());
    }

    @Test
    void neverRepeatsTooSoonNeitherTheCreatureNorTheMoment() {
        MobMemory m = withGift(0);
        assertTrue(Recall.pick(mob, m, me, 5_000, lucky).isPresent());
        assertTrue(Recall.pick(mob, m, me, 5_000 + Recall.MOB_COOLDOWN - 1, lucky).isEmpty(), "a criatura descansa 5 minutos");
        assertTrue(Recall.pick(mob, m, me, 5_000 + Recall.MOB_COOLDOWN + 1, lucky).isEmpty(), "o mesmo momento so volta depois de 30 minutos");
        assertTrue(Recall.pick(mob, m, me, 5_000 + Recall.FACT_COOLDOWN + 1, lucky).isPresent());
    }

    @Test
    void onlyMomentsOfThisPlayerThatAreNotTooRecentAndNotTheFirstMeeting() {
        assertTrue(Recall.pick(mob, withGift(0), other, 5_000, lucky).isEmpty(), "momento de outro jogador");
        assertTrue(Recall.pick(mob, withGift(4_990), me, 5_000, lucky).isEmpty(), "acabou de acontecer: a conversa ja trata disso");
        assertTrue(Recall.pick(mob, MemoryRules.firstMeeting(MobMemory.create(UUID.randomUUID()), me, 0), me, 5_000, lucky).isEmpty(), "so o primeiro encontro nao e marcante");
    }

    @Test
    void mostRepliesHaveNoRecall() {
        MobMemory m = withGift(0);
        int hits = 0;
        Random rnd = new Random(1);
        for (int i = 0; i < 1000; i++) {
            Recall.reset();
            hits += Recall.pick(mob, m, me, 5_000, rnd).isPresent() ? 1 : 0;
        }
        assertTrue(hits > 220 && hits < 380, "cerca de 30%: " + hits);
        assertEquals(Optional.empty(), Recall.pick(mob, MobMemory.create(UUID.randomUUID()), me, 5_000, lucky), "sem momentos, sem lembranca");
    }
}
