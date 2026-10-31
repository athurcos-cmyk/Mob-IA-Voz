package br.com.mobvoz.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PluralTest {
    @Test
    void namesReadNaturallyInSpeech() {
        assertEquals("barras de ouro", Values.name("minecraft:gold_ingot", true));
        assertEquals("papéis", Values.name("minecraft:paper", true));
        assertEquals("sílex", Values.name("minecraft:flint", true));
        assertEquals("lã", Values.name("minecraft:white_wool", true));
        assertEquals("carvão", Values.name("minecraft:coal", true));
        assertEquals("maçãs", Values.name("minecraft:apple", true));
        assertEquals("pães", Values.name("minecraft:bread", true));
        assertEquals("blocos de grama", Values.name("minecraft:grass_block", true));
        assertEquals("teias", Values.name("minecraft:cobweb", true));
        assertEquals("pérolas do fim", Values.name("minecraft:ender_pearl", true));
        assertEquals("barra de ouro", Values.name("minecraft:gold_ingot", false));
    }
}
