package br.com.mobvoz.gametest;

import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.server.ConversationService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Fase 4 em jogo real (sidecar + Ollama rodando): "O que voce gosta de comer?" (PCM sintetico no lugar do
 * microfone) -> STT -> IA -> TTS -> audio entregue pelo EntityAudioChannel do SVC SO a vaca da mira.
 * Depois, T09: a vaca morre no meio da fala -> sessao volta a IDLE sem erro.
 */
public class SpokenConversationGameTest implements FabricClientGameTest {
    private static short[] question() throws IOException {
        try (InputStream in = SpokenConversationGameTest.class.getResourceAsStream("/assets/pergunta_comer.pcm")) {
            byte[] b = in.readAllBytes();
            short[] s = new short[b.length / 2];
            ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(s);
            return s;
        }
    }

    /** Alimenta o "microfone" em tempo real (20 ms por frame) e termina com silencio. */
    private static Thread speakAsync(short[] pcm) {
        Thread t = new Thread(() -> {
            try {
                for (int i = 0; i + 960 <= pcm.length; i += 960) {
                    short[] f = new short[960];
                    System.arraycopy(pcm, i, f, 0, 960);
                    VoiceInput.onFrame(f);
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.start();
        return t;
    }

    private static void waitFor(ClientGameTestContext ctx, String what, int timeoutMs, java.util.function.BooleanSupplier cond,
                                Set<Integer> seen) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        while (!cond.getAsBoolean()) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError("timeout esperando: " + what + " (estados vistos: " + seen + ")");
            }
            ctx.waitTicks(2);
            seen.add(VoiceInput.status());
            Thread.sleep(50);
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        var cfg = new br.com.mobvoz.config.ServerConfig();
        cfg.ambientChatter = false; // as vacas vizinhas conversariam entre si e o teste acharia que "outra vaca falou"
        br.com.mobvoz.config.ServerConfig.set(cfg);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var server = world.getServer();
            server.runCommand("tp @p 0.5 -60 0.5 0 17");
            server.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b}");   // alvo (na mira)
            server.runCommand("summon cow -2.5 -60 3.5 {NoAI:1b,Silent:1b}");  // vizinhas silenciosas
            server.runCommand("summon cow 3.5 -60 3.5 {NoAI:1b,Silent:1b}");
            context.waitTicks(20);
            context.runOnClient(mc -> VoiceConsent.set(true));
            UUID cow = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            if (cow == null) {
                throw new AssertionError("a vaca central nao esta na mira");
            }
            short[] q = question();
            Set<Integer> seen = new LinkedHashSet<>();

            // 1) Conversa completa.
            long t0 = System.currentTimeMillis();
            Thread talker = speakAsync(q);
            waitFor(context, "pacote enviado ao servidor", 60_000, () -> VoiceInput.sentCount() == 1, seen);
            waitFor(context, "vaca falando", 60_000, () -> VoiceInput.status() == StatusPayload.SPEAKING, seen);
            context.takeScreenshot("mobvoz_fase4_vaca_falando");
            waitFor(context, "fim da fala (IDLE)", 60_000, () -> VoiceInput.status() == StatusPayload.IDLE
                    && VoiceInput.receivedFrames(cow) > 0, seen);
            talker.join();
            long total = System.currentTimeMillis() - t0;
            int frames = VoiceInput.receivedFrames(cow);
            System.out.println("[gametest] conversa completa em " + total + " ms; frames de audio recebidos da vaca-alvo: " + frames
                    + "; estados: " + seen);
            if (frames < 25) {
                throw new AssertionError("audio da vaca nao chegou ao cliente (frames=" + frames + ")");
            }
            for (UUID other : context.computeOnClient(mc -> mc.level.getEntitiesOfClass(net.minecraft.world.entity.animal.Cow.class,
                    mc.player.getBoundingBox().inflate(20)).stream().map(c -> c.getUUID()).toList())) {
                if (!other.equals(cow) && VoiceInput.receivedFrames(other) != 0) {
                    throw new AssertionError("outra vaca falou: " + other);
                }
            }
            if (ConversationService.activeSessions() != 0) {
                throw new AssertionError("sessao nao liberada");
            }

            // 2) T09: a vaca morre no meio da fala -> cancela sem travar e volta a IDLE.
            Thread.sleep(1500); // cooldown
            seen.clear();
            int before = VoiceInput.sentCount();
            Thread talker2 = speakAsync(q);
            waitFor(context, "2o pacote enviado", 60_000, () -> VoiceInput.sentCount() == before + 1, seen);
            waitFor(context, "vaca falando (2)", 60_000, () -> VoiceInput.status() == StatusPayload.SPEAKING, seen);
            server.runCommand("kill " + cow);
            waitFor(context, "IDLE apos a morte da vaca", 20_000, () -> VoiceInput.status() == StatusPayload.IDLE, seen);
            talker2.join();
            context.waitTicks(20);
            if (ConversationService.activeSessions() != 0) {
                throw new AssertionError("sessao vazou apos a morte da vaca");
            }
            System.out.println("[gametest] T09 ok: fala cancelada, sessoes ativas = 0");
            context.runOnClient(mc -> VoiceConsent.set(false));
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(e);
        } finally {
            br.com.mobvoz.config.ServerConfig.set(new br.com.mobvoz.config.ServerConfig());
        }
    }
}
