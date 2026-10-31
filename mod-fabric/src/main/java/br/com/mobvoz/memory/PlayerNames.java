package br.com.mobvoz.memory;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * O jogador disse o proprio nome? (PURO, sem IA.) Duas formas: frase explicita ("meu nome e Arthur", "me chamo Arthur", "pode me chamar de
 * Arthur", "eu sou o Arthur" com maiuscula) ou resposta curta a uma pergunta de nome da criatura ("Qual e o seu nome?" -> "Arthur").
 * So letras, ate 20 caracteres e 2 palavras; palavras comuns ("humano", "jogador", profissoes...) nunca viram nome.
 */
public final class PlayerNames {
    private static final String NAME = "([\\p{L}][\\p{L}'\\-]{1,19}(?: [\\p{L}][\\p{L}'\\-]{1,19})?)";
    private static final Pattern EXPLICIT = Pattern.compile(
            "(?iu)\\b(?:meu nome (?:é|e|eh|seria)|me chamo|meu nome tá|pode me chamar de|podem me chamar de|me chama de|me chamam de|chamo-me)\\s+(?:o |a )?" + NAME);
    private static final Pattern I_AM = Pattern.compile("\\b(?:[Ee]u sou|[Ss]ou) (?:o |a )?(\\p{Lu}[\\p{L}'\\-]{1,19})\\b");
    private static final Pattern ASKED = Pattern.compile("(?iu)\\bnome\\b.*\\?|\\bquem (?:é|e) (?:você|voce)\\b.*\\?|\\bcomo (?:você|voce) se chama\\b|\\bcomo (?:te|se) chamam?\\b");
    private static final Pattern BARE = Pattern.compile("^(?:(?:é|e|eh|sou|me chamo|pode chamar de)\\s+)?(?:o |a )?" + NAME + "[.!]*$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Palavras que nao sao nome (comparadas em minusculas, sem acento). */
    private static final Set<String> NOT_NAMES = Set.of("humano", "humana", "jogador", "jogadora", "mortal", "amigo", "amiga", "dono", "mestre",
            "aldeao", "aldea", "fazendeiro", "pescador", "bibliotecario", "ferreiro", "clerigo", "cacador", "pastor", "armeiro", "sim", "nao",
            "oi", "ola", "tudo", "bem", "voce", "eu", "ele", "ela", "criador", "rei", "heroi", "guerreiro", "minerador", "construtor",
            "bom", "boa", "dia", "tarde", "noite", "obrigado", "obrigada", "por", "favor", "nada", "ninguem", "alguem", "teste", "steve", "alex");

    private PlayerNames() {
    }

    /** @param lastMobLine ultima fala da criatura a este jogador ("" se nao houve). */
    public static Optional<String> extract(String playerText, String lastMobLine) {
        if (playerText == null) {
            return Optional.empty();
        }
        String text = playerText.strip();
        Matcher m = EXPLICIT.matcher(text);
        if (m.find()) {
            return clean(m.group(1));
        }
        m = I_AM.matcher(text);
        if (m.find()) {
            return clean(m.group(1));
        }
        if (lastMobLine != null && ASKED.matcher(lastMobLine).find() && text.split("\\s+").length <= 3) {
            m = BARE.matcher(text);
            if (m.matches()) {
                return clean(m.group(1));
            }
        }
        return Optional.empty();
    }

    static Optional<String> clean(String raw) {
        StringBuilder out = new StringBuilder();
        for (String w : raw.strip().split("\\s+")) {
            if (isCommon(w)) {
                return Optional.empty();
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(w.substring(0, 1).toUpperCase(Locale.ROOT)).append(w.substring(1).toLowerCase(Locale.ROOT));
        }
        return out.length() < 2 || out.length() > 20 ? Optional.empty() : Optional.of(out.toString());
    }

    private static boolean isCommon(String w) {
        String n = java.text.Normalizer.normalize(w.toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return NOT_NAMES.contains(n);
    }
}
