package br.com.mobvoz.gametest;

import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.orders.OrderPolicy;
import br.com.mobvoz.orders.Orders;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Fase 10 em jogo real: zumbi pactuado defende o jogador e nao fere aliados (T38 base); creeper pactuado nao fere o
 * pagador durante a tregua (T21) e ferir o creeper rompe o pacto (T22).
 */
public class AllianceGameTest implements FabricClientGameTest {
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

    private static UUID spawn(TestSingleplayerContext w, EntityType<?> t, double x, double z) {
        return w.getServer().computeOnServer(s -> {
            Mob m = (Mob) t.create(s.overworld(), EntitySpawnReason.COMMAND);
            m.setPos(x, -60, z);
            m.setPersistenceRequired();
            s.overworld().addFreshEntity(m);
            return m.getUUID();
        });
    }

    private static void trust(TestSingleplayerContext w, UUID mob) {
        w.getServer().runOnServer(s -> {
            UUID p = player(s).getUUID();
            MemoryStore.update(mob(s, mob), m -> m.withRelation(p, r -> new MobMemory.Relationship(0.9f, 0.9f, 0)));
        });
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode survival @p");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");

            // ---- defesa + dano amigo ----
            UUID zombie = spawn(world, EntityType.ZOMBIE, 2.5, 4.5);
            UUID ally2 = spawn(world, EntityType.ZOMBIE, -2.5, 4.5);
            UUID skeleton = spawn(world, EntityType.SKELETON, 0.5, 12.5);
            for (UUID id : List.of(zombie, ally2, skeleton)) {
                world.getServer().runOnServer(s -> mob(s, id).setNoAi(false));
            }
            trust(world, zombie);
            trust(world, ally2);
            var res = world.getServer().computeOnServer(s -> {
                var d1 = Orders.handle(mob(s, zombie), player(s), List.of("CEASE_HOSTILITY", "DEFEND_PLAYER"), false);
                var d2 = Orders.handle(mob(s, ally2), player(s), List.of("CEASE_HOSTILITY", "DEFEND_PLAYER"), false);
                return List.of(d1, d2);
            });
            check(res.stream().allMatch(l -> l.stream().allMatch(OrderPolicy.Decision::accepted)), "pacto/defesa deveriam ser aceitos: " + res);

            // o esqueleto "acerta" o jogador -> o zumbi defensor mira o esqueleto
            cmd.runCommand("tag " + skeleton + " add skel");
            cmd.runCommand("execute as @p run damage @p 1 minecraft:mob_attack by @e[tag=skel,limit=1]");
            context.waitTicks(40);
            UUID target = world.getServer().computeOnServer(s -> mob(s, zombie).getTarget() == null ? null : mob(s, zombie).getTarget().getUUID());
            System.out.println("[gametest] defesa: alvo do zumbi aliado = " + target + " (esqueleto = " + skeleton + ")");
            check(skeleton.equals(target), "o aliado deveria atacar o esqueleto agressor");

            // dano amigo: o zumbi aliado nao fere o outro aliado nem o dono
            float hpBefore = world.getServer().computeOnServer(s -> mob(s, ally2).getHealth());
            cmd.runCommand("tag " + zombie + " add z1");
            cmd.runCommand("damage " + ally2 + " 5 minecraft:mob_attack by @e[tag=z1,limit=1]");
            float hpPlayer = world.getServer().computeOnServer(s -> player(s).getHealth());
            cmd.runCommand("damage @p 5 minecraft:mob_attack by @e[tag=z1,limit=1]");
            context.waitTicks(5);
            float hpAfter = world.getServer().computeOnServer(s -> mob(s, ally2).getHealth());
            float hpPlayerAfter = world.getServer().computeOnServer(s -> player(s).getHealth());
            System.out.println("[gametest] dano amigo: aliado " + hpBefore + "->" + hpAfter + ", dono " + hpPlayer + "->" + hpPlayerAfter);
            check(hpAfter >= hpBefore, "aliado ferido por aliado");
            check(hpPlayerAfter >= hpPlayer, "dono ferido pelo proprio defensor");
            cmd.runCommand("kill @e[type=!player]");

            // ---- creeper: tregua protege o pagador; ferir o creeper rompe o pacto ----
            UUID creeper = spawn(world, EntityType.CREEPER, 1.5, 3.5);
            trust(world, creeper);
            var cd = world.getServer().computeOnServer(s -> Orders.handle(mob(s, creeper), player(s), List.of("CEASE_HOSTILITY"), false));
            check(cd.get(0).accepted(), "creeper confiavel deveria aceitar tregua: " + cd);
            cmd.runCommand("tag " + creeper + " add cr");
            float hp0 = world.getServer().computeOnServer(s -> player(s).getHealth());
            cmd.runCommand("damage @p 6 minecraft:mob_attack by @e[tag=cr,limit=1]");
            context.waitTicks(5);
            float hp1 = world.getServer().computeOnServer(s -> player(s).getHealth());
            System.out.println("[gametest] T21: vida do jogador apos 'ataque' do creeper em tregua " + hp0 + "->" + hp1);
            check(hp1 >= hp0, "o creeper pactuado feriu o pagador durante a tregua");

            cmd.runCommand("damage @e[tag=cr,limit=1] 1 minecraft:player_attack by @p");
            context.waitTicks(5);
            var broken = world.getServer().computeOnServer(s -> new Object[] {Orders.get(mob(s, creeper)), MemoryStore.get(mob(s, creeper))});
            var o = (br.com.mobvoz.orders.MobOrders) broken[0];
            var m = (MobMemory) broken[1];
            System.out.println("[gametest] T22: truceUntil=" + o.truceUntil() + " pactBroken=" + m.counts().get("PACT_BROKEN"));
            check(o.truceUntil() == 0 && o.truceFor().isEmpty(), "a tregua deveria ter acabado");
            check(m.counts().getOrDefault("PACT_BROKEN", 0) == 1, "traicao nao registrada como fato");
        }
    }
}
