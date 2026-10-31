package br.com.mobvoz.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mobvoz.memory.MobMemory.Relationship;
import br.com.mobvoz.orders.OrderPolicy.Decision;
import br.com.mobvoz.orders.OrderPolicy.Subject;
import br.com.mobvoz.orders.OrderPolicy.Verdict;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderPolicyTest {
    private static final Subject COW = new Subject("minecraft:cow", true, false);
    private static final Subject PIG = new Subject("minecraft:pig", true, false);
    private static final Subject CHICKEN = new Subject("minecraft:chicken", true, false);
    private static final Subject CREEPER = new Subject("minecraft:creeper", true, true);
    private static final Subject VILLAGER = new Subject("minecraft:villager", false, false);
    private static final Relationship NEUTRAL = Relationship.NEUTRAL;
    private static final String ME = "player-a";

    private static Decision d(String a, Subject s, Relationship r, boolean kin, MobOrders cur, boolean otherOnline) {
        return OrderPolicy.decide(a, s, r, kin, cur, ME, otherOnline, 1000);
    }

    /** T11: pedir para seguir sem incentivo -> aceita ou recusa conforme o perfil, sem hack. */
    @Test
    void followDependsOnSpeciesAndWarmth() {
        assertEquals(Verdict.ACCEPTED, d("FOLLOW_PLAYER", COW, NEUTRAL, false, MobOrders.NONE, false).verdict());
        assertEquals(Verdict.ACCEPTED, d("FOLLOW_PLAYER", CHICKEN, NEUTRAL, false, MobOrders.NONE, false).verdict());
        assertEquals(Verdict.REFUSED, d("FOLLOW_PLAYER", PIG, NEUTRAL, false, MobOrders.NONE, false).verdict());
        // presentes (afinidade/confianca) convencem o porco teimoso
        Relationship friend = new Relationship(0.2f, 0.4f, 0);
        assertEquals(Verdict.ACCEPTED, d("FOLLOW_PLAYER", PIG, friend, false, MobOrders.NONE, false).verdict());
        // medo atrapalha
        assertEquals(Verdict.REFUSED, d("FOLLOW_PLAYER", COW, new Relationship(0, 0, 0.8f), false, MobOrders.NONE, false).verdict());
    }

    /** T35/T36: acreditar que o jogador e "pai" faz a galinha confiar mais, sem virar fato. */
    @Test
    void believedKinshipMakesPigFollow() {
        assertEquals(Verdict.REFUSED, d("FOLLOW_PLAYER", PIG, NEUTRAL, false, MobOrders.NONE, false).verdict());
        assertEquals(Verdict.ACCEPTED, d("FOLLOW_PLAYER", PIG, new Relationship(0.1f, 0.1f, 0), true, MobOrders.NONE, false).verdict());
    }

    /** T40: nenhuma especie e bloqueada na conversa; so a execucao depende do adaptador (recusa honesta). */
    @Test
    void unsupportedMobsGetHonestRefusalNotSuccess() {
        Decision v = d("FOLLOW_PLAYER", VILLAGER, new Relationship(1, 1, 0), true, MobOrders.NONE, false);
        assertEquals(Verdict.UNSUPPORTED, v.verdict());
        assertFalse(v.accepted());
        assertEquals(MobOrders.NONE, OrderPolicy.apply(v, MobOrders.NONE, ME, "overworld", 0, 0, 0, 10)); // nada muda
    }

    @Test
    void hostileMobsNeedMuchMoreTrustToFollow() {
        assertEquals(Verdict.REFUSED, d("FOLLOW_PLAYER", CREEPER, new Relationship(0.3f, 0.3f, 0), false, MobOrders.NONE, false).verdict());
        assertEquals(Verdict.ACCEPTED, d("FOLLOW_PLAYER", CREEPER, new Relationship(0.8f, 0.8f, 0), false, MobOrders.NONE, false).verdict());
    }

    /** T38/T39: ficar + portal; depois "me segue" troca o estado sem perder a ordem de portal. */
    @Test
    void stayPortalHoldThenFollow() {
        MobOrders o = MobOrders.NONE;
        Decision stay = d("STAY_AT", COW, NEUTRAL, false, o, false);
        o = OrderPolicy.apply(stay, o, ME, "minecraft:overworld", 10, 64, 20, 5);
        assertEquals(MobOrders.Mode.STAY, o.mode());
        assertEquals(20.0, o.az());
        Decision hold = d("PORTAL_HOLD", COW, NEUTRAL, false, o, false);
        o = OrderPolicy.apply(hold, o, ME, "minecraft:overworld", 10, 64, 20, 6);
        assertTrue(o.portalHold());
        o = OrderPolicy.apply(d("FOLLOW_PLAYER", COW, NEUTRAL, false, o, false), o, ME, "minecraft:overworld", 30, 64, 30, 50);
        assertEquals(MobOrders.Mode.FOLLOW, o.mode());
        assertTrue(o.portalHold(), "seguir nao anula a ordem de portal");
        assertEquals(Verdict.ALREADY, d("PORTAL_HOLD", COW, NEUTRAL, false, o, false).verdict());
        o = OrderPolicy.apply(d("PORTAL_RELEASE", COW, NEUTRAL, false, o, false), o, ME, "", 0, 0, 0, 60);
        assertFalse(o.portalHold());
    }

    @Test
    void portalAndDismissNeedAnOrderFromThisPlayer() {
        assertEquals(Verdict.NOT_APPLICABLE, d("PORTAL_HOLD", COW, NEUTRAL, false, MobOrders.NONE, false).verdict());
        assertEquals(Verdict.NOT_APPLICABLE, d("DISMISS_ALLY", COW, NEUTRAL, false, MobOrders.NONE, false).verdict());
        MobOrders other = MobOrders.NONE.follow("player-b", 0);
        assertEquals(Verdict.NOT_APPLICABLE, d("DISMISS_ALLY", COW, NEUTRAL, false, other, true).verdict());
    }

    /** T42: dono online tem prioridade; outro jogador nao toma a criatura. */
    @Test
    void ownerHasPriorityWhileOnline() {
        MobOrders other = MobOrders.NONE.follow("player-b", 0);
        assertEquals(Verdict.REFUSED, d("FOLLOW_PLAYER", COW, NEUTRAL, false, other, true).verdict());
        assertEquals(Verdict.ACCEPTED, d("FOLLOW_PLAYER", COW, NEUTRAL, false, other, false).verdict()); // dono offline: reassocia
    }

    @Test
    void dismissKeepsNothingActiveButMemoriesAreElsewhere() {
        MobOrders o = MobOrders.NONE.follow(ME, 0).withPortalHold(true);
        o = OrderPolicy.apply(d("DISMISS_ALLY", COW, NEUTRAL, false, o, false), o, ME, "", 0, 0, 0, 5);
        assertFalse(o.active());
    }

    /** T21/T22 (base): tregua so para hostil, com confianca, prazo e cooldown. */
    @Test
    void truceRules() {
        assertEquals(Verdict.NOT_APPLICABLE, d("CEASE_HOSTILITY", COW, NEUTRAL, false, MobOrders.NONE, false).verdict());
        assertEquals(Verdict.REFUSED, d("CEASE_HOSTILITY", CREEPER, NEUTRAL, false, MobOrders.NONE, false).verdict());
        Relationship ok = new Relationship(0.5f, 0.5f, 0);
        Decision t = d("CEASE_HOSTILITY", CREEPER, ok, false, MobOrders.NONE, false);
        assertEquals(Verdict.ACCEPTED, t.verdict());
        MobOrders o = OrderPolicy.apply(t, MobOrders.NONE, ME, "", 0, 0, 0, 1000);
        assertEquals(1000 + OrderPolicy.TRUCE_TICKS, o.truceUntil());
        assertEquals(Verdict.ALREADY, OrderPolicy.decide("CEASE_HOSTILITY", CREEPER, ok, false, o, ME, false, 1500).verdict());
        assertEquals(Verdict.REFUSED, OrderPolicy.decide("CEASE_HOSTILITY", CREEPER, ok, false, o, "player-b", false,
                1000 + OrderPolicy.TRUCE_TICKS + 10).verdict()); // cooldown depois da tregua
    }

    @Test
    void unknownActionsAreDroppedByFilter() {
        assertEquals(List.of("FOLLOW_PLAYER", "STAY_AT"), OrderPolicy.filter(List.of("minecraft:give", "FOLLOW_PLAYER", "STAY_AT", "PORTAL_HOLD")));
    }

    @Test
    void ordersSurviveSaveAndReload() {
        MobOrders o = MobOrders.NONE.stay(ME, "minecraft:overworld", 1.5, 64, -3.25, 99).withPortalHold(true).withTruce(ME, 500, 900);
        var json = MobOrders.CODEC.encodeStart(JsonOps.INSTANCE, o).getOrThrow();
        assertEquals(o, MobOrders.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }
}
