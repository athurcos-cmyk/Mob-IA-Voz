package br.com.mobvoz.server;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.knowledge.KnowledgeStore;
import br.com.mobvoz.memory.MemoryReport;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.target.TargetPicker;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Comandos que precisam do SERVIDOR (o cliente repassa {@code /mobvoz memoria} e {@code /mobvoz dificuldade}): o que a criatura da mira
 * sabe do jogador, e a dificuldade da confianca. A raiz e {@code mobvozinfo} para nao colidir com os comandos do cliente ({@code /mobvoz ...}).
 */
public final class MobVozCommands {
    private MobVozCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(Commands.literal("mobvozinfo")
                .then(Commands.literal("memoria").executes(ctx -> memory(ctx.getSource())))
                .then(Commands.literal("conversas")
                        .executes(ctx -> chatter(ctx.getSource(), null))
                        .then(Commands.argument("nivel", StringArgumentType.word()).executes(ctx -> chatter(ctx.getSource(), StringArgumentType.getString(ctx, "nivel")))))
                .then(Commands.literal("motorvoz")
                        .executes(ctx -> engine(ctx.getSource(), null))
                        .then(Commands.argument("motor", StringArgumentType.word()).executes(ctx -> engine(ctx.getSource(), StringArgumentType.getString(ctx, "motor")))))
                .then(Commands.literal("dificuldade")
                        .executes(ctx -> difficulty(ctx.getSource(), null))
                        .then(Commands.argument("nivel", StringArgumentType.word()).executes(ctx -> difficulty(ctx.getSource(), StringArgumentType.getString(ctx, "nivel")))))));
    }

    /** A criatura sob a mira do jogador (ate o alcance padrao da mira), ou null. */
    static Mob aimedMob(ServerPlayer p) {
        Vec3 from = p.getEyePosition();
        Vec3 look = p.getViewVector(1.0F).scale(TargetPicker.RANGE);
        List<Mob> mobs = p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().expandTowards(look).inflate(1.0), m -> m.isAlive() && !m.isSpectator());
        List<AABB> boxes = mobs.stream().map(m -> m.getBoundingBox().inflate(m.getPickRadius())).toList();
        int i = TargetPicker.nearest(from, from.add(look), boxes);
        return i < 0 ? null : mobs.get(i);
    }

    private static int memory(CommandSourceStack src) {
        ServerPlayer p = src.getPlayer();
        if (p == null) {
            return 0;
        }
        Mob mob = aimedMob(p);
        if (mob == null) {
            src.sendSuccess(() -> Component.literal("MobVoz: mire numa criatura (a estrela aparece) e use /mobvoz memoria."), false);
            return 0;
        }
        var mem = MemoryStore.get(mob);
        long now = p.level().getGameTime();
        double warmth = OrderPolicy.warmth(mem.relationOf(p.getUUID()), false);
        String faction = KnowledgeStore.factionOf(Profiles.keyOf(mob));
        double grudge = faction.isEmpty() ? 0 : KnowledgeStore.get(p.getServer()).trustPenalty(faction, p.getUUID(), now, mob.getX(), mob.getZ());
        warmth -= grudge;
        String name = mob.hasCustomName() ? mob.getCustomName().getString() : "";
        var view = new MemoryReport.View(name, Profiles.ofMob(mob).label(), mem.nameOf(p.getUUID()), mem, p.getUUID(), now, warmth,
                OrderPolicy.followThreshold(Orders.subjectOf(mob)), ServerConfig.current().trustDifficulty, grudge);
        MemoryReport.lines(view).forEach(l -> src.sendSuccess(() -> Component.literal(l), false));
        return 1;
    }

    /** Quantas conversas de ambiente entre as criaturas (as falas que voce escuta sem falar com ninguem). */
    private static int chatter(CommandSourceStack src, String level) {
        var cfg = ServerConfig.current();
        if (level == null) {
            src.sendSuccess(() -> Component.literal("MobVoz: conversas de ambiente = " + cfg.ambientLevel + ". Troque com /mobvoz conversas nenhuma|poucas|normal|muitas."), false);
            return 1;
        }
        if (!ServerConfig.isAmbientLevel(level)) {
            src.sendFailure(Component.literal("MobVoz: use nenhuma, poucas, normal ou muitas."));
            return 0;
        }
        if (!src.getServer().isSingleplayer() && !src.hasPermission(2)) {
            src.sendFailure(Component.literal("MobVoz: só o administrador do servidor muda as conversas de ambiente."));
            return 0;
        }
        cfg.ambientLevel = level.toLowerCase();
        ServerConfig.save();
        br.com.mobvoz.social.Ambient.reset();
        src.sendSuccess(() -> Component.literal("MobVoz: conversas de ambiente agora são " + cfg.ambientLevel + switch (cfg.ambientLevel) {
            case "nenhuma" -> " (as criaturas só falam com você)";
            case "poucas" -> " (o padrão: uma de vez em quando, nunca em sequência)";
            case "normal" -> " (uma a cada ~45 s por perto)";
            default -> " (bem animado: várias por minuto)";
        }), true);
        return 1;
    }

    /** Motor das vozes: auto (o padrao local), kokoro, piper ou nuvem (Azure, so com a chave do dono do PC). */
    private static int engine(CommandSourceStack src, String name) {
        var cfg = ServerConfig.current();
        if (name == null) {
            src.sendSuccess(() -> Component.literal("MobVoz: motor das vozes = " + cfg.voiceEngine + ". Troque com /mobvoz motorvoz auto|kokoro|piper|nuvem."), false);
            return 1;
        }
        if (!java.util.Set.of("auto", "kokoro", "piper", "nuvem").contains(name.toLowerCase())) {
            src.sendFailure(Component.literal("MobVoz: use auto, kokoro, piper ou nuvem."));
            return 0;
        }
        if (!src.getServer().isSingleplayer() && !src.hasPermission(2)) {
            src.sendFailure(Component.literal("MobVoz: só o administrador do servidor muda o motor das vozes."));
            return 0;
        }
        cfg.voiceEngine = name.toLowerCase();
        ServerConfig.save();
        src.sendSuccess(() -> Component.literal("MobVoz: motor das vozes agora é " + cfg.voiceEngine
                + ("nuvem".equals(cfg.voiceEngine) ? " (precisa da chave do Azure; sem ela, ou passando do limite do dia, volta sozinho para a voz local)" : "")), true);
        return 1;
    }

    private static int difficulty(CommandSourceStack src, String level) {
        var cfg = ServerConfig.current();
        if (level == null) {
            src.sendSuccess(() -> Component.literal("MobVoz: dificuldade da confiança = " + cfg.trustDifficulty
                    + ". Troque com /mobvoz dificuldade facil|normal|dificil."), false);
            return 1;
        }
        if (!ServerConfig.isDifficulty(level)) {
            src.sendFailure(Component.literal("MobVoz: use facil, normal ou dificil."));
            return 0;
        }
        if (!src.getServer().isSingleplayer() && !src.hasPermission(2)) {
            src.sendFailure(Component.literal("MobVoz: só o administrador do servidor muda a dificuldade."));
            return 0;
        }
        cfg.trustDifficulty = level.toLowerCase();
        ServerConfig.save();
        src.sendSuccess(() -> Component.literal("MobVoz: dificuldade da confiança agora é " + cfg.trustDifficulty
                + (cfg.trustDifficulty.equals("facil") ? " (as criaturas aceitam seguir com bem menos)" : cfg.trustDifficulty.equals("dificil")
                ? " (as criaturas pedem bem mais confiança)" : " (o padrão)")), true);
        return 1;
    }
}
