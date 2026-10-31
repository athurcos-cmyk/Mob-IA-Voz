package br.com.mobvoz.gametest;

import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.Orders;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * Fase 6 em jogo real: executor de ordens. Parte A (deterministica): seguir de verdade, ficar, teleporte seguro,
 * PORTAL_HOLD (com controle sem a ordem, que ATRAVESSA), persistencia em save/load. Parte B (fala + LLM): "me segue" a uma
 * vaca vira ordem real; o mesmo pedido ao porco teimoso e recusado sem alterar o mundo (T11, T43).
 */
public class OrdersGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static ServerPlayer player(MinecraftServer s) {
        return s.getPlayerList().getPlayers().get(0);
    }

    private static Mob mob(MinecraftServer s, UUID id) {
        for (ServerLevel l : s.getAllLevels()) {
            if (l.getEntity(id) instanceof Mob m) {
                return m;
            }
        }
        return null;
    }

    private static UUID summon(TestSingleplayerContext w, ClientGameTestContext c, String type, double x, double y, double z) {
        return w.getServer().computeOnServer(s -> {
            ServerLevel l = s.overworld();
            var e = EntityType.byString(type).orElseThrow().create(l, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            e.setPos(x, y, z);
            ((Mob) e).setPersistenceRequired();
            l.addFreshEntity(e);
            return e.getUUID();
        });
    }

    private static short[] pcm(String name) throws IOException {
        try (InputStream in = OrdersGameTest.class.getResourceAsStream("/assets/" + name)) {
            byte[] b = in.readAllBytes();
            short[] s = new short[b.length / 2];
            ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(s);
            return s;
        }
    }

    private static void speak(short[] pcm) throws InterruptedException {
        for (int i = 0; i + 960 <= pcm.length; i += 960) {
            short[] f = new short[960];
            System.arraycopy(pcm, i, f, 0, 960);
            VoiceInput.onFrame(f);
            Thread.sleep(20);
        }
    }

    private static void converse(ClientGameTestContext ctx, String file) throws InterruptedException, IOException {
        int before = VoiceInput.sentCount();
        Thread t = new Thread(() -> {
            try {
                speak(pcm(file));
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        long end = System.currentTimeMillis() + 90_000;
        boolean sawReply = false;
        while (true) {
            check(System.currentTimeMillis() < end, "timeout na conversa (status " + VoiceInput.status() + ")");
            ctx.waitTicks(4);
            Thread.sleep(100);
            if (VoiceInput.status() == StatusPayload.SPEAKING || VoiceInput.status() == StatusPayload.ERROR) {
                sawReply = true;
            }
            if (sawReply && VoiceInput.status() == StatusPayload.IDLE && VoiceInput.sentCount() > before) {
                break;
            }
        }
        t.join();
    }

    private static void wait(ClientGameTestContext c, int ticks) {
        c.waitTicks(ticks);
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create();
        UUID held;
        MobOrders heldBefore;
        try {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            UUID cow = summon(world, context, "minecraft:cow", 0.5, -60, 6.5);
            wait(context, 20);

            // ---- A1: seguir de verdade (anda ate o jogador, e teleporta se ficar muito longe) ----
            var d1 = world.getServer().computeOnServer(s -> Orders.handle(mob(s, cow), player(s), List.of("FOLLOW_PLAYER"), false));
            check(d1.get(0).accepted(), "vaca deveria aceitar seguir: " + d1);
            cmd.runCommand("tp @p 0.5 -60 25.5");
            wait(context, 300);
            double dist = world.getServer().computeOnServer(s -> (double) mob(s, cow).distanceTo(player(s)));
            System.out.println("[gametest] A1 seguir: distancia vaca-jogador apos 15 s = " + dist);
            check(dist < 8, "a vaca nao seguiu (dist " + dist + ")");
            cmd.runCommand("tp @p 90.5 -60 90.5");
            wait(context, 200);
            double far = world.getServer().computeOnServer(s -> (double) mob(s, cow).distanceTo(player(s)));
            System.out.println("[gametest] A1 teleporte seguro: distancia apos o jogador ir a 90 blocos = " + far);
            check(far < 12, "a vaca nao alcancou o jogador distante (dist " + far + ")");

            // ---- A2: ficar (nao segue mais; fica perto do ponto) ----
            cmd.runCommand("tp @p 0.5 -60 0.5");
            wait(context, 40);
            world.getServer().runOnServer(s -> Orders.handle(mob(s, cow), player(s), List.of("DISMISS_ALLY"), false));
            var d2 = world.getServer().computeOnServer(s -> Orders.handle(mob(s, cow), player(s), List.of("STAY_AT", "PORTAL_HOLD"), false));
            check(d2.size() == 2 && d2.stream().allMatch(OrderPolicy.Decision::accepted), "ficar+portal deveriam ser aceitos: " + d2);
            double[] anchor = world.getServer().computeOnServer(s -> new double[] {mob(s, cow).getX(), mob(s, cow).getZ()});
            cmd.runCommand("tp @p " + (anchor[0] + 30) + " -60 " + (anchor[1]));
            wait(context, 300);
            double drift = world.getServer().computeOnServer(s -> Math.hypot(mob(s, cow).getX() - anchor[0], mob(s, cow).getZ() - anchor[1]));
            System.out.println("[gametest] A2 ficar: deslocamento do ponto = " + drift);
            check(drift < 5, "a vaca saiu do ponto (" + drift + ")");

            // ---- A3: PORTAL_HOLD x controle sem ordem ----
            cmd.runCommand("tp @p 12.5 -60 5.5");
            cmd.runCommand("fill 10 -60 10 13 -56 10 minecraft:obsidian");
            cmd.runCommand("fill 11 -59 10 12 -57 10 minecraft:nether_portal[axis=x]");
            UUID control = summon(world, context, "minecraft:cow", 12.5, -59, 10.5);
            world.getServer().runOnServer(s -> {
                Mob m = mob(s, cow);
                m.teleportTo(11.5, -59, 10.5);
            });
            wait(context, 120);
            boolean controlGone = world.getServer().computeOnServer(s -> s.overworld().getEntity(control) == null);
            boolean heldStays = world.getServer().computeOnServer(s -> s.overworld().getEntity(cow) != null);
            System.out.println("[gametest] A3 portal: controle atravessou=" + controlGone + ", vaca com PORTAL_HOLD ficou=" + heldStays);
            check(heldStays, "a vaca com PORTAL_HOLD atravessou o portal!");
            check(controlGone, "controle inconclusivo: a vaca sem ordem nao atravessou o portal");
            held = cow;
            heldBefore = world.getServer().computeOnServer(s -> Orders.get(mob(s, cow)));
            check(heldBefore.mode() == MobOrders.Mode.STAY && heldBefore.portalHold(), "ordens inesperadas: " + heldBefore);
        } catch (RuntimeException e) {
            throw e;
        }

        // ---- A4: salvar -> sair -> carregar: ordens persistem ----
        var save = world.getWorldSave();
        world.close();
        context.waitTicks(20);
        try (TestSingleplayerContext reloaded = save.open()) {
            reloaded.getClientWorld().waitForChunksRender();
            context.waitTicks(40);
            MobOrders after = reloaded.getServer().computeOnServer(s -> Orders.get(mob(s, held)));
            check(after.withOwnerSeen(heldBefore.ownerLastSeen()).equals(heldBefore), "ordens mudaram ao recarregar:\n" + heldBefore + "\n" + after);
            check(reloaded.getServer().computeOnServer(s -> Orders.trackedCount()) >= 1, "ordem nao foi re-registrada no ticker");
            System.out.println("[gametest] A4 persistencia ok: " + after);

            // ---- B: por voz (STT + analisador LLM + politica do servidor + fala) ----
            reloaded.getServer().runCommand("gamemode creative @p");
            reloaded.getServer().runCommand("kill @e[type=!player]");
            reloaded.getServer().runCommand("tp @p 0.5 -60 0.5 0 17");
            UUID cow2 = summon(reloaded, context, "minecraft:cow", 0.5, -60, 3.5);
            reloaded.getServer().runOnServer(s -> mob(s, cow2).setNoAi(true)); // parada na mira durante STT+IA (~20 s)
            context.waitTicks(20);
            context.runOnClient(mc -> VoiceConsent.set(true));
            check(context.computeOnClient(mc -> MobVozClient.target() != null), "vaca nao esta na mira");
            converse(context, "fala_seguir.pcm");
            MobOrders o = reloaded.getServer().computeOnServer(s -> Orders.get(mob(s, cow2)));
            System.out.println("[gametest] B vaca apos 'Me segue até a minha casa.': " + o.mode());
            check(o.mode() == MobOrders.Mode.FOLLOW, "a fala 'me segue' nao virou ordem real (modo " + o.mode() + ")");

            Thread.sleep(1500);
            reloaded.getServer().runCommand("kill @e[type=!player]");
            UUID pig = summon(reloaded, context, "minecraft:pig", 0.5, -60, 3.5);
            reloaded.getServer().runOnServer(s -> mob(s, pig).setNoAi(true));
            context.waitTicks(20);
            converse(context, "fala_seguir.pcm");
            MobOrders po = reloaded.getServer().computeOnServer(s -> Orders.get(mob(s, pig)));
            System.out.println("[gametest] B porco apos o mesmo pedido: " + po.mode());
            check(po.mode() == MobOrders.Mode.NONE, "o porco teimoso deveria recusar sem incentivo");
            context.runOnClient(mc -> VoiceConsent.set(false));
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(e);
        }
    }
}
