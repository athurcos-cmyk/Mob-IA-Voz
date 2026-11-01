package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.trade.Trades;
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
 * Ponta a ponta com a IA real (modo chat): pedir ao golem algo que NAO esta na receita dele (picareta de diamante), pagar, e ouvir o causo da entrega;
 * pedir armadura a um zumbi (que a IA acha plausivel: equipamento de aventureiros mortos). Imprime as falas para conferir o tom.
 */
public class BroadTradeChatGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static void say(ClientGameTestContext c, String text) throws InterruptedException {
        ChatMode.lastSubtitle = null;
        c.runOnClient(mc -> mc.player.connection.sendChat(text));
        waitSubtitle(c, text);
    }

    private static void waitSubtitle(ClientGameTestContext c, String what) throws InterruptedException {
        long end = System.currentTimeMillis() + 90_000;
        while (ChatMode.lastSubtitle == null) {
            check(System.currentTimeMillis() < end, "sem resposta para: " + what);
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

    private static int onGround(TestSingleplayerContext w, String item) {
        return w.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30))
                .stream().filter(e -> BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString().equals(item)).mapToInt(e -> e.getItem().getCount()).sum());
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig fixed = new ServerConfig();
        fixed.dynamicPrices = false;
        ServerConfig.set(fixed);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon iron_golem 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(20);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            UUID golem = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(golem != null, "o golem nao esta na mira");

            say(context, "Golem, me vende uma picareta de diamante?");
            System.out.println("[gametest] BROADCHAT 1) golem: " + ChatMode.lastSubtitle);
            var t = world.getServer().computeOnServer(s -> Trades.get((Mob) s.overworld().getEntity(golem)));
            System.out.println("[gametest] BROADCHAT oferta: " + t.offerRecipe() + " unidades=" + t.offerUnits());
            check(t.hasOffer() && t.offerRecipe().contains("diamond_pickaxe"), "o golem deveria abrir a venda da picareta de diamante (fora da receita dele): " + t);

            ChatMode.lastSubtitle = null;
            drop(world, "minecraft:emerald", 12);
            long end = System.currentTimeMillis() + 60_000;
            while (onGround(world, "minecraft:diamond_pickaxe") == 0 && System.currentTimeMillis() < end) {
                context.waitTicks(10);
            }
            check(onGround(world, "minecraft:diamond_pickaxe") == 1, "deveria ter entregue a picareta");
            long wait = System.currentTimeMillis() + 60_000;
            while (ChatMode.lastSubtitle == null && System.currentTimeMillis() < wait) {
                context.waitTicks(10);
            }
            System.out.println("[gametest] BROADCHAT 2) golem entregando: " + ChatMode.lastSubtitle);
            Thread.sleep(6000);

            // armadura de um zumbi (equipamento de aventureiros que morreram tentando)
            cmd.runCommand("kill @e[type=!player]");
            cmd.runCommand("kill @e[type=item]");
            cmd.runCommand("summon zombie 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(30);
            UUID zb = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(zb != null, "o zumbi nao esta na mira");
            say(context, "Zumbi, me dá uma armadura de ferro boa?");
            System.out.println("[gametest] BROADCHAT 3) zumbi: " + ChatMode.lastSubtitle);
            var tz = world.getServer().computeOnServer(s -> Trades.get((Mob) s.overworld().getEntity(zb)));
            System.out.println("[gametest] BROADCHAT oferta do zumbi: " + tz.offerRecipe());
            check(tz.hasOffer() && tz.offerRecipe().contains("iron_chestplate"), "o zumbi deveria topar vender uma armadura de ferro: " + tz);

            // ameaca: a vaca (medrosa) larga o couro de medo e conta de onde veio
            cmd.runCommand("kill @e[type=!player]");
            cmd.runCommand("kill @e[type=item]");
            cmd.runCommand("summon cow 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(30);
            check(context.computeOnClient(mc -> MobVozClient.target() != null), "a vaca nao esta na mira");
            say(context, "Vaca, me dá dois couros, que eu não te mato!");
            System.out.println("[gametest] BROADCHAT 4) vaca ameacada: " + ChatMode.lastSubtitle);
            context.waitTicks(20);
            System.out.println("[gametest] BROADCHAT couros no chao: " + onGround(world, "minecraft:leather"));
            check(onGround(world, "minecraft:leather") >= 1, "a vaca assustada deveria largar o couro");
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
