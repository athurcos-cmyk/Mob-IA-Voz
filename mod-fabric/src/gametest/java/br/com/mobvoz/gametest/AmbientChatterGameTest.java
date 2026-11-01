package br.com.mobvoz.gametest;

import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryRules;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.net.UtterancePayload;
import br.com.mobvoz.social.Ambient;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * Mundo vivo + nome do jogador, em jogo real (sem IA nas falas ambiente): (1) dois aldeoes calmos conversam sozinhos perto do jogador,
 * com legenda e voz; (2) quem sabe o nome conta ao outro, que o aprende (cada criatura tem a sua consciencia); (3) o jogador diz o nome a
 * UMA criatura e so ela o guarda. Chamado "Humano" ate la.
 */
public class AmbientChatterGameTest implements FabricClientGameTest {
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

    private static String nameOf(TestSingleplayerContext w, String tag) {
        return w.getServer().computeOnServer(s -> MemoryStore.get(byTag(s, tag)).nameOf(player(s).getUUID()));
    }

    private static void waitScene(ClientGameTestContext c, int sceneFrom, int linesFrom) throws InterruptedException {
        long end = System.currentTimeMillis() + 90_000;
        while ((Ambient.scenesPlayed() <= sceneFrom || Ambient.linesSpoken() - linesFrom < 2) && System.currentTimeMillis() < end) {
            c.waitTicks(20);
            Thread.sleep(100);
        }
        check(Ambient.scenesPlayed() > sceneFrom, "nenhuma conversa ambiente comecou em 90 s");
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig.set(new ServerConfig()); // ambientChatter e chatterEnabled ligados
        Ambient.reset();
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set day");
            cmd.runCommand("weather clear");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("gamerule doWeatherCycle false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon villager 6.5 -60 6.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"v1\"]}");
            cmd.runCommand("summon villager 8.5 -60 6.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"v2\"]}");
            cmd.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
            context.waitTicks(40);

            // 1) conversa ambiente espontanea (assunto livre do dia claro)
            int s0 = Ambient.scenesPlayed();
            int l0 = Ambient.linesSpoken();
            waitScene(context, s0, l0);
            Thread.sleep(8000); // deixa a cena terminar de falar
            System.out.println("[gametest] AMB1 conversa ambiente: cenas=" + (Ambient.scenesPlayed() - s0) + " falas=" + (Ambient.linesSpoken() - l0)
                    + " ultimas faladas=" + br.com.mobvoz.server.ConversationService.announced());
            check(Ambient.linesSpoken() - l0 >= 2, "a cena ambiente deveria ter ao menos 2 falas");

            // 2) fofoca do nome: v1 sabe o nome e conta; v2 aprende. A vaca (longe) continua sem saber.
            UUID pid = world.getServer().computeOnServer(s -> player(s).getUUID());
            world.getServer().runOnServer(s -> MemoryStore.update(byTag(s, "v1"), m -> MemoryRules.firstMeeting(m, pid, 0).withName(pid, "Arthur")));
            check(nameOf(world, "v2").isEmpty(), "v2 nao deveria saber o nome antes da fofoca");
            Ambient.reset();
            Ambient.setOnlyFamily("jogador_nome_conta");
            s0 = Ambient.scenesPlayed();
            l0 = Ambient.linesSpoken();
            waitScene(context, s0, l0);
            Ambient.setOnlyFamily(null);
            context.waitTicks(20);
            System.out.println("[gametest] AMB2 fofoca do nome: v1='" + nameOf(world, "v1") + "' v2='" + nameOf(world, "v2") + "' vaca='" + nameOf(world, "vaca") + "'");
            check("Arthur".equals(nameOf(world, "v2")), "v2 deveria ter aprendido o nome ouvindo v1");
            check(nameOf(world, "vaca").isEmpty(), "a vaca nao ouviu a conversa e nao deveria saber o nome");
            Thread.sleep(6000);

            // 3) o jogador diz o nome a UMA criatura (texto digitado): so a vaca da mira aprende
            Ambient.setOnlyFamily("__nenhuma__"); // sem conversas ambiente atrapalhando a sessao
            context.waitTicks(40);
            UUID cow = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(cow != null, "a vaca nao esta na mira");
            context.runOnClient(mc -> ClientPlayNetworking.send(new UtterancePayload(cow, "Oi! Meu nome é Bia.", true)));
            long end = System.currentTimeMillis() + 15_000;
            while (!"Bia".equals(nameOf(world, "vaca")) && System.currentTimeMillis() < end) {
                context.waitTicks(5);
                Thread.sleep(100);
            }
            System.out.println("[gametest] AMB3 nome dito a vaca: vaca='" + nameOf(world, "vaca") + "' v1='" + nameOf(world, "v1") + "'");
            check("Bia".equals(nameOf(world, "vaca")), "a vaca deveria guardar o nome dito pelo jogador");
            check("Arthur".equals(nameOf(world, "v1")), "o nome que v1 sabia nao deveria mudar");
            Thread.sleep(8000); // deixa a conversa terminar

            // 4) atras de uma parede ninguem ve o jogador: nao comentam o que ele segura. Tirada a parede, comentam.
            cmd.runCommand("fill 4 -61 4 11 -57 9 stone hollow");
            cmd.runCommand("item replace entity @p weapon.mainhand with minecraft:diamond");
            Ambient.reset();
            Ambient.setOnlyFamily("jogador_item");
            int s1 = Ambient.scenesPlayed();
            long quiet = System.currentTimeMillis() + 40_000; // ~8 sorteios (40% a cada 5 s): sem a regra, quase certo que falariam
            while (System.currentTimeMillis() < quiet) {
                context.waitTicks(20);
                Thread.sleep(100);
            }
            System.out.println("[gametest] AMB4 atras da parede: cenas sobre o item=" + (Ambient.scenesPlayed() - s1));
            check(Ambient.scenesPlayed() == s1, "falaram do item do jogador atras de uma parede");
            cmd.runCommand("fill 4 -60 4 11 -57 9 air");
            int l1 = Ambient.linesSpoken();
            waitScene(context, s1, l1);
            System.out.println("[gametest] AMB4 sem a parede: cenas=" + (Ambient.scenesPlayed() - s1));
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            Ambient.setOnlyFamily(null);
            Ambient.reset();
            ServerConfig.set(new ServerConfig());
        }
    }
}
