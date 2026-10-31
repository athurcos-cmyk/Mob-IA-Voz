package br.com.mobvoz.net;

import br.com.mobvoz.MobVozMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente -> servidor: o jogador autoriza (ou revoga) que sua voz alimente criaturas. Padrao no servidor: NEGADO. */
public record ConsentPayload(boolean on) implements CustomPacketPayload {
    public static final Type<ConsentPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "consent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConsentPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, ConsentPayload::on, ConsentPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
