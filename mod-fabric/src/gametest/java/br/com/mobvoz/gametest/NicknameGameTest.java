package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * Apelido por voz, com o LLM real: "vou te chamar de Mimosa" faz a vaca receber o nome visivel (e usa-lo na fala); uma vaca que desconfia do
 * jogador recusa; pergunta e palavra comum nao nomeiam. Confere o ESTADO (nome da entidade), que nao depende do humor do LLM.
 */
public class NicknameGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static void say(ClientGameTestContext c, String text) throws InterruptedException {
        ChatMode.lastSubtitle = null;
        c.runOnClient(mc -> mc.player.connection.sendChat(text));
        long end = System.currentTimeMillis() + 90_000;
        while (ChatMode.lastSubtitle == null) {
            check(System.currentTimeMillis() < end, "sem resposta para: " + text);
            c.waitTicks(5);
            Thread.sleep(100);
        }
        long idle = System.currentTimeMillis() + 60_000;
        while (br.com.mobvoz.client.voice.VoiceInput.status() != br.com.mobvoz.net.StatusPayload.IDLE) {
            check(System.currentTimeMillis() < idle, "a criatura nao terminou de falar");
            c.waitTicks(5);
            Thread.sleep(100);
        }
        Thread.sleep(1600);
    }

    private static String nameOf(TestSingleplayerContext w, String tag) {
        return w.getServer().computeOnServer(s -> {
            Mob m = s.overworld().getEntitiesOfClass(Mob.class, new AABB(-20, -70, -20, 20, 40, 20), x -> x.getTags().contains(tag)).get(0);
            return m.hasCustomName() ? m.getCustomName().getString() : "";
        });
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        var cfg = new ServerConfig();
        cfg.ambientChatter = false; // as duas vacas conversariam entre si e trocariam a legenda
        cfg.mobsInitiate = false; // a criatura nao puxa assunto no meio do teste
        cfg.naturalEvents = false; // sem cumprimentos espontaneos: eles ocupam a criatura e mascaram a resposta
        ServerConfig.set(cfg);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set day");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon cow 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
            context.waitTicks(20);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));

            say(context, "Posso te chamar de Mimosa?"); // pergunta: nao nomeia
            check(nameOf(world, "vaca").isEmpty(), "pergunta nao deveria nomear: " + nameOf(world, "vaca"));
            say(context, "Vou te chamar de amigo."); // palavra comum: nao nomeia
            check(nameOf(world, "vaca").isEmpty(), "palavra comum nao deveria virar apelido");

            say(context, "Vou te chamar de Mimosa.");
            System.out.println("[gametest] NICK1 -> nome=" + nameOf(world, "vaca") + " fala=" + ChatMode.lastSubtitle);
            check("Mimosa".equals(nameOf(world, "vaca")), "a vaca deveria ter virado Mimosa: '" + nameOf(world, "vaca") + "'");

            // desconfianca: uma vaca machucada pelo jogador varias vezes recusa
            cmd.runCommand("summon cow 4.5 -60 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"brava\"]}");
            context.waitTicks(10);
            world.getServer().runOnServer(s -> {
                ServerPlayer p = s.getPlayerList().getPlayers().get(0);
                Mob m = s.overworld().getEntitiesOfClass(Mob.class, new AABB(-20, -70, -20, 20, 40, 20), x -> x.getTags().contains("brava")).get(0);
                for (int i = 0; i < 4; i++) {
                    int tick = i;
                    MemoryStore.update(m, mem -> MemoryRules.damagedByPlayer(mem, p.getUUID(), tick));
                }
            });
            cmd.runCommand("tp @p 0.5 -60 0.5 -90 17"); // olha para a vaca brava (x+)
            context.waitTicks(10);
            say(context, "Seu nome agora é Rex.");
            System.out.println("[gametest] NICK2 (desconfiada) -> nome='" + nameOf(world, "brava") + "' fala=" + ChatMode.lastSubtitle);
            check(nameOf(world, "brava").isEmpty(), "a vaca que desconfia nao deveria aceitar o apelido");
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
