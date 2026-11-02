package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.social.Gifts;
import br.com.mobvoz.trade.Trades;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Presente jogado no chao (o caso do dono: ovo para a vaca, sem SHIFT). Olhando para a criatura e jogando o item de verdade (Q), ela vem pegar 1 e reage como no
 * SHIFT; olhando para outro lado ninguem pega; item jogado para PAGAR uma troca aberta nao vira presente.
 */
public class GroundGiftGameTest implements FabricClientGameTest {
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

    /** Joga o item como o jogador faz com a tecla Q (mesma funcao do jogo). */
    private static void throwItem(TestSingleplayerContext w, String item, int count) {
        w.getServer().runOnServer(s -> player(s).drop(new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item)), count), false, true)); // retainOwnership = true: igual a tecla Q
    }

    private static int onGround(TestSingleplayerContext w, String item) {
        return w.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30))
                .stream().filter(e -> BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString().equals(item)).mapToInt(e -> e.getItem().getCount()).sum());
    }

    private static long gifts(TestSingleplayerContext w, UUID mobId) {
        return w.getServer().computeOnServer(s -> MemoryStore.get(mob(s, mobId)).facts().stream().filter(f -> f.type().equals("GIFT")).count());
    }

    private static void waitFor(ClientGameTestContext c, int maxTicks, java.util.function.BooleanSupplier cond) {
        for (int i = 0; i < maxTicks / 10 && !cond.getAsBoolean(); i++) {
            c.waitTicks(10);
        }
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
            cmd.runCommand("time set day");
            cmd.runCommand("weather clear");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon cow 0.5 -60 3.5 {PersistenceRequired:1b,Tags:[\"vaca\"]}"); // com IA: ela anda ate o item
            context.waitTicks(30);
            UUID cow = world.getServer().computeOnServer(s -> s.overworld().getEntities(net.minecraft.world.entity.EntityType.COW, e -> true).get(0).getUUID());

            // 1) o caso do dono: ovo jogado olhando para a vaca -> ela pega 1 e reage (presente)
            check(gifts(world, cow) == 0, "sem presentes no comeco");
            throwItem(world, "minecraft:egg", 3);
            context.waitTicks(5);
            float d0 = world.getServer().computeOnServer(s -> mob(s, cow).distanceTo(player(s)));
            System.out.println("[gametest] GROUNDGIFT pendentes logo apos jogar: " + Gifts.pendingCount() + " | vaca a " + d0 + " blocos");
            waitFor(context, 300, () -> gifts(world, cow) > 0);
            String eggDist = world.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30)).stream().findFirst()
                    .map(e -> "" + e.distanceTo(mob(s, cow))).orElse("sem ovo"));
            System.out.println("[gametest] GROUNDGIFT depois de esperar: pendentes " + Gifts.pendingCount() + " | distancia vaca-ovo " + eggDist);
            System.out.println("[gametest] GROUNDGIFT presentes da vaca: " + gifts(world, cow) + " | ovos no chao: " + onGround(world, "minecraft:egg"));
            check(gifts(world, cow) == 1, "a vaca deveria ter recebido o ovo como presente");
            check(onGround(world, "minecraft:egg") == 2, "ela pega UM, os outros dois ficam no chao: " + onGround(world, "minecraft:egg"));

            // 2) olhando para o outro lado: ninguem pega (nao era para ela)
            cmd.runCommand("kill @e[type=item]");
            cmd.runCommand("tp @p 0.5 -60 0.5 120 17");
            context.waitTicks(10);
            throwItem(world, "minecraft:egg", 1);
            context.waitTicks(160);
            check(gifts(world, cow) == 1 && onGround(world, "minecraft:egg") == 1, "jogado para outro lado nao e presente: " + gifts(world, cow));

            // 3) pagamento de uma troca aberta nao vira presente
            cmd.runCommand("kill @e[type=item]");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("tp @e[tag=vaca] 0.5 -60 3.5");
            context.waitTicks(10);
            var open = world.getServer().computeOnServer(s -> Trades.open(mob(s, cow), player(s), 0, "", 0));
            check(open.verdict().equals("ACEITO"), "a vaca deveria abrir a troca: " + open);
            throwItem(world, "minecraft:wheat", 2);
            context.waitTicks(60);
            check(Gifts.pendingCount() == 0, "item de pagamento nao entra como presente pendente");
            check(gifts(world, cow) == 1, "o pagamento nao conta como presente");
        }
    }
}
