package br.com.mobvoz.net;

import br.com.mobvoz.server.ConsentRegistry;
import br.com.mobvoz.server.ConversationService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Registro dos pacotes (comum a cliente e servidor). Os receptores rodam na thread do servidor. */
public final class MobVozNet {
    private MobVozNet() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(UtterancePayload.TYPE, UtterancePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ConsentPayload.TYPE, ConsentPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ConsentPayload.TYPE, (p, ctx) -> ConsentRegistry.set(ctx.player().getUUID(), p.on()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ConsentRegistry.set(handler.getPlayer().getUUID(), false));
        PayloadTypeRegistry.playS2C().register(StatusPayload.TYPE, StatusPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SubtitlePayload.TYPE, SubtitlePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UtterancePayload.TYPE,
                (payload, ctx) -> ConversationService.onUtterance(ctx.player(), payload));
    }
}
