package br.com.mobvoz.gametest;

import br.com.mobvoz.knowledge.KnowledgeStore;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Fase 11 em jogo real: cura do zumbi-aldeao preserva identidade e fatos (T23); atacar um enderman marca a faccao inteira
 * (T24); o registro historico vem so de eventos verificados e sobrevive a salvar/carregar (T25/T33).
 */
public class KnowledgeGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static ServerPlayer player(MinecraftServer s) {
        return s.getPlayerList().getPlayers().get(0);
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create();
        world.getClientWorld().waitForChunksRender();
        var cmd = world.getServer();
        cmd.runCommand("gamemode survival @p");
        cmd.runCommand("time set midnight");
        cmd.runCommand("gamerule doDaylightCycle false");
        cmd.runCommand("tp @p 0.5 -60 0.5 0 17");

        // ---- T23: zumbi-aldeao curado continua "ele mesmo" ----
        cmd.runCommand("summon zombie_villager 3.5 -60 5.5 {ConversionTime:90,PersistenceRequired:1b,Tags:[\"zv\"]}");
        context.waitTicks(5);
        UUID zv = world.getServer().computeOnServer(s -> {
            Mob m = s.overworld().getEntities(EntityType.ZOMBIE_VILLAGER, e -> true).get(0);
            UUID p = player(s).getUUID();
            MemoryStore.update(m, mem -> MemoryRules.gift(mem, p, "minecraft:golden_apple", 1, 5));
            return m.getUUID();
        });
        MobMemory before = world.getServer().computeOnServer(s -> MemoryStore.get((Mob) s.overworld().getEntity(zv)));
        context.waitTicks(140);
        MobMemory after = world.getServer().computeOnServer(s -> s.overworld().getEntities(EntityType.VILLAGER, e -> true).stream()
                .findFirst().map(v -> MemoryStore.get(v)).orElse(null));
        System.out.println("[gametest] T23: identidade antes=" + before.identityId() + " depois=" + (after == null ? null : after.identityId())
                + " conversao=" + (after == null ? null : after.counts()));
        check(after != null, "o zumbi-aldeao nao virou aldeao");
        check(after.identityId().equals(before.identityId()), "identidade nao preservada na cura");
        check(after.counts().getOrDefault("GIFT", 0) == 1 && after.counts().getOrDefault("CONVERSION", 0) == 1, "fatos nao preservados: " + after.counts());
        check(world.getServer().computeOnServer(s -> s.overworld().getEntities(EntityType.VILLAGER, e -> true).size()) == 1, "aldeao duplicado");

        // ---- T24: atacar um enderman marca a faccao; T25: registro historico so com eventos verificados ----
        cmd.runCommand("summon enderman 0.5 -60 8.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"e1\"]}");
        context.waitTicks(10);
        for (int i = 0; i < 2; i++) {
            cmd.runCommand("execute as @p run damage @e[tag=e1,limit=1] 1 minecraft:player_attack by @p");
            context.waitTicks(60);
        }
        cmd.runCommand("gamerule doImmediateRespawn true");
        cmd.runCommand("kill @p");
        context.waitTicks(40);
        UUID me = world.getServer().computeOnServer(s -> player(s).getUUID());
        float pen = world.getServer().computeOnServer(s -> KnowledgeStore.get(s).trustPenalty("enderman", me));
        int hist = world.getServer().computeOnServer(s -> KnowledgeStore.get(s).history().size());
        System.out.println("[gametest] T24: penalidade da faccao enderman=" + pen + "; eventos no registro historico=" + hist);
        check(pen >= 0.29f, "enderman nao memorizou o ataque (" + pen + ")");
        check(hist >= 1 && world.getServer().computeOnServer(s -> KnowledgeStore.get(s).history().stream().allMatch(e -> e.type().equals("PLAYER_DEATH"))),
                "registro historico com eventos nao verificados");

        // ---- T26: a fama na aldeia e LOCAL: bater num aldeao marca a vila dele, nao uma a 200 blocos ----
        cmd.runCommand("forceload add 200 10");
        cmd.runCommand("summon villager 4.5 -60 10.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"va\"]}");
        cmd.runCommand("summon villager 200.5 -60 10.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vb\"]}");
        context.waitTicks(20);
        cmd.runCommand("execute as @p run damage @e[tag=va,limit=1] 1 minecraft:player_attack by @p");
        context.waitTicks(40);
        float[] vp = world.getServer().computeOnServer(s -> {
            long now = s.overworld().getGameTime();
            Mob a = s.overworld().getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(-10, -70, -10, 20, 40, 30), m -> m.getTags().contains("va")).get(0);
            Mob b = s.overworld().getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(190, -70, -10, 220, 40, 30), m -> m.getTags().contains("vb")).get(0);
            var k = KnowledgeStore.get(s);
            return new float[] {k.trustPenalty("aldeia", me, now, a.getX(), a.getZ()), k.trustPenalty("aldeia", me, now, b.getX(), b.getZ()),
                    k.factionLines("aldeia", me, now, b.getX(), b.getZ()).size()};
        });
        System.out.println("[gametest] T26: vila atingida penalidade=" + vp[0] + "; vila a 200 blocos penalidade=" + vp[1] + " falas=" + vp[2]);
        check(vp[0] > 0.1f, "a vila do aldeao atingido deveria desconfiar (" + vp[0] + ")");
        check(vp[1] == 0f && vp[2] == 0f, "a vila distante nao deveria saber (" + vp[1] + ", falas " + vp[2] + ")");

        // ---- T33: salvar/carregar preserva ----
        var kBefore = world.getServer().computeOnServer(s -> KnowledgeStore.get(s));
        var save = world.getWorldSave();
        world.close();
        context.waitTicks(20);
        try (TestSingleplayerContext again = save.open()) {
            again.getClientWorld().waitForChunksRender();
            context.waitTicks(20);
            var kAfter = again.getServer().computeOnServer(s -> KnowledgeStore.get(s));
            check(kAfter.equals(kBefore), "conhecimento mudou ao recarregar:\n" + kBefore + "\n" + kAfter);
            System.out.println("[gametest] T33: conhecimento coletivo preservado apos salvar/carregar");
        }
    }
}
