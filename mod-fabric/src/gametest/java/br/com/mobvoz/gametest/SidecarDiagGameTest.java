package br.com.mobvoz.gametest;

import br.com.mobvoz.client.voice.VoiceUi;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Fase 3 em jogo real: texto de diagnostico -> sidecar -> Ollama -> resposta pt-BR no chat, sem travar o jogo.
 * Com -Dmobvoz.test.expect=offline espera o caminho de falha (sidecar desligado, T06).
 */
public class SidecarDiagGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        boolean expectOffline = "offline".equals(System.getProperty("mobvoz.test.expect"));
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var server = world.getServer();
            server.runCommand("tp @p 0.5 -60 0.5 0 17");
            server.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b}");
            context.waitTicks(20);
            VoiceUi.lastDiagReply = null;
            context.runOnClient(mc -> mc.player.connection.sendCommand("mobvoz diag falar O que voce gosta de comer?"));
            long ticksBefore = context.computeOnClient(mc -> mc.level.getGameTime());
            for (int i = 0; i < 120 && VoiceUi.lastDiagReply == null; i++) {
                context.waitTicks(5);
                Thread.sleep(250);
            }
            String reply = VoiceUi.lastDiagReply;
            long ticksAfter = context.computeOnClient(mc -> mc.level.getGameTime());
            System.out.println("[gametest] resposta diag: " + reply + " (ticks decorridos: " + (ticksAfter - ticksBefore) + ")");
            context.takeScreenshot(expectOffline ? "mobvoz_fase3_sidecar_offline" : "mobvoz_fase3_resposta");
            if (reply == null) {
                throw new AssertionError("nenhuma resposta (nem erro) em 30 s: o jogo travou ou o comando falhou");
            }
            if (expectOffline != reply.contains("offline")) {
                throw new AssertionError("esperado offline=" + expectOffline + " mas veio: " + reply);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }
}
