package br.com.mobvoz.gametest;

import br.com.mobvoz.trade.MobTrades;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Fase 7 em jogo real (T12-T15): troca fisica porco/cenoura. Item errado intocado; 64 jogadas, so 3 tomadas e 61 ficam no
 * chao; recompensa fisica exatamente 1; orcamento (4a troca recusada); e salvar/sair NO MEIO da reserva -> carregar conclui
 * exatamente uma vez (sem duplicar nem perder).
 */
public class PhysicalTradeGameTest implements FabricClientGameTest {
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

    /** Joga o item como o jogador faria (thrower = jogador), 2 blocos a frente do porco. */
    private static void drop(TestSingleplayerContext w, String item, int count) {
        w.getServer().runOnServer(s -> {
            ServerPlayer p = player(s);
            Item it = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item));
            ItemEntity e = new ItemEntity(s.overworld(), 0.5, -59.5, 3.5, new ItemStack(it, count));
            e.setThrower(p);
            e.setPickUpDelay(40);
            s.overworld().addFreshEntity(e);
        });
    }

    /** Itens do jogador no mundo: no chao OU no inventario (a sobra devolvida voa ate ele e e pega). */
    private static int onGround(TestSingleplayerContext w, String item) {
        return w.getServer().computeOnServer(s -> {
            var it = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item));
            int ground = s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30)).stream()
                    .filter(e -> e.getItem().getItem() == it).mapToInt(e -> e.getItem().getCount()).sum();
            return ground + (s.getPlayerList().getPlayers().isEmpty() ? 0 : player(s).getInventory().countItem(it));
        });
    }

    private static void clearItems(TestSingleplayerContext w) {
        w.getServer().runCommand("kill @e[type=item]");
        w.getServer().runCommand("clear @p");
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        var fixedCfg = new br.com.mobvoz.config.ServerConfig();
        fixedCfg.dynamicPrices = false; // conta quantidades exatas: preco-base fixo
        br.com.mobvoz.config.ServerConfig.set(fixedCfg);
        TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create();
        world.getClientWorld().waitForChunksRender();
        world.getServer().runCommand("gamemode creative @p");
        world.getServer().runCommand("tp @p 0.5 -60 0.5 0 17");
        UUID pig = world.getServer().computeOnServer(s -> {
            Mob m = (Mob) EntityType.PIG.create(s.overworld(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            m.setPos(0.5, -60, 5.5);
            s.overworld().addFreshEntity(m);
            return m.getUUID();
        });
        context.waitTicks(20);

        // 0) sem oferta aberta nada acontece (requireActiveContract)
        drop(world, "minecraft:carrot", 3);
        context.waitTicks(100);
        check(onGround(world, "minecraft:carrot") == 3 && onGround(world, "minecraft:apple") == 0, "sem oferta, o porco nao deveria negociar");
        clearItems(world);

        // 1) T13: item errado -> intocado
        var r0 = world.getServer().computeOnServer(s -> Trades.open(mob(s, pig), player(s), 0, "", 0));
        check(r0.verdict().equals("ACEITO"), "oferta deveria abrir: " + r0);
        System.out.println("[gametest] condicoes: " + r0.reason());
        drop(world, "minecraft:diamond", 1);
        context.waitTicks(120);
        check(onGround(world, "minecraft:diamond") == 1 && onGround(world, "minecraft:apple") == 0, "item errado foi tocado!");
        System.out.println("[gametest] T13 ok: item errado intocado, sem recompensa");
        clearItems(world);

        // 2) T12/T14: 64 cenouras para custo 3 -> toma 3, devolve (deixa) 61, entrega 1 maca fisica
        drop(world, "minecraft:carrot", 64);
        for (int i = 0; i < 40 && onGround(world, "minecraft:apple") == 0; i++) {
            context.waitTicks(10);
        }
        context.waitTicks(10);
        int carrots = onGround(world, "minecraft:carrot");
        int apples = onGround(world, "minecraft:apple");
        MobTrades led = world.getServer().computeOnServer(s -> Trades.get(mob(s, pig)));
        System.out.println("[gametest] T12/T14: cenouras no chao=" + carrots + " macas=" + apples + " ledger.done=" + led.done().size()
                );
        check(carrots == 61, "deveria sobrar 61 cenouras no chao, ha " + carrots);
        check(apples == 1, "deveria haver exatamente 1 maca de recompensa, ha " + apples);
        check(led.done().size() == 1 && !led.hasTx(), "ledger inesperado: " + led);
        clearItems(world);

        // 3) sem cooldown: trocas seguidas continuam sendo aceitas (a 4a oferta tambem)
        for (int n = 2; n <= 3; n++) {
            check(world.getServer().computeOnServer(s -> Trades.open(mob(s, pig), player(s), 0, "", 0)).verdict().equals("ACEITO"), "oferta " + n);
            drop(world, "minecraft:carrot", 3);
            for (int i = 0; i < 40 && onGround(world, "minecraft:apple") == 0; i++) {
                context.waitTicks(10);
            }
            context.waitTicks(10);
            clearItems(world);
        }
        var r4 = world.getServer().computeOnServer(s -> Trades.open(mob(s, pig), player(s), 0, "", 0));
        System.out.println("[gametest] sem cooldown: 4a oferta -> " + r4);
        check(r4.verdict().equals("ACEITO"), "a 4a troca deveria ser aceita (nao ha mais cooldown)");
        clearItems(world);

        // 4) T15: salvar/sair NO MEIO (RESERVADO) -> carregar conclui exatamente uma vez
        world.getServer().runOnServer(s -> {
            Mob m = mob(s, pig);
            m.setAttached(MobTrades.TYPE, MobTrades.EMPTY); // novo livro-razao para este cenario
        });
        check(world.getServer().computeOnServer(s -> Trades.open(mob(s, pig), player(s), 0, "", 0)).verdict().equals("ACEITO"), "oferta p/ T15");
        drop(world, "minecraft:carrot", 10);
        int guard = 0;
        while (!world.getServer().computeOnServer(s -> Trades.get(mob(s, pig)).hasTx())) {
            context.waitTicks(1);
            check(++guard < 600, "a reserva nao aconteceu");
        }
        String txId = world.getServer().computeOnServer(s -> Trades.get(mob(s, pig)).txId());
        System.out.println("[gametest] T15: RESERVADO (tx " + txId + "); salvando e saindo no meio da troca");
        int apple0 = onGround(world, "minecraft:apple");
        check(apple0 == 0, "recompensa nao deveria existir antes de concluir");
        var save = world.getWorldSave();
        world.close();
        context.waitTicks(20);
        try (TestSingleplayerContext again = save.open()) {
            again.getClientWorld().waitForChunksRender();
            for (int i = 0; i < 40 && onGround(again, "minecraft:apple") == 0; i++) {
                context.waitTicks(10);
            }
            context.waitTicks(40); // espera para provar que nao paga de novo
            MobTrades after = again.getServer().computeOnServer(s -> Trades.get(mob(s, pig)));
            int ap = onGround(again, "minecraft:apple");
            int ca = onGround(again, "minecraft:carrot");
            System.out.println("[gametest] T15 apos recarregar: macas=" + ap + " cenouras no chao=" + ca + " tx concluida=" + after.isDone(txId));
            check(ap == 1, "esperada exatamente 1 maca apos recarregar, ha " + ap);
            check(ca == 7, "o excedente (7) deveria seguir no chao, ha " + ca);
            check(after.isDone(txId) && !after.hasTx(), "transacao nao encerrada: " + after);
        }
    }
}
