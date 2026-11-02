package br.com.mobvoz.net;

import br.com.mobvoz.MobVozMod;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Cliente -> servidor: "estou prestes a falar com esta criatura" (ela esta na mira). O servidor a segura parada, virada para o jogador e perto dele
 * por alguns segundos, para ela nao ir embora no meio da fala. O servidor revalida distancia e linha de visao; este pacote so pede.
 */
public record AttentionPayload(UUID mob) implements CustomPacketPayload {
    public static final Type<AttentionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "attention"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttentionPayload> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, AttentionPayload::mob, AttentionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
