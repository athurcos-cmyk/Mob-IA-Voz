package br.com.mobvoz.social;

import br.com.mobvoz.memory.MobMemory.Relationship;
import br.com.mobvoz.orders.MobOrders;

/** Estado de vinculo derivado (docs/12): calculado do que o servidor sabe, nunca dito pelo LLM. */
public final class Bonds {
    public enum Bond { UNKNOWN, ACQUAINTANCE, FRIEND, ALLY, FOLLOWER, GUARD, RIVAL, HOSTILE, BETRAYED }

    private Bonds() {
    }

    public static Bond of(boolean met, Relationship r, MobOrders o, String player, boolean pactBroken) {
        if (pactBroken) {
            return Bond.BETRAYED;
        }
        boolean mine = o.owner().equals(player);
        if (mine && o.defend()) {
            return Bond.GUARD;
        }
        if (mine && o.mode() == MobOrders.Mode.FOLLOW) {
            return Bond.FOLLOWER;
        }
        if (mine && o.mode() == MobOrders.Mode.STAY) {
            return Bond.ALLY;
        }
        if (!met) {
            return Bond.UNKNOWN;
        }
        double w = r.trust() * 0.5 + r.affinity() * 0.5 - r.fear() * 0.5;
        if (w <= -0.3) {
            return r.fear() > 0.4 ? Bond.HOSTILE : Bond.RIVAL;
        }
        return w >= 0.3 ? Bond.FRIEND : Bond.ACQUAINTANCE;
    }
}
