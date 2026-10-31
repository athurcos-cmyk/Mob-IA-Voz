package br.com.mobvoz.gametest;

import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.net.UtterancePayload;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.Orders;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

/**
 * Fase 13 em SERVIDOR DEDICADO real com um cliente conectado: sem consentimento no servidor o enunciado e ignorado
 * (mesmo forjado pelo cliente); com consentimento, a conversa por voz chega ao servidor autoritativo e a ordem e aplicada la.
 */
public class DedicatedServerGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static MobOrders ordersOf(net.minecraft.server.MinecraftServer srv, UUID id) {
        for (ServerLevel l : srv.getAllLevels()) {
            if (l.getEntity(id) instanceof Mob m) {
                return Orders.get(m);
            }
        }
        return MobOrders.NONE;
    }

    private static short[] pcm() throws IOException {
        try (InputStream in = DedicatedServerGameTest.class.getResourceAsStream("/assets/fala_seguir.pcm")) {
            byte[] b = in.readAllBytes();
            short[] s = new short[b.length / 2];
            ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(s);
            return s;
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        try (TestDedicatedServerContext server = context.worldBuilder().setUseConsistentSettings(true).createServer()) {
            try (TestServerConnection conn = server.connect()) {
                conn.getClientWorld().waitForChunksRender();
                server.runCommand("gamemode creative @p");
                server.runCommand("tp @p 0.5 -60 0.5 0 17");
                server.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
                context.waitTicks(60);
                context.takeScreenshot("mobvoz_fase13_dedicado");
                UUID id = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
                check(id != null, "a vaca do servidor dedicado nao esta na mira do cliente");
                context.runOnClient(mc -> VoiceConsent.set(false));

                // 1) sem consentimento no servidor: enunciado forjado e ignorado
                context.runOnClient(mc -> ClientPlayNetworking.send(new UtterancePayload(id, "me segue", false)));
                context.waitTicks(40);
                Thread.sleep(3000);
                context.waitTicks(10);
                MobOrders o0 = server.computeOnServer(s -> ordersOf(s, id));
                check(o0.mode() == MobOrders.Mode.NONE && VoiceInput.status() != StatusPayload.THINKING,
                        "o servidor processou um enunciado SEM consentimento: " + o0);
                System.out.println("[gametest] T30: sem opt-in no servidor dedicado -> enunciado ignorado");

                // 2) com consentimento: voz real -> STT local do cliente -> servidor dedicado aplica a ordem
                context.runOnClient(mc -> VoiceConsent.set(true));
                context.waitTicks(20);
                short[] p = pcm();
                Thread t = new Thread(() -> {
                    try {
                        for (int i = 0; i + 960 <= p.length; i += 960) {
                            short[] f = new short[960];
                            System.arraycopy(p, i, f, 0, 960);
                            VoiceInput.onFrame(f);
                            Thread.sleep(20);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                t.start();
                long end = System.currentTimeMillis() + 90_000;
                while (server.computeOnServer(s -> ordersOf(s, id).mode()) != MobOrders.Mode.FOLLOW) {
                    check(System.currentTimeMillis() < end, "a ordem nao chegou ao servidor dedicado (status " + VoiceInput.status() + ")");
                    context.waitTicks(5);
                    Thread.sleep(100);
                }
                t.join();
                System.out.println("[gametest] Fase 13: dedicado aplicou FOLLOW a partir da fala do cliente remoto");
                context.runOnClient(mc -> VoiceConsent.set(false));
                context.waitTicks(20);
            }
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(e);
        }
    }
}
