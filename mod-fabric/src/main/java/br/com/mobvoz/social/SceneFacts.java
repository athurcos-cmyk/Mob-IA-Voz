package br.com.mobvoz.social;

import br.com.mobvoz.profile.Mentions;
import br.com.mobvoz.trade.Values;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * Fatos da CENA, verificados pelo servidor, para a criatura comentar com naturalidade (hora, clima, vida do jogador, o que ele segura,
 * quem esta por perto). A IA so recebe fatos: nunca inventa o clima nem quem esta perto.
 */
public final class SceneFacts {
    public record Inputs(long dayTime, boolean raining, boolean thundering, String dimension, float playerHealth, int playerFood,
                         String heldItem, Map<String, Integer> nearby, float mobHealth, float mobMaxHealth, String armor) {
        public Inputs(long dayTime, boolean raining, boolean thundering, String dimension, float playerHealth, int playerFood,
                      String heldItem, Map<String, Integer> nearby, float mobHealth, float mobMaxHealth) {
            this(dayTime, raining, thundering, dimension, playerHealth, playerFood, heldItem, nearby, mobHealth, mobMaxHealth, "");
        }
    }

    private SceneFacts() {
    }

    /** Puro (testavel): ate 5 linhas curtas em pt-BR, so do que e relevante. */
    public static List<String> format(Inputs in) {
        List<String> out = new ArrayList<>();
        long t = ((in.dayTime() % 24000) + 24000) % 24000;
        out.add("Cena agora: " + (t < 12000 ? "é de dia." : t < 13000 ? "o sol está se pondo." : t < 23000 ? "é noite." : "o dia está amanhecendo."));
        if (in.thundering()) {
            out.add("Cena agora: há uma tempestade com raios.");
        } else if (in.raining()) {
            out.add("Cena agora: está chovendo.");
        }
        if (in.dimension().equals("the_nether")) {
            out.add("Cena agora: vocês estão no Nether.");
        } else if (in.dimension().equals("the_end")) {
            out.add("Cena agora: vocês estão no Fim.");
        }
        if (in.playerHealth() <= 6) {
            out.add("Cena agora: o jogador está muito ferido (" + Math.max(1, Math.round(in.playerHealth() / 2)) + " corações).");
        } else if (in.playerHealth() <= 12) {
            out.add("Cena agora: o jogador está ferido.");
        }
        if (in.playerFood() <= 6) {
            out.add("Cena agora: o jogador parece estar com fome.");
        }
        if (!in.heldItem().isEmpty()) {
            out.add("Cena agora: o jogador está segurando " + in.heldItem() + ".");
        }
        if (!in.armor().isEmpty()) {
            out.add("Cena agora: o jogador veste " + in.armor() + ".");
        }
        if (!in.nearby().isEmpty()) {
            List<String> parts = new ArrayList<>();
            in.nearby().forEach((name, n) -> parts.add(n + " " + name));
            out.add("Cena agora: perto de vocês há " + String.join(", ", parts) + ".");
        }
        if (in.mobMaxHealth() > 0 && in.mobHealth() < in.mobMaxHealth() * 0.5f) {
            out.add("Cena agora: você mesmo está ferido.");
        }
        return out.size() > 5 ? out.subList(0, 5) : out;
    }

    /** Nome (pt-BR) do que o jogador segura, se o jogo conhece o item; "" caso contrario. */
    public static String heldName(ServerPlayer player) {
        var held = player.getMainHandItem();
        if (!held.isEmpty()) {
            String id = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
            if (Values.known(id)) {
                return Values.name(id, held.getCount() != 1);
            }
        }
        return "";
    }

    private static final String[][] MATERIALS = {{"leather", "couro"}, {"chainmail", "malha"}, {"golden", "ouro"}, {"iron", "ferro"}, {"diamond", "diamante"},
            {"netherite", "netherite"}};

    /** O que o jogador veste, pelo melhor material: "uma armadura de ferro" (3 ou mais pecas) ou "pecas de armadura de ferro"; "" se nada. */
    public static String armorName(ServerPlayer player) {
        int best = -1;
        int pieces = 0;
        for (var slot : new net.minecraft.world.entity.EquipmentSlot[] {net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            var st = player.getItemBySlot(slot);
            if (st.isEmpty()) {
                continue;
            }
            String path = BuiltInRegistries.ITEM.getKey(st.getItem()).getPath();
            for (int i = 0; i < MATERIALS.length; i++) {
                if (path.startsWith(MATERIALS[i][0] + "_")) {
                    pieces++;
                    best = Math.max(best, i);
                }
            }
        }
        if (best < 0) {
            return "";
        }
        return (pieces >= 3 ? "uma armadura de " : "pe\u00e7as de armadura de ") + MATERIALS[best][1];
    }

    /** Server thread: junta as entradas reais do mundo. */
    public static List<String> lines(Mob mob, ServerPlayer player) {
        var level = player.level();
        String heldName = heldName(player);
        Map<String, Integer> near = new LinkedHashMap<>();
        for (Mob m : level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(12), x -> x != mob && x.isAlive())) {
            String label = Mentions.labelOf(BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).toString());
            if (label != null && (near.size() < 4 || near.containsKey(label))) {
                near.merge(label, 1, Integer::sum);
            }
        }
        List<String> out = new ArrayList<>(format(new Inputs(level.getDayTime(), level.isRaining(), level.isThundering(), level.dimension().location().getPath(),
                player.getHealth(), player.getFoodData().getFoodLevel(), heldName, near, mob.getHealth(), mob.getMaxHealth(), armorName(player))));
        if (mob.hasCustomName()) { // apelido dado por voz ou etiqueta de nome
            out.add(0, "Seu nome é " + mob.getCustomName().getString() + " (foi o jogador, ou alguém, que lhe deu esse nome).");
        }
        return out;
    }

}
