package br.com.mobvoz.memory;

import br.com.mobvoz.ai.SidecarClient.Proposal;
import br.com.mobvoz.memory.MobMemory.Claim;
import br.com.mobvoz.memory.MobMemory.Fact;
import br.com.mobvoz.memory.MobMemory.Stance;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Regras puras (testaveis): como fatos verificados mudam a relacao, como o LLM e autorizado a mexer SO em
 * alegacoes/crencas (nunca em fatos) e como a memoria vira texto para o prompt.
 */
public final class MemoryRules {
    public static final List<String> CAPABILITIES = List.of("REGISTER_CLAIM", "UPDATE_BELIEF");
    private static final int DAMAGE_DEDUPE_TICKS = 20;

    private MemoryRules() {
    }

    // ---- fatos verificados (chamados SOMENTE por codigo do servidor) ----

    public static MobMemory firstMeeting(MobMemory m, UUID player, long tick) {
        if (m.knows(player)) {
            return m;
        }
        return m.withFact(new Fact("FIRST_MET", player, "", tick)).withRelation(player, r -> r);
    }

    public static MobMemory gift(MobMemory m, UUID player, String itemId, int count, long tick) {
        return gift(m, player, itemId, count, tick, 1.0, "");
    }

    /**
     * Presente com reacao do perfil: {@code likeFactor} 1 = gosta, ~0.15 = indiferente, < 0 = nao gosta. Retornos
     * decrescentes: o presente N rende menos (piso 10%), entao spammar 64 itens nao zera o jogo.
     */
    public static MobMemory gift(MobMemory m, UUID player, String itemId, int count, long tick, double likeFactor, String reaction) {
        double diminishing = Math.max(0.05, 1.0 - m.counts().getOrDefault("GIFT", 0) / 12.0); // os primeiros presentes pesam; spammar 64 itens nao compra a criatura
        var before = m.relationOf(player);
        boolean apology = likeFactor > 0.5 && before.trust() * 0.5 + before.affinity() * 0.5 - before.fear() * 0.5 < 0; // presente a quem esta chateado perdoa mais rapido
        float k = (float) (likeFactor * diminishing * (apology ? 2 : 1));
        String detail = itemId + " x" + count + (reaction.isEmpty() ? "" : " (" + reaction + ")");
        return firstMeeting(m, player, tick)
                .withFact(new Fact("GIFT", player, detail, tick))
                .withRelation(player, r -> r.plus(0.04f * k, 0.08f * k, -0.04f * k));
    }

    /** Troca concluida pelo servidor (fato verificado). */
    public static MobMemory trade(MobMemory m, UUID player, String recipeId, long tick) {
        return firstMeeting(m, player, tick)
                .withFact(new Fact("TRADE", player, recipeId, tick))
                .withRelation(player, r -> r.plus(0.08f, 0.05f, -0.03f));
    }

    /** O jogador feriu quem tinha tregua com ele: traicao registrada (fato verificado) e confianca despenca. */
    public static MobMemory pactBroken(MobMemory m, UUID player, long tick) {
        return firstMeeting(m, player, tick)
                .withFact(new Fact("PACT_BROKEN", player, "", tick))
                .withRelation(player, r -> r.plus(-0.5f, -0.3f, 0.3f));
    }

    /** A criatura aceitou ser domada (fato verificado): confianca e afinidade sobem. */
    public static MobMemory tamed(MobMemory m, UUID player, long tick) {
        return firstMeeting(m, player, tick)
                .withFact(new Fact("TAMED", player, "", tick))
                .withRelation(player, r -> r.plus(0.10f, 0.15f, -0.10f));
    }

    /** O jogador devolveu, sem ser pedido, algo que a criatura tinha entregado: honestidade (fato verificado). */
    public static MobMemory honestReturn(MobMemory m, UUID player, String detail, long tick) {
        return firstMeeting(m, player, tick)
                .withFact(new Fact("HONEST_RETURN", player, detail, tick))
                .withRelation(player, r -> r.plus(0.15f, 0.08f, -0.05f));
    }

    /** O jogador AMEACOU por palavras (tom reconhecido pelo analisador): so medo/confianca; nunca vira agressao fisica verificada. */
    public static MobMemory threatened(MobMemory m, UUID player, long tick) {
        return firstMeeting(m, player, tick)
                .withFact(new Fact("THREAT_SPOKEN", player, "", tick))
                .withRelation(player, r -> r.plus(-0.10f, -0.05f, 0.25f));
    }

    public static MobMemory comforted(MobMemory m, UUID player, long tick) {
        return firstMeeting(m, player, tick)
                .withFact(new Fact("COMFORTED", player, "", tick))
                .withRelation(player, r -> r.plus(0.05f, 0.03f, -0.20f));
    }

    public static MobMemory damagedByPlayer(MobMemory m, UUID player, long tick) {
        for (Fact f : m.facts()) { // golpes em rajada contam como um so evento
            if (f.type().equals("DAMAGED_BY_PLAYER") && f.player().equals(player) && tick - f.tick() < DAMAGE_DEDUPE_TICKS) {
                return m;
            }
        }
        return firstMeeting(m, player, tick)
                .withFact(new Fact("DAMAGED_BY_PLAYER", player, "", tick))
                .withRelation(player, r -> r.plus(-0.12f, -0.08f, 0.15f)); // um clique sem querer nao pode arruinar tudo
    }

    /** Teto da afinidade que so conversa amistosa constroi: papo ajuda a quebrar o gelo, mas confianca de verdade vem de presentes e atos. */
    public static final float CHAT_AFFINITY_CAP = 0.12f;

