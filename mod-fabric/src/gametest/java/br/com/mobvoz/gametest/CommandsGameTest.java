package br.com.mobvoz.gametest;

import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.client.HelpText;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.server.TrustBar;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * Comandos e qualidade de vida, em jogo real: /mobvoz ajuda (todos os comandos existem e estao explicados), /mobvoz memoria (precisa de
 * criatura na mira), /mobvoz dificuldade (muda o que as criaturas exigem; salva), cache de audio das falas fixas (a repeticao sai na hora).
 */
public class CommandsGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static ServerPlayer player(MinecraftServer s) {
        return s.getPlayerList().getPlayers().get(0);
    }

    private static Mob byTag(MinecraftServer s, String tag) {
        return s.overworld().getEntitiesOfClass(Mob.class, new AABB(-30, -70, -30, 30, 40, 30), x -> x.getTags().contains(tag)).get(0);
    }

    private static int run(TestSingleplayerContext w, String command) {
        return w.getServer().computeOnServer(s -> {
            try {
                return s.getCommands().getDispatcher().execute(command, player(s).createCommandSourceStack());
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                System.out.println("[gametest] comando recusado: " + command + " -> " + e.getMessage());
                return -1;
            }
        });
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig.set(new ServerConfig());
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set day");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
            context.waitTicks(20);

            // 1) /mobvoz ajuda: todo subcomando existe no comando de verdade e esta explicado no texto
            String help = String.join("\n", HelpText.lines());
            var root = context.computeOnClient(mc -> ClientCommandManager.getActiveDispatcher().getRoot().getChild("mobvoz"));
            check(root != null, "o comando /mobvoz nao existe no cliente");
            for (String c : HelpText.COMMANDS) {
                check(root.getChild(c) != null, "/mobvoz " + c + " esta na ajuda mas nao existe");
                check(help.contains("/mobvoz " + c), "/mobvoz " + c + " nao esta explicado na ajuda");
            }
            for (var child : root.getChildren()) {
                if (!child.getName().equals("diag")) {
                    check(HelpText.COMMANDS.contains(child.getName()), "/mobvoz " + child.getName() + " existe mas a ajuda nao o conhece");
                }
            }
            System.out.println("[gametest] CMD1 ajuda: " + HelpText.COMMANDS.size() + " comandos conferidos (" + HelpText.lines().size() + " linhas)");

            // 2) /mobvoz memoria: com criatura na mira mostra; sem criatura avisa
            UUID pid = world.getServer().computeOnServer(s -> player(s).getUUID());
            world.getServer().runOnServer(s -> MemoryStore.update(byTag(s, "vaca"), m -> MemoryRules.gift(MemoryRules.firstMeeting(m, pid, 0), pid, "minecraft:wheat", 1, 10, 1.0, "gostou")));
            check(run(world, "mobvozinfo memoria") == 1, "com a vaca na mira o comando deveria mostrar a memoria");
            cmd.runCommand("tp @p 0.5 -60 0.5 180 17"); // de costas para a vaca
            context.waitTicks(5);
            check(run(world, "mobvozinfo memoria") == 0, "sem criatura na mira o comando deveria avisar (resultado 0)");
            System.out.println("[gametest] CMD2 memoria: com alvo=1, sem alvo=0");

            // 3) /mobvoz dificuldade: valida, muda e o efeito aparece na confianca exigida
            check(run(world, "mobvozinfo dificuldade") == 1 && ServerConfig.current().trustDifficulty.equals("normal"), "padrao = normal");
            check(run(world, "mobvozinfo dificuldade impossivel") == 0 && ServerConfig.current().trustDifficulty.equals("normal"), "nivel invalido e recusado");
            check(run(world, "mobvozinfo motorvoz nuvem") == 1 && ServerConfig.current().voiceEngine.equals("nuvem"), "motor da voz: nuvem");
            check(run(world, "mobvozinfo motorvoz xyz") == 0 && ServerConfig.current().voiceEngine.equals("nuvem"), "motor invalido e recusado");
            check(run(world, "mobvozinfo motorvoz auto") == 1 && ServerConfig.current().voiceEngine.equals("auto"), "motor volta para auto");
            double[] pct = new double[3];
            boolean[] follows = new boolean[3];
            String[] levels = {"dificil", "normal", "facil"};
            for (int i = 0; i < 3; i++) {
                check(run(world, "mobvozinfo dificuldade " + levels[i]) == 1 && ServerConfig.current().trustDifficulty.equals(levels[i]), "mudar para " + levels[i]);
                String tag = "porco" + i;
                cmd.runCommand("summon pig " + (3.5 + i) + " -60 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"" + tag + "\"]}");
                context.waitTicks(5);
                final int idx = i;
                // calor 0,15: o porco normal (limite 0,2) recusa; o facil (0,12) aceita; o dificil (0,28) recusa
                world.getServer().runOnServer(s -> MemoryStore.update(byTag(s, tag),
                        m -> m.withRelation(pid, r -> new br.com.mobvoz.memory.MobMemory.Relationship(0.3f, 0f, 0f))));
                pct[idx] = world.getServer().computeOnServer(s -> TrustBar.progress(0.15, br.com.mobvoz.orders.OrderPolicy.followThreshold(Orders.subjectOf(byTag(s, tag)))));
                follows[idx] = world.getServer().computeOnServer(s -> Orders.handle(byTag(s, tag), player(s), List.of("FOLLOW_PLAYER"), false).get(0).accepted());
            }
            System.out.println("[gametest] CMD3 dificuldade: barra dificil/normal/facil = " + pct[0] + "/" + pct[1] + "/" + pct[2] + "; segue = " + follows[0] + "/" + follows[1] + "/" + follows[2]);
            check(pct[0] < pct[1] && pct[1] < pct[2], "a barra deveria encher mais no facil: " + pct[0] + " " + pct[1] + " " + pct[2]);
            check(!follows[0] && !follows[1] && follows[2], "so o porco do nivel facil deveria aceitar seguir com calor 0,15");
            ServerConfig.set(new ServerConfig());

            // 4) cache de audio das falas fixas (usa o TTS real do sidecar)
            SidecarClient sc = SidecarClient.local();
            long h0 = SidecarClient.AUDIO.hits();
            long t0 = System.nanoTime();
            var first = sc.synthesizeCached("Que susto, hein!", "pt_BR-faber-medium", 0.881, 0.95).join();
            long coldMs = (System.nanoTime() - t0) / 1_000_000;
            check(first.isPresent() && first.get().length > 4800, "o TTS deveria devolver audio (sidecar ligado?)");
            long t1 = System.nanoTime();
            var again = sc.synthesizeCached("Que susto, hein!", "pt_BR-faber-medium", 0.889, 0.95).join(); // outro individuo: tom parecido
            long warmMs = (System.nanoTime() - t1) / 1_000_000;
            System.out.println("[gametest] CMD4 cache de audio: 1a vez " + coldMs + " ms, 2a vez " + warmMs + " ms, acertos " + (SidecarClient.AUDIO.hits() - h0)
                    + ", " + SidecarClient.AUDIO.size() + " falas (" + SidecarClient.AUDIO.bytes() / 1024 + " KB)");
            check(again.isPresent() && again.get() == first.get(), "a 2a vez deveria devolver o MESMO audio do cache");
            check(SidecarClient.AUDIO.hits() == h0 + 1, "deveria contar 1 acerto");
            check(warmMs < Math.max(50, coldMs / 4), "a repeticao deveria ser quase instantanea: " + warmMs + " ms contra " + coldMs + " ms");
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
