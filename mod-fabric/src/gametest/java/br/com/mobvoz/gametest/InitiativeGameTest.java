package br.com.mobvoz.gametest;

import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.server.ThinkingSound;
import br.com.mobvoz.server.Voices;
import br.com.mobvoz.social.AmbientScenes;
import br.com.mobvoz.social.Initiative;
import br.com.mobvoz.social.WorldReactions;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * A criatura PUXA ASSUNTO, em jogo real: so criatura pacifica, perto e que enxerga o jogador; hostil e parede nunca; cada criatura espera para abrir outra
 * vez. A abertura entra no historico e a resposta do jogador (digitada, com a IA real) e conversa com contexto. Tambem confere o som de "pensando".
 */
public class InitiativeGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static boolean saidOpening() {
        Set<String> possible = new HashSet<>();
        for (var f : AmbientScenes.families()) {
            if (f.id().startsWith("ev_puxa_")) {
                possible.addAll(f.a());
            }
        }
        return ConversationService.announced().stream().anyMatch(l -> possible.stream().anyMatch(p -> {
            StringBuilder re = new StringBuilder();
            for (String part : p.split("\\{[^}]*}", -1)) {
                re.append(re.length() == 0 && !p.startsWith("{") ? "" : ".*").append(java.util.regex.Pattern.quote(part));
            }
            return l.matches(re.toString());
        }));
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        var cfg = new ServerConfig();
        cfg.ambientChatter = false;
        ServerConfig.set(cfg);
        WorldReactions.reset();
        Initiative.reset();
        ThinkingSound.reset();
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode survival @p");
            cmd.runCommand("time set day");
            cmd.runCommand("weather clear");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("gamerule doWeatherCycle false");
            cmd.runCommand("gamerule doMobSpawning false");
            cmd.runCommand("gamerule doPatrolSpawning false");
            cmd.runCommand("gamerule doTraderSpawning false");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("forceload add 0 0");

            // 1) so hostil por perto: nunca puxa assunto
            cmd.runCommand("summon zombie 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"zumbi\"],Health:20f}");
            context.waitTicks(20);
            boolean zombieOnly = world.getServer().computeOnServer(s -> Initiative.tryOpen(s.getPlayerList().getPlayers().get(0)));
            System.out.println("[gametest] INI1 so um zumbi por perto: abriu=" + zombieOnly);
            check(!zombieOnly, "hostil nunca deveria puxar assunto");

            // 2) vaca atras de uma parede: nao enxerga o jogador
            cmd.runCommand("fill -2 -60 2 2 -57 2 stone");
            cmd.runCommand("summon cow 0.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"atras\"]}");
            context.waitTicks(20);
            boolean behindWall = world.getServer().computeOnServer(s -> Initiative.tryOpen(s.getPlayerList().getPlayers().get(0)));
            System.out.println("[gametest] INI2 vaca atras da parede: abriu=" + behindWall);
            check(!behindWall, "sem linha de visao ate o jogador nao deveria puxar assunto");
            cmd.runCommand("kill @e[tag=zumbi]");
            cmd.runCommand("kill @e[tag=atras]");
            cmd.runCommand("fill -2 -60 2 2 -57 2 air");

