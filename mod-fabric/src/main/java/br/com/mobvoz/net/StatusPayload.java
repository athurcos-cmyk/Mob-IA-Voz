package br.com.mobvoz.net;

import br.com.mobvoz.MobVozMod;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: estado da conversa com um mob (para o quadro de status discreto). */
public record StatusPayload(UUID target, int state) implements CustomPacketPayload {
    public static final int IDLE = 0;
    public static final int THINKING = 1;
    public static final int SPEAKING = 2;
    public static final int ERROR = 3;
    public static final int BUSY = 4;

    public static final Type<StatusPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StatusPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, StatusPayload::target,
            ByteBufCodecs.VAR_INT, StatusPayload::state,
            StatusPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
