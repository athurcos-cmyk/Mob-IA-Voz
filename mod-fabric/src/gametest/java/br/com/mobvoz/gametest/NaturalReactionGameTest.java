package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.net.StatusPayload;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * Conversa natural com consequencias (LLM real, pelo modo chat): dizer ao porco que vai transforma-lo em comida faz ele FUGIR de
 * verdade; o mesmo ao Warden nao o move (natureza ancestral) mas fica registrado como medo/confianca, nunca como agressao.
 */
public class NaturalReactionGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static Mob mob(MinecraftServer s, UUID id) {
        for (ServerLevel l : s.getAllLevels()) {
            if (l.getEntity(id) instanceof Mob m) {
                return m;
            }
        }
        return null;
    }

    private static ServerPlayer player(MinecraftServer s) {
        return s.getPlayerList().getPlayers().get(0);
    }

    private static void say(ClientGameTestContext c, String text) throws InterruptedException {
        ChatMode.lastSubtitle = null;
        c.runOnClient(mc -> mc.player.connection.sendChat(text));
        long end = System.currentTimeMillis() + 90_000;
        while (ChatMode.lastSubtitle == null) {
            check(System.currentTimeMillis() < end, "sem resposta para: " + text);
            c.waitTicks(5);
            Thread.sleep(100);
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon pig 0.5 -60 3.5 {PersistenceRequired:1b,Silent:1b,Tags:[\"porco\"]}");
            context.waitTicks(10);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            UUID pig = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(pig != null, "porco nao esta na mira");
            double d0 = world.getServer().computeOnServer(s -> (double) mob(s, pig).distanceTo(player(s)));

            say(context, "Vou te transformar em comida e te comer no jantar!");
            context.waitTicks(100); // ~5 s de corrida
            double d1 = world.getServer().computeOnServer(s -> (double) mob(s, pig).distanceTo(player(s)));
            var m = world.getServer().computeOnServer(s -> MemoryStore.get(mob(s, pig)));
            float fear = m.relationOf(world.getServer().computeOnServer(s -> player(s).getUUID())).fear();
            System.out.println("[gametest] porco: distancia " + d0 + " -> " + d1 + "; medo=" + fear + "; fala='" + ChatMode.lastSubtitle + "'; fatos=" + m.counts());
            check(d1 > d0 + 4, "o porco nao fugiu (distancia " + d0 + " -> " + d1 + ")");
            check(fear > 0.2f && m.counts().getOrDefault("THREAT_SPOKEN", 0) == 1, "medo/ameaca nao registrados: " + m.counts() + " medo=" + fear);
            check(m.counts().getOrDefault("DAMAGED_BY_PLAYER", 0) == 0, "palavras nao podem virar agressao fisica");
            while (VoiceInput.status() != StatusPayload.IDLE) {
                context.waitTicks(5);
            }
            context.takeScreenshot("mobvoz_porco_foge");

            // Warden: cego e ancestral, ameaca nao o move; fica so o registro verbal
            cmd.runCommand("kill @e[type=pig]");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon warden 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}");
            context.waitTicks(20);
            UUID w = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(w != null, "Warden nao esta na mira");
            Thread.sleep(1600);
            say(context, "Vou te matar, seu monstro cego!");
            context.waitTicks(40);
            boolean targeted = world.getServer().computeOnServer(s -> mob(s, w).getTarget() != null);
            System.out.println("[gametest] Warden: fala='" + ChatMode.lastSubtitle + "'; alvo=" + targeted);
            check(!targeted, "o Warden nao deveria atacar por causa de palavras");
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
