package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.trade.Favors;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Ponta a ponta com a IA real (modo chat), o caso do dono: "Esqueleto, me segue!" -> ele recusa por falta de confianca e COBRA em itens que
 * aceita (o servidor abre o pedagio, a IA so repete o preco); o jogador joga alguns agora, o resto depois; pago, ele passa a seguir.
 */
public class FavorChatGameTest implements FabricClientGameTest {
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

    private static void drop(TestSingleplayerContext w, String item, int count) {
        w.getServer().runOnServer(s -> {
            ItemEntity e = new ItemEntity(s.overworld(), 0.5, -59.5, 2.5, new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item)), count));
            e.setThrower(s.getPlayerList().getPlayers().get(0));
            e.setPickUpDelay(40);
            s.overworld().addFreshEntity(e);
        });
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
            cmd.runCommand("difficulty normal");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon skeleton 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(20);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            UUID sk = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(sk != null, "o esqueleto nao esta na mira");

            say(context, "Esqueleto, me segue!");
            System.out.println("[gametest] FAVORCHAT 1) legenda: " + ChatMode.lastSubtitle);
            var f = world.getServer().computeOnServer(s -> Favors.get((Mob) s.overworld().getEntity(sk)));
            check(f.active() && f.action().equals("FOLLOW_PLAYER"), "a recusa deveria abrir um pedagio: " + f);
            double price = f.price();
            int bones = (int) Math.ceil(price / 0.5);
            System.out.println("[gametest] FAVORCHAT preco=" + price + " pontos = " + bones + " ossos");

            say(context, "Jogo uns ossos agora e o resto depois, pode ser?");
            System.out.println("[gametest] FAVORCHAT 2) legenda: " + ChatMode.lastSubtitle);

            drop(world, "minecraft:bone", bones / 2);
            long end = System.currentTimeMillis() + 20_000;
            while (System.currentTimeMillis() < end && world.getServer().computeOnServer(s -> Favors.get((Mob) s.overworld().getEntity(sk)).paidTenths()) == 0) {
                context.waitTicks(10);
            }
            int paid1 = world.getServer().computeOnServer(s -> Favors.get((Mob) s.overworld().getEntity(sk)).paidTenths());
            System.out.println("[gametest] FAVORCHAT pago na 1a parte: " + paid1 + " decimos");
            check(paid1 > 0, "ele deveria ter pego os ossos do chao");
            check(world.getServer().computeOnServer(s -> Orders.get((Mob) s.overworld().getEntity(sk)).mode() == MobOrders.Mode.NONE), "ainda nao deveria seguir");

            drop(world, "minecraft:bone", bones);
            end = System.currentTimeMillis() + 20_000;
            while (System.currentTimeMillis() < end && world.getServer().computeOnServer(s -> Orders.get((Mob) s.overworld().getEntity(sk)).mode() != MobOrders.Mode.FOLLOW)) {
                context.waitTicks(10);
            }
            check(world.getServer().computeOnServer(s -> Orders.get((Mob) s.overworld().getEntity(sk)).mode() == MobOrders.Mode.FOLLOW), "pago o total, deveria seguir");
            int left = world.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30)).stream()
                    .mapToInt(e -> e.getItem().getCount()).sum());
            System.out.println("[gametest] FAVORCHAT sobra no chao: " + left + " (jogou " + (bones / 2 + bones) + ", preco " + bones + ")");
            check(left > 0, "a sobra do pagamento deveria ficar com o jogador");
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
