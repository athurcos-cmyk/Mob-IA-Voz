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
    public boolean bossContractsEnabled = false;
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
    public int ambientCooldownSeconds = 60;

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
        if (voiceEngine == null || !java.util.Set.of("auto", "kokoro", "piper").contains(voiceEngine.toLowerCase())) {
            voiceEngine = "auto";
        }
        voiceEngine = voiceEngine.toLowerCase();
        return this;
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