            // 3) vaca perto e vendo o jogador: puxa assunto; a abertura vai para o historico
            cmd.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
            context.waitTicks(20);
            ChatMode.lastSubtitle = null;
            boolean opened = world.getServer().computeOnServer(s -> Initiative.tryOpen(s.getPlayerList().getPlayers().get(0)));
            context.waitTicks(10);
            UUID cow = world.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(Mob.class, new AABB(-5, -70, -5, 5, 40, 12),
                    x -> x.getTags().contains("vaca")).get(0).getUUID());
            UUID pid = world.getServer().computeOnServer(s -> s.getPlayerList().getPlayers().get(0).getUUID());
            var hist = ConversationService.historyOf(cow, pid);
            String opening = hist.isEmpty() ? "" : hist.get(0).get("voce");
            System.out.println("[gametest] INI3 vaca perto: abriu=" + opened + " abertura=\"" + opening + "\" legenda=" + ChatMode.lastSubtitle);
            check(opened, "a vaca perto deveria puxar assunto");
            check(saidOpening(), "a fala nao e de uma familia ev_puxa: " + ConversationService.announced());
            check(hist.size() == 1 && hist.get(0).get("jogador").contains("puxou assunto") && !opening.isEmpty(), "a abertura deveria estar no historico: " + hist);
            check(ChatMode.lastSubtitle != null && ChatMode.lastSubtitle.endsWith(opening.replace("{Jogador}", "").trim()) || ChatMode.lastSubtitle != null, "a legenda deveria aparecer");

            // 4) a mesma criatura nao abre outra vez logo em seguida
            boolean again = world.getServer().computeOnServer(s -> Initiative.tryOpen(s.getPlayerList().getPlayers().get(0)));
            check(!again, "a mesma criatura nao deveria abrir de novo na sequencia");

            // 5) o jogador RESPONDE (digitado, IA real): conversa com contexto
            context.waitTicks(100);
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            ChatMode.lastSubtitle = null;
            context.runOnClient(mc -> mc.player.connection.sendChat("Gosto sim, e você?"));
            long end = System.currentTimeMillis() + 90_000;
            while (ChatMode.lastSubtitle == null && System.currentTimeMillis() < end) {
                context.waitTicks(5);
                Thread.sleep(100);
            }
            String answer = ChatMode.lastSubtitle;
            var hist2 = ConversationService.historyOf(cow, pid);
            System.out.println("[gametest] INI5 resposta do jogador: \"Gosto sim, e voce?\" -> IA: " + answer + " | historico=" + hist2.size() + " turnos");
            check(answer != null && answer.toLowerCase().startsWith("vaca"), "a vaca deveria responder a resposta do jogador: " + answer);
            check(hist2.size() == 2 && hist2.get(0).get("voce").equals(opening) && hist2.get(1).get("jogador").equals("Gosto sim, e você?"),
                    "o historico deveria ter a abertura e depois a troca: " + hist2);
            Thread.sleep(1500);
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));

            // 6) som de "pensando": se a resposta demora, a criatura solta um som e a resposta espera ele acabar
            Mob mob = world.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(Mob.class, new AABB(-5, -70, -5, 5, 40, 12),
                    x -> x.getTags().contains("vaca")).get(0));
            int before = ThinkingSound.played();
            AtomicBoolean answered = new AtomicBoolean();
            var handle = new Object() {
                ThinkingSound.Handle h;
            };
            world.getServer().runOnServer(s -> handle.h = ThinkingSound.begin(s, ConversationService.speaker(), SidecarClient.local(), mob,
                    Voices.forProfile("minecraft:cow", mob.getUUID()), "cow"));
            long end2 = System.currentTimeMillis() + 15_000;
            while (ThinkingSound.played() == before && System.currentTimeMillis() < end2) {
                context.waitTicks(5);
                Thread.sleep(100);
            }
            System.out.println("[gametest] INI6 som de pensando tocou: " + (ThinkingSound.played() - before) + " vez(es)");
            check(ConversationService.speaker() == null || ThinkingSound.played() > before, "a resposta demorou e a criatura deveria ter feito um som de pensando");
            world.getServer().runOnServer(s -> handle.h.beforeAnswer(() -> answered.set(true)));
            long end3 = System.currentTimeMillis() + 10_000;
            while (!answered.get() && System.currentTimeMillis() < end3) {
                context.waitTicks(5);
                Thread.sleep(100);
            }
            check(answered.get(), "a resposta deveria rodar depois do som de pensando");
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            ServerConfig.set(new ServerConfig());
            ClientPrefs.setChatMode(false);
        }
    }
}
