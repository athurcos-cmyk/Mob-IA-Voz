package br.com.mobvoz.config;

import br.com.mobvoz.MobVozMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Configuracao do MUNDO/SERVIDOR (config/mobvoz/server.json). Padroes conservadores: contratos com chefes
 * ficam DESLIGADOS ate o admin ligar (docs/02 e config/balance.sample.json). Sem a chave, vale o padrao.
 */
public final class ServerConfig {
    /** "low" | "balanced" | "high": mexe so em carga (cenas ambiente, limite de seguidores). */
    public String preset = "balanced";
    public boolean requireVoiceConsent = true;
    public boolean chatterEnabled = true;
    public int maxActiveFollowersPerPlayer = 15;
    /** Chefes (Warden, Wither, Dragao) podem firmar pacto com o jogador: seguir, guardar, defender, tregua. Ligado por padrao; false = todos os chefes recusam. */
    public boolean bossContractsEnabled = true;
    /** Preco da troca varia (confianca, humor do dia, barganha) dentro de uma faixa segura; false = sempre o preco-base da receita. */
    public boolean dynamicPrices = true;
    /** Falas espontaneas (aviso de perigo dos aliados, cumprimento, reacao a honestidade/ovo). false = a criatura so fala quando falam com ela. */
    public boolean naturalEvents = true;
    /** false = chefe sob contrato (aliado ou em tregua) NAO destroi blocos (Wither, caveiras, Dragao); true = destroi como sempre. */
    public boolean bossDestructionOnAlliance = false;
    public boolean creativeSandbox = false;
    /** Mundo vivo: criaturas calmas conversam entre si perto do jogador (falas prontas, sem IA; precisa de chatterEnabled). */
    public boolean ambientChatter = true;
    /** Motor de voz: "auto" (o que o servico de IA usa: Kokoro, mais natural; ou Piper se faltar) | "kokoro" | "piper" (modo leve: menos tempo por fala). */
    public String voiceEngine = "auto";
    /** Intervalo minimo entre conversas ambiente na mesma regiao (cada criatura espera o triplo). */
    public int ambientCooldownSeconds = 60; // em desuso: vale ambientLevel
    /** Quantas conversas de ambiente: "nenhuma" | "poucas" (padrao) | "normal" | "muitas". Mude com /mobvoz conversas. */
    public String ambientLevel = "poucas";

    public static boolean isAmbientLevel(String s) {
        return s != null && java.util.Set.of("nenhuma", "poucas", "normal", "muitas").contains(s.toLowerCase());
    }
    /** Quanto custa conquistar a confianca de uma criatura: "facil" (aceita seguir com bem menos), "normal" ou "dificil". */
    public String trustDifficulty = "normal";
    /** Som curto de "pensando" ("Hmm...", "Muuu?") quando a resposta demora: disfarca a espera. false = silencio ate a resposta. */
    public boolean thinkingFillers = false;
    /** Criaturas pacificas puxam assunto com o jogador de vez em quando (falas prontas, sem IA; a resposta do jogador e conversa normal com a IA). */
    public boolean mobsInitiate = true;
    /** A criatura que recusa um pedido (seguir, ficar, lutar...) so por falta de confianca aceita ser PAGA com itens que ela gosta; o pagamento pode ser aos poucos. */
    public boolean favorPayments = true;
    /** A criatura com quem o jogador fala (ou vai falar, na mira) fica parada, virada para ele e chega perto, em vez de ir embora no meio da fala. */
    public boolean conversationAttention = true;
    /** Jogar um item no chao olhando para uma criatura e dar de presente (ela vem pegar 1 e reage), alem do SHIFT + botao direito. */
    public boolean groundGifts = true;
    /** Intervalo medio entre uma abertura de conversa e a proxima, por jogador (30 a 3600 s). */
    public int initiativeSeconds = 240;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile ServerConfig current = new ServerConfig();

    public static ServerConfig current() {
        return current;
    }

    public static void set(ServerConfig c) {
        current = c;
    }

    /** Aplica o preset de carga (sem sobrescrever o que o admin definiu explicitamente alem do preset). */
    public ServerConfig normalized() {
        if ("low".equals(preset)) {
            chatterEnabled = false; // o preset leve poupa as cenas ambiente; nunca reduz o limite de aliados
        }
        maxActiveFollowersPerPlayer = Math.max(0, Math.min(maxActiveFollowersPerPlayer, 32));
        if (voiceEngine == null || !java.util.Set.of("auto", "kokoro", "piper", "nuvem").contains(voiceEngine.toLowerCase())) {
            voiceEngine = "auto";
        }
        voiceEngine = voiceEngine.toLowerCase();
        trustDifficulty = isDifficulty(trustDifficulty) ? trustDifficulty.toLowerCase() : "normal";
        initiativeSeconds = Math.max(30, Math.min(initiativeSeconds, 3600));
        return this;
    }

    public static boolean isDifficulty(String d) {
        return d != null && java.util.Set.of("facil", "normal", "dificil").contains(d.toLowerCase());
    }

    /** Multiplica a confianca EXIGIDA para seguir/ficar/defender: facil 0,6 (3 em 5 presentes), normal 1, dificil 1,4. */
    public static double trustFactor(String difficulty) {
        return switch (difficulty == null ? "normal" : difficulty.toLowerCase()) {
            case "facil" -> 0.6;
            case "dificil" -> 1.4;
            default -> 1.0;
        };
    }

    public double trustFactor() {
        return trustFactor(trustDifficulty);
    }

    /** Grava a configuracao atual em config/mobvoz/server.json (usado por /mobvoz dificuldade). */
    public static void save() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("mobvoz").resolve("server.json");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(current), StandardCharsets.UTF_8);
        } catch (IOException e) {
            MobVozMod.LOGGER.warn("nao consegui gravar config/mobvoz/server.json: {}", e.toString());
        }
    }

    public static ServerConfig parse(String json) {
        ServerConfig c = GSON.fromJson(json, ServerConfig.class);
        return (c == null ? new ServerConfig() : c).normalized();
    }

    /** Le (ou cria com os padroes) o arquivo; JSON invalido = padroes seguros + aviso (nunca derruba o jogo). */
    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("mobvoz").resolve("server.json");
        try {
            if (Files.exists(file)) {
                current = parse(Files.readString(file, StandardCharsets.UTF_8));
            } else {
                Files.createDirectories(file.getParent());
                Files.writeString(file, GSON.toJson(new ServerConfig()), StandardCharsets.UTF_8);
                current = new ServerConfig();
            }
        } catch (IOException | RuntimeException e) {
            MobVozMod.LOGGER.warn("config/mobvoz/server.json invalido ({}); usando padroes seguros", e.toString());
            current = new ServerConfig();
        }
    }
}
