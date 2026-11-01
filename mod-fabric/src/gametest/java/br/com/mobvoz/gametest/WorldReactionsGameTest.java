package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.social.AmbientScenes;
import br.com.mobvoz.social.WorldReactions;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * Reacoes ao mundo em jogo real, SEM IA (so falas fixas): creeper que explode, raio, "Ai!" de quem apanha, agradecimento da vila ao matar
 * um monstro, queda do jogador e a fala de presente. Confere que a fala dita pertence as familias do acontecimento.
 */
public class WorldReactionsGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    /** Todas as falas possiveis das familias cujo id comeca por um dos prefixos (com os espacos {x} cortados no primeiro). */
    private static Set<String> lines(String... prefixes) {
        Set<String> out = new HashSet<>();
        for (var f : AmbientScenes.families()) {
            for (String p : prefixes) {
                if (f.id().startsWith(p)) {
                    out.addAll(f.a());
                    out.addAll(f.b());
                    out.addAll(f.c());
                }
            }
        }
        return out;
    }

    /** A fala dita (ja com os espacos preenchidos) corresponde a alguma fala das familias? Compara o pedaco fixo antes do primeiro "{". */
    private static boolean said(Set<String> possible) {
        return ConversationService.announced().stream().anyMatch(l -> possible.stream().anyMatch(p -> {
            StringBuilder re = new StringBuilder();
            for (String part : p.split("\\{[^}]*}", -1)) { // os espacos {x} viram ".*"; o resto tem de bater igual
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
        cfg.ambientChatter = false; // so as reacoes, sem a conversa de ambiente por cima
        ServerConfig.set(cfg);
        WorldReactions.reset();
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode survival @p");
            cmd.runCommand("time set day");
            cmd.runCommand("weather clear");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("gamerule doWeatherCycle false");
            cmd.runCommand("gamerule doMobSpawning false");
            cmd.runCommand("gamerule doPatrolSpawning false"); // patrulha de saqueadores dispararia a reacao dos aldeoes sozinha
            cmd.runCommand("gamerule doTraderSpawning false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon villager 4.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"v1\"]}");
            cmd.runCommand("summon villager 6.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"v2\"]}");
            context.waitTicks(40);

            // 1) creeper explode perto dos aldeoes
            int f0 = WorldReactions.fired();
            cmd.runCommand("summon creeper 9.5 -60 4.5 {NoAI:1b,Silent:1b,ignited:1b,Fuse:5,ExplosionRadius:0b}");
            context.waitTicks(60);
            System.out.println("[gametest] WR1 creeper: reacoes " + f0 + " -> " + WorldReactions.fired() + " falas=" + ConversationService.announced());
            check(WorldReactions.fired() > f0, "ninguem reagiu ao creeper que explodiu");
            check(said(lines("ev_boom")), "a fala nao e de uma familia ev_boom: " + ConversationService.announced());

            // 2) raio cai perto
            int f1 = WorldReactions.fired();
            context.waitTicks(320);
            cmd.runCommand("summon lightning_bolt -8.5 -60 -6.5");
            context.waitTicks(60);
            System.out.println("[gametest] WR2 raio: reacoes " + f1 + " -> " + WorldReactions.fired() + " falas=" + ConversationService.announced());
            check(WorldReactions.fired() > f1, "ninguem reagiu ao raio");
            check(said(lines("ev_raio")), "a fala nao e de uma familia ev_raio");

            // 3) o "Ai!" de quem apanha (70% de chance por pancada): ate 6 aldeoes diferentes
            int f2 = WorldReactions.fired();
            for (int i = 0; i < 6 && WorldReactions.fired() == f2; i++) {
                cmd.runCommand("summon villager " + (2.5 + i) + " -60 8.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"ai" + i + "\"]}");
                context.waitTicks(10);
                cmd.runCommand("execute as @p run damage @e[tag=ai" + i + ",limit=1] 1 minecraft:player_attack by @p");
                context.waitTicks(40);
            }
            System.out.println("[gametest] WR3 pancada: reacoes " + f2 + " -> " + WorldReactions.fired() + " falas=" + ConversationService.announced());
            check(WorldReactions.fired() > f2, "ninguem disse 'Ai!' depois de 6 pancadas");
            check(said(lines("ev_ai_")), "a fala nao e de uma familia ev_ai");

            // 4) o jogador mata um monstro perto da vila: agradecimento
            int f3 = WorldReactions.fired();
            context.waitTicks(420);
            cmd.runCommand("summon zombie 7.5 -60 2.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"zumbi\"]}");
            context.waitTicks(10);
            cmd.runCommand("execute as @p run damage @e[tag=zumbi,limit=1] 100 minecraft:player_attack by @p");
            context.waitTicks(60);
            System.out.println("[gametest] WR4 monstro morto: reacoes " + f3 + " -> " + WorldReactions.fired() + " falas=" + ConversationService.announced());
            check(WorldReactions.fired() > f3, "a vila nao agradeceu");
            check(said(lines("ev_obrigado")), "a fala nao e de uma familia ev_obrigado");

            // 5) o jogador cai de muito alto
            int f4 = WorldReactions.fired();
            context.waitTicks(420);
            cmd.runCommand("damage @p 10 minecraft:fall");
            context.waitTicks(60);
            System.out.println("[gametest] WR5 queda: reacoes " + f4 + " -> " + WorldReactions.fired() + " falas=" + ConversationService.announced());
            check(WorldReactions.fired() > f4, "ninguem reagiu a queda");
            check(said(lines("ev_queda")), "a fala nao e de uma familia ev_queda");

            // 6) presente: a criatura fala (gostou)
            cmd.runCommand("summon cow 2.5 -60 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
            context.waitTicks(10);
            int f5 = WorldReactions.fired();
            world.getServer().runOnServer(s -> {
                Mob cow = s.overworld().getEntitiesOfClass(Mob.class, new AABB(-20, -70, -20, 20, 40, 20), x -> x.getTags().contains("vaca")).get(0);
                WorldReactions.speakAs(s.getPlayerList().getPlayers().get(0), cow, "ev:gift_like", "trigo", 0);
            });
            context.waitTicks(40);
            System.out.println("[gametest] WR6 presente: reacoes " + f5 + " -> " + WorldReactions.fired() + " falas=" + ConversationService.announced());
            check(WorldReactions.fired() > f5, "a vaca nao falou do presente");
            check(said(lines("ev_presente_gosta")), "a fala nao e de uma familia ev_presente_gosta");
        } finally {
            ServerConfig.set(new ServerConfig());
            WorldReactions.reset();
        }
    }
}
