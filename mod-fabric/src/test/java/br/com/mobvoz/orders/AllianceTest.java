package br.com.mobvoz.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.memory.MobMemory.Relationship;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import br.com.mobvoz.orders.OrderPolicy.Verdict;
import br.com.mobvoz.social.Bonds;
import br.com.mobvoz.social.Bonds.Bond;
import com.mojang.serialization.JsonOps;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AllianceTest {
    private static final String ME = "p1";
    private static final Relationship TRUSTED = new Relationship(0.8f, 0.8f, 0);

    /** T40: pedido de alianca nao e bloqueado por especie; so a execucao depende de adaptador (resposta honesta). */
    @Test
    void anySpeciesGetsADecisionNeverASilentBlock() {
        Subject chicken = new Subject("minecraft:chicken", true, false, false);   // anda, nao luta
        Subject zombie = new Subject("minecraft:zombie", true, true, true);
        Subject warden = new Subject("minecraft:warden", false, true, true);       // sem adaptador de movimento
        Subject villager = new Subject("minecraft:villager", false, false, false);
        for (Subject s : new Subject[] {chicken, zombie, warden, villager}) {
            for (String a : new String[] {"FOLLOW_PLAYER", "DEFEND_PLAYER", "CEASE_HOSTILITY"}) {
                var d = OrderPolicy.decide(a, s, TRUSTED, true, MobOrders.NONE, ME, false, 0);
                assertTrue(d.verdict() != null && !d.reason().equals("pedido desconhecido"), s.entityType() + " " + a);
            }
        }
        assertEquals(Verdict.UNSUPPORTED, OrderPolicy.decide("DEFEND_PLAYER", chicken, TRUSTED, true, MobOrders.NONE, ME, false, 0).verdict());
        assertEquals(Verdict.ACCEPTED, OrderPolicy.decide("DEFEND_PLAYER", zombie, TRUSTED, false, MobOrders.NONE, ME, false, 0).verdict());
        assertEquals(Verdict.UNSUPPORTED, OrderPolicy.decide("FOLLOW_PLAYER", warden, TRUSTED, false, MobOrders.NONE, ME, false, 0).verdict());
    }

    @Test
    void defendNeedsMoreTrustThanFollow() {
        Subject zombie = new Subject("minecraft:zombie", true, true, true);
        Relationship mid = new Relationship(0.65f, 0.65f, 0); // 0.65: acompanha (0.6) mas nao luta (0.7)
        assertEquals(Verdict.ACCEPTED, OrderPolicy.decide("FOLLOW_PLAYER", zombie, mid, false, MobOrders.NONE, ME, false, 0).verdict());
        assertEquals(Verdict.REFUSED, OrderPolicy.decide("DEFEND_PLAYER", zombie, mid, false, MobOrders.NONE, ME, false, 0).verdict());
    }

    @Test
    void defendPersistsAndDismissClearsIt() {
        Subject zombie = new Subject("minecraft:zombie", true, true, true);
        var d = OrderPolicy.decide("DEFEND_PLAYER", zombie, TRUSTED, false, MobOrders.NONE, ME, false, 0);
        MobOrders o = OrderPolicy.apply(d, MobOrders.NONE, ME, "ow", 0, 0, 0, 5);
        assertTrue(o.defend() && o.owner().equals(ME) && o.active());
        var json = MobOrders.CODEC.encodeStart(JsonOps.INSTANCE, o).getOrThrow();
        assertEquals(o, MobOrders.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        var dis = OrderPolicy.decide("DISMISS_ALLY", zombie, TRUSTED, false, o, ME, false, 9);
        assertEquals(Verdict.ACCEPTED, dis.verdict());
        assertTrue(!OrderPolicy.apply(dis, o, ME, "", 0, 0, 0, 9).defend());
    }

    /** T22: ferir o pactuado rompe o pacto: tregua some, cooldown longo, confianca despenca. */
    @Test
    void breakingAPactTurnsTheCreatureAgainstYou() {
        MobOrders o = MobOrders.NONE.withTruce(ME, 5000, 6200).truceBroken(9000);
        assertEquals("", o.truceFor());
        assertEquals(0, o.truceUntil());
        var d = OrderPolicy.decide("CEASE_HOSTILITY", new Subject("minecraft:creeper", true, true), TRUSTED, false, o, ME, false, 3000);
        assertEquals(Verdict.REFUSED, d.verdict(), "sem nova tregua durante o cooldown");
        UUID u = UUID.randomUUID();
        MobMemory m = MemoryRules.pactBroken(MemoryRules.gift(MobMemory.create(UUID.randomUUID()), u, "minecraft:gunpowder", 1, 0), u, 10);
        assertTrue(m.relationOf(u).trust() < 0f);
        assertEquals(1, m.counts().get("PACT_BROKEN"));
        assertTrue(MemoryRules.factLines(m, u, 100).stream().anyMatch(l -> l.contains("quebrou o pacto")));
    }

    @Test
    void bondStatesAreDerivedFromServerFacts() {
        MobOrders none = MobOrders.NONE;
        assertEquals(Bond.UNKNOWN, Bonds.of(false, Relationship.NEUTRAL, none, ME, false));
        assertEquals(Bond.ACQUAINTANCE, Bonds.of(true, Relationship.NEUTRAL, none, ME, false));
        assertEquals(Bond.FRIEND, Bonds.of(true, TRUSTED, none, ME, false));
        assertEquals(Bond.RIVAL, Bonds.of(true, new Relationship(-0.8f, -0.2f, 0.1f), none, ME, false));
        assertEquals(Bond.HOSTILE, Bonds.of(true, new Relationship(-0.8f, -0.8f, 0.9f), none, ME, false));
        assertEquals(Bond.FOLLOWER, Bonds.of(true, TRUSTED, none.follow(ME, 0), ME, false));
        assertEquals(Bond.GUARD, Bonds.of(true, TRUSTED, none.follow(ME, 0).withDefend(ME, true), ME, false));
        assertEquals(Bond.BETRAYED, Bonds.of(true, TRUSTED, none, ME, true));
    }
}
