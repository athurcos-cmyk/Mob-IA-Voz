package br.com.mobvoz.gametest;

import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.client.voice.VoiceInput;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** T04/T05 em jogo real, com PCM sintetico no lugar do microfone: consentimento, alvo unico, HUD. */
public class VoiceInputGameTest implements FabricClientGameTest {
    private static short[] frame(int amp) {
        short[] f = new short[960];
        for (int i = 0; i < f.length; i++) {
            f[i] = (short) (amp * Math.sin(i * 0.2));
        }
        return f;
    }

    private static void speak(int ms) throws InterruptedException {
        for (int t = 0; t < ms; t += 20) {
            VoiceInput.onFrame(frame(8000));
            Thread.sleep(20);
        }
    }

    private static void expect(int expected, String what) {
        int got = VoiceInput.detectedCount();
        if (got != expected) {
            throw new AssertionError(what + ": esperado " + expected + " falas, obtido " + got);
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var server = world.getServer();
            server.runCommand("tp @p 0.5 -60 0.5 0 17");
            server.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b}");
            context.waitTicks(20);

            // T04: sem consentimento o audio nem e tocado.
            context.runOnClient(mc -> VoiceConsent.set(false));
            speak(1500);
            Thread.sleep(900);
            context.waitTicks(5);
            expect(0, "sem consentimento");

            // Com consentimento e mira na vaca: exatamente uma fala; HUD "Ouvindo" durante a fala.
            context.runOnClient(mc -> VoiceConsent.set(true));
            Thread talker = new Thread(() -> {
                try {
                    speak(1500);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
            talker.start();
            Thread.sleep(500);
            context.waitTicks(2);
            context.takeScreenshot("mobvoz_fase2_ouvindo");
            talker.join();
            Thread.sleep(900);
            context.waitTicks(5);
            expect(1, "com consentimento e alvo");

            // Sem ninguem na mira, a fala e ignorada.
            server.runCommand("tp @p 0.5 -60 0.5 90 17");
            context.waitTicks(10);
            speak(1500);
            Thread.sleep(900);
            context.waitTicks(5);
            expect(1, "sem alvo na mira");

            context.runOnClient(mc -> VoiceConsent.set(false)); // nao deixar o perfil de teste com voz ligada
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }
}
