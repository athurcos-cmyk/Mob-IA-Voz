package br.com.mobvoz.client.voice;

/** Opt-in explicito para o microfone ir ao MobVoz. Padrao: desligado. Persistido por {@link br.com.mobvoz.client.ClientPrefs}. */
public final class VoiceConsent {
    private VoiceConsent() {
    }

    public static boolean granted() {
        return br.com.mobvoz.client.ClientPrefs.voiceConsent();
    }

    public static void set(boolean value) {
        br.com.mobvoz.client.ClientPrefs.setVoiceConsent(value);
        if (!value) {
            VoiceInput.reset();
        }
        sendToServer();
    }

    /** Informa o servidor (quando conectado a um com o MobVoz). O servidor trata ausencia como NEGADO. */
    public static void sendToServer() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.getConnection() != null && net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(
                    br.com.mobvoz.net.ConsentPayload.TYPE)) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new br.com.mobvoz.net.ConsentPayload(granted()));
            }
        });
    }
}
