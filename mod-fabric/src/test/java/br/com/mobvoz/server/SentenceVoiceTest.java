package br.com.mobvoz.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

/** Voz por frase, som de "pensando" e o contexto de quando a criatura puxa assunto. */
class SentenceVoiceTest {
    @Test
    void shortTextIsNotSplit() {
        assertEquals(List.of("Muuu! O capim está bom."), SentenceVoice.split("Muuu! O capim está bom."));
        assertEquals(List.of(""), SentenceVoice.split(null));
    }

    @Test
    void longTextSplitsAfterSentencesAndEachPartIsLongEnough() {
        String t = "Muuu, hoje o capim está uma delícia e o sol está quente. Você já provou um pouco? Eu adoro ficar aqui o dia todo. E você, o que gosta de fazer?";
        List<String> parts = SentenceVoice.split(t);
        assertTrue(parts.size() >= 2 && parts.size() <= SentenceVoice.MAX_PARTS, parts.toString());
        assertEquals(t, String.join(" ", parts), "nada se perde nem se repete");
        for (String p : parts.subList(0, parts.size() - 1)) {
            assertTrue(p.length() >= SentenceVoice.MIN_CHUNK, "pedaco curto demais: " + p);
        }
        assertTrue(parts.get(0).endsWith(".") || parts.get(0).endsWith("?") || parts.get(0).endsWith("!"));
    }

    @Test
    void ellipsisIsNotASentenceEnd() {
        String t = "Hmm... deixa eu pensar um pouco sobre isso, porque é uma pergunta difícil de responder... Acho que sim, mas não tenho certeza nenhuma.";
        List<String> parts = SentenceVoice.split(t);
        assertEquals(t, String.join(" ", parts));
        for (String p : parts) {
            assertFalse(p.startsWith("."), p);
        }
        assertTrue(parts.stream().noneMatch(p -> p.equals("Hmm...")), "reticencias nao quebram a fala: " + parts);
    }

    @Test
    void manySentencesAreMergedIntoAtMostFourParts() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append("Esta é a frase número um dois três. ");
        }
        List<String> parts = SentenceVoice.split(sb.toString());
        assertTrue(parts.size() <= SentenceVoice.MAX_PARTS, "partes: " + parts.size());
        assertEquals(sb.toString().strip().replaceAll("\\s+", " "), String.join(" ", parts));
    }

    @Test
    void aTinyLastSentenceJoinsThePreviousOne() {
        List<String> parts = SentenceVoice.split("Eu gosto muito de ficar aqui deitada no capim fresco, sabe? Sim.");
        assertEquals(1, parts.size(), parts.toString());
    }

    @Test
    void synthesisRunsOneAtATimeInOrderAndSurvivesAFailure() {
        List<CompletableFuture<Optional<short[]>>> pending = new ArrayList<>();
        List<String> started = new ArrayList<>();
        SentenceVoice.Voiced v = SentenceVoice.chain(List.of("a", "b", "c"), p -> {
            started.add(p);
            CompletableFuture<Optional<short[]>> f = new CompletableFuture<>();
            pending.add(f);
            return f;
        });
        assertEquals(List.of("a"), started, "so a 1a comeca; o TTS e um so");
        pending.get(0).complete(Optional.of(new short[] {1}));
        assertEquals(List.of("a", "b"), started);
        pending.get(1).completeExceptionally(new RuntimeException("tts caiu"));
        assertEquals(List.of("a", "b", "c"), started, "uma frase que falha nao trava as seguintes");
        pending.get(2).complete(Optional.of(new short[] {3}));
        assertTrue(v.first().isDone() && v.first().join().isPresent());
        assertTrue(v.parts().get(1).isCompletedExceptionally());
        assertEquals(3, v.parts().get(2).join().get()[0]);
    }

    @Test
    void thinkingSoundsAreSpeciesStyledAndNeverRepeatInARow() {
        Random rnd = new Random(7);
        String last = null;
        for (int i = 0; i < 200; i++) {
            String s = ThinkingSound.pick("cow", last, rnd);
            assertFalse(s.equals(last), "repetiu seguido: " + s);
            assertTrue(s.toLowerCase().contains("mu") || s.contains("ruminar") || s.contains("peraí"), "a vaca fala como vaca: " + s);
            last = s;
        }
        assertFalse(ThinkingSound.pick("espécie-sem-lista", null, rnd).isBlank(), "especie sem lista usa o grupo padrao");
        for (String sp : List.of("pig", "sheep", "chicken", "villager", "iron_golem", "wolf", "cat", "horse")) {
            assertFalse(ThinkingSound.pick(sp, null, rnd).isBlank(), sp);
        }
    }

    @Test
    void theAnswerWaitsForThePlayingSoundButNotForOneThatNeverStarted() {
        ThinkingSound.Handle h = new ThinkingSound.Handle();
        List<String> order = new ArrayList<>();
        h.beforeAnswer(() -> order.add("resposta")); // nenhum som tocando: sai na hora
        assertEquals(List.of("resposta"), order);
    }

    @Test
    void anOpeningIsRememberedAndGivesTheNextAnswerItsContext() {
        UUID mob = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        assertEquals(List.of("fato"), ConversationService.withOpening(List.of("fato"), mob, player), "sem abertura nada muda");
        ConversationService.rememberOpening(mob, player, "Qual foi o seu maior susto?");
        var hist = ConversationService.historyOf(mob, player);
        assertEquals(1, hist.size());
        assertEquals("Qual foi o seu maior susto?", hist.get(0).get("voce"));
        List<String> facts = ConversationService.withOpening(List.of("fato"), mob, player);
        assertEquals(2, facts.size());
        assertTrue(facts.get(0).contains("Qual foi o seu maior susto?") && facts.get(0).contains("continue ESSE mesmo assunto"), facts.get(0));
        assertEquals("fato", facts.get(1));
        // se a conversa seguiu (a ultima fala da criatura ja nao e a abertura), o aviso some
        ConversationService.rememberOpening(UUID.randomUUID(), player, "outra criatura");
        assertEquals(List.of("fato"), ConversationService.withOpening(List.of("fato"), UUID.randomUUID(), player));
    }
}
