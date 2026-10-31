package br.com.mobvoz.trade;

import br.com.mobvoz.MobVozMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

/**
 * Livro-razao de trocas de UMA criatura (anexo persistente, salvo junto com ela). Maquina de estados imutavel:
 * oferta aberta (+ itens em que o jogador a CONVENCEU, + quantas UNIDADES do produto) -> RESERVADO (itens exatos ja tirados do
 * chao e anotados aqui) -> concluido OU reembolsado. Ids encerrados ficam em {@code done}: processar a mesma transacao duas
 * vezes e no-op. Itens "id=quantidade";
 * interesses persuadidos "id=milesimos" (0..800).
 */
public record MobTrades(String offerRecipe, String offerPlayer, long offerExpires, List<String> persuaded, int persuasions,
                        String txId, String txRecipe, String txPlayer, long txTick, List<String> txItems,
                        List<String> done, int offerUnits, int txUnits, int offerPrice) {
    public static final int MAX_DONE = 32;
    public static final MobTrades EMPTY = new MobTrades("", "", 0, List.of(), 0, "", "", "", 0, List.of(), List.of(), 1, 1, 0);

    public static final Codec<MobTrades> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("offerRecipe").forGetter(MobTrades::offerRecipe),
            Codec.STRING.fieldOf("offerPlayer").forGetter(MobTrades::offerPlayer),
            Codec.LONG.fieldOf("offerExpires").forGetter(MobTrades::offerExpires),
            Codec.STRING.listOf().optionalFieldOf("persuaded", List.of()).forGetter(MobTrades::persuaded),
            Codec.INT.optionalFieldOf("persuasions", 0).forGetter(MobTrades::persuasions),
            Codec.STRING.fieldOf("txId").forGetter(MobTrades::txId),
            Codec.STRING.fieldOf("txRecipe").forGetter(MobTrades::txRecipe),
            Codec.STRING.fieldOf("txPlayer").forGetter(MobTrades::txPlayer),
            Codec.LONG.fieldOf("txTick").forGetter(MobTrades::txTick),
            Codec.STRING.listOf().optionalFieldOf("txItems", List.of()).forGetter(MobTrades::txItems),
            Codec.STRING.listOf().fieldOf("done").forGetter(MobTrades::done),
            Codec.INT.optionalFieldOf("offerUnits", 1).forGetter(MobTrades::offerUnits),
            Codec.INT.optionalFieldOf("txUnits", 1).forGetter(MobTrades::txUnits),
            Codec.INT.optionalFieldOf("offerPrice", 0).forGetter(MobTrades::offerPrice)).apply(i, MobTrades::new));

    public static final AttachmentType<MobTrades> TYPE = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "trades"), CODEC);

    public boolean hasOffer() {
        return !offerRecipe.isEmpty();
    }

    public boolean hasTx() {
        return !txId.isEmpty();
    }

    public boolean active() {
        return hasOffer() || hasTx();
    }

    public boolean isDone(String id) {
        return done.contains(id);
    }

    public Map<String, Integer> persuadedMap() {
        return parse(persuaded);
    }

    public Map<String, Integer> reservedMap() {
        return parse(txItems);
    }

    static Map<String, Integer> parse(List<String> l) {
        Map<String, Integer> m = new HashMap<>();
        for (String s : l) {
            int i = s.lastIndexOf('=');
            if (i > 0) {
                try {
                    m.merge(s.substring(0, i), Integer.parseInt(s.substring(i + 1)), Integer::sum);
                } catch (NumberFormatException ignored) {
                    // entrada invalida de save antigo: ignorada
                }
            }
        }
        return m;
    }

    public static List<String> encode(Map<String, Integer> m) {
        List<String> out = new ArrayList<>();
        new java.util.TreeMap<>(m).forEach((k, v) -> out.add(k + "=" + v));
        return List.copyOf(out);
    }

    /** Abre a oferta de 1 unidade (mantem a persuasao se for a mesma oferta do mesmo jogador). */
    public MobTrades open(String recipeId, String player, long expires) {
        return open(recipeId, player, expires, 1);
    }

    public MobTrades open(String recipeId, String player, long expires, int units) {
        return open(recipeId, player, expires, units, 0);
    }

    /** {@code priceTenths}: preco POR UNIDADE travado nesta oferta (decimos de ponto; 0 = o da receita). Reabrir a mesma oferta mantem o travado. */
    public MobTrades open(String recipeId, String player, long expires, int units, int priceTenths) {
        boolean same = offerRecipe.equals(recipeId) && offerPlayer.equals(player);
        return new MobTrades(recipeId, player, expires, same ? persuaded : List.of(), same ? persuasions : 0,
                txId, txRecipe, txPlayer, txTick, txItems, done, Math.max(1, units), txUnits, same && offerPrice > 0 ? offerPrice : priceTenths);
    }

    /** Preco por unidade desta oferta: o travado, ou {@code recipePrice} se nenhum foi travado. */
    public double unitPrice(double recipePrice) {
        return offerPrice > 0 ? offerPrice / 10.0 : recipePrice;
    }

    /** Barganha bem-sucedida: novo preco travado (conta como uma tentativa de convencer). */
    public MobTrades withHaggledPrice(int priceTenths) {
        return new MobTrades(offerRecipe, offerPlayer, offerExpires, persuaded, persuasions + 1, txId, txRecipe, txPlayer, txTick,
                txItems, done, offerUnits, txUnits, priceTenths);
    }

    public MobTrades withPersuasion(String item, int milli) {
        Map<String, Integer> m = persuadedMap();
        m.put(item, milli);
        return new MobTrades(offerRecipe, offerPlayer, offerExpires, encode(m), persuasions + 1, txId, txRecipe, txPlayer, txTick,
                txItems, done, offerUnits, txUnits, offerPrice);
    }

    /** Interesse JULGADO pela IA para um item fora do gosto fixo (nao conta como tentativa de convencer). Nao rebaixa o que ja existe. */
    public MobTrades withJudged(String item, int milli) {
        Map<String, Integer> m = persuadedMap();
        if (m.getOrDefault(item, 0) >= milli) {
            return this;
        }
        m.put(item, milli);
        return new MobTrades(offerRecipe, offerPlayer, offerExpires, encode(m), persuasions, txId, txRecipe, txPlayer, txTick,
                txItems, done, offerUnits, txUnits, offerPrice);
    }

    public MobTrades closeOffer() {
        return new MobTrades("", "", 0, List.of(), 0, txId, txRecipe, txPlayer, txTick, txItems, done, 1, txUnits, 0);
    }

    /** Itens exatos ja tirados do chao: a oferta vira transacao RESERVADA (mesmo tick, mesmo objeto salvo). */
    public MobTrades reserve(String id, String recipe, String player, long tick, Map<String, Integer> items) {
        return reserve(id, recipe, player, tick, items, Math.max(1, offerUnits));
    }

    public MobTrades reserve(String id, String recipe, String player, long tick, Map<String, Integer> items, int units) {
        return new MobTrades("", "", 0, List.of(), 0, id, recipe, player, tick, encode(items), done, 1, Math.max(1, units), 0);
    }

    private MobTrades finish() {
        List<String> nd = new ArrayList<>(done);
        nd.add(txId);
        while (nd.size() > MAX_DONE) {
            nd.remove(0);
        }
        return new MobTrades(offerRecipe, offerPlayer, offerExpires, persuaded, persuasions, "", "", "", 0, List.of(), List.copyOf(nd),
                offerUnits, 1, offerPrice);
    }

    public MobTrades completed() {
        return finish();
    }

    public MobTrades refunded() {
        return finish();
    }
}
