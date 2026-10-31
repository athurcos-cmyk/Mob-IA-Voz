package br.com.mobvoz.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Sonda do atraso do servidor (STATUS.md). Com {@code JAVA_TOOL_OPTIONS=-Dmobvoz.lagprobe=1} vale para QUALQUER teste em jogo:
 * quando um tick passa de 400 ms, um vigia imprime {@code [lag]} com a pilha da thread do servidor (e os ticks acima de 100 ms).
 * Sozinha ({@code -Ponly=LagProbeGameTest}) e o controle: mundo ocioso, sem IA, em que a thread de teste dorme 2,5 s.
 */
public class LagProbeGameTest implements FabricClientGameTest {
    private static volatile long tickStart;
    private static volatile boolean inTick;
    private static volatile long lastEnd = System.nanoTime();
    private static volatile Thread dog;

    static {
        if (System.getProperty("mobvoz.lagprobe") != null) {
            ServerTickEvents.START_SERVER_TICK.register(s -> {
                if (dog == null) {
                    lastEnd = System.nanoTime();
                    watch(Thread.currentThread());
                }
                tickStart = System.nanoTime();
                inTick = true;
            });
            ServerTickEvents.END_SERVER_TICK.register(s -> {
                long ms = (System.nanoTime() - tickStart) / 1_000_000;
                inTick = false;
                lastEnd = System.nanoTime();
                if (ms > 100) {
                    System.out.println("[lag] tick " + s.getTickCount() + " levou " + ms + " ms");
                }
            });
            System.out.println("[lag] sonda ativa");
            ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
                if (dog != null) {
                    dog.interrupt();
                    dog = null;
                }
            });
        }
    }

    private static long gcMillis() {
        long t = 0;
        for (var b : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) {
            t += Math.max(0, b.getCollectionTime());
        }
        return t;
    }

    private static void watch(Thread serverThread) {
        dog = new Thread(() -> {
            long lastDump = 0;
            long gcPrev = gcMillis();
            while (!Thread.currentThread().isInterrupted()) {
                long t = lastEnd;
                long over = (System.nanoTime() - t) / 1_000_000;
                if (over > 400 && t != lastDump) {
                    lastDump = t;
                    long gc = gcMillis();
                    StringBuilder sb = new StringBuilder("[lag] servidor sem terminar tick ha " + over + " ms (dentro de tick=" + inTick
                            + ", GC acumulado +" + (gc - gcPrev) + " ms); pilha de " + serverThread.getName() + ":\n");
                    gcPrev = gc;
                    for (StackTraceElement e : serverThread.getStackTrace()) {
                        sb.append("    ").append(e).append('\n');
                    }
                    System.out.println(sb);
                    try {
                        java.nio.file.Files.writeString(java.nio.file.Path.of("lagprobe.txt"), sb + "\n", java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                    } catch (java.io.IOException ignored) {
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "lag-watchdog");
        dog.setDaemon(true);
        dog.start();
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (!"LagProbeGameTest".equals(System.getProperty("mobvoz.test.only"))) {
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            // controle: 20 s so com waitTicks (sem lag esperado), depois a thread de teste dorme 2,5 s (o servidor deve "atrasar" 2,5 s)
            long end = System.currentTimeMillis() + 20_000L;
            while (System.currentTimeMillis() < end) {
                context.waitTicks(20);
            }
            System.out.println("[lag] controle: dormindo 2500 ms na thread de teste");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            context.waitTicks(100);
            System.out.println("[lag] fim da sonda ociosa");
        }
    }
}
