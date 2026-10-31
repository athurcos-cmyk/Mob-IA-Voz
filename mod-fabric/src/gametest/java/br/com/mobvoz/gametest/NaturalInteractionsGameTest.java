package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.knowledge.KnowledgeStore;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.social.SceneFacts;
import br.com.mobvoz.trade.Trades;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Interacoes naturais em jogo real: domar por conversa (confianca + argumento), honestidade lembrada pela criatura, pelo grupo e pelo
 * registro do Dragao, fatos de cena, aviso de perigo de um aliado e cumprimento de quem ela conhece.
 */
public class NaturalInteractionsGameTest implements FabricClientGameTest {
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

    private static UUID spawn(TestSingleplayerContext w, String type, double x, double z, String nbt) {
        w.getServer().runCommand("summon " + type + " " + x + " -60 " + z + " " + nbt);
        return w.getServer().computeOnServer(s -> s.overworld().getEntities(EntityType.byString(type).orElseThrow(), e -> e.isAlive() && !e.isRemoved())
                .stream().reduce((a, b) -> b).orElseThrow().getUUID());
    }

    private static void trust(TestSingleplayerContext w, UUID mob, float t) {
        w.getServer().runOnServer(s -> {
            UUID p = player(s).getUUID();
            MemoryStore.update(mob(s, mob), m -> m.withRelation(p, r -> new MobMemory.Relationship(t, t, 0)));
        });
    }

    private static List<OrderPolicy.Decision> tame(TestSingleplayerContext w, UUID mob, double bonus) {
        return w.getServer().computeOnServer(s -> Orders.handle(mob(s, mob), player(s), List.of("TAME_ME"), false, bonus));
    }

