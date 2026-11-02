package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.social.Attention;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * A criatura com quem o jogador vai falar nao vai embora: enquanto ela esta na mira (e por uns segundos depois que a mira escapa), o servidor a segura
 * parada, virada para o jogador e perto dele. Vale para quem usa voz ou chat. Com {@code conversationAttention=false} ela segue a vida.
 */
public class AttentionGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static double[] pos(TestSingleplayerContext w, UUID id) {
        return w.getServer().computeOnServer(s -> {
            for (ServerLevel l : s.getAllLevels()) {
                if (l.getEntity(id) instanceof Mob m) {
                    return new double[] {m.getX(), m.getZ(), m.distanceTo(s.getPlayerList().getPlayers().get(0))};
                }
            }
            return new double[] {0, 0, 99};
        });
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig.set(new ServerConfig());
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set day");
            cmd.runCommand("weather clear");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon cow 0.5 -60 5.5 {PersistenceRequired:1b,Tags:[\"vaca\"]}"); // com IA: sem a atencao ela passeia
            context.waitTicks(20);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            UUID cow = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(cow != null, "a vaca deveria estar na mira");

            // 1) na mira: o cliente avisa e o servidor segura; ela chega perto e fica parada olhando
            context.waitTicks(100);
            check(Attention.isHolding(cow), "o servidor deveria estar segurando a vaca");
            double[] p0 = pos(world, cow);
            System.out.println("[gametest] ATENCAO vaca a " + String.format("%.1f", p0[2]) + " blocos do jogador");
            check(p0[2] <= 3.6, "a vaca deveria ter chegado perto (ate ~3 blocos): " + p0[2]);
            context.waitTicks(60);
            double[] p1 = pos(world, cow);
            double moved = Math.hypot(p1[0] - p0[0], p1[1] - p0[1]);
            System.out.println("[gametest] ATENCAO vaca andou " + String.format("%.2f", moved) + " blocos em 3 s parada");
            check(moved < 0.6, "a vaca deveria ficar parada: andou " + moved);
            float yawErr = world.getServer().computeOnServer(s -> {
                Mob m = null;
                for (ServerLevel l : s.getAllLevels()) {
                    if (l.getEntity(cow) instanceof Mob mm) {
                        m = mm;
                    }
                }
                var p = s.getPlayerList().getPlayers().get(0);
                double dx = p.getX() - m.getX();
                double dz = p.getZ() - m.getZ();
                float want = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
                return Math.abs(net.minecraft.util.Mth.wrapDegrees(m.getYHeadRot() - want));
            });
            System.out.println("[gametest] ATENCAO erro de direcao da cabeca: " + yawErr + " graus");
            check(yawErr < 40, "a vaca deveria estar virada para o jogador: " + yawErr);

            // 2) a mira escapa (o jogador vira o mouse): a estrela fica uns segundos e depois solta
            cmd.runCommand("tp @p 0.5 -60 0.5 60 17");
            context.waitTicks(20);
            check(context.computeOnClient(mc -> MobVozClient.target() != null), "a folga da mira deveria manter o alvo 1 s depois de a mira escapar");

            // 2b) a folga FUNCIONA, nao e so a estrela: com o mouse longe da vaca, a fala dentro da folga e respondida (o servidor sabe que foi ela que o jogador mirou)
            context.runOnClient(mc -> br.com.mobvoz.client.ChatMode.lastSubtitle = null);
            context.runOnClient(mc -> net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new br.com.mobvoz.net.UtterancePayload(cow, "Oi vaca, tudo bem?", true)));
            long end = System.currentTimeMillis() + 60_000;
            while (br.com.mobvoz.client.ChatMode.lastSubtitle == null && System.currentTimeMillis() < end) {
                context.waitTicks(10);
            }
            System.out.println("[gametest] ATENCAO resposta com o mouse longe (dentro da folga): " + br.com.mobvoz.client.ChatMode.lastSubtitle);
            check(br.com.mobvoz.client.ChatMode.lastSubtitle != null, "a fala dentro da folga da mira deveria ser respondida");
            while (br.com.mobvoz.client.voice.VoiceInput.status() != br.com.mobvoz.net.StatusPayload.IDLE && System.currentTimeMillis() < end) {
                context.waitTicks(10);
            }
            context.waitTicks(200); // a folga acaba
            context.waitTicks(40);
            check(context.computeOnClient(mc -> MobVozClient.target() == null), "depois da folga o alvo deveria soltar");

            // 3) desligado: ela segue a vida (o servidor nao a segura)
            // 2c) passada a folga, falar com a vaca sem mirar NAO e aceito (um alvo por fala: o servidor nao responde a quem nao esta mirando)
            context.runOnClient(mc -> br.com.mobvoz.client.ChatMode.lastSubtitle = null);
            context.runOnClient(mc -> net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new br.com.mobvoz.net.UtterancePayload(cow, "Oi de novo!", true)));
            context.waitTicks(160);
            check(br.com.mobvoz.client.ChatMode.lastSubtitle == null, "sem mirar e fora da folga a fala nao deveria ser respondida: " + br.com.mobvoz.client.ChatMode.lastSubtitle);

            ServerConfig off = new ServerConfig();
            off.conversationAttention = false;
            ServerConfig.set(off);
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("tp @e[tag=vaca] 0.5 -60 5.5");
            context.waitTicks(100);
            check(!Attention.isHolding(cow), "com conversationAttention=false ninguem e segurado");
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
            AABB unused = new AABB(0, 0, 0, 1, 1, 1);
            check(unused != null, "ok");
        }
    }
}
