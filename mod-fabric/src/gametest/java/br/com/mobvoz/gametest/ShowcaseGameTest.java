package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.social.Ambient;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * VITRINE (nao e teste de regressao): monta cenas arrumadas, conversa de verdade com as criaturas (precisa do sidecar; para prints bons,
 * rode-o com a IA propria) e tira prints limpos para a pagina do projeto. Rode:
 * {@code gradlew runClientGameTest -Ponly=ShowcaseGameTest[:farm|:village|:warden] --offline} (sem sufixo = tudo).
 * Saida: build/run/clientGameTest/screenshots/.
 */
public class ShowcaseGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static ServerPlayer player(MinecraftServer s) {
        return s.getPlayerList().getPlayers().get(0);
    }

    private static Mob byTag(MinecraftServer s, String tag) {
        for (ServerLevel l : s.getAllLevels()) {
            for (Mob m : l.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(-90, -70, -90, 90, 40, 90), x -> x.getTags().contains(tag))) {
                return m;
            }
        }
        return null;
    }

    private static void clearChat(ClientGameTestContext c) {
        c.runOnClient(mc -> mc.gui.getChat().clearMessages(true));
    }

    /** Digita {@code text} (chat privado com a criatura na mira) e espera a legenda da resposta. */
    private static void say(ClientGameTestContext c, String text) throws InterruptedException {
        ChatMode.lastSubtitle = null;
        c.runOnClient(mc -> {
            mc.gui.getChat().addMessage(Component.literal("§7<§fVocê§7>§r " + text)); // eco visual do que foi digitado
            mc.player.connection.sendChat(text);
        });
        long end = System.currentTimeMillis() + 120_000;
        while (ChatMode.lastSubtitle == null) {
            check(System.currentTimeMillis() < end, "sem resposta para: " + text);
            c.waitTicks(5);
            Thread.sleep(100);
        }
        c.waitTicks(25); // deixa o HUD "falando" e a legenda aparecerem
    }

    /** Espera a criatura terminar de falar (o mod descarta enunciados enquanto ela esta ocupada). */
    private static void settle(ClientGameTestContext c) throws InterruptedException {
        long end = System.currentTimeMillis() + 45_000;
        while (VoiceInput.status() != StatusPayload.IDLE && System.currentTimeMillis() < end) {
            c.waitTicks(5);
            Thread.sleep(100);
        }
        c.waitTicks(40);
    }

    /** Posiciona a camera ({@code pos} = "x y z yaw pitch") e confirma que a estrela esta na criatura com a tag esperada. */
    private static void aim(TestSingleplayerContext w, ClientGameTestContext c, String pos, String tag) {
        w.getServer().runCommand("tp @p " + pos);
        c.waitTicks(25);
        UUID target = c.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
        UUID want = w.getServer().computeOnServer(sv -> byTag(sv, tag).getUUID());
        check(want.equals(target), "a mira nao esta em '" + tag + "' (alvo atual: " + target + ")");
    }

    private static void farm(TestSingleplayerContext world, ClientGameTestContext context) throws InterruptedException {
        var cmd = world.getServer();
        cmd.runCommand("fill -6 -61 2 8 -61 12 minecraft:grass_block");
        cmd.runCommand("fill -3 -60 3 4 -60 3 minecraft:oak_fence");
        cmd.runCommand("fill -3 -60 8 4 -60 8 minecraft:oak_fence");
        cmd.runCommand("fill -3 -60 4 -3 -60 7 minecraft:oak_fence");
        cmd.runCommand("fill 4 -60 4 4 -60 7 minecraft:oak_fence");
        cmd.runCommand("fill 0 -60 3 1 -60 3 minecraft:air"); // entrada do cercado: a cerca nao pode tapar a mira
        cmd.runCommand("setblock 3 -60 4 minecraft:hay_block");
        cmd.runCommand("setblock 3 -59 4 minecraft:hay_block");
        cmd.runCommand("setblock -2 -60 7 minecraft:hay_block");
        for (int i = 0; i < 7; i++) {
            cmd.runCommand("setblock " + (-5 + i) + " -60 " + (4 + (i % 3)) + (i % 2 == 0 ? " minecraft:poppy" : " minecraft:dandelion"));
        }
        cmd.runCommand("item replace entity @p weapon.mainhand with minecraft:wheat 12");
        cmd.runCommand("summon cow 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"],Rotation:[180f,0f]}");
        cmd.runCommand("summon cow -1.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Rotation:[160f,0f]}");
        context.waitTicks(60);

        aim(world, context, "0.5 -60 0.5 0 8", "vaca");
        context.runOnClient(mc -> mc.getToastManager().clear()); // some o aviso de "receita desbloqueada" (pegar trigo)
        context.waitTicks(10);
        clearChat(context);
        context.takeScreenshot("showcase_01_estrela_na_vaca");
        say(context, "Oi, vaca! Tudo bem por aqui?");
        context.takeScreenshot("showcase_02_conversa_com_vaca");
        settle(context);
        say(context, "Meu nome é Bia.");
        context.takeScreenshot("showcase_03_a_vaca_aprende_o_nome");
        settle(context);

        // mundo vivo: as vacas reparam no trigo que o jogador segura e querem um pouco (conversa entre elas, sem IA)
        clearChat(context);
        Ambient.reset();
        Ambient.setOnlyFamily("bicho_quer_comida");
        int s0 = Ambient.scenesPlayed();
        long end = System.currentTimeMillis() + 120_000;
        while (Ambient.scenesPlayed() <= s0) {
            check(System.currentTimeMillis() < end, "as vacas nao conversaram");
            context.waitTicks(10);
            Thread.sleep(100);
        }
        context.waitTicks(90);
        Ambient.setOnlyFamily("__nenhuma__");
        context.takeScreenshot("showcase_03b_vacas_querem_o_trigo");
        Thread.sleep(12000);
        cmd.runCommand("kill @e[type=minecraft:cow]");
    }

    private static void village(TestSingleplayerContext world, ClientGameTestContext context) throws InterruptedException {
        var cmd = world.getServer();
        cmd.runCommand("tp @p 0.5 -60 0.5 0 5");
        cmd.runCommand("place structure minecraft:village_plains 0 -60 28");
        context.waitTicks(100);
        cmd.runCommand("item replace entity @p weapon.mainhand with minecraft:wheat 12");
        context.waitTicks(70); // some o nome do item segurado
        // foto aerea da vila
        cmd.runCommand("effect give @p minecraft:slow_falling 40 0 true"); // flutua devagar na altura da foto aerea
        cmd.runCommand("tp @p 0.5 -44 -6 0 32");
        context.waitTicks(20);
        context.runOnClient(mc -> mc.getToastManager().clear());
        clearChat(context);
        context.takeScreenshot("showcase_00_vila_aerea");
        cmd.runCommand("effect clear @p minecraft:slow_falling");

        // nivel do chao: um fazendeiro e um bibliotecario (parados) na rua, perto do golem e dos aldeoes da propria vila
        cmd.runCommand("tp @p 0.5 -60 8.5 0 4");
        context.waitTicks(40);
        // limpa a area da cena (aldeoes e golem que a vila gerou e que andam por ai atravessariam a mira)
        cmd.runCommand("kill @e[x=0.5,y=-60,z=10,distance=..8,type=minecraft:villager]");
        cmd.runCommand("kill @e[x=0.5,y=-60,z=10,distance=..8,type=minecraft:iron_golem]");
        cmd.runCommand("kill @e[x=0.5,y=-60,z=10,distance=..8,type=minecraft:cat]");
        cmd.runCommand("summon villager -1.5 -60 11.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"v1\"],VillagerData:{profession:\"minecraft:farmer\",level:2,type:\"minecraft:plains\"},Rotation:[180f,0f]}");
        cmd.runCommand("summon villager 2.5 -60 11.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"v2\"],VillagerData:{profession:\"minecraft:librarian\",level:2,type:\"minecraft:plains\"},Rotation:[180f,0f]}");
        context.waitTicks(80);
        context.runOnClient(mc -> mc.getToastManager().clear());
        aim(world, context, "0.5 -60 8.5 34 10", "v1");
        clearChat(context);
        say(context, "Oi! Quero trocar trigo por esmeralda. Tem como?");
        context.takeScreenshot("showcase_04_negociacao_com_aldeao");
        settle(context);

        // mundo vivo: os dois aldeoes conversam sozinhos sobre quem esta na vila
        clearChat(context);
        cmd.runCommand("tp @p 0.5 -60 8.5 0 6");
        Ambient.reset();
        Ambient.setOnlyFamily("jogador_item");
        int s0 = Ambient.scenesPlayed();
        long end = System.currentTimeMillis() + 120_000;
        while (Ambient.scenesPlayed() <= s0) {
            check(System.currentTimeMillis() < end, "os aldeoes nao conversaram");
            context.waitTicks(10);
            Thread.sleep(100);
        }
        context.waitTicks(100);
        Ambient.setOnlyFamily("__nenhuma__");
        context.takeScreenshot("showcase_05_aldeoes_conversando");
        Thread.sleep(16000); // a cena termina de falar antes do proximo print
        cmd.runCommand("kill @e[type=minecraft:villager,tag=v2]");
    }

    private static void warden(TestSingleplayerContext world, ClientGameTestContext context) throws InterruptedException {
        var cmd = world.getServer();
        cmd.runCommand("summon warden 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"warden\"],Rotation:[180f,0f]}");
        world.getServer().runOnServer(s -> {
            UUID p = player(s).getUUID();
            MemoryStore.update(byTag(s, "warden"), m -> MemoryRules.firstMeeting(m, p, 0).withRelation(p,
                    r -> new br.com.mobvoz.memory.MobMemory.Relationship(1f, 1f, 0)));
        });
        aim(world, context, "0.5 -60 0.5 0 6", "warden");
        clearChat(context);
        say(context, "Warden, venha comigo. Eu cuido de você e não faço barulho.");
        context.takeScreenshot("showcase_06_warden_aceita_o_pacto");
        settle(context);
        cmd.runCommand("kill @e[type=minecraft:warden]");
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        String only = System.getProperty("mobvoz.test.only", "");
        if (!only.isEmpty() && !only.startsWith("ShowcaseGameTest")) {
            return;
        }
        String part = only.contains(":") ? only.substring(only.indexOf(':') + 1) : "";
        Ambient.reset();
        Ambient.setOnlyFamily("__nenhuma__"); // sem conversas ambiente no meio dos dialogos
        ServerConfig on = new ServerConfig();
        on.bossContractsEnabled = true;
        ServerConfig.set(on);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            for (String r : new String[] {"sendCommandFeedback false", "commandBlockOutput false", "doDaylightCycle false", "doWeatherCycle false",
                    "doMobSpawning false", "announceAdvancements false"}) {
                cmd.runCommand("gamerule " + r);
            }
            cmd.runCommand("time set 4000");
            cmd.runCommand("weather clear");
            cmd.runCommand("gamemode creative @p");
            context.runOnClient(mc -> VoiceConsent.set(false));
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            context.runOnClient(mc -> {
                try {
                    mc.getWindow().setWindowed(1280, 720);
                } catch (Throwable ignored) {
                    // fica no tamanho padrao do teste
                }
            });
            context.waitTicks(60);
            if (part.isEmpty() || part.equals("farm")) {
                farm(world, context);
            }
            if (part.isEmpty() || part.equals("warden")) {
                warden(world, context);
            }
            if (part.isEmpty() || part.equals("village")) {
                village(world, context);
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            Ambient.setOnlyFamily(null);
            ServerConfig.set(new ServerConfig());
        }
    }
}
