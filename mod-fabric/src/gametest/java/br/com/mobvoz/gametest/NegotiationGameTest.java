package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.server.ConversationService;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Trocas com FALAS (em jogo real): aldeao fazendeiro pede feno e devolve a sobra comentando; negocio nao fechado quando o
 * combinado nao e cumprido; esqueleto Wither ignora ferro, quer a picareta, pode ser convencido (com limites); peixe so da coisas
 * do mar e diz de onde vieram.
 */
public class NegotiationGameTest implements FabricClientGameTest {
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
            ItemEntity e = new ItemEntity(s.overworld(), 0.5, -59.5, 3.5, new ItemStack(
                    BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item)), count));
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

    private static UUID spawn(TestSingleplayerContext w, String type, String nbt, double z) {
        w.getServer().runCommand("summon " + type + " 0.5 -60 " + z + " " + nbt);
        return w.getServer().computeOnServer(s -> s.overworld().getEntities(net.minecraft.world.entity.EntityType.byString(type).orElseThrow(), e -> true)
                .get(0).getUUID());
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

            // ---- A) fazendeiro: "pega 3 fenos" -> jogo 4 -> ele devolve 1 e comenta ----
            UUID farmer = spawn(world, "villager", "{NoAI:1b,Silent:1b,PersistenceRequired:1b,VillagerData:{profession:\"minecraft:farmer\",level:1,type:\"minecraft:plains\"}}", 5.5);
            var open = world.getServer().computeOnServer(s -> Trades.open(mob(s, farmer), player(s), 0, "", 0));
            System.out.println("[gametest] fazendeiro: " + open.reason());
            check(open.verdict().equals("ACEITO") && open.reason().contains("feno"), "fazendeiro deveria aceitar feno: " + open);
            drop(world, "minecraft:hay_block", 4);
            waitFor(context, 400, () -> onGround(world, "minecraft:emerald") > 0 && said("como eu falei"));
            context.waitTicks(20);
            System.out.println("[gametest] fala: " + ConversationService.announced());
            check(onGround(world, "minecraft:emerald") == 1, "esmeralda nao entregue");
            int hayBack = onGround(world, "minecraft:hay_block") + world.getServer().computeOnServer(s -> player(s).getInventory().countItem(net.minecraft.world.item.Items.HAY_BLOCK));
            check(hayBack == 1, "o 4o feno deveria voltar ao jogador/chao (ha " + hayBack + ")");
            check(said("Só precisava de 3") && said("de volta com você"), "falta a fala sobre a sobra: " + ConversationService.announced());

            // ---- B) nao cumpriu: valor insuficiente e prazo vencido ----
            cmd.runCommand("kill @e[type=item]");
            check(world.getServer().computeOnServer(s -> Trades.open(mob(s, farmer), player(s), 0, "", 0)).verdict().equals("ACEITO"), "reabrir");
            drop(world, "minecraft:hay_block", 1);
            waitFor(context, 400, () -> said("por enquanto só deu"));
            check(said("Ainda falta"), "falta a fala de valor insuficiente");
            check(onGround(world, "minecraft:emerald") == 0 && onGround(world, "minecraft:hay_block") == 1, "nada deveria ser tomado/pago");
            world.getServer().runOnServer(s -> { // forca o prazo curto para provar o "negocio desfeito"
                Mob m = mob(s, farmer);
                MobTrades t = Trades.get(m);
                m.setAttached(MobTrades.TYPE, t.open(t.offerRecipe(), t.offerPlayer(), s.overworld().getGameTime() + 15));
            });
            waitFor(context, 300, () -> said("esquece o trato"));
            check(said("esquece o trato"), "falta a fala de negocio desfeito: " + ConversationService.announced());
            System.out.println("[gametest] B ok: valor insuficiente e negocio desfeito foram falados; nada foi tomado");

            // ---- C) esqueleto Wither: ferro nao interessa, picareta sim, convencer com limite ----
            cmd.runCommand("kill @e[type=item]");
            UUID ws = spawn(world, "wither_skeleton", "{NoAI:1b,Silent:1b,PersistenceRequired:1b}", 5.5);
            var wopen = world.getServer().computeOnServer(s -> Trades.open(mob(s, ws), player(s), 0, "minecraft:wither_skeleton_skull", 3));
            System.out.println("[gametest] wither: " + wopen.verdict() + " / " + wopen.reason());
            check(wopen.verdict().equals("ACEITO") && wopen.reason().contains("9 diamantes") && wopen.reason().contains("Limite")
                    && !wopen.reason().contains("pontos"), "oferta do Wither (3 caveiras): " + wopen);
            String ev = world.getServer().computeOnServer(s -> Trades.evaluate(mob(s, ws), java.util.Map.of("minecraft:cobblestone", 3, "minecraft:diamond_pickaxe", 1)));
            System.out.println("[gametest] avaliacao: " + ev);
            check(ev.contains("3 pedregulhos: sem interesse") && ev.contains("1 picareta de diamante: interessa"), "avaliacao: " + ev);
            drop(world, "minecraft:cobblestone", 3);
            waitFor(context, 300, () -> said("pedregulhos não me interessam"));
            check(said("bom motivo"), "o Wither deveria abrir espaco para ser convencido: " + ConversationService.announced());
            var weak = world.getServer().computeOnServer(s -> Trades.persuade(mob(s, ws), "minecraft:cobblestone", 0, 1, 0.0));
            var good = world.getServer().computeOnServer(s -> Trades.persuade(mob(s, ws), "minecraft:cobblestone", 2, 1, 0.0));
            System.out.println("[gametest] convencer sem motivo=" + weak.verdict() + " com motivo forte=" + good.verdict());
            check(weak.verdict().equals("RECUSADO") && good.verdict().equals("CONVENCIDO"), "persuasao");
            check(world.getServer().computeOnServer(s -> Trades.evaluate(mob(s, ws), java.util.Map.of("minecraft:cobblestone", 3))).contains("3 pedregulhos: interessa"),
                    "pedregulho deveria passar a interessar");
            cmd.runCommand("kill @e[type=item]");
            drop(world, "minecraft:diamond_pickaxe", 1);
            drop(world, "minecraft:gold_ingot", 28);
            waitFor(context, 800, () -> onGround(world, "minecraft:wither_skeleton_skull") > 0);
            check(onGround(world, "minecraft:wither_skeleton_skull") == 3, "caveiras: " + onGround(world, "minecraft:wither_skeleton_skull"));
            var again = world.getServer().computeOnServer(s -> Trades.open(mob(s, ws), player(s), 0, "minecraft:wither_skeleton_skull", 1));
            System.out.println("[gametest] 4a caveira: " + again.verdict() + " / " + again.reason());
            check(again.verdict().equals("ACEITO") && !again.reason().contains("dias de jogo"),
                    "nao ha mais cooldown: a 4a caveira abre normalmente: " + again);
            System.out.println("[gametest] C ok: pedido de 3 caveiras = 1 negocio (picareta + 28 ouros) -> 3 caveiras; sem cooldown");

            // ---- D) peixe: so da coisas do mar e diz de onde vieram ----
            cmd.runCommand("kill @e[type=item]");
            UUID cod = spawn(world, "cod", "{NoAI:1b,Silent:1b,PersistenceRequired:1b}", 5.5);
            check(world.getServer().computeOnServer(s -> Trades.open(mob(s, cod), player(s), 0, "minecraft:iron_chestplate", 1)).verdict().equals("IMPOSSIVEL"),
                    "peixe nao pode dar armadura");
            check(world.getServer().computeOnServer(s -> Trades.open(mob(s, cod), player(s), 0, "", 0)).verdict().equals("ACEITO"), "peixe oferta");
            drop(world, "minecraft:kelp", 16);
            waitFor(context, 600, () -> onGround(world, "minecraft:prismarine_shard") > 0 && said("profundezas do oceano"));
            check(onGround(world, "minecraft:prismarine_shard") == 1 && said("profundezas do oceano"), "recompensa do peixe: " + ConversationService.announced());
            System.out.println("[gametest] D ok: peixe nega armadura; entrega prismarinho 'das profundezas do oceano'");

            // ---- E) negociacao ABERTA: item fora do catalogo julgado pela IA; pagamento fora do gosto fixo ----
            cmd.runCommand("kill @e[type=item]");
            UUID golem = spawn(world, "iron_golem", "{NoAI:1b,Silent:1b,PersistenceRequired:1b}", 5.5);
            var gopen = world.getServer().computeOnServer(s -> Trades.open(mob(s, golem), player(s), 0, "minecraft:iron_pickaxe", 1, "YES"));
            System.out.println("[gametest] golem (IA: plausivel): " + gopen.verdict() + " / " + gopen.reason());
            check(gopen.verdict().equals("ACEITO") && gopen.reason().contains("picareta de ferro") && !gopen.reason().contains("pontos"), "golem deveria improvisar a picareta: " + gopen);
            check(world.getServer().computeOnServer(s -> Trades.evaluate(mob(s, golem), java.util.Map.of("minecraft:raw_iron", 10))).contains("sem interesse"),
                    "antes do julgamento o ferro bruto nao interessa");
            world.getServer().runOnServer(s -> Trades.applyJudgments(mob(s, golem), java.util.Map.of("minecraft:raw_iron", "YES")));
            String gev = world.getServer().computeOnServer(s -> Trades.evaluate(mob(s, golem), java.util.Map.of("minecraft:raw_iron", 10)));
            System.out.println("[gametest] golem avalia ferro bruto apos o julgamento: " + gev);
            check(gev.contains("interessa") && gev.contains("basta"), "10 ferros brutos (70%) deveriam bastar: " + gev);
            drop(world, "minecraft:raw_iron", 11);
            waitFor(context, 800, () -> onGround(world, "minecraft:iron_pickaxe") > 0);
            check(onGround(world, "minecraft:iron_pickaxe") == 1, "picareta improvisada nao entregue");
            check(onGround(world, "minecraft:raw_iron") >= 1 && onGround(world, "minecraft:raw_iron") <= 3, "a sobra do ferro bruto deveria voltar (o preco agora e menor; ha " + onGround(world, "minecraft:raw_iron") + ")");
            // pedido que a IA julgou impossivel e sem sorte (valor alto): recusado, nada abre
            var nope = world.getServer().computeOnServer(s -> Trades.open(mob(s, golem), player(s), 0, "minecraft:netherite_block", 1, "YES"));
            check(nope.verdict().equals("IMPOSSIVEL"), "o que passa de 400 pontos nunca e improvisado, nem com a IA dizendo que sim: " + nope);
            System.out.println("[gametest] E ok: picareta de ferro improvisada pelo golem, paga com ferro bruto (julgado pela IA); item acima de 400 pontos recusado");

            // ---- F) preco VARIAVEL: travado na oferta, dentro da faixa, com barganha limitada ----
            ServerConfig.set(new ServerConfig()); // dynamicPrices = true (padrao)
            cmd.runCommand("kill @e[type=!player]");
            UUID f2 = spawn(world, "villager", "{NoAI:1b,Silent:1b,PersistenceRequired:1b,VillagerData:{profession:\"minecraft:farmer\",level:1,type:\"minecraft:plains\"}}", 5.5);
            var friendly = world.getServer().computeOnServer(s -> Trades.open(mob(s, f2), player(s), 0.9, "", 0));
            double locked = world.getServer().computeOnServer(s -> Trades.get(mob(s, f2)).unitPrice(13.0));
            System.out.println("[gametest] preco travado (amigo, warmth 0.9): " + locked + " | " + friendly.reason());
            check(friendly.verdict().equals("ACEITO") && locked >= 10.0 && locked <= 13.0 * 1.25 + 0.1, "preco fora da faixa: " + locked);
            check(locked < 13.0, "um amigo deveria pagar menos que o preco-base (13): " + locked);
            var reopened = world.getServer().computeOnServer(s -> Trades.open(mob(s, f2), player(s), -0.2, "", 0));
            check(reopened.verdict().equals("JA_ATIVO") && friendly.reason().endsWith(reopened.reason()), "reabrir deveria manter o preco travado: " + reopened);
            check(world.getServer().computeOnServer(s -> Trades.get(mob(s, f2)).unitPrice(13.0)) == locked, "preco mudou ao reabrir");
            var h1 = world.getServer().computeOnServer(s -> Trades.haggle(mob(s, f2), 2, 1, 0.5));
            double afterHaggle = world.getServer().computeOnServer(s -> Trades.get(mob(s, f2)).unitPrice(13.0));
            System.out.println("[gametest] barganha: " + h1.verdict() + " " + locked + " -> " + afterHaggle);
            check(h1.verdict().equals("CONVENCIDO") && afterHaggle < locked && afterHaggle >= 10.0, "barganha deveria baixar, sem passar do valor: " + afterHaggle);
            var h0 = world.getServer().computeOnServer(s -> Trades.haggle(mob(s, f2), 0, 1, 0.5));
            check(h0.verdict().equals("RECUSADO"), "desconto sem argumento nao baixa");
            for (int i = 0; i < 4; i++) {
                world.getServer().computeOnServer(s -> Trades.haggle(mob(s, f2), 2, 2, 1.0));
            }
            double floorP = world.getServer().computeOnServer(s -> Trades.get(mob(s, f2)).unitPrice(13.0));
            check(floorP >= 10.0 && floorP >= 13.0 * 0.70 - 0.1, "piso violado: " + floorP);
            check(world.getServer().computeOnServer(s -> Trades.haggle(mob(s, f2), 2, 2, 1.0)).verdict().equals("RECUSADO"), "limite de tentativas");
            System.out.println("[gametest] F ok: preco travado na oferta, amigo paga menos, barganha limitada (piso " + floorP + ")");
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
