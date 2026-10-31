package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.trade.TradeCatalog;
import br.com.mobvoz.trade.Trades;
import br.com.mobvoz.trade.Values;
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
 * GENERALIDADE: o mesmo motor de negociacao serve a TODA criatura do catalogo. Para cada receita (golem, enderman, aranha,
 * esqueleto, blaze, aldeoes por profissao, peixes, porco, Wither...) spawna o mob, abre a oferta, joga o pagamento (com 1 item
 * a mais de proposito) e confere: entrega exata, sobra devolvida. Depois: mob hostil em negociacao NAO ataca quem negocia.
 */
public class AllTradersGameTest implements FabricClientGameTest {
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

    private static int have(TestSingleplayerContext w, String item) {
        return w.getServer().computeOnServer(s -> {
            var it = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item));
            return s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30)).stream()
                    .filter(e -> e.getItem().getItem() == it).mapToInt(e -> e.getItem().getCount()).sum() + player(s).getInventory().countItem(it);
        });
    }

    private static String nbt(String profile) {
        String base = "{NoAI:1b,Silent:1b,PersistenceRequired:1b";
        if (profile.contains("#")) {
            return base + ",VillagerData:{profession:\"minecraft:" + profile.substring(profile.indexOf('#') + 1) + "\",level:1,type:\"minecraft:plains\"}}";
        }
        return base + "}";
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig fixed = new ServerConfig();
        fixed.dynamicPrices = false; // estes testes contam quantidades exatas: preco-base fixo
        ServerConfig.set(fixed);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");

            // todo id de item do catalogo (valores, gostos e saidas) existe de verdade nesta versao: nada de "ar" entregue por engano
            for (String id : Values.ids()) {
                check(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id)), "item inexistente em values.json: " + id);
            }
            for (var r : TradeCatalog.ALL) {
                for (String id : Profiles.of(r.profile()).wants()) {
                    check(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id)), r.id() + ": item aceito inexistente: " + id);
                }
            }
            System.out.println("[gametest] todos os " + Values.ids().size() + " itens de values.json existem no registro do jogo");
            int done = 0;
            // por criatura: a receita padrao e a ULTIMA (itens diferentes, prova que o pedido escolhe a receita certa)
            var sample = TradeCatalog.ALL.stream().filter(x -> {
                var all = TradeCatalog.recipesFor(x.profile());
                return all.get(0) == x || all.get(all.size() - 1) == x;
            }).toList();
            for (var r : sample) {
                cmd.runCommand("kill @e[type=!player]");
                cmd.runCommand("kill @e[type=item]");
                cmd.runCommand("clear @p");
                context.waitTicks(25); // deixa o anterior sumir (animacao de morte)
                String type = r.profile().contains("#") ? "minecraft:villager" : r.profile();
                cmd.runCommand("summon " + type + " 0.5 -60 5.5 " + nbt(r.profile()));
                context.waitTicks(5);
                UUID id = world.getServer().computeOnServer(s -> s.overworld().getEntities(
                        net.minecraft.world.entity.EntityType.byString(type).orElseThrow(), e -> e.isAlive() && !e.isRemoved()).get(0).getUUID());
                var p = Profiles.of(r.profile());
                var open = world.getServer().computeOnServer(s -> Trades.open(mob(s, id), player(s), 0, r.outputItem(), 0)); // pede exatamente este item
                check(open.verdict().equals("ACEITO"), r.id() + ": a oferta nao abriu: " + open);
                // paga com o item aceito de MAIOR valor (quantidade n) + 1 de sobra
                String item = p.wants().stream().filter(i -> Values.value(i) > 0 && Math.ceil(r.price() / Values.value(i)) <= 40)
                        .max((a, b) -> Double.compare(Values.value(a), Values.value(b))).orElseThrow();
                int n = (int) Math.ceil(r.price() / Values.value(item) - 1e-9);
                world.getServer().runOnServer(s -> {
                    ItemEntity e = new ItemEntity(s.overworld(), 0.5, -59.5, 3.5, new ItemStack(
                            BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item)), n + 1));
                    e.setThrower(player(s));
                    e.setPickUpDelay(40);
                    s.overworld().addFreshEntity(e);
                });
                for (int i = 0; i < 60 && have(world, r.outputItem()) < r.outputCount(); i++) {
                    context.waitTicks(10);
                }
                context.waitTicks(10);
                int got = have(world, r.outputItem());
                int back = have(world, item);
                System.out.println("[gametest] " + r.id() + ": pagou " + n + " " + Values.name(item, n != 1) + " (+1 de sobra) -> recebeu " + got + " "
                        + Values.name(r.outputItem(), got != 1) + "; sobra devolvida=" + back);
                check(got == r.outputCount(), r.id() + ": esperado " + r.outputCount() + " de " + r.outputItem() + ", recebeu " + got);
                check(back == 1, r.id() + ": a sobra (1) deveria voltar, voltou " + back);
                done++;
            }
            System.out.println("[gametest] GENERALIDADE: " + done + " receitas de " + done + " criaturas funcionaram pelo mesmo motor");

            // mob hostil COM IA negociando nao ataca quem negocia; atacar o mob cancela a oferta
            cmd.runCommand("kill @e[type=!player]");
            cmd.runCommand("clear @p");
            cmd.runCommand("gamemode survival @p");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("summon spider 0.5 -60 4.5 {PersistenceRequired:1b}");
            context.waitTicks(5);
            UUID sp = world.getServer().computeOnServer(s -> s.overworld().getEntities(net.minecraft.world.entity.EntityType.SPIDER, e -> true).get(0).getUUID());
            float hp0 = world.getServer().computeOnServer(s -> player(s).getHealth());
            check(world.getServer().computeOnServer(s -> Trades.open(mob(s, sp), player(s), 0, "", 0)).verdict().equals("ACEITO"), "aranha oferta");
            context.waitTicks(240);
            float hp1 = world.getServer().computeOnServer(s -> player(s).getHealth());
            boolean targeting = world.getServer().computeOnServer(s -> mob(s, sp).getTarget() != null);
            System.out.println("[gametest] aranha hostil negociando: vida " + hp0 + " -> " + hp1 + "; mirando o jogador=" + targeting);
            check(hp1 >= hp0 && !targeting, "a aranha atacou quem negociava");
            cmd.runCommand("execute as @p run damage @e[type=spider,limit=1] 1 minecraft:player_attack by @p");
            context.waitTicks(10);
            check(!world.getServer().computeOnServer(s -> Trades.get(mob(s, sp)).hasOffer()), "atacar a aranha deveria cancelar a oferta");
            System.out.println("[gametest] atacar quem negocia cancela a oferta");

            // so CONVERSAR (sem oferta, sem alianca): zumbi com IA nao ataca; quando a conversa acaba, ataca
            cmd.runCommand("kill @e[type=!player]");
            cmd.runCommand("summon zombie 0.5 -60 3.5 {PersistenceRequired:1b}");
            context.waitTicks(5);
            UUID zb = world.getServer().computeOnServer(s -> s.overworld().getEntities(net.minecraft.world.entity.EntityType.ZOMBIE, e -> true).get(0).getUUID());
            cmd.runCommand("effect give @p minecraft:regeneration 1 0 true");
            float z0 = world.getServer().computeOnServer(s -> player(s).getHealth());
            world.getServer().runOnServer(s -> {
                br.com.mobvoz.social.Parley.touch(mob(s, zb), player(s));
                mob(s, zb).setTarget(player(s));
            });
            context.waitTicks(200);
            float z1 = world.getServer().computeOnServer(s -> player(s).getHealth());
            boolean zt = world.getServer().computeOnServer(s -> mob(s, zb).getTarget() != null);
            System.out.println("[gametest] zumbi com a conversa em andamento: vida " + z0 + " -> " + z1 + "; mirando=" + zt);
            check(z1 >= z0 && !zt, "o zumbi atacou quem so estava conversando");
            world.getServer().runOnServer(s -> br.com.mobvoz.social.Parley.end(mob(s, zb)));
            context.waitTicks(200);
            float z2 = world.getServer().computeOnServer(s -> player(s).getHealth());
            boolean zt2 = world.getServer().computeOnServer(s -> mob(s, zb).getTarget() != null);
            System.out.println("[gametest] depois da conversa: vida " + z2 + "; mirando=" + zt2);
            check(z2 < z1 || zt2, "sem a conversa o zumbi deveria voltar a ser hostil (controle do teste)");
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
