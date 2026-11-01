package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.trade.MobTrades;
import br.com.mobvoz.trade.Trades;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Mob;

/**
 * Ponta a ponta com o LLM real, pelo modo chat: a frase do jogador ("preciso de 3 cabecas Wither, te dou 3 ferros e 1 picareta de
 * diamante") abre a negociacao no servidor; um argumento forte convence a criatura a aceitar ferro (com limite). Confere o ESTADO
 * do servidor e que a criatura respondeu (legenda).
 */
public class NegotiationChatGameTest implements FabricClientGameTest {
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
        long idle = System.currentTimeMillis() + 60_000; // espera a criatura TERMINAR de falar (senao ela esta "ocupada", de proposito)
        while (br.com.mobvoz.client.voice.VoiceInput.status() != br.com.mobvoz.net.StatusPayload.IDLE) {
            check(System.currentTimeMillis() < idle, "a criatura nao terminou de falar");
            c.waitTicks(5);
            Thread.sleep(100);
        }
        Thread.sleep(1600); // cooldown do jogador
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig fixed = new ServerConfig();
        fixed.dynamicPrices = false; // estes testes contam quantidades exatas: preco-base fixo
        ServerConfig.set(fixed);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon wither_skeleton 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(20);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            UUID ws = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(ws != null, "esqueleto Wither nao esta na mira");

            say(context, "Ei esqueleto Wither, eu preciso de 3 cabeças Wither e te dou 3 ferros e 1 picareta de diamante.");
            MobTrades t1 = world.getServer().computeOnServer(s -> Trades.get((Mob) s.overworld().getEntity(ws)));
            System.out.println("[gametest] 1) legenda: " + ChatMode.lastSubtitle);
            check(t1.hasOffer() && t1.offerRecipe().equals("wither_skeleton_wither_skeleton_skull_v2"), "a negociacao nao abriu no servidor: " + t1);
            check(t1.persuasions() == 0, "ninguem convenceu ninguem ainda"); // persuasions: so argumento do jogador; o mapa persuaded tambem recebe o que o juiz de IA acha que ela aceitaria

            say(context, "Com esses ferros você forja espadas e fica muito mais poderoso, aceita!");
            MobTrades t2 = world.getServer().computeOnServer(s -> Trades.get((Mob) s.overworld().getEntity(ws)));
            System.out.println("[gametest] 2) legenda: " + ChatMode.lastSubtitle + " | persuadido=" + t2.persuaded());
            check(t2.persuasions() >= 1 && t2.persuadedMap().getOrDefault("minecraft:iron_ingot", 0) > 0, "o argumento nao mudou o interesse: " + t2);
            check(t2.persuadedMap().get("minecraft:iron_ingot") <= 800, "teto de convencimento violado");
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
