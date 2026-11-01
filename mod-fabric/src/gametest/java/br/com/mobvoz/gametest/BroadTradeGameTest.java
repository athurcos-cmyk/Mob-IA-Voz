package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.social.Reactions.Reaction;
import br.com.mobvoz.trade.Extortion;
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
 * Trocas amplas: qualquer item do jogo pode ser pedido a qualquer criatura que o julgue plausivel (a IA diz YES/MAYBE; o servidor poe o preco pela
 * raridade, baixo), paga-se com moedas que todos aceitam, nunca com o mesmo material do que ela da (ferro por ferro), e ameacar faz a medrosa
 * ceder o que ela teria, a valente so se estiver em desvantagem.
 */
public class BroadTradeGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static ServerPlayer player(MinecraftServer s) {
        return s.getPlayerList().getPlayers().get(0);
    }

    private static Mob byTag(MinecraftServer s, String tag) {
        for (ServerLevel l : s.getAllLevels()) {
            for (Mob m : l.getEntitiesOfClass(Mob.class, new AABB(-60, -70, -60, 60, 40, 60), x -> x.getTags().contains(tag))) {
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
                .stream().filter(e -> BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString().equals(item)).mapToInt(e -> e.getItem().getCount()).sum());
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
            cmd.runCommand("difficulty normal");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon iron_golem 0.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"golem\"]}");
            cmd.runCommand("summon cow 4.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
            cmd.runCommand("summon zombie 8.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"zumbi\"]}");
            cmd.runCommand("summon warden -4.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"warden\"]}");
            context.waitTicks(30);

            // 1) item FORA da receita do golem (picareta de diamante): a IA disse YES, o servidor poe o preco pela raridade (baixo) e aceita moedas
            var open = world.getServer().computeOnServer(s -> Trades.open(byTag(s, "golem"), player(s), 0, "minecraft:diamond_pickaxe", 1, "YES"));
            System.out.println("[gametest] BROAD picareta de diamante do golem: " + open);
            check(open.verdict().equals("ACEITO") && open.reason().contains("picareta de diamante"), "o golem deveria topar vender a picareta: " + open);
            check(open.reason().contains("esmeralda"), "esmeralda e moeda para todos: " + open.reason());
            drop(world, "minecraft:emerald", 8);
            waitFor(context, 400, () -> onGround(world, "minecraft:diamond_pickaxe") > 0);
            check(onGround(world, "minecraft:diamond_pickaxe") == 1, "deveria ter entregue a picareta de diamante");
            System.out.println("[gametest] BROAD esmeraldas devolvidas (sobra): " + onGround(world, "minecraft:emerald"));
            check(onGround(world, "minecraft:emerald") >= 1, "a sobra das esmeraldas deveria voltar");

            // 2) ninguem paga ferro para ganhar ferro: peitoral de ferro do golem, pagando em barras de ferro NAO fecha; em esmeraldas fecha
            cmd.runCommand("kill @e[type=item]");
            context.waitTicks(10);
            var open2 = world.getServer().computeOnServer(s -> Trades.open(byTag(s, "golem"), player(s), 0, "minecraft:iron_chestplate", 1, ""));
            System.out.println("[gametest] BROAD peitoral de ferro do golem: " + open2.reason());
            check(open2.verdict().equals("ACEITO") && !open2.reason().contains("barras de ferro") , "nao se paga ferro para ganhar ferro: " + open2.reason());
            drop(world, "minecraft:iron_ingot", 12);
            context.waitTicks(120);
            check(onGround(world, "minecraft:iron_chestplate") == 0 && onGround(world, "minecraft:iron_ingot") == 12, "barras de ferro nao pagam o peitoral de ferro");
            cmd.runCommand("kill @e[type=item]");
            drop(world, "minecraft:emerald", 6);
            waitFor(context, 400, () -> onGround(world, "minecraft:iron_chestplate") > 0);
            check(onGround(world, "minecraft:iron_chestplate") == 1, "esmeraldas pagam o peitoral de ferro");

            // 3) itens baratos saem em quantidade: 10 farinhas de osso do golem saem por uns centavos de esmeralda
            cmd.runCommand("kill @e[type=item]");
            var open3 = world.getServer().computeOnServer(s -> Trades.open(byTag(s, "golem"), player(s), 0, "minecraft:bone_meal", 10, "YES"));
            System.out.println("[gametest] BROAD 10 farinhas de osso: " + open3.reason());
            check(open3.verdict().equals("ACEITO"), "farinha de osso e barata: " + open3);

            // 4) ameaca: a vaca (medrosa) cede o couro; o zumbi (valente) nao cede sem estar em desvantagem; com espada na mao, cede; o Warden nunca
            cmd.runCommand("kill @e[type=item]");
            var cow = world.getServer().computeOnServer(s -> Extortion.demand(byTag(s, "vaca"), player(s), "minecraft:leather", 2, "YES", Reaction.FLEE));
            System.out.println("[gametest] BROAD ameaca a vaca: " + cow);
            check(cow.get("resultado").equals("ENTREGOU"), "a vaca assustada deveria ceder: " + cow);
            context.waitTicks(10);
            check(onGround(world, "minecraft:leather") == 2, "deveria largar 2 couros");
            var z1 = world.getServer().computeOnServer(s -> Extortion.demand(byTag(s, "zumbi"), player(s), "minecraft:rotten_flesh", 1, "YES", Reaction.FIGHT));
            System.out.println("[gametest] BROAD ameaca o zumbi sem espada: " + z1);
            check(z1.get("resultado").equals("RECUSOU"), "o zumbi valente nao cede a quem nao parece perigoso: " + z1);
            cmd.runCommand("item replace entity @p weapon.mainhand with minecraft:iron_sword");
            context.waitTicks(5);
            var z2 = world.getServer().computeOnServer(s -> Extortion.demand(byTag(s, "zumbi"), player(s), "minecraft:rotten_flesh", 1, "YES", Reaction.FIGHT));
            System.out.println("[gametest] BROAD ameaca o zumbi com espada: " + z2);
            check(z2.get("resultado").equals("ENTREGOU"), "com espada na mao o zumbi cede: " + z2);
            var w = world.getServer().computeOnServer(s -> Extortion.demand(byTag(s, "warden"), player(s), "minecraft:amethyst_shard", 1, "YES", Reaction.FIGHT));
            check(w.get("resultado").equals("RECUSOU"), "o Warden nunca cede de medo: " + w);
            var nothing = world.getServer().computeOnServer(s -> Extortion.demand(byTag(s, "vaca"), player(s), "minecraft:elytra", 1, "YES", Reaction.FLEE));
            check(nothing.get("resultado").equals("RECUSOU"), "elytra vale demais para ceder de medo: " + nothing);
            var absurd = world.getServer().computeOnServer(s -> Extortion.demand(byTag(s, "vaca"), player(s), "minecraft:diamond_pickaxe", 1, "NO", Reaction.FLEE));
            check(absurd.get("resultado").equals("SEM_O_ITEM"), "o que ela nao teria nao e entregue nem ameacada: " + absurd);
            UUID golem = world.getServer().computeOnServer(s -> byTag(s, "golem").getUUID());
            check(golem != null, "golem ainda existe");
        }
    }
}
