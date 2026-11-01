package br.com.mobvoz.orders;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * O BOM MOTIVO de quem NAO pode acompanhar, ficar de guarda ou lutar: quem vive na agua, quem voa (o Ghast inclusive) e os aldeoes nao seguem o
 * jogador (piglins, hoglins e os demais do Nether SEGUEM e fazem alianca), e dizem por que, do jeito do seu mundo. O motivo vai para a IA no resultado da ordem ("motivo"); ela o diz na voz da criatura, sem inventar
 * outra desculpa. Varios motivos por grupo, sorteados, para nao soar sempre igual.
 */
public final class Refusals {
    private static final Set<String> AQUATIC = Set.of("cod", "salmon", "tropical_fish", "pufferfish", "squid", "glow_squid", "dolphin", "axolotl", "turtle",
            "guardian", "elder_guardian", "drowned");
    private static final Set<String> FLYING = Set.of("bee", "bat", "allay", "parrot", "phantom", "vex", "ghast");
    private static final Set<String> VILLAGE = Set.of("villager", "wandering_trader");

    private static final List<String> FOLLOW_AQUATIC = List.of(
            "vive na água e fora dela não respira nem se move direito, então não pode acompanhar quem anda em terra",
            "o mar é o lar e o único lugar onde ela aguenta viver; longe da água ela não sobreviveria",
            "só consegue seguir caminhos de água, e o jogador anda em terra firme: não existe caminho em comum",
            "nada bem, mas em seco fica indefesa; por isso precisa ficar onde há água");
    private static final List<String> FOLLOW_FLYING = List.of(
            "voa livre, e o jogador anda no chão: não conseguiria acompanhar o ritmo nem os caminhos dele sem se perder",
            "é uma criatura do ar e voa por onde o vento leva; seguir um humano a pé não combina com ela",
            "tem o próprio ritmo de voo e o próprio caminho; prefere ficar por perto a se prender a quem anda no chão",
            "se afastaria do que conhece (ninho, flores, cavernas) e se perderia; fica melhor onde está");
    private static final List<String> FOLLOW_VILLAGE = List.of(
            "tem trabalho, casa e rotina na vila e não larga isso para andar por aí; prefere conversar ou negociar aqui mesmo",
            "a vila depende dele: sem ele, o ofício e a rotina param; por isso fica",
            "não sai da vila, que é onde está a cama, o trabalho e quem ele protege");
    private static final List<String> FOLLOW_OTHER = List.of(
            "só se move do jeito da própria espécie (pulando, escalando, andando devagar) e não consegue acompanhar o passo de um humano",
            "o lugar dela é o próprio território (charco, montanha, duna, escuro) e longe dele ela não se sente segura",
            "se move de um jeito que não combina com acompanhar alguém a pé; prefere ficar onde conhece");
    private static final List<String> FOLLOW_WARDEN = List.of(
            "vive no escuro profundo e só se guia por vibrações; a luz e o barulho de quem anda por aí o incomodam");

    private static final List<String> STAY_GUARD = List.of(
            "não monta guarda: o jeito dela é seguir o próprio ritmo e voltar ao lugar onde vive",
            "não sabe esperar parada num ponto; sempre volta para o lugar onde se sente em casa");

    private static final List<String> FIGHT_PEACEFUL = List.of(
            "é pacífica e não sabe atacar: diante do perigo só foge ou se esconde",
            "não tem garras, presas nem força para lutar; o que sabe é fugir e se esconder",
            "luta não é com ela: protege a si mesma sumindo de vista");

    private Refusals() {
    }

    /** Sem o prefixo "minecraft:" nem a profissao ("villager#farmer" -> "villager"). */
    static String simple(String entityType) {
        String t = entityType == null ? "" : entityType;
        if (t.contains("#")) {
            t = t.substring(0, t.indexOf('#'));
        }
        return t.substring(t.indexOf(':') + 1);
    }

    /** Por que nao consegue acompanhar ({@code FOLLOW_PLAYER}). */
    public static String cannotFollow(String entityType) {
        return cannotFollow(entityType, ThreadLocalRandom.current());
    }

    static String cannotFollow(String entityType, Random rnd) {
        String t = simple(entityType);
        List<String> pool = AQUATIC.contains(t) ? FOLLOW_AQUATIC : FLYING.contains(t) ? FOLLOW_FLYING : VILLAGE.contains(t) ? FOLLOW_VILLAGE
                : t.equals("warden") ? FOLLOW_WARDEN : FOLLOW_OTHER;
        return pool.get(rnd.nextInt(pool.size()));
    }

    /** Por que nao consegue ficar de guarda num ponto ({@code STAY_AT}). */
    public static String cannotStay(String entityType) {
        String t = simple(entityType);
        if (VILLAGE.contains(t) || AQUATIC.contains(t) || FLYING.contains(t)) {
            return cannotFollow(entityType); // o mesmo motivo de mundo: ela ja vive onde vive
        }
        return STAY_GUARD.get(ThreadLocalRandom.current().nextInt(STAY_GUARD.size()));
    }

    /** Por que nao sabe lutar ({@code DEFEND_PLAYER}): bicho pacifico, sem ataque. */
    public static String cannotFight(String entityType) {
        return FIGHT_PEACEFUL.get(ThreadLocalRandom.current().nextInt(FIGHT_PEACEFUL.size()));
    }

    /** Para testes: todos os grupos e seus motivos. */
    static Map<String, List<String>> groups() {
        return Map.of("aquatic", FOLLOW_AQUATIC, "flying", FOLLOW_FLYING, "village", FOLLOW_VILLAGE, "other", FOLLOW_OTHER,
                "warden", FOLLOW_WARDEN, "stay", STAY_GUARD, "fight", FIGHT_PEACEFUL);
    }
}