    /** Uma fala do jogador que nao foi ameaca: soma um pouquinho de afinidade (ate {@link #CHAT_AFFINITY_CAP}). */
    public static MobMemory chatted(MobMemory m, UUID player, long tick) {
        return firstMeeting(m, player, tick).withRelation(player, r -> r.affinity() < CHAT_AFFINITY_CAP ? r.plus(0f, 0.01f, 0f) : r);
    }

    // ---- propostas do LLM: so alegacoes/crencas, com saneamento ----

    /** Aplica as propostas validas; o resto e ignorado. Jamais cria Fact nem mexe em Relationship. */
    public static MobMemory applyProposals(MobMemory m, UUID speaker, List<Proposal> proposals, long tick) {
        for (Proposal p : proposals) {
            switch (p.type()) {
                case "REGISTER_CLAIM" -> {
                    String text = sanitize(p.params().get("text"));
                    if (text == null) {
                        continue;
                    }
                    Stance s = p.params().get("stance") instanceof String str ? Stance.parse(str) : Stance.UNKNOWN;
                    m = m.withClaim(new Claim(UUID.nameUUIDFromBytes((speaker + text).getBytes()), speaker, text, tick, s));
                }
                case "UPDATE_BELIEF" -> {
                    if (p.params().get("claim_index") instanceof Number n && p.params().get("stance") instanceof String str) {
                        int i = n.intValue();
                        // So a crenca sobre alegacoes DESTE jogador, e so a que existe.
                        if (i >= 0 && i < m.claims().size() && m.claims().get(i).player().equals(speaker)) {
                            m = m.withStance(i, Stance.parse(str));
                        }
                    }
                }
                default -> { } // qualquer outra coisa: nao executa
            }
        }
        return m;
    }

    private static final java.util.regex.Pattern KINSHIP = java.util.regex.Pattern.compile(
            "\\b(pai|m[ãa]e|filh[oa]|irm[ãa]o?|criador|dono)\\b", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);

    /** A alegacao fala de parentesco/dono? (a creatura que acredita nela confia mais em quem pede) */
    public static boolean isKinship(String claimText) {
        return KINSHIP.matcher(claimText).find();
    }

    static String sanitize(Object o) {
        if (!(o instanceof String s)) {
            return null;
        }
        s = s.replaceAll("[\\p{Cntrl}]", " ").trim().replaceAll("\\s+", " ");
        if (s.length() < 3) {
            return null;
        }
        return s.length() > MobMemory.MAX_CLAIM_TEXT ? s.substring(0, MobMemory.MAX_CLAIM_TEXT) : s;
    }

    // ---- memoria -> contexto do prompt (pt-BR) ----

    public static List<String> factLines(MobMemory m, UUID speaker, long nowTick) {
        List<String> out = new ArrayList<>();
        for (Fact f : m.facts()) {
            String who = f.player().equals(speaker) ? "o jogador que fala com você" : "outro jogador";
            String when = ago(nowTick - f.tick());
            switch (f.type()) {
                case "FIRST_MET" -> out.add("Você conheceu " + who + " " + when + ".");
                case "GIFT" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1) + " te deu de presente "
                        + f.detail() + " " + when + ".");
                case "CONVERSION" -> out.add("Você se transformou (" + f.detail() + ") " + when + ", mas continua sendo você mesmo.");
                case "TRADE" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1)
                        + " fez uma troca justa com você (" + f.detail() + ") " + when + ".");
                case "TAMED" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1)
                        + " te convenceu a aceitar ser dele " + when + ".");
                case "HONEST_RETURN" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1)
                        + " devolveu, sem você pedir, o que você tinha entregado (" + f.detail() + ") " + when + ": foi honesto, o que é raro.");
                case "PACT_BROKEN" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1)
                        + " quebrou o pacto de paz ferindo você " + when + ".");
                case "THREAT_SPOKEN" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1)
                        + " te ameaçou com palavras " + when + " (interpretação da fala; você não foi ferido por isso).");
                case "COMFORTED" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1) + " te acalmou com palavras gentis " + when + ".");
                case "DAMAGED_BY_PLAYER" -> out.add(who.substring(0, 1).toUpperCase(Locale.ROOT) + who.substring(1)
                        + " te machucou " + when + ".");
                default -> { }
            }
        }
        for (Map.Entry<String, Integer> e : m.counts().entrySet()) { // totais de eventos que sairam da lista detalhada
            long detailed = m.facts().stream().filter(f -> f.type().equals(e.getKey())).count();
            if (e.getValue() > detailed && !e.getKey().equals("FIRST_MET")) {
                out.add("No total, o evento " + e.getKey() + " aconteceu " + e.getValue() + " vezes.");
            }
        }
        return out;
    }

    /** Alegacoes ja com indice (para UPDATE_BELIEF) e a crenca atual. */
    public static List<String> claimLines(MobMemory m, UUID speaker) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < m.claims().size(); i++) {
            Claim c = m.claims().get(i);
            String who = c.player().equals(speaker) ? "o jogador que fala com você" : "outro jogador";
            out.add("[" + i + "] " + who + " afirmou: \"" + c.text() + "\" (sua crença: " + c.stance().name() + ")");
        }
        return out;
    }

    static String ago(long ticks) {
        long s = ticks / 20;
        if (s < 60) {
            return "há poucos segundos";
        }
        if (s < 3600) {
            return "há cerca de " + s / 60 + " minuto(s)";
        }
        return "há cerca de " + s / 3600 + " hora(s)";
    }
}
