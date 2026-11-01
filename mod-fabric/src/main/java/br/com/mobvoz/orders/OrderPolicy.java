package br.com.mobvoz.orders;

import br.com.mobvoz.memory.MobMemory.Relationship;
import java.util.List;
import java.util.Set;

/**
 * Decisao PURA do servidor sobre um pedido proposto (nunca o LLM decide): suportado? quer? ja esta ativo?
 * A recusa e parte da fantasia: depende de confianca/afinidade/medo, temperamento da especie e adaptador.
 */
public final class OrderPolicy {
    public static final Set<String> ACTIONS = Set.of("FOLLOW_PLAYER", "STAY_AT", "PORTAL_HOLD", "PORTAL_RELEASE", "DISMISS_ALLY",
            "CEASE_HOSTILITY", "DEFEND_PLAYER", "TAME_ME");
    public static final long TRUCE_TICKS = 2400;       // 2 min
    public static final long TRUCE_COOLDOWN_TICKS = 1200;
    public static final long OWNER_ABSENT_TIMEOUT = 6000; // 5 min sem o dono: ordem expira

    public enum Verdict { ACCEPTED, REFUSED, UNSUPPORTED, ALREADY, NOT_APPLICABLE }

    public record Decision(String action, Verdict verdict, String reason) {
        public boolean accepted() {
            return verdict == Verdict.ACCEPTED;
        }
    }

    /** O que a criatura e capaz de fazer agora (adaptadores implementados) e quem ela e. */
    public record Subject(String entityType, boolean canWalkFollow, boolean hostile, boolean canFight, boolean boss, boolean tameable) {
        public Subject(String entityType, boolean canWalkFollow, boolean hostile, boolean canFight, boolean boss) {
            this(entityType, canWalkFollow, hostile, canFight, boss, false);
        }

        public Subject(String entityType, boolean canWalkFollow, boolean hostile) {
            this(entityType, canWalkFollow, hostile, hostile, false, false);
        }

        public Subject(String entityType, boolean canWalkFollow, boolean hostile, boolean canFight) {
            this(entityType, canWalkFollow, hostile, canFight, false, false);
        }
    }

    /**
     * Quanto um bom ARGUMENTO ("vou cuidar de voce, te dar abrigo") soma ao "calor" da criatura num pedido. {@code strength} 0..2 vem do
     * analisador (so opina); o efeito e limitado aqui: ate +0,25, escalado pela credulidade, e zero se a criatura tem medo de quem pede.
     */
    public static double appealBonus(int strength, int credulity, double fear) {
        if (strength <= 0 || fear > 0.5) {
            return 0;
        }
        return Math.min(0.25, 0.10 * Math.min(2, strength) * (0.6 + 0.2 * Math.max(0, Math.min(2, credulity))));
    }

    /** Governanca do mundo (config): contratos com chefes e teto de seguidores por jogador. */
    public record Rules(boolean bossContracts, int maxFollowers, int followersNow) {
        public static final Rules NONE = new Rules(false, Integer.MAX_VALUE, 0);
    }

    private OrderPolicy() {
    }

    /** Pedidos "de ordem" (seguir/ficar/...) em ordem de aplicacao; o resto e descartado. */
    public static List<String> filter(List<String> proposed) {
        return proposed.stream().filter(ACTIONS::contains).distinct().limit(2).toList();
    }

    /** Quanto a criatura gosta do jogador (0 = neutro). Parentesco acreditado soma. */
    public static double warmth(Relationship r, boolean kinshipBelieved) {
        return r.trust() * 0.5 + r.affinity() * 0.5 - r.fear() * 0.5 + (kinshipBelieved ? 0.25 : 0);
    }

    /** Chefe so firma pacto com quem ganhou muita confianca. */
    public static final double BOSS_MIN_TRUST = 0.75;

    public static double followThreshold(Subject s) {
        double t = br.com.mobvoz.profile.Profiles.of(s.entityType()).follow();
        if (s.boss()) {
            return Math.max(t, BOSS_MIN_TRUST);
        }
        return s.hostile() ? Math.max(t, 0.6) : t; // porco teimoso precisa de incentivo; hostis, de muita confianca
    }

