package br.com.mobvoz.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/** Preferencias do cliente (config/mobvoz-client.properties): consentimento de voz, modo chat e legendas. Tudo desligado por padrao. */
public final class ClientPrefs {
    private static volatile boolean voiceConsent;
    private static volatile boolean chatMode;
    private static volatile boolean captions;

    static {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file())) {
            p.load(in);
        } catch (IOException ignored) {
            // primeira execucao: tudo desligado
        }
        voiceConsent = Boolean.parseBoolean(p.getProperty("voice_consent", "false"));
        chatMode = Boolean.parseBoolean(p.getProperty("chat_mode", "false"));
        captions = Boolean.parseBoolean(p.getProperty("captions", "false"));
    }

    private ClientPrefs() {
    }

    public static boolean voiceConsent() {
        return voiceConsent;
    }

    public static boolean chatMode() {
        return chatMode;
    }

    /** Legendas valem sempre no modo chat (quem nao fala tambem pode nao ouvir) ou quando ligadas a parte. */
    public static boolean showSubtitles() {
        return captions || chatMode;
    }

    public static boolean captions() {
        return captions;
    }

    public static void setVoiceConsent(boolean v) {
        voiceConsent = v;
        save();
    }

    public static void setChatMode(boolean v) {
        chatMode = v;
        save();
    }

    public static void setCaptions(boolean v) {
        captions = v;
        save();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("mobvoz-client.properties");
    }

    private static synchronized void save() {
        Properties p = new Properties();
        p.setProperty("voice_consent", Boolean.toString(voiceConsent));
        p.setProperty("chat_mode", Boolean.toString(chatMode));
        p.setProperty("captions", Boolean.toString(captions));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "MobVoz IA - voice_consent (microfone), chat_mode (digitar para a criatura na mira), captions (legendas)");
        } catch (IOException e) {
            br.com.mobvoz.client.voice.VoiceInput.LOGGER.warn("Nao foi possivel salvar as preferencias: {}", e.toString());
        }
    }
}
