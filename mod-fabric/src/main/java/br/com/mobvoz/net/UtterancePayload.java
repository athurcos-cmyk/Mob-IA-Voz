package br.com.mobvoz.net;

import br.com.mobvoz.MobVozMod;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Cliente -> servidor: o que o jogador disse (ja transcrito) e para QUEM (um unico alvo). O servidor nao confia
 * nisso (typed = digitado no chat, sem microfone): revalida distancia/mira/limites antes de qualquer coisa.
 * {@code byName}: o jogador CHAMOU a criatura pelo apelido ("Mimosa, vem aqui!") em vez de mirar; o servidor confere o nome e a distancia.
 */
public record UtterancePayload(UUID target, String text, boolean typed, boolean byName) implements CustomPacketPayload {
    /** Sem chamar por nome: o alvo e quem esta na mira. */
    public UtterancePayload(UUID target, String text, boolean typed) {
        this(target, text, typed, false);
    }

    public static final int MAX_TEXT = 300;
    public static final Type<UtterancePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "utterance"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UtterancePayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UtterancePayload::target,
            ByteBufCodecs.stringUtf8(MAX_TEXT), UtterancePayload::text,
            ByteBufCodecs.BOOL, UtterancePayload::typed,
            ByteBufCodecs.BOOL, UtterancePayload::byName,
            UtterancePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