    public static Decision decide(String action, Subject s, Relationship rel, boolean kinshipBelieved, MobOrders cur,
                                  String player, boolean otherOwnerOnline, long tick) {
        return decide(action, s, rel, kinshipBelieved, cur, player, otherOwnerOnline, tick, Rules.NONE);
    }

    public static Decision decide(String action, Subject s, Relationship rel, boolean kinshipBelieved, MobOrders cur,
                                  String player, boolean otherOwnerOnline, long tick, Rules rules) {
        if (s.boss() && !rules.bossContracts() && Set.of("FOLLOW_PLAYER", "STAY_AT", "DEFEND_PLAYER", "CEASE_HOSTILITY").contains(action)) {
            // Chefes: contratos desligados por padrao no mundo. Ligados, valem as regras normais com confianca minima mais alta
            // (BOSS_MIN_TRUST) e so o que o adaptador do chefe consegue (Dragao: sem seguir/ficar, so lutar e tregua).
            return new Decision(action, Verdict.REFUSED, "contratos com chefes estao desativados neste mundo");
        }
        if ((action.equals("FOLLOW_PLAYER") || action.equals("DEFEND_PLAYER")) && rules.followersNow() >= rules.maxFollowers()
                && !(cur.owner().equals(player) && (cur.mode() == MobOrders.Mode.FOLLOW || cur.defend()))) {
            return new Decision(action, Verdict.REFUSED, "quem pede ja tem aliados demais com ele");
        }
        double warmth = warmth(rel, kinshipBelieved);
        boolean mine = cur.mode() != MobOrders.Mode.NONE && cur.owner().equals(player);
        switch (action) {
            case "FOLLOW_PLAYER" -> {
                if (!s.canWalkFollow()) {
                    return new Decision(action, Verdict.UNSUPPORTED, s.boss() ? "e um ser preso ao proprio territorio (o Fim, o monumento, as ruinas): nao sai dele para acompanhar ninguem"
                            : "nao consegue acompanhar (movimento ainda sem adaptador)");
                }
                if (cur.mode() == MobOrders.Mode.FOLLOW && mine) {
                    return new Decision(action, Verdict.ALREADY, "ja esta seguindo");
                }
                if (cur.mode() != MobOrders.Mode.NONE && !mine && otherOwnerOnline) {
                    return new Decision(action, Verdict.REFUSED, "ja obedece a outro jogador");
                }
                if (warmth < followThreshold(s)) {
                    return new Decision(action, Verdict.REFUSED, "ainda nao confia o bastante em quem pede");
                }
                return new Decision(action, Verdict.ACCEPTED, "");
            }
            case "STAY_AT" -> {
                if (!s.canWalkFollow()) {
                    return new Decision(action, Verdict.UNSUPPORTED, s.boss() ? "e um ser preso ao proprio territorio (o Fim, o monumento, as ruinas): nao sai dele para ficar de guarda em outro lugar"
                            : "nao consegue ficar de guarda num ponto (sem adaptador)");
                }
                if (cur.mode() == MobOrders.Mode.STAY && mine) {
                    return new Decision(action, Verdict.ALREADY, "ja esta esperando ali");
                }
                if (cur.mode() != MobOrders.Mode.NONE && !mine && otherOwnerOnline) {
                    return new Decision(action, Verdict.REFUSED, "ja obedece a outro jogador");
                }
                if (!mine && warmth < followThreshold(s) - 0.15) {
                    return new Decision(action, Verdict.REFUSED, "nao confia o bastante para obedecer");
                }
                return new Decision(action, Verdict.ACCEPTED, "");
            }
            case "PORTAL_HOLD" -> {
                if (!mine) {
                    return new Decision(action, Verdict.NOT_APPLICABLE, "nao esta acompanhando nem esperando este jogador");
                }
                return cur.portalHold() ? new Decision(action, Verdict.ALREADY, "ja evita portais")
                        : new Decision(action, Verdict.ACCEPTED, "");
            }
            case "PORTAL_RELEASE" -> {
                if (!mine || !cur.portalHold()) {
                    return new Decision(action, Verdict.NOT_APPLICABLE, "nao havia ordem de evitar portais");
                }
                return new Decision(action, Verdict.ACCEPTED, "");
            }
            case "DEFEND_PLAYER" -> {
                if (!s.canFight()) {
                    return new Decision(action, Verdict.UNSUPPORTED, "nao sabe lutar (sem ataque/adaptador de combate)");
                }
                if (cur.defend() && (cur.owner().isEmpty() || cur.owner().equals(player))) {
                    return new Decision(action, Verdict.ALREADY, "ja defende");
                }
                if (!cur.owner().isEmpty() && !mine && otherOwnerOnline) {
                    return new Decision(action, Verdict.REFUSED, "ja obedece a outro jogador");
                }
                if (warmth < followThreshold(s) + 0.1) {
                    return new Decision(action, Verdict.REFUSED, "nao confia o bastante para lutar por quem pede");
                }
                return new Decision(action, Verdict.ACCEPTED, "");
            }
            case "TAME_ME" -> {
                if (!s.tameable()) {
                    return new Decision(action, Verdict.UNSUPPORTED, "nao e uma criatura que se deixe domar");
                }
                if (warmth < followThreshold(s) + 0.10) {
                    return new Decision(action, Verdict.REFUSED, "ainda nao confia o bastante para aceitar ser de quem pede");
                }
                return new Decision(action, Verdict.ACCEPTED, "decidiu e aceitou ser do jogador AGORA: ele passa a ser seu dono e pode montar e cuidar de voce; confirme isso na sua voz, sem duvida");
            }
            case "DISMISS_ALLY" -> {
                if (!mine && !(cur.defend() && cur.owner().equals(player))) {
                    return new Decision(action, Verdict.NOT_APPLICABLE, "nao estava sob ordens deste jogador");
                }
                return new Decision(action, Verdict.ACCEPTED, "");
            }
            case "CEASE_HOSTILITY" -> {
                if (!s.hostile()) {
                    return new Decision(action, Verdict.NOT_APPLICABLE, "nao esta hostil");
                }
                if (tick < cur.truceUntil() && cur.truceFor().equals(player)) {
                    return new Decision(action, Verdict.ALREADY, "ja ha tregua");
                }
                if (tick < cur.truceCooldownUntil()) {
                    return new Decision(action, Verdict.REFUSED, "tregua recente; precisa esperar");
                }
                if (warmth < (s.boss() ? 0.5 : 0.3)) {
                    return new Decision(action, Verdict.REFUSED, "nao confia o bastante para baixar a guarda");
                }
                return new Decision(action, Verdict.ACCEPTED, "");
            }
            default -> {
                return new Decision(action, Verdict.UNSUPPORTED, "pedido desconhecido");
            }
        }
    }

    /** Aplica uma decisao ACEITA (pura): devolve as novas ordens. {@code dim/x/y/z}: onde o mob esta agora. */
    public static MobOrders apply(Decision d, MobOrders cur, String player, String dim, double x, double y, double z, long tick) {
        if (!d.accepted()) {
            return cur;
        }
        return switch (d.action()) {
            case "FOLLOW_PLAYER" -> cur.follow(player, tick).withDefend(player, true); // seguir = acompanhar E proteger
            case "STAY_AT" -> cur.stay(player, dim, x, y, z, tick);
            case "DEFEND_PLAYER" -> (cur.mode() == MobOrders.Mode.NONE ? cur.follow(player, tick) : cur).withDefend(player, true);
            case "PORTAL_HOLD" -> cur.withPortalHold(true);
            case "PORTAL_RELEASE" -> cur.withPortalHold(false);
            case "DISMISS_ALLY" -> cur.dismissed();
            case "CEASE_HOSTILITY" -> cur.withTruce(player, tick + TRUCE_TICKS, tick + TRUCE_TICKS + TRUCE_COOLDOWN_TICKS);
            default -> cur;
        };
    }
}
