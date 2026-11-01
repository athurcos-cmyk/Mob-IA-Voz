package br.com.mobvoz.server;

import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Profiles;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

/** "Barrinha de confianca" mostrada na barra de acao: quanto a criatura ja gosta do jogador, em relacao ao que ela exige para segui-lo. */
public final class TrustBar {
    private static final int CELLS = 10;

    private TrustBar() {
    }

    /** 0..1: calor atual / calor exigido para seguir (1 = ja aceita seguir). Calor negativo = 0. */
    public static double progress(double warmth, double needed) {
        if (needed <= 0) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, warmth / needed));
    }

    public static Component text(Mob mob, UUID player) {
        double warmth = OrderPolicy.warmth(MemoryStore.get(mob).relationOf(player), false);
        double p = progress(warmth, OrderPolicy.followThreshold(Orders.subjectOf(mob)));
        int on = (int) Math.round(p * CELLS);
        String state = warmth < -0.05 ? " desconfiada" : p >= 1.0 ? " já aceita te seguir" : "";
        return Component.literal("Confiança de " + Profiles.ofMob(mob).label() + ": ")
                .append(Component.literal("|".repeat(on)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("|".repeat(CELLS - on)).withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(" " + Math.round(p * 100) + "%" + state).withStyle(ChatFormatting.WHITE));
    }
}
