package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.trade.Favors;
import java.util.List;
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
 * O caso real do dono: pediu a um esqueleto que o seguisse, ele recusou por falta de confianca, e o jogo nao reconhecia o que o jogador
 * largava no chao. Agora a recusa por confianca vira um PEDAGIO em itens que ele aceita; o pagamento pode ser aos poucos (jogou so alguns
 * agora), ele pega so o necessario, avisa o que falta, ignora o que nao serve e, pago, passa a seguir.
 */
public class FavorGameTest implements FabricClientGameTest {
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

    private static void drop(TestSingleplayerContext w, String item, int count) {
        w.getServer().runOnServer(s -> {
            ItemEntity e = new ItemEntity(s.overworld(), 0.5, -59.5, 3.5, new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item)), count));
            e.setThrower(player(s));
            e.setPickUpDelay(40);
            s.overworld().addFreshEntity(e);
        });
    }

    private static int onGround(TestSingleplayerContext w, String item) {
        return w.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30))
                .stream().filter(e -> BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString().equals(item))
                .mapToInt(e -> e.getItem().getCount()).sum());
    }

    private static boolean said(String fragment) {
        return ConversationService.announced().stream().anyMatch(l -> l.contains(fragment));
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
        ServerConfig.set(new ServerConfig());
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon skeleton 0.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(20);
            UUID sk = world.getServer().computeOnServer(s -> s.overworld().getEntities(net.minecraft.world.entity.EntityType.SKELETON, e -> true).get(0).getUUID());

            // 1) "me segue": recusa por falta de confianca -> pedagio em itens que ELE aceita (ossos, flechas, penas...), nao o que a IA inventar
            var open = world.getServer().computeOnServer(s -> {
                Mob m = mob(s, sk);
                var d = Orders.handle(m, player(s), List.of("FOLLOW_PLAYER"), false, 0).get(0);
                check(Favors.trustRefusal(d), "deveria recusar por falta de confianca: " + d);
                return Favors.open(m, player(s), "FOLLOW_PLAYER", 0.0);
            });
            System.out.println("[gametest] FAVOR pedagio: " + open);
            check(open != null && open.verdict().equals("CONDICIONAL") && open.reason().contains("16 ossos"), "pedagio em ossos: " + open);

            // 2) item que ele nao quer: ignora e diz o que aceitaria
            drop(world, "minecraft:cobblestone", 5);
            waitFor(context, 200, () -> said("Prefiro"));
            check(said("Prefiro") && onGround(world, "minecraft:cobblestone") == 5, "deveria ignorar pedregulho e dizer o que prefere: " + ConversationService.announced());

            // 3) paga so uma parte agora: ele pega, guarda e avisa o que falta; ainda NAO segue
            drop(world, "minecraft:bone", 8);
            waitFor(context, 300, () -> onGround(world, "minecraft:bone") == 0);
            check(onGround(world, "minecraft:bone") == 0, "deveria ter pego os 8 ossos do chao");
            long paid = world.getServer().computeOnServer(s -> (long) Favors.get(mob(s, sk)).paidTenths());
            check(paid == 40, "8 ossos = 4 pontos pagos: " + paid);
            check(said("Recebi 8 ossos"), "deveria avisar o que recebeu: " + ConversationService.announced());
            check(world.getServer().computeOnServer(s -> Orders.get(mob(s, sk)).mode() == MobOrders.Mode.NONE), "nao deveria seguir antes de pagar tudo");

            // 4) "daria um milhao de ossos": joga 100; ele pega SO o que falta e passa a seguir; a sobra fica com o jogador
            drop(world, "minecraft:bone", 100);
            waitFor(context, 300, () -> world.getServer().computeOnServer(s -> Orders.get(mob(s, sk)).mode() == MobOrders.Mode.FOLLOW));
            check(world.getServer().computeOnServer(s -> Orders.get(mob(s, sk)).mode() == MobOrders.Mode.FOLLOW), "pago por inteiro, deveria seguir: favor="
                    + world.getServer().computeOnServer(s -> Favors.get(mob(s, sk)).toString()) + " ossosNoChao=" + onGround(world, "minecraft:bone")
                    + " ordens=" + world.getServer().computeOnServer(s -> Orders.get(mob(s, sk)).toString()) + " falas=" + ConversationService.announced());
            check(onGround(world, "minecraft:bone") == 92, "so deveria pegar 8 dos 100 (faltavam 4 pontos): sobraram " + onGround(world, "minecraft:bone"));
            check(said("Pagou direitinho"), "deveria confirmar: " + ConversationService.announced());
            check(!world.getServer().computeOnServer(s -> Favors.get(mob(s, sk)).active()), "o pedagio deveria estar encerrado");

            // 5) quem nao confia nem um pouco nao vende o favor
            cmd.runCommand("summon skeleton 4.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"hostil\"]}");
            context.waitTicks(10);
            var none = world.getServer().computeOnServer(s -> {
                Mob m = s.overworld().getEntitiesOfClass(Mob.class, new AABB(-30, -70, -30, 30, -40, 30), e -> e.getTags().contains("hostil")).get(0);
                return Favors.open(m, player(s), "FOLLOW_PLAYER", -0.8);
            });
            check(none == null, "com desconfianca total nao deveria haver pedagio: " + none);
            check(world.getServer().computeOnServer(s -> OrderPolicy.trustNeeded("FOLLOW_PLAYER", Orders.subjectOf(mob(s, sk))) > 0), "limiar de seguir");
            System.out.println("[gametest] FAVOR ok: " + ConversationService.announced());
        }
    }
}
