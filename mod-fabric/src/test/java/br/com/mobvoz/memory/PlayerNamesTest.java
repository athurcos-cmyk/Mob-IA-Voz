package br.com.mobvoz.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerNamesTest {
    private static Optional<String> x(String text) {
        return PlayerNames.extract(text, "");
    }

    @Test
    void explicitPhrases() {
        assertEquals(Optional.of("Arthur"), x("Meu nome é Arthur"));
        assertEquals(Optional.of("Arthur"), x("oi, me chamo arthur!"));
        assertEquals(Optional.of("Maria Clara"), x("pode me chamar de Maria Clara"));
        assertEquals(Optional.of("Arthur"), x("Eu sou o Arthur, prazer"));
        assertEquals(Optional.of("Zé"), x("meu nome e zé"));
    }

    @Test
    void notNames() {
        assertTrue(x("Eu sou fazendeiro").isEmpty());          // minuscula: profissao, nao nome
        assertTrue(x("Sou o Humano mais forte").isEmpty());    // palavra comum
        assertTrue(x("meu nome é jogador").isEmpty());
        assertTrue(x("quero trocar feno").isEmpty());
        assertTrue(x("Me segue").isEmpty());
        assertTrue(x("Arthur").isEmpty(), "sem pergunta de nome, uma palavra solta nao vira nome");
    }

    @Test
    void shortAnswerToANameQuestion() {
        assertEquals(Optional.of("Arthur"), PlayerNames.extract("Arthur", "Qual é o seu nome, afinal?"));
        assertEquals(Optional.of("Arthur"), PlayerNames.extract("É o Arthur.", "E como você se chama?"));
        assertEquals(Optional.of("Bia"), PlayerNames.extract("bia", "Posso saber o seu nome?"));
        assertTrue(PlayerNames.extract("Sim", "Qual é o seu nome?").isEmpty());
        assertTrue(PlayerNames.extract("Não quero dizer agora, mas obrigado", "Qual é o seu nome?").isEmpty());
        assertTrue(PlayerNames.extract("Arthur", "Tenho uns trigos, quer?").isEmpty(), "a criatura nao perguntou o nome");
    }

    @Test
    void eachCreatureKeepsItsOwnNameAndItPersists() {
        UUID player = UUID.randomUUID();
        MobMemory a = MobMemory.create(UUID.randomUUID());
        MobMemory b = MobMemory.create(UUID.randomUUID());
        a = a.withName(player, "Arthur");
        assertEquals("Arthur", a.nameOf(player));
        assertEquals("", b.nameOf(player), "outra criatura nao sabe");
        var json = MobMemory.CODEC.encodeStart(JsonOps.INSTANCE, a).getOrThrow();
        assertEquals("Arthur", MobMemory.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().nameOf(player));
        // memoria antiga (sem o campo) continua carregando
        var old = json.getAsJsonObject().deepCopy();
        old.remove("names");
        assertEquals("", MobMemory.CODEC.parse(JsonOps.INSTANCE, old).getOrThrow().nameOf(player));
        // mudar outra coisa nao apaga o nome
        assertEquals("Arthur", a.withRelation(player, r -> r.plus(0.1f, 0, 0)).nameOf(player));
    }
}
