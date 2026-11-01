package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.orders.Orders;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;

/**
 * Warden e Wither aliados so atacam quem e hostil ao dono (monstros, quem o mira ou o feriu), nunca porco, vaca, aldeao ou aliado, embora
 * sozinhos ataquem tudo. Sem pacto continuam os mesmos de sempre (controle). Contratos com chefes vem LIGADOS por padrao.
 */
public class BossAlliesGameTest implements FabricClientGameTest {
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
            for (Mob m : l.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(-60, -70, -60, 60, 40, 60), x -> x.getTags().contains(tag))) {
                return m;
            }
        }
        return null;
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        check(new ServerConfig().bossContractsEnabled, "contratos com chefes deveriam vir ligados por padrao");
        ServerConfig.set(new ServerConfig());
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("gamerule doMobGriefing false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon warden 6.5 -60 6.5 {PersistenceRequired:1b,Tags:[\"w\"]}");
            cmd.runCommand("summon wither 12.5 -58 6.5 {PersistenceRequired:1b,NoAI:1b,Tags:[\"wt\"]}");
            cmd.runCommand("summon pig 8.5 -60 6.5 {PersistenceRequired:1b,Tags:[\"porco\"]}");
            cmd.runCommand("summon cow 9.5 -60 8.5 {PersistenceRequired:1b,Tags:[\"vaca\"]}");
            cmd.runCommand("summon zombie 14.5 -60 8.5 {PersistenceRequired:1b,NoAI:1b,Silent:1b,Tags:[\"zumbi\"]}");
            cmd.runCommand("summon wolf 7.5 -60 10.5 {PersistenceRequired:1b,Tags:[\"lobo\"]}");
            context.waitTicks(40);

            // controle: sem pacto os chefes continuam atacando tudo (Warden por raiva, Wither pelo seletor)
            boolean[] free = world.getServer().computeOnServer(s -> new boolean[] {
                    ((Warden) byTag(s, "w")).canTargetEntity(byTag(s, "porco")),
                    ((WitherBoss) byTag(s, "wt")).canAttack(byTag(s, "porco"))});
            System.out.println("[gametest] BOSSALLIES sem pacto: Warden mira porco=" + free[0] + ", Wither mira porco=" + free[1]);
            check(free[0] && free[1], "sem pacto os chefes continuam atacando tudo: " + free[0] + " " + free[1]);

            // pacto: quem paga ou ganha confianca passa a seguir
            var dw = world.getServer().computeOnServer(s -> Orders.handle(byTag(s, "w"), player(s), List.of("FOLLOW_PLAYER"), false, 0, true).get(0));
            var dt = world.getServer().computeOnServer(s -> Orders.handle(byTag(s, "wt"), player(s), List.of("FOLLOW_PLAYER"), false, 0, true).get(0));
            check(dw.accepted() && dt.accepted(), "com contratos ligados e pagos os chefes aceitam seguir: " + dw + " / " + dt);

            record Row(String what, boolean wardenTargets, boolean witherTargets) {
            }
            java.util.function.Function<String, Row> probe = tag -> world.getServer().computeOnServer(s -> {
                LivingEntity t = tag.equals("jogador") ? player(s) : byTag(s, tag);
                return new Row(tag, ((Warden) byTag(s, "w")).canTargetEntity(t), ((WitherBoss) byTag(s, "wt")).canAttack(t));
            });
            for (String peace : List.of("porco", "vaca", "lobo", "jogador")) {
                Row r = probe.apply(peace);
                System.out.println("[gametest] BOSSALLIES aliados x " + peace + ": Warden mira=" + r.wardenTargets() + ", Wither mira=" + r.witherTargets());
                check(!r.wardenTargets() && !r.witherTargets(), "chefe aliado nao pode atacar " + peace + ": " + r);
            }
            Row hostile = probe.apply("zumbi");
            System.out.println("[gametest] BOSSALLIES aliados x zumbi: Warden mira=" + hostile.wardenTargets() + ", Wither mira=" + hostile.witherTargets());
            check(hostile.wardenTargets() && hostile.witherTargets(), "chefe aliado ataca monstro: " + hostile);

            // quem ataca o dono passa a ser hostil a ele: a vaca (que nao e monstro) mirando o jogador entra na lista
            world.getServer().runOnServer(s -> {
                Mob cow = byTag(s, "vaca");
                cow.setTarget(player(s));
            });
            Row angry = probe.apply("vaca");
            System.out.println("[gametest] BOSSALLIES vaca mirando o dono: Warden mira=" + angry.wardenTargets() + ", Wither mira=" + angry.witherTargets());
            check(angry.wardenTargets() && angry.witherTargets(), "quem ataca o dono e hostil a ele: " + angry);
            world.getServer().runOnServer(s -> byTag(s, "vaca").setTarget(null));

            // na pratica: o Warden aliado fica entre porco, vaca e lobo por 20 s sem machuca-los
            UUID w = world.getServer().computeOnServer(s -> byTag(s, "w").getUUID());
            for (int i = 0; i < 8; i++) {
                context.waitTicks(50);
                System.out.println("[gametest] BOSSALLIES t+" + (i + 1) + "x1s warden: " + world.getServer().computeOnServer(s -> {
                    Mob m = null;
                    for (ServerLevel l : s.getAllLevels()) {
                        if (l.getEntity(w) instanceof Mob mm) {
                            m = mm;
                        }
                    }
                    return m == null ? "SUMIU (warden no mundo: " + s.overworld().getEntities(net.minecraft.world.entity.EntityType.WARDEN, e -> true).size() + ")" : m.blockPosition().toShortString() + " vivo=" + m.isAlive() + " pose=" + m.getPose() + " dig=" + m.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.DIG_COOLDOWN) + " atividades=" + m.getBrain().getActiveActivities() + " vida=" + m.getHealth() + " alvo=" + m.getTarget() + " ordens=" + Orders.get(m).mode();
                }));
            }
            float[] hp = world.getServer().computeOnServer(s -> new float[] {byTag(s, "porco").getHealth(), byTag(s, "vaca").getHealth(), byTag(s, "lobo").getHealth()});
            System.out.println("[gametest] BOSSALLIES vida depois de 20 s ao lado do Warden aliado: porco=" + hp[0] + " vaca=" + hp[1] + " lobo=" + hp[2]);
            check(hp[0] >= 10 && hp[1] >= 10 && hp[2] >= 8, "o Warden aliado machucou os bichos pacificos: " + java.util.Arrays.toString(hp));
            check(world.getServer().computeOnServer(s -> Orders.get(byTag(s, "w")).owner().equals(player(s).getUUID().toString())), "o dono e o jogador: " + w);
        }
    }
}
