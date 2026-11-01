package br.com.mobvoz.memory;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * O jogador deu um apelido a criatura? ("vou te chamar de Mimosa", "seu nome agora e Rex", "te batizo de Bolinha".) PURO, sem IA.
 * So afirmacoes: pergunta, negacao ("nao vou te chamar de...") e palavras comuns ("amigo", "humano"...) nunca viram apelido.
 * Letras, ate 20 caracteres e 2 palavras (a segunda com maiuscula); alguns palavroes ficam de fora.
 */
public final class MobNicknames {
    private static final String WORD = "[\\p{L}][\\p{L}'\\-]{1,19}";
    private static final Pattern NICK = Pattern.compile("(?iu)\\b(?:te (?:cham(?:ar|o)|batiz(?:ar|o)) (?:de|como)|(?:cham(?:ar|o)|batiz(?:ar|o)) voc[eê] (?:de|como)"
            + "|te d(?:ar|ou) o nome de|(?:seu|teu) nome (?:agora )?(?:é|e|vai ser|será|sera)|voc[eê] (?:agora )?(?:se chama|vai se chamar))\\s+(?:o |a )?"
            + "(" + WORD + "(?: (?-i:\\p{Lu})[\\p{L}'\\-]{1,19})?)");
    private static final Pattern NEGATED = Pattern.compile("(?iu)\\b(?:não|nao|nunca|jamais|se|posso|podemos|que tal)\\b"); // negacao ou hipotese/pedido antes do apelido

    /** Palavroes (sem acento, minusculas) que nao viram apelido: o nome aparece sobre a cabeca da criatura para todos. */
    private static final Set<String> BLOCKED = Set.of("puta", "puto", "caralho", "porra", "merda", "fdp", "viado", "buceta", "bosta", "cuzao", "arrombado",
            "otario", "retardado", "negro", "preto", "macaco", "nazista", "hitler", "estuprador", "vagabunda", "vagabundo");

    private MobNicknames() {
    }

    public static Optional<String> extract(String playerText) {
        if (playerText == null) {
            return Optional.empty();
        }
        String text = playerText.strip();
        Matcher m = NICK.matcher(text);
        if (!m.find() || text.substring(m.end()).stripLeading().startsWith("?") || NEGATED.matcher(text.substring(0, m.start())).find()) {
            return Optional.empty();
        }
        return PlayerNames.clean(m.group(1)).filter(n -> !blocked(n));
    }

    static boolean blocked(String name) {
        for (String w : name.split(" ")) {
            String n = Normalizer.normalize(w.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            if (BLOCKED.contains(n)) {
                return true;
            }
        }
        return false;
    }
}
