package br.com.mobvoz.gametest;

import br.com.mobvoz.client.MobVozClient;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** T02/T03 em jogo real: 10 vacas, mirar uma -> 1 alvo; desviar -> nenhum. */
public class TargetStarGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var server = world.getServer();
            server.runCommand("gamemode creative @p");
            server.runCommand("tp @p 0.5 -60 0.5 0 17");
            // 10 vacas paradas em fila lateral a 3 blocos; a do meio (x=0.5) fica na mira.
            for (int i = 0; i < 10; i++) {
                server.runCommand("summon cow " + (-3.5 + i) + " -60 3.5 {NoAI:1b,Silent:1b}");
            }
            context.waitTicks(20);
            context.takeScreenshot("mobvoz_fase1_mira_em_uma_vaca");
            assertEquals(1, context.computeOnClient(mc -> MobVozClient.target() == null ? 0 : 1));

            server.runCommand("tp @p 0.5 -60 0.5 90 17");
            context.waitTicks(10);
            context.takeScreenshot("mobvoz_fase1_mira_desviada");
            assertEquals(0, context.computeOnClient(mc -> MobVozClient.target() == null ? 0 : 1));

            server.runCommand("tp @p 0.5 -60 0.5 0 17");
            context.waitTicks(10);
            server.runCommand("tp @p 0.5 -60 -4.5 0 17");
            context.waitTicks(10); // 8 blocos: fora do alcance de 6
            assertEquals(0, context.computeOnClient(mc -> MobVozClient.target() == null ? 0 : 1));
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("esperado " + expected + " alvo(s), obtido " + actual);
        }
    }
}
