package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.orders.OrderPolicy.Verdict;
import br.com.mobvoz.orders.Orders;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Fase 12 em jogo real (sem IA; chama o executor de ordens como o servidor faria): contratos desligados recusam; ligados, o Warden
 * segue sem ferir o dono e o trai se for ferido por ele; o Wither segue, nao quebra blocos sob contrato (com controle que quebra) e
 * suas caveiras nao ferem blocos; o Dragao faz tregua (controle: investe) e defende, mas nao segue (honesto).
 */
public class BossAllianceGameTest implements FabricClientGameTest {
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

    private static UUID spawn(TestSingleplayerContext w, String type, double x, double y, double z, boolean noAi) {
        return w.getServer().computeOnServer(s -> {
            Mob m = (Mob) EntityType.byString(type).orElseThrow().create(s.overworld(), EntitySpawnReason.COMMAND);
            m.setPos(x, y, z);
            m.setPersistenceRequired();
            m.setNoAi(noAi);
            s.overworld().addFreshEntity(m);
            return m.getUUID();
        });
    }

    private static void trust(TestSingleplayerContext w, UUID mob) {
        w.getServer().runOnServer(s -> {
            UUID p = player(s).getUUID();
            MemoryStore.update(mob(s, mob), m -> m.withRelation(p, r -> new MobMemory.Relationship(1f, 1f, 0)));
        });
    }

    private static List<br.com.mobvoz.orders.OrderPolicy.Decision> order(TestSingleplayerContext w, UUID id, String action) {
        return w.getServer().computeOnServer(s -> Orders.handle(mob(s, id), player(s), List.of(action), false));
    }

    private static double dist(TestSingleplayerContext w, UUID id) {
        return w.getServer().computeOnServer(s -> mob(s, id) == null ? -1.0 : (double) mob(s, id).distanceTo(player(s)));
    }

    private static String describe(TestSingleplayerContext w, UUID id) {
        return w.getServer().computeOnServer(s -> {
            Mob m = mob(s, id);
            return m == null ? "SUMIU" : m.position() + " pose=" + m.getPose() + " vida=" + m.getHealth() + " ordens=" + Orders.get(m).mode();
        });
    }

    private static float health(TestSingleplayerContext w) {
        return w.getServer().computeOnServer(s -> player(s).getHealth());
    }

