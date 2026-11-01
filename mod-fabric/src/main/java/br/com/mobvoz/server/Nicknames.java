package br.com.mobvoz.server;

import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.OrderPolicy;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

/** Apelido dado por voz: o SERVIDOR decide (a criatura aceita ou nao) e aplica o nome visivel; a IA so comenta o resultado. */
public final class Nicknames {
    public enum Verdict { ACEITO, RECUSADO, IMPOSSIVEL, JA_ATIVO }

    /** Chefes com barra de vida propria: o nome mudaria a barra para todos. */
    private static final Set<String> NO_NICKNAME = Set.of("minecraft:ender_dragon", "minecraft:wither");

    private Nicknames() {
    }

    /** Puro (testavel). {@code current}: nome atual ("" = nenhum). Desconfianca ou medo recusam; o resto aceita. */
    public static Verdict decide(String typeId, String current, String nick, double warmth, double fear) {
        if (NO_NICKNAME.contains(typeId)) {
            return Verdict.IMPOSSIVEL;
        }
        if (current.equals(nick)) {
            return Verdict.JA_ATIVO;
        }
        return warmth < -0.05 || fear > 0.5 ? Verdict.RECUSADO : Verdict.ACEITO;
    }

    /** Server thread. Devolve o resultado no formato de "resultado_das_ordens". */
    public static Map<String, String> apply(Mob mob, UUID player, String nick) {
        var rel = MemoryStore.get(mob).relationOf(player);
        String current = mob.hasCustomName() ? mob.getCustomName().getString() : "";
        Verdict v = decide(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString(), current, nick, OrderPolicy.warmth(rel, false), rel.fear());
        String reason = switch (v) {
            case ACEITO -> {
                mob.setCustomName(Component.literal(nick));
                mob.setCustomNameVisible(true);
                mob.setPersistenceRequired(); // como a etiqueta de nome: criatura com nome nao some sozinha
                yield "o jogador deu a você o apelido '" + nick + "'; a partir de agora é assim que te chamam e o nome aparece sobre a sua cabeça. "
                        + "Reaja ao apelido no seu estilo (gostou, achou engraçado, estranhou) e use-o";
            }
            case RECUSADO -> "ainda não confia o bastante em quem pede para aceitar um apelido";
            case IMPOSSIVEL -> "uma criatura como você não ganha apelido";
            case JA_ATIVO -> "você já se chama assim";
        };
        return Map.of("pedido", "APELIDAR", "resultado", v.name(), "motivo", reason);
    }
}
