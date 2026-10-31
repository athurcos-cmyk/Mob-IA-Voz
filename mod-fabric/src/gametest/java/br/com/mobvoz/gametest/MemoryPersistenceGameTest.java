package br.com.mobvoz.gametest;

import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.net.StatusPayload;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Items;

/**
 * Fase 5 em jogo real (T10/T33/T35): presente (SHIFT+clique) vira FATO; "sou seu pai" falado vira ALEGACAO com crenca
 * (nunca fato); salvar -> sair -> carregar preserva identidade, fato e alegacao e nao duplica nada.
 */
public class MemoryPersistenceGameTest implements FabricClientGameTest {
    private static short[] pcm(String name) throws IOException {
        try (InputStream in = MemoryPersistenceGameTest.class.getResourceAsStream("/assets/" + name)) {
            byte[] b = in.readAllBytes();
            short[] s = new short[b.length / 2];
            ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(s);
            return s;
        }
    }

    private static Thread speak(short[] pcm) {
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

    private static MobMemory memoryOf(TestSingleplayerContext w, UUID mob) {
        return w.getServer().computeOnServer(server -> {
            for (var level : server.getAllLevels()) {
                if (level.getEntity(mob) instanceof net.minecraft.world.entity.Mob m) {
                    return MemoryStore.get(m);
                }
            }
            throw new AssertionError("mob nao encontrado no servidor: " + mob);
        });
    }

    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        UUID chicken;
        MobMemory before;
        TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create();
        try {
            world.getClientWorld().waitForChunksRender();
            var server = world.getServer();
            server.runCommand("gamemode survival @p");
            server.runCommand("give @p minecraft:wheat 5");
            server.runCommand("tp @p 0.5 -60 0.5 0 17");
            server.runCommand("summon chicken 0.5 -60 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(20);
            chicken = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(chicken != null && context.computeOnClient(mc -> MobVozClient.target() instanceof Chicken), "galinha nao esta na mira");

            // 1) Presente real: SHIFT + botao direito com trigo na mao.
            context.getInput().holdKey(o -> o.keyShift);
            context.waitTicks(3);
            context.runOnClient(mc -> mc.gameMode.interact(mc.player, MobVozClient.target(), InteractionHand.MAIN_HAND));
            context.waitTicks(5);
            context.getInput().releaseKey(o -> o.keyShift);
            int wheat = context.computeOnClient(mc -> mc.player.getInventory().countItem(Items.WHEAT));
            MobMemory m1 = memoryOf(world, chicken);
            System.out.println("[gametest] apos presente: trigo=" + wheat + " fatos=" + m1.facts());
            check(wheat == 4, "o presente deveria consumir exatamente 1 trigo (restou " + wheat + ")");
            check(m1.counts().getOrDefault("GIFT", 0) == 1, "fato GIFT nao registrado");

            // 2) "Eu sou seu pai": vira alegacao com crenca; NAO vira fato.
            context.runOnClient(mc -> VoiceConsent.set(true));
            Thread.sleep(1200);
            int sentBefore = VoiceInput.sentCount();
            Thread t = speak(pcm("fala_pai.pcm"));
            long end = System.currentTimeMillis() + 90_000;
            while (!(VoiceInput.sentCount() > sentBefore && VoiceInput.status() == StatusPayload.IDLE
                    && memoryOf(world, chicken).claims().size() > 0)) {
                check(System.currentTimeMillis() < end, "timeout: alegacao nao registrada (status " + VoiceInput.status() + ")");
                context.waitTicks(4);
                Thread.sleep(100);
            }
            t.join();
            before = memoryOf(world, chicken);
            System.out.println("[gametest] apos fala: alegacoes=" + before.claims() + " fatos=" + before.facts().size()
                    + " relacao=" + before.relationOf(context.computeOnClient(mc -> mc.player.getUUID())));
            check(before.claims().size() == 1, "esperada 1 alegacao");
            check(before.facts().stream().noneMatch(f -> f.detail().toLowerCase().contains("pai")), "alegacao virou fato!");
            check(before.counts().getOrDefault("GIFT", 0) == 1 && before.counts().getOrDefault("FIRST_MET", 0) == 1,
                    "fatos alterados pela fala: " + before.counts());
            context.runOnClient(mc -> VoiceConsent.set(false));
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(e);
        }

        // 3) Salvar, sair e carregar.
        var save = world.getWorldSave();
        world.close();
        context.waitTicks(20);
        try (TestSingleplayerContext reloaded = save.open()) {
            reloaded.getClientWorld().waitForChunksRender();
            context.waitTicks(20);
            MobMemory after = memoryOf(reloaded, chicken);
            System.out.println("[gametest] apos recarregar: " + after);
            check(after.equals(before), "memoria mudou ao recarregar!\nantes=" + before + "\ndepois=" + after);
            long chickens = reloaded.getServer().computeOnServer(server -> server.getAllLevels().iterator().next()
                    .getEntities(net.minecraft.world.entity.EntityType.CHICKEN, e -> true).size());
            check(chickens == 1, "galinha duplicada/perdida ao recarregar: " + chickens);
            System.out.println("[gametest] T10/T33 ok: identidade, fato e alegacao preservados; 1 galinha");
        }
    }
}
