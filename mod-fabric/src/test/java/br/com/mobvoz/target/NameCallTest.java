package br.com.mobvoz.target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.target.NameCall.Candidate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NameCallTest {
    @Test
    void callsByNameAtTheStartOrAtTheEnd() {
        assertTrue(NameCall.addresses("Mimosa, vem aqui!", "Mimosa"));
        assertTrue(NameCall.addresses("Ei, Mimosa! Me segue?", "Mimosa"));
        assertTrue(NameCall.addresses("Me segue, Rex!", "Rex"));
        assertTrue(NameCall.addresses("Vem comigo, mimosa", "Mimosa"), "sem diferenciar maiuscula");
        assertTrue(NameCall.addresses("Oi Dona Maria tudo bem", "Dona Maria"), "nome de duas palavras");
        assertTrue(NameCall.addresses("Mímosa, vem!", "Mimosa"), "sem diferenciar acento (o reconhecimento de voz erra acento)");
    }

    @Test
    void talkingAboutSomeoneIsNotCallingThem() {
        assertFalse(NameCall.addresses("Ontem eu vi a Mimosa na colina perto do rio e ela estava bem", "Mimosa"));
        assertFalse(NameCall.addresses("Quem quer cenoura?", "Mimosa"));
        assertFalse(NameCall.addresses("Mimosinha, vem aqui", "Mimosa"), "palavra inteira, nao pedaco");
        assertFalse(NameCall.addresses("", "Mimosa"));
        assertFalse(NameCall.addresses("Mimosa", ""));
    }

    @Test
    void picksTheCalledOneWithinRangeAndPrefersTheLongerThenNearerName() {
        UUID mimosa = UUID.randomUUID();
        UUID rex = UUID.randomUUID();
        UUID far = UUID.randomUUID();
        var all = List.of(new Candidate(mimosa, "Mimosa", 10), new Candidate(rex, "Rex", 5), new Candidate(far, "Bolinha", NameCall.RANGE + 1));
        assertEquals(mimosa, NameCall.pick("Mimosa, vem aqui", all).orElseThrow().id());
        assertEquals(rex, NameCall.pick("Rex me segue", all).orElseThrow().id());
        assertTrue(NameCall.pick("Bolinha vem", all).isEmpty(), "longe demais para ouvir");
        assertTrue(NameCall.pick("Oi gente", all).isEmpty());
        UUID dona = UUID.randomUUID();
        UUID maria = UUID.randomUUID();
        var two = List.of(new Candidate(maria, "Maria", 3), new Candidate(dona, "Dona Maria", 20));
        assertEquals(dona, NameCall.pick("Dona Maria, vem", two).orElseThrow().id(), "o nome mais longo vence");
        var twins = List.of(new Candidate(UUID.randomUUID(), "Rex", 9), new Candidate(rex, "Rex", 4));
        assertEquals(rex, NameCall.pick("Rex, senta", twins).orElseThrow().id(), "empate de nome: a mais perto");
    }
}
