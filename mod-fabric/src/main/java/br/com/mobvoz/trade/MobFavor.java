package br.com.mobvoz.trade;

import br.com.mobvoz.MobVozMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

/**
 * Pedagio por um favor (seguir, ficar de guarda, lutar, domar, trégua) que a criatura recusou por falta de confianca, mas aceita por
 * pagamento. Anexo persistente: o que ja foi pago fica anotado ({@code taken}, "id=quantidade") ate o pedido ser cumprido ou o prazo
 * acabar (ai o pago volta ao jogador). Valores em decimos de ponto, como o resto do comercio.
 */
public record MobFavor(String action, String player, long expires, int priceTenths, int paidTenths, List<String> taken) {
    public static final MobFavor EMPTY = new MobFavor("", "", 0, 0, 0, List.of());

    public static final Codec<MobFavor> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("action").forGetter(MobFavor::action),
            Codec.STRING.fieldOf("player").forGetter(MobFavor::player),
            Codec.LONG.fieldOf("expires").forGetter(MobFavor::expires),
            Codec.INT.fieldOf("priceTenths").forGetter(MobFavor::priceTenths),
            Codec.INT.fieldOf("paidTenths").forGetter(MobFavor::paidTenths),
            Codec.STRING.listOf().optionalFieldOf("taken", List.of()).forGetter(MobFavor::taken)).apply(i, MobFavor::new));

    public static final AttachmentType<MobFavor> TYPE = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "favor"), CODEC);

    public boolean active() {
        return !action.isEmpty();
    }

    public double price() {
        return priceTenths / 10.0;
    }

    public double paid() {
        return paidTenths / 10.0;
    }

    public Map<String, Integer> takenMap() {
        return MobTrades.parse(taken);
    }

    public MobFavor withPayment(int addTenths, Map<String, Integer> addItems) {
        Map<String, Integer> all = takenMap();
        addItems.forEach((k, v) -> all.merge(k, v, Integer::sum));
        return new MobFavor(action, player, expires, priceTenths, paidTenths + addTenths, MobTrades.encode(all));
    }
}
