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
 * Com o LLM real: so CITAR ou OFERECER um item a quem gosta dele (esqueleto Wither e ouro) NAO abre proposta de troca; dizer que quer
 * trocar abre. (Antes, "eu te dou um bloco de ouro" ou "voce me daria um bloco de ouro?" faziam a criatura propor a cabeca dela sem
 * ninguem pedir.) Confere so o ESTADO do servidor (hasOffer), que nao depende do humor do LLM.
 */
public class MentionOnlyGameTest implements FabricClientGameTest {
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
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon wither_skeleton 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(20);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            UUID ws = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(ws != null, "esqueleto Wither nao esta na mira");

            for (String frase : new String[] {"Quem criou você?", "Eu te dou um bloco de ouro.", "Você me daria um bloco de ouro?", "Aceita um bloco de ouro de presente?"}) {
                say(context, frase);
                MobTrades t = world.getServer().computeOnServer(s -> Trades.get((Mob) s.overworld().getEntity(ws)));
                System.out.println("[gametest] \"" + frase + "\" -> " + ChatMode.lastSubtitle);
                check(!t.hasOffer(), "abriu proposta sem ninguem pedir, para: \"" + frase + "\" -> " + t);
            }

            say(context, "Quer trocar? Tenho ouro.");
            MobTrades t = world.getServer().computeOnServer(s -> Trades.get((Mob) s.overworld().getEntity(ws)));
            System.out.println("[gametest] troca pedida -> " + ChatMode.lastSubtitle);
            check(t.hasOffer(), "pediu para trocar e a criatura nao abriu a negociacao: " + t);
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
