package br.com.mobvoz.gametest;

import br.com.mobvoz.social.Chatter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Fase 9 em jogo real (T19/T20): dois zumbis conversam 2-3 falas no combate; sem spam; combate pesado limitado. */
public class ChatterGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static void hit(TestSingleplayerContext w, String selector) {
        w.getServer().runCommand("execute as @p run damage " + selector + " 1 minecraft:player_attack by @p");
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon zombie 0.5 -60 6.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"a\"]}");
            cmd.runCommand("summon zombie 2.5 -60 6.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"b\"]}");
            context.waitTicks(20);

            int before = Chatter.linesSpoken();
            hit(world, "@e[tag=a,limit=1]");
            long end = System.currentTimeMillis() + 60_000;
            while (Chatter.linesSpoken() - before < 2 && System.currentTimeMillis() < end) {
                context.waitTicks(10);
                Thread.sleep(100);
            }
            // espera a cena ESTABILIZAR: nenhuma fala nova por 15 s seguidos (a 3a fala pode demorar por causa do LLM/TTS)
            int stable = Chatter.linesSpoken();
            long quietSince = System.currentTimeMillis();
            while (System.currentTimeMillis() - quietSince < 15_000) {
                context.waitTicks(10);
                Thread.sleep(200);
                if (Chatter.linesSpoken() != stable) {
                    stable = Chatter.linesSpoken();
                    quietSince = System.currentTimeMillis();
                }
            }
            int lines = Chatter.linesSpoken() - before;
            System.out.println("[gametest] T19: falas da cena entre os dois zumbis = " + lines);
            check(lines >= 2 && lines <= 3, "esperadas 2-3 falas, houve " + lines);

            // spam: golpes em rajada logo depois nao geram nova cena (cooldown por regiao e por criatura)
            for (int i = 0; i < 6; i++) {
                hit(world, "@e[tag=a,limit=1]");
                hit(world, "@e[tag=b,limit=1]");
                context.waitTicks(10);
            }
            context.waitTicks(100);
            int after = Chatter.linesSpoken() - before;
            check(after == lines, "spam: houve novas falas durante o cooldown (" + lines + " -> " + after + ")");

            // T20: 30 zumbis golpeados -> teto de cenas/min
            long scenesBefore = Chatter.scheduler().scenesApproved();
            for (int i = 0; i < 30; i++) {
                cmd.runCommand("summon zombie " + (-8 + (i % 10) * 1.6) + " -60 " + (10 + (i / 10) * 3) + " {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            }
            context.waitTicks(20);
            for (int i = 0; i < 10; i++) {
                hit(world, "@e[type=zombie,limit=30,sort=random]");
                context.waitTicks(5);
            }
            context.waitTicks(60);
            long scenes = Chatter.scheduler().scenesApproved() - scenesBefore;
            System.out.println("[gametest] T20: cenas aprovadas no combate com 32 zumbis = " + scenes);
            check(scenes <= 2, "combate pesado gerou cenas demais: " + scenes);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
