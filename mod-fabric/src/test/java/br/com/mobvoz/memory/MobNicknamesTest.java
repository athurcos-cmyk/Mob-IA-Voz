package br.com.mobvoz.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class MobNicknamesTest {
    private static Optional<String> nick(String s) {
        return MobNicknames.extract(s);
    }

    @Test
    void recognizesTheNaturalWaysToNameACreature() {
        assertEquals(Optional.of("Mimosa"), nick("Vou te chamar de Mimosa."));
        assertEquals(Optional.of("Rex"), nick("Seu nome agora é Rex!"));
        assertEquals(Optional.of("Bolinha"), nick("Eu te batizo de Bolinha"));
        assertEquals(Optional.of("Bolinha"), nick("Vou chamar você de bolinha, tá?"));
        assertEquals(Optional.of("Dona Maria"), nick("Você agora se chama Dona Maria"));
        assertEquals(Optional.of("Pipoca"), nick("Oi! Vou te chamar de Pipoca porque você é pequena"), "so uma palavra: o resto da frase nao entra");
        assertEquals(Optional.of("Mimosa"), nick("Teu nome vai ser Mimosa."));
    }

    @Test
    void questionsNegationsAndCommonWordsAreNotNicknames() {
        assertTrue(nick("Posso te chamar de Mimosa?").isEmpty());
        assertTrue(nick("Seu nome é Rex?").isEmpty());
        assertTrue(nick("E se eu te chamar de Rex, tudo bem?").isEmpty());
        assertTrue(nick("Eu não vou te chamar de Mimosa.").isEmpty());
        assertTrue(nick("Vou te chamar de amigo").isEmpty());
        assertTrue(nick("Qual é o seu nome?").isEmpty());
        assertTrue(nick("Meu nome é Rafa").isEmpty(), "o proprio nome do jogador e outro assunto");
        assertTrue(nick("Vou te chamar de caralho").isEmpty());
        assertTrue(nick("").isEmpty());
    }
}