    private static int solidCount(TestSingleplayerContext w, int x, int y, int z, int r, net.minecraft.world.level.block.Block b) {
        return w.getServer().computeOnServer(s -> {
            int n = 0;
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = 0; dy <= 4; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (s.overworld().getBlockState(new BlockPos(x + dx, y + dy, z + dz)).is(b)) {
                            n++;
                        }
                    }
                }
            }
            return n;
        });
    }

    /** Preenche com pedra o entorno do Wither parado (3x4x3 acima do pe, onde ele quebra) e o fere sem dono; devolve quantos blocos restam. */
    private static int hurtAndCount(ClientGameTestContext c, TestSingleplayerContext w, UUID wither, int x, int z) {
        w.getServer().runCommand("fill " + (x - 1) + " -58 " + (z - 1) + " " + (x + 1) + " -55 " + (z + 1) + " minecraft:stone");
        w.getServer().runOnServer(s -> {
            Mob m = mob(s, wither);
            m.hurtServer(s.overworld(), s.overworld().damageSources().generic(), 1f);
        });
        c.waitTicks(60);
        return solidCount(w, x, -58, z, 1, Blocks.STONE);
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig cfg = new ServerConfig();
        cfg.bossContractsEnabled = false;
        ServerConfig.set(cfg);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode survival @p");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");

            // ---- B0: contratos desligados (padrao): o Warden recusa, mesmo com confianca maxima ----
            UUID warden = spawn(world, "minecraft:warden", 0.5, -60, 14.5, false);
            world.getServer().runOnServer(sv -> br.com.mobvoz.orders.BossAdapter.keepAboveGround((Warden) mob(sv, warden))); // como o spawn vanilla
            trust(world, warden);
            var off = order(world, warden, "FOLLOW_PLAYER").get(0);
            check(off.verdict() == Verdict.REFUSED, "contratos desligados deveriam recusar: " + off);
            System.out.println("[gametest] B0 contratos desligados -> " + off.verdict());

            // ---- B1: Warden aliado segue e NAO fere o dono ----
            ServerConfig on = new ServerConfig();
            on.bossContractsEnabled = true;
            ServerConfig.set(on);
            var d1 = order(world, warden, "FOLLOW_PLAYER").get(0);
            check(d1.accepted(), "Warden de confianca maxima deveria aceitar seguir: " + d1);
            cmd.runCommand("tp @p 0.5 -60 40.5 0 17");
            for (int i = 0; i < 8; i++) {
                context.waitTicks(50);
                System.out.println("[gametest] B1 t=" + (i + 1) * 50 + ": " + describe(world, warden) + " dist=" + dist(world, warden));
            }
            double dw = dist(world, warden);
            System.out.println("[gametest] B1 Warden seguiu: distancia = " + dw + ", vida do jogador = " + health(world));
            check(dw < 10, "o Warden nao seguiu o dono (dist " + dw + ")");
            check(health(world) >= 20f, "o Warden aliado feriu o dono (vida " + health(world) + ")");

            // ---- B2: traicao por gatilho: o dono fere o Warden -> contrato rompido e ele se volta contra o dono (visto no mesmo tick,
            //      antes de o golpe de 30 de dano matar o jogador) ----
            boolean[] r = world.getServer().computeOnServer(s -> {
                Warden w = (Warden) mob(s, warden);
                w.hurtServer(s.overworld(), s.overworld().damageSources().playerAttack(player(s)), 2f);
                return new boolean[] {Orders.get(w).owner().isEmpty() && !Orders.get(w).defend(),
                        w.getTarget() == player(s) || w.getAngerManagement().getActiveAnger(player(s)) > 0};
            });
            System.out.println("[gametest] B2 traicao: contrato rompido = " + r[0] + ", Warden mira o dono = " + r[1]);
            check(r[0], "ferir o Warden aliado deveria romper o contrato");
            check(r[1], "o Warden traido deveria se voltar contra o dono");
            cmd.runCommand("kill @e[type=minecraft:warden]");
            cmd.runCommand("effect give @p minecraft:instant_health 1 10");
            cmd.runCommand("gamemode creative @p");
            context.waitTicks(20);

            // ---- B3: Wither parado: CONTROLE sem contrato quebra blocos; com contrato nao quebra ----
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            UUID wA = spawn(world, "minecraft:wither", 30.5, -58, 30.5, false);
            int left = hurtAndCount(context, world, wA, 30, 30);
            System.out.println("[gametest] B3 controle (sem contrato): blocos de pedra restantes = " + left + " de 36");
            check(left < 36, "controle falhou: o Wither sem contrato deveria quebrar blocos (restaram " + left + ")");
            UUID wB = spawn(world, "minecraft:wither", 14.5, -58, 30.5, false);
            trust(world, wB);
            var d3 = order(world, wB, "STAY_AT").get(0);
            check(d3.accepted(), "Wither de confianca maxima deveria aceitar ficar de guarda: " + d3);
            int keep = hurtAndCount(context, world, wB, 14, 30);
            System.out.println("[gametest] B3 com contrato: blocos de pedra restantes = " + keep + " de 36");
            check(keep == 36, "o Wither sob contrato quebrou blocos (restaram " + keep + ")");

            // ---- B4: caveira do Wither sob contrato nao fere blocos (controle: sem contrato fere) ----
            int[] results = new int[2];
            for (int i = 0; i < 2; i++) {
                UUID shooter = i == 0 ? wA : wB;
                int bx = 22 + i * 6;
                cmd.runCommand("setblock " + bx + " -60 26 minecraft:dirt");
                cmd.runOnServer(s -> {
                    ServerLevel l = s.overworld();
                    WitherSkull sk = new WitherSkull(l, mob(s, shooter), new Vec3(0, 0, 0.5)); // voa para o sul ate a terra
                    sk.setPos(bx + 0.5, -59.5, 23.5);
                    l.addFreshEntity(sk);
                });
                context.waitTicks(30);
                results[i] = world.getServer().computeOnServer(s -> s.overworld().getBlockState(new BlockPos(bx, -60, 26)).is(Blocks.DIRT) ? 1 : 0);
            }
            System.out.println("[gametest] B4 caveira: bloco inteiro sem contrato=" + results[0] + " (esperado 0), com contrato=" + results[1] + " (esperado 1)");
            check(results[0] == 0, "controle falhou: a caveira sem contrato deveria quebrar a terra");
            check(results[1] == 1, "a caveira do Wither sob contrato quebrou o bloco");
            cmd.runCommand("kill @e[type=minecraft:wither]");

            cmd.runCommand("gamemode survival @p");
            // ---- B5: Dragao: nao segue (honesto), trégua para a investida (controle: sem trégua continua), defende ----
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            UUID dragon = spawn(world, "minecraft:ender_dragon", 0.5, -20, 28.5, false);
            trust(world, dragon);
            var dfl = order(world, dragon, "FOLLOW_PLAYER").get(0);
            check(dfl.verdict() == Verdict.UNSUPPORTED, "o Dragao nao deveria seguir: " + dfl);
            Vec3 target = world.getServer().computeOnServer(s -> player(s).position());
            // controle: investida sem trégua segue ate perto do alvo
            world.getServer().runOnServer(s -> {
                EnderDragon d = (EnderDragon) mob(s, dragon);
                d.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                d.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(target);
            });
            context.waitTicks(15);
            boolean charging0 = world.getServer().computeOnServer(s -> ((EnderDragon) mob(s, dragon)).getPhaseManager().getCurrentPhase().getPhase()
                    == EnderDragonPhase.CHARGING_PLAYER);
            System.out.println("[gametest] B5 controle: investindo sem trégua apos 15 ticks = " + charging0);
            check(charging0, "controle falhou: a investida deveria continuar sem trégua");
            var dtr = order(world, dragon, "CEASE_HOSTILITY").get(0);
            check(dtr.accepted(), "o Dragao de confianca maxima deveria aceitar trégua: " + dtr);
            context.waitTicks(25);
            boolean charging1 = world.getServer().computeOnServer(s -> {
                var ph = ((EnderDragon) mob(s, dragon)).getPhaseManager().getCurrentPhase().getPhase();
                return ph == EnderDragonPhase.CHARGING_PLAYER || ph == EnderDragonPhase.STRAFE_PLAYER;
            });
            System.out.println("[gametest] B5 com trégua: ainda investe/ataca em rajada = " + charging1);
            check(!charging1, "o Dragao em trégua continuou atacando o jogador");
            var ddf = order(world, dragon, "DEFEND_PLAYER").get(0);
            check(ddf.accepted(), "o Dragao de confianca maxima deveria aceitar defender: " + ddf);
            UUID zombie = spawn(world, "minecraft:zombie", 3.5, -60, 3.5, true);
            world.getServer().runOnServer(s -> {
                Mob z = mob(s, zombie);
                player(s).hurtServer(s.overworld(), s.overworld().damageSources().mobAttack(z), 1f);
            });
            context.waitTicks(3);
            boolean charges = world.getServer().computeOnServer(s -> ((EnderDragon) mob(s, dragon)).getPhaseManager().getCurrentPhase().getPhase()
                    == EnderDragonPhase.CHARGING_PLAYER);
            System.out.println("[gametest] B5 defesa: Dragao investe contra o agressor do jogador = " + charges);
            check(charges, "o Dragao defensor deveria investir contra o agressor");
            cmd.runCommand("kill @e[type=minecraft:ender_dragon]");
            cmd.runCommand("kill @e[type=minecraft:zombie]");
            cmd.runCommand("effect give @p minecraft:instant_health 1 10");

            // ---- B6: Wither aliado voa ate o dono e NAO o fere (jogador em sobrevivencia) ----
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            UUID wf = spawn(world, "minecraft:wither", 12.5, -57, 12.5, false);
            trust(world, wf);
            var d6 = order(world, wf, "FOLLOW_PLAYER").get(0);
            check(d6.accepted(), "Wither de confianca maxima deveria aceitar seguir: " + d6);
            cmd.runCommand("tp @p 0.5 -60 28.5 0 17");
            context.waitTicks(300);
            double dwf = dist(world, wf);
            System.out.println("[gametest] B6 Wither seguiu: distancia = " + dwf + ", vida do jogador = " + health(world));
            check(dwf >= 0 && dwf < 12, "o Wither nao seguiu o dono (dist " + dwf + ")");
            check(health(world) >= 20f, "o Wither aliado feriu o dono (vida " + health(world) + ")");
            cmd.runCommand("kill @e[type=minecraft:wither]");
        } finally {
            ServerConfig.set(new ServerConfig());
        }
    }
}
