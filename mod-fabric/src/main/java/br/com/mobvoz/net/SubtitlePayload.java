package br.com.mobvoz.net;

import br.com.mobvoz.MobVozMod;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: legenda do que a criatura disse (texto ja validado). Vale para o modo chat e para quem quer legendas. */
public record SubtitlePayload(UUID mob, String speaker, String text) implements CustomPacketPayload {
    public static final Type<SubtitlePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "subtitle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SubtitlePayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SubtitlePayload::mob,
            ByteBufCodecs.stringUtf8(64), SubtitlePayload::speaker,
            ByteBufCodecs.stringUtf8(400), SubtitlePayload::text,
            SubtitlePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
