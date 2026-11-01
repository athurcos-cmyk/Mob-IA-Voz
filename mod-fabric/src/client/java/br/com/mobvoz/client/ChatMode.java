package br.com.mobvoz.client;

import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.net.SubtitlePayload;
import br.com.mobvoz.net.UtterancePayload;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

/**
 * Modo chat (para quem nao tem microfone): com uma criatura na mira, o que voce digita no chat vai SO para ela (nao vira
 * mensagem publica) e a resposta aparece como legenda no chat. Sem criatura na mira, o chat funciona normalmente.
 * Fala a criatura pode continuar sendo OUVIDA (SVC) por quem tem audio; sem Piper/SVC, so a legenda.
 */
public final class ChatMode {
    /** Ultima legenda recebida (diagnostico/teste). */
    public static volatile String lastSubtitle;

    private ChatMode() {
    }

    public static void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (!ClientPrefs.chatMode()) {
                return true;
            }
            var call = br.com.mobvoz.target.NameCall.pick(message, MobVozClient.namedNearby());
            if (call.isPresent()) { // "Mimosa, vem aqui!": chamou uma criatura de apelido, mesmo sem mirar nela
                return !send(call.get().id(), message, true);
            }
            Mob target = MobVozClient.target();
            if (target == null) {
                return true; // ninguem na mira: chat normal
            }
            return !send(target, message); // enviado a criatura = cancela a mensagem publica
        });
        ClientPlayNetworking.registerGlobalReceiver(SubtitlePayload.TYPE, (payload, ctx) -> {
            lastSubtitle = payload.speaker() + ": " + payload.text();
            if (ClientPrefs.showSubtitles()) {
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal("§e[" + payload.speaker() + "]§r " + payload.text()));
            }
        });
    }

    /** Envia um texto digitado ao alvo. @return true se foi enviado (false: servidor sem MobVoz, texto vazio/grande). */
    public static boolean send(Mob target, String text) {
        return send(target.getUUID(), text, false);
    }

    /** @param byName a criatura foi CHAMADA pelo apelido (nao esta na mira): o servidor confere o nome e a distancia. */
    public static boolean send(java.util.UUID target, String text, boolean byName) {
        String t = text.strip();
        var mc = Minecraft.getInstance();
        if (t.isEmpty()) {
            return false;
        }
        if (!ClientPlayNetworking.canSend(UtterancePayload.TYPE)) {
            mc.gui.getChat().addMessage(Component.literal("MobVoz: este servidor não tem o MobVoz."));
            return false;
        }
        if (t.length() > UtterancePayload.MAX_TEXT) {
            t = t.substring(0, UtterancePayload.MAX_TEXT);
            mc.gui.getChat().addMessage(Component.literal("MobVoz: mensagem cortada em " + UtterancePayload.MAX_TEXT + " caracteres."));
        }
        VoiceInput.setStatus(StatusPayload.THINKING);
        ClientPlayNetworking.send(new UtterancePayload(target, t, true, byName));
        return true;
    }
}