    private static int onGround(TestSingleplayerContext w, String item) {
        return w.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(-30, -70, -30, 30, -40, 30))
                .stream().filter(e -> BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString().equals(item))
                .mapToInt(e -> e.getItem().getCount()).sum());
    }

    private static void thrownByPlayer(TestSingleplayerContext w, String item, int count, double x, double z) {
        w.getServer().runOnServer(s -> {
            ItemEntity e = new ItemEntity(s.overworld(), x, -59.5, z, new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(item)), count));
            e.setThrower(player(s));
            e.setPickUpDelay(40);
            s.overworld().addFreshEntity(e);
        });
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
        ServerConfig cfg = new ServerConfig();
        cfg.dynamicPrices = false; // conta quantidades exatas na troca do Enderman
        ServerConfig.set(cfg);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode survival @p");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");

            // ---- A) domar por conversa ----
            UUID horse = spawn(world, "horse", 3.5, 3.5, "{PersistenceRequired:1b}");
            UUID stranger = spawn(world, "horse", -3.5, 3.5, "{PersistenceRequired:1b}");
            UUID wolf = spawn(world, "wolf", 1.5, 6.5, "{PersistenceRequired:1b}");
            UUID cow = spawn(world, "cow", -1.5, 6.5, "{PersistenceRequired:1b}");
            trust(world, horse, 0.4f);
            trust(world, wolf, 0.4f);
            var cold = tame(world, stranger, 0);
            check(cold.get(0).verdict() == OrderPolicy.Verdict.REFUSED && !world.getServer().computeOnServer(s -> ((AbstractHorse) mob(s, stranger)).isTamed()),
                    "cavalo desconhecido nao deveria aceitar: " + cold);
            var cowRes = tame(world, cow, 0);
            check(cowRes.get(0).verdict() == OrderPolicy.Verdict.UNSUPPORTED, "vaca nao e domavel: " + cowRes);
            var h = tame(world, horse, 0);
            boolean horseTamed = world.getServer().computeOnServer(s -> ((AbstractHorse) mob(s, horse)).isTamed()
                    && player(s).getUUID().equals(((AbstractHorse) mob(s, horse)).getOwnerReference().getUUID()));
            check(h.get(0).accepted() && horseTamed, "o cavalo confiante deveria aceitar e ser do jogador: " + h);
            check(tame(world, horse, 0).get(0).verdict() == OrderPolicy.Verdict.ALREADY, "domar de novo = ja e sua");
            var w = tame(world, wolf, 0);
            check(w.get(0).accepted() && world.getServer().computeOnServer(s -> ((TamableAnimal) mob(s, wolf)).isTame()), "lobo confiante: " + w);
            // um argumento forte ajuda o cavalo desconhecido? com rapport minimo sim, sem nada nao
            trust(world, stranger, 0.1f);
            var argued = tame(world, stranger, OrderPolicy.appealBonus(2, 2, 0));
            check(argued.get(0).accepted(), "um bom argumento + um pouco de rapport deveria bastar: " + argued);
            System.out.println("[gametest] A ok: cavalo confiante domado; desconhecido recusou; vaca nao e domavel; lobo domado; argumento forte + rapport domou o outro cavalo");

            // ---- B) honestidade: o Enderman entrega a perola; o jogador devolve; ele, o grupo e o Dragao sabem ----
            cmd.runCommand("kill @e[type=!player]");
            cmd.runCommand("kill @e[type=item]");
            UUID ender = spawn(world, "enderman", 0.5, 4.5, "{NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            check(world.getServer().computeOnServer(s -> Trades.open(mob(s, ender), player(s), 0, "", 0)).verdict().equals("ACEITO"), "oferta do Enderman");
            thrownByPlayer(world, "minecraft:grass_block", 13, 0.5, 3.5);
            waitFor(context, 600, () -> onGround(world, "minecraft:ender_pearl") >= 1);
            check(onGround(world, "minecraft:ender_pearl") == 1, "a perola deveria ter sido entregue");
            cmd.runCommand("kill @e[type=item]");
            context.waitTicks(10);
            thrownByPlayer(world, "minecraft:ender_pearl", 1, 0.5, 3.0); // devolve, sem ninguem pedir
            context.waitTicks(30);
            long tick = world.getServer().computeOnServer(s -> s.overworld().getGameTime());
            UUID pid = world.getServer().computeOnServer(s -> player(s).getUUID());
            var lines = world.getServer().computeOnServer(s -> MemoryRules.factLines(MemoryStore.get(mob(s, ender)), pid, tick));
            System.out.println("[gametest] memoria do Enderman: " + lines);
            check(lines.stream().anyMatch(l -> l.contains("devolveu") && l.contains("honesto")), "fato de honestidade nao registrado: " + lines);
            var fac = world.getServer().computeOnServer(s -> KnowledgeStore.get(s).factionLines("enderman", pid, tick));
            var hist = world.getServer().computeOnServer(s -> KnowledgeStore.get(s).historyLines(pid, tick));
            System.out.println("[gametest] grupo: " + fac + " | registro do mundo (Dragao): " + hist);
            check(fac.stream().anyMatch(l -> l.contains("devolveu")), "o grupo dos Endermen deveria saber");
            check(hist.stream().anyMatch(l -> l.contains("honestamente")), "o Dragao deveria receber o fato no registro");
            // sem entrega nova, devolver de novo nao registra outra vez (cada devolucao consome a entrega)
            int before = world.getServer().computeOnServer(s -> MemoryStore.get(mob(s, ender)).facts().stream().filter(f -> f.type().equals("HONEST_RETURN")).toList().size());
            thrownByPlayer(world, "minecraft:ender_pearl", 1, 0.5, 3.0);
            context.waitTicks(20);
            int after = world.getServer().computeOnServer(s -> MemoryStore.get(mob(s, ender)).facts().stream().filter(f -> f.type().equals("HONEST_RETURN")).toList().size());
            check(before == 1 && after == 1, "sem nova entrega nao ha nova devolucao: " + before + " -> " + after);
            System.out.println("[gametest] B ok: devolucao honesta lembrada pelo Enderman, pelo grupo e pelo registro do Dragao; sem farm");

            // ---- C) cena real ----
            world.getServer().runOnServer(sv -> sv.overworld().setWeatherParameters(0, 6000, true, false));
            cmd.runCommand("kill @e[type=!player]");
            UUID pig = spawn(world, "pig", 0.5, 3.5, "{NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            spawn(world, "zombie", 6.5, 6.5, "{NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            world.getServer().runOnServer(sv -> player(sv).setHealth(8f));
            context.waitTicks(80); // a chuva leva alguns ticks para "pegar"
            var scene = world.getServer().computeOnServer(s -> SceneFacts.lines(mob(s, pig), player(s)));
            System.out.println("[gametest] cena: " + scene);
            String all = String.join(" | ", scene);
            check(all.contains("é noite") && all.contains("chovendo") && all.contains("ferido") && all.contains("zumbi"), "cena incompleta: " + all);
            cmd.runCommand("weather clear");
            world.getServer().runOnServer(sv -> player(sv).setHealth(20f));
            System.out.println("[gametest] C ok: a cena reflete noite, chuva, vida do jogador e quem esta perto");

            // ---- D) aviso de perigo do aliado e cumprimento de quem ja conhece ----
            cmd.runCommand("kill @e[type=!player]");
            UUID ally = spawn(world, "zombie", 2.5, 3.5, "{PersistenceRequired:1b}");
            world.getServer().runOnServer(s -> mob(s, ally).setNoAi(false));
            trust(world, ally, 0.9f);
            check(world.getServer().computeOnServer(s -> Orders.handle(mob(s, ally), player(s), List.of("CEASE_HOSTILITY", "DEFEND_PLAYER"), false))
                    .stream().allMatch(l -> l.accepted()), "aliado");
            int e0 = ConversationService.eventsStarted();
            UUID skel = spawn(world, "skeleton", 0.5, 7.5, "{PersistenceRequired:1b}");
            world.getServer().runOnServer(s -> mob(s, skel).setTarget(player(s)));
            waitFor(context, 400, () -> ConversationService.eventsStarted() > e0);
            check(ConversationService.eventsStarted() > e0, "o aliado deveria avisar do esqueleto que se aproxima");
            System.out.println("[gametest] aviso de perigo disparado pelo aliado (eventos " + e0 + " -> " + ConversationService.eventsStarted() + ")");
            cmd.runCommand("kill @e[type=!player]");
            cmd.runCommand("kill @e[type=item]");
            context.waitTicks(30);
            UUID farmer = spawn(world, "villager", 2.0, 3.0, "{NoAI:1b,Silent:1b,PersistenceRequired:1b,VillagerData:{profession:\"minecraft:farmer\",level:1,type:\"minecraft:plains\"}}");
            world.getServer().runOnServer(s -> MemoryStore.update(mob(s, farmer), m -> MemoryRules.trade(m, pid, "villager_farmer_emerald_v2", 0)));
            int e1 = ConversationService.eventsStarted();
            waitFor(context, 600, () -> ConversationService.eventsStarted() > e1);
            check(ConversationService.eventsStarted() > e1, "o aldeao que ja negociou com o jogador deveria cumprimenta-lo");
            UUID stranger2 = spawn(world, "villager", -2.0, 3.0, "{NoAI:1b,Silent:1b,PersistenceRequired:1b,VillagerData:{profession:\"minecraft:butcher\",level:1,type:\"minecraft:plains\"}}");
            int e2 = ConversationService.eventsStarted();
            context.waitTicks(200);
            check(ConversationService.eventsStarted() == e2, "desconhecido nao deveria cumprimentar sozinho");
            System.out.println("[gametest] D ok: aliado avisa do perigo; quem conhece cumprimenta; desconhecido fica quieto");
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
