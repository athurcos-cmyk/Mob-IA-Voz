package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
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
 * Os do Nether SEGUEM e fazem alianca: piglin, piglin zumbificado, hoglin e esqueleto wither aceitam "me segue" (o piglin e o hoglin tem IA por "brain", e
 * andam ate o jogador pelo adaptador do brain), nao miram quem os comanda e se afastam do ponto de partida junto com ele. O Ghast, que voa, recusa
 * com um bom motivo (e o Strider/Blaze seguem como os demais). Tudo no mundo comum (hoglin e piglin imunes a zumbificacao).
 */
public class NetherAlliesGameTest implements FabricClientGameTest {
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
            if (m instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin p) {
                p.setImmuneToZombification(true);
            }
            if (m instanceof net.minecraft.world.entity.monster.hoglin.Hoglin h) {
                h.setImmuneToZombification(true);
            }
            s.overworld().addFreshEntity(m);
            return m.getUUID();
        });
    }

    private static void trust(TestSingleplayerContext w, UUID mob) {
        w.getServer().runOnServer(s -> {
            UUID p = player(s).getUUID();
            MemoryStore.update(mob(s, mob), m -> m.withRelation(p, r -> new MobMemory.Relationship(0.95f, 0.95f, 0)));
        });
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        var cfg = new ServerConfig();
        cfg.ambientChatter = false;
        cfg.mobsInitiate = false;
        ServerConfig.set(cfg);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set day");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("gamerule doMobSpawning false");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("forceload add -30 -30 30 30");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");

            // 1) quem segue: aceita o pedido, e anda ate o jogador quando ele se afasta
            record Case(String name, EntityType<?> type, double x) {
            }
            List<Case> cases = List.of(new Case("piglin", EntityType.PIGLIN, 3.5), new Case("piglin zumbificado", EntityType.ZOMBIFIED_PIGLIN, -3.5),
                    new Case("hoglin", EntityType.HOGLIN, 6.5), new Case("esqueleto wither", EntityType.WITHER_SKELETON, -6.5), new Case("blaze", EntityType.BLAZE, 9.5));
            List<UUID> ids = new java.util.ArrayList<>();
            for (Case c : cases) {
                UUID id = spawn(world, c.type(), c.x(), 3.5);
                ids.add(id);
                trust(world, id);
            }
            context.waitTicks(10);
            for (int i = 0; i < cases.size(); i++) {
                UUID id = ids.get(i);
                var res = world.getServer().computeOnServer(s -> Orders.handle(mob(s, id), player(s), List.of("FOLLOW_PLAYER", "DEFEND_PLAYER"), false));
                System.out.println("[gametest] NETHER1 " + cases.get(i).name() + ": " + res);
                check(res.stream().allMatch(d -> d.accepted() || d.verdict() == OrderPolicy.Verdict.ALREADY), cases.get(i).name() + " deveria aceitar seguir e defender: " + res);
            }
            // o jogador se afasta: os aliados vao atras
            cmd.runCommand("tp @p 0.5 -60 -22.5 0 17");
            context.waitTicks(260);
            for (int i = 0; i < cases.size(); i++) {
                UUID id = ids.get(i);
                double d = world.getServer().computeOnServer(s -> (double) mob(s, id).distanceTo(player(s)));
                System.out.println("[gametest] NETHER2 " + cases.get(i).name() + " ficou a " + Math.round(d * 10) / 10.0 + " blocos do jogador (comecou a ~26)");
                check(d < 12, cases.get(i).name() + " deveria ter vindo atras do jogador, mas esta a " + d + " blocos");
            }
            // ... e ninguem feriu o dono (criativo: sem dano) nem o mira
            for (int i = 0; i < cases.size(); i++) {
                UUID id = ids.get(i);
                boolean targetsOwner = world.getServer().computeOnServer(s -> mob(s, id).getTarget() == player(s));
                check(!targetsOwner, cases.get(i).name() + " nao deveria mirar quem o comanda");
            }

            // 2) o Ghast voa: recusa, com um bom motivo (nada de "sem adaptador")
            UUID ghast = spawn(world, EntityType.GHAST, 4.5, -18.5);
            trust(world, ghast);
            var gres = world.getServer().computeOnServer(s -> Orders.handle(mob(s, ghast), player(s), List.of("FOLLOW_PLAYER"), false));
            System.out.println("[gametest] NETHER3 ghast: " + gres);
            check(gres.size() == 1 && !gres.get(0).accepted() && !gres.get(0).reason().toLowerCase().contains("adaptador")
                    && gres.get(0).reason().length() > 30, "o ghast deveria recusar com um bom motivo: " + gres);
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
