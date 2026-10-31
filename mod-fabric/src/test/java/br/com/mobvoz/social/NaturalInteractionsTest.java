package br.com.mobvoz.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.knowledge.Knowledge;
import br.com.mobvoz.knowledge.Knowledge.Event;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.memory.MobMemory.Relationship;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import br.com.mobvoz.orders.OrderPolicy.Verdict;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Interacoes naturais: domar por conversa, honestidade lembrada pelo grupo e pelo Dragao, cena, aviso e cumprimento. */
class NaturalInteractionsTest {
    private static final Subject HORSE = new Subject("minecraft:horse", true, false, false, false, true);
    private static final Subject COW = new Subject("minecraft:cow", true, false);
    private static final UUID ME = UUID.randomUUID();

    private static Verdict tame(Subject s, double trust, double bonus) {
        Relationship r = new Relationship((float) trust + (float) (2 * bonus), 0, 0); // mesmo calculo do Orders.handle
        return OrderPolicy.decide("TAME_ME", s, r, false, MobOrders.NONE, "p", false, 0).verdict();
    }

    @Test
    void onlyTameableCreaturesCanBeTamedAndTheyNeedSomeRapport() {
        assertTrue(OrderPolicy.ACTIONS.contains("TAME_ME"));
        assertEquals(Verdict.UNSUPPORTED, tame(COW, 1.0, 0), "vaca nao e domavel");
        assertEquals(Verdict.REFUSED, tame(HORSE, 0.0, 0), "estranho nao doma");
        assertEquals(Verdict.REFUSED, tame(HORSE, 0.3, 0), "confianca baixa (calor 0,15 < 0,20)");
        assertEquals(Verdict.ACCEPTED, tame(HORSE, 0.4, 0), "um pouco de confianca basta num cavalo");
    }

    /** O jogador convence com argumentos, mas o efeito e limitado e medo anula. */
    @Test
    void aGoodArgumentHelpsButIsBounded() {
        assertEquals(0, OrderPolicy.appealBonus(0, 2, 0), 1e-9, "sem motivo, nada");
        double weak = OrderPolicy.appealBonus(1, 1, 0);
        double strong = OrderPolicy.appealBonus(2, 1, 0);
        assertTrue(weak > 0 && strong > weak && strong <= 0.25, weak + " / " + strong);
        assertTrue(OrderPolicy.appealBonus(2, 2, 0) > strong, "credulidade ajuda");
        assertEquals(0, OrderPolicy.appealBonus(2, 2, 0.8), 1e-9, "com medo de quem pede, argumento nenhum funciona");
        assertEquals(0.20, OrderPolicy.appealBonus(99, 2, 0), 1e-9, "teto: a forca maxima e 2, mesmo que o analisador exagere");
        // sem rapport nenhum, nem um argumento forte doma; com um pouco, doma
        assertEquals(Verdict.REFUSED, tame(HORSE, 0.0, OrderPolicy.appealBonus(2, 1, 0)));
        assertEquals(Verdict.ACCEPTED, tame(HORSE, 0.1, OrderPolicy.appealBonus(2, 2, 0)));
    }

    @Test
    void honestyBecomesAVerifiedFactThatTheSpeciesAndTheDragonKnow() {
        long t = 5000;
        MobMemory m = MemoryRules.honestReturn(MobMemory.create(UUID.randomUUID()), ME, "4 pérolas do Fim", t);
        assertTrue(MemoryRules.factLines(m, ME, t + 100).stream().anyMatch(l -> l.contains("devolveu") && l.contains("honesto") && l.contains("4 pérolas")));
        assertTrue(m.relationOf(ME).trust() > 0.1f, "confianca sobe");

        Knowledge k = Knowledge.EMPTY.withFaction("enderman", new Event("HONEST_RETURN", ME, "4 pérolas do Fim", t))
                .withHistory(new Event("HONEST_RETURN", ME, "um(a) enderman", t));
        assertTrue(k.factionLines("enderman", ME, t + 100).stream().anyMatch(l -> l.contains("Todos os da sua espécie sabem") && l.contains("devolveu")));
        assertTrue(k.historyLines(ME, t + 100).stream().anyMatch(l -> l.contains("Registro do mundo") && l.contains("honestamente") && l.contains("enderman")),
                "o Dragao recebe isso como fato do registro, nao como invencao");
        assertTrue(k.historyLines(UUID.randomUUID(), t + 100).get(0).contains("outro jogador"), "e distingue quem foi");
        MobMemory tamed = MemoryRules.tamed(MobMemory.create(UUID.randomUUID()), ME, t);
        assertTrue(MemoryRules.factLines(tamed, ME, t).stream().anyMatch(l -> l.contains("aceitar ser dele")));
    }

    @Test
    void sceneFactsAreShortAndOnlyWhatIsRelevant() {
        var night = SceneFacts.format(new SceneFacts.Inputs(18000, true, false, "overworld", 4f, 20, "uma espada de diamante",
                Map.of("zumbi", 2, "vaca", 1), 20f, 20f));
        String all = String.join(" | ", night);
        assertTrue(all.contains("é noite") && all.contains("chovendo") && all.contains("muito ferido (2 corações)") && all.contains("espada de diamante")
                && all.contains("2 zumbi"), all);
        assertTrue(night.size() <= 5, "no maximo 5 linhas: " + night.size());
        var calm = SceneFacts.format(new SceneFacts.Inputs(1000, false, false, "overworld", 20f, 20, "", Map.of(), 20f, 20f));
        assertEquals(List.of("Cena agora: é de dia."), calm, "dia calmo e tudo bem: so a hora");
        assertTrue(String.join(" ", SceneFacts.format(new SceneFacts.Inputs(1000, false, true, "the_nether", 20f, 3, "", Map.of(), 2f, 20f)))
                .matches(".*tempestade.*Nether.*fome.*você mesmo está ferido.*"));
    }

    @Test
    void greetingAndWarningRulesAreBoundedAndOnlyForKnownFaces() {
        assertFalse(Events.knows(List.of("FIRST_MET")), "so o primeiro encontro nao conta como conhecido");
        assertTrue(Events.knows(List.of("FIRST_MET", "TRADE")));
        assertTrue(Events.knows(List.of("HONEST_RETURN")));
        assertTrue(Events.greetingDue(null, 100, Events.GREET_COOLDOWN), "nunca cumprimentou");
        assertFalse(Events.greetingDue(100L, 100 + Events.GREET_COOLDOWN - 1, Events.GREET_COOLDOWN));
        assertTrue(Events.greetingDue(100L, 100 + Events.GREET_COOLDOWN, Events.GREET_COOLDOWN));
        assertTrue(Events.WARN_COOLDOWN >= 600 && Events.GREET_PLAYER_COOLDOWN >= 1200, "sem spam");
    }
}
