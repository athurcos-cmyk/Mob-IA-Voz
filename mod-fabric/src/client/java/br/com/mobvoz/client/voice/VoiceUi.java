package br.com.mobvoz.client.voice;

import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.net.StatusPayload;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

/** Consentimento (/mobvoz voz sim|nao|status) e indicador discreto de "ouvindo". */
public final class VoiceUi {
    private static final SidecarClient SIDECAR = SidecarClient.local();

    /** Ultima resposta do comando de diagnostico (usada pelo gametest). */
    public static volatile String lastDiagReply;

    private VoiceUi() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("mobvoz")
                        .then(ClientCommandManager.literal("voz")
                                .then(ClientCommandManager.argument("opcao", StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            b.suggest("sim");
                                            b.suggest("nao");
                                            b.suggest("status");
                                            return b.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            String o = StringArgumentType.getString(ctx, "opcao");
                                            var src = ctx.getSource();
                                            switch (o) {
                                                case "sim" -> {
                                                    VoiceConsent.set(true);
                                                    src.sendFeedback(Component.literal("MobVoz: voz ATIVADA. Mire num mob e fale."));
                                                }
                                                case "nao" -> {
                                                    VoiceConsent.set(false);
                                                    src.sendFeedback(Component.literal("MobVoz: voz DESATIVADA. Nenhum audio e usado."));
                                                }
                                                default -> src.sendFeedback(Component.literal("MobVoz: voz "
                                                        + (VoiceConsent.granted() ? "ativada" : "desativada")
                                                        + "; falas detectadas: " + VoiceInput.detectedCount()));
                                            }
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("chat")
                                .then(ClientCommandManager.argument("opcao", StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            b.suggest("sim");
                                            b.suggest("nao");
                                            b.suggest("status");
                                            return b.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            String o = StringArgumentType.getString(ctx, "opcao");
                                            if (o.equals("sim") || o.equals("nao")) {
                                                br.com.mobvoz.client.ClientPrefs.setChatMode(o.equals("sim"));
                                            }
                                            ctx.getSource().sendFeedback(Component.literal("MobVoz: modo chat "
                                                    + (br.com.mobvoz.client.ClientPrefs.chatMode()
                                                    ? "LIGADO (mire numa criatura e escreva no chat; sem alvo o chat é normal)" : "desligado")));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("legenda")
                                .then(ClientCommandManager.argument("opcao", StringArgumentType.word())
                                        .executes(ctx -> {
                                            br.com.mobvoz.client.ClientPrefs.setCaptions(StringArgumentType.getString(ctx, "opcao").equals("sim"));
                                            ctx.getSource().sendFeedback(Component.literal("MobVoz: legendas "
                                                    + (br.com.mobvoz.client.ClientPrefs.captions() ? "ligadas" : "desligadas (sempre ligadas no modo chat)")));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("falar")
                                .then(ClientCommandManager.argument("texto", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            var mob = MobVozClient.target();
                                            if (mob == null) {
                                                ctx.getSource().sendFeedback(Component.literal("MobVoz: mire numa criatura (estrela) primeiro."));
                                                return 0;
                                            }
                                            return br.com.mobvoz.client.ChatMode.send(mob, StringArgumentType.getString(ctx, "texto")) ? 1 : 0;
                                        })))
                        .then(ClientCommandManager.literal("ajuda").executes(ctx -> {
                            br.com.mobvoz.client.HelpText.lines().forEach(l -> ctx.getSource().sendFeedback(Component.literal(l)));
                            return 1;
                        }))
                        // precisam do SERVIDOR (memoria da criatura, configuracao): o cliente repassa o comando
                        .then(ClientCommandManager.literal("memoria").executes(ctx -> {
                            Minecraft.getInstance().player.connection.sendCommand("mobvozinfo memoria");
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("dificuldade")
                                .executes(ctx -> {
                                    Minecraft.getInstance().player.connection.sendCommand("mobvozinfo dificuldade");
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("nivel", StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            b.suggest("facil");
                                            b.suggest("normal");
                                            b.suggest("dificil");
                                            return b.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            Minecraft.getInstance().player.connection.sendCommand("mobvozinfo dificuldade " + StringArgumentType.getString(ctx, "nivel"));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("status").executes(ctx -> {
                            var src = ctx.getSource();
                            var mc = Minecraft.getInstance();
                            boolean server = net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(
                                    br.com.mobvoz.net.UtterancePayload.TYPE);
                            SIDECAR.health().thenAccept(h -> mc.execute(() -> {
                                src.sendFeedback(Component.literal("MobVoz: sidecar " + h + " | servidor com MobVoz: " + (server ? "sim" : "não")
                                        + " | voz " + (VoiceConsent.granted() ? "ativada" : "desativada")));
                                src.sendFeedback(Component.literal("MobVoz métricas: " + br.com.mobvoz.config.Metrics.summary(0,
                                        br.com.mobvoz.social.Chatter.linesSpoken())));
                            }));
                            return 1;
                        }))
                        // Diagnostico (fase 3): texto -> sidecar -> resposta no chat. NAO e a experiencia final (voz).
                        .then(ClientCommandManager.literal("diag")
                                .then(ClientCommandManager.literal("falar")
                                        .then(ClientCommandManager.argument("texto", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    var src = ctx.getSource();
                                                    var mob = MobVozClient.target();
                                                    if (mob == null) {
                                                        src.sendFeedback(Component.literal("MobVoz: mire num mob (estrela) primeiro."));
                                                        return 0;
                                                    }
                                                    String type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
                                                    var mc = Minecraft.getInstance();
                                                    SIDECAR.reply(type, StringArgumentType.getString(ctx, "texto"), List.of(), List.of())
                                                            .thenAccept(r -> mc.execute(() -> {
                                                                String msg = r.offline() ? "MobVoz: sidecar de IA offline (jogo segue normal)."
                                                                        : "[" + r.source() + " " + r.llmMs() + "ms] " + r.speechText();
                                                                lastDiagReply = msg;
                                                                src.sendFeedback(Component.literal(msg));
                                                            }));
                                                    return 1;
                                                }))))));

        HudRenderCallback.EVENT.register((g, tick) -> {
            if (br.com.mobvoz.client.ClientPrefs.chatMode() && !VoiceConsent.granted()) {
                var f = Minecraft.getInstance().font;
                int st = VoiceInput.status();
                String t = st == StatusPayload.THINKING ? "Pensando..." : st == StatusPayload.ERROR ? "Sem resposta (IA offline?)"
                        : MobVozClient.target() != null ? "Chat: escreva para a criatura" : "Modo chat (mire numa criatura)";
                g.drawString(f, t, 6, g.guiHeight() - 14, st == StatusPayload.ERROR ? 0xFFFF5555 : 0x88AAAAAA, true);
                return;
            }
            if (!VoiceConsent.granted()) {
                return;
            }
            var font = Minecraft.getInstance().font;
            boolean on = VoiceInput.listening();
            int st = VoiceInput.status();
            String text;
            int color = 0x88AAAAAA;
            if (on) {
                text = "● Ouvindo";
                color = 0xFF55FF55;
            } else {
                switch (st) {
                    case VoiceInput.UNDERSTANDING -> { text = "Entendendo..."; color = 0xFFFFFF55; }
                    case StatusPayload.THINKING -> { text = "Pensando..."; color = 0xFFFFFF55; }
                    case StatusPayload.SPEAKING -> { text = "Falando"; color = 0xFF55FFFF; }
                    case StatusPayload.ERROR -> { text = "Sem resposta (IA offline?)"; color = 0xFFFF5555; }
                    case StatusPayload.BUSY -> { text = "Ocupado, tente de novo"; color = 0xFFFFAA00; }
                    case VoiceInput.NOT_UNDERSTOOD -> { text = "Nao entendi a fala"; color = 0xFFFFAA00; }
                    case VoiceInput.NO_SERVER -> { text = "Servidor sem MobVoz"; color = 0xFFFF5555; }
                    default -> text = "Voz ativa";
                }
            }
            g.drawString(font, text, 6, g.guiHeight() - 14, color, true);
        });
    }
}
