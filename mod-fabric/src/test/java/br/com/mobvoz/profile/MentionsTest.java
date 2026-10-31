package br.com.mobvoz.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MentionsTest {
    @Test
    void recognisesOtherCreaturesByWholeWordAndLongestNameFirst() {
        assertEquals(List.of("minecraft:cow"), Mentions.mentioned("minecraft:pig", "O que você acha das vacas?"));
        assertEquals(List.of("minecraft:chicken"), Mentions.mentioned("minecraft:pig", "E das galinhas?"));
        assertEquals(List.of("minecraft:wither_skeleton"), Mentions.mentioned("minecraft:cow", "Você já viu um esqueleto Wither?"));
        assertEquals(List.of("minecraft:iron_golem"), Mentions.mentioned("minecraft:cow", "O que você acha do golem de ferro?"));
        assertTrue(Mentions.mentioned("minecraft:pig", "Você gosta de porcos?").isEmpty(), "nao cita a si mesmo");
        assertTrue(Mentions.mentioned("minecraft:pig", "Tudo bem com você?").isEmpty());
        assertTrue(Mentions.mentioned("minecraft:pig", "vacancia").isEmpty(), "palavra inteira, nao pedaco");
    }

    @Test
    void factsCarryLoreSoTheCreatureCanHaveAnOpinionWithoutInventing() {
        var f = Mentions.facts("minecraft:cow", "O que você acha dos esqueletos?");
        assertEquals(1, f.size());
        assertTrue(f.get(0).contains("O jogador citou esqueleto") && f.get(0).contains("opinião"), f.get(0));
        assertTrue(Mentions.facts("minecraft:cow", "oi").isEmpty());
    }
}
