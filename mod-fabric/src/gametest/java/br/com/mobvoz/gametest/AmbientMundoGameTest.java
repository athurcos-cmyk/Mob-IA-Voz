package br.com.mobvoz.gametest;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.social.Ambient;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Conversa de ambiente para alem de aldeoes e bichos de fazenda: cada dupla (aquaticos, voadores, bichos da mata, Nether, hostis, bichos de
 * especies diferentes, inclusive na ordem trocada) tem de conversar sozinha perto do jogador, com ao menos 2 falas, usando a familia pedida.
 */
public class AmbientMundoGameTest implements FabricClientGameTest {
    /** tipoA, tipoB, familia, na agua? */
    private static final Object[][] DUPLAS = {
        {"bee", "bee", "abelha_flores", false},
        {"parrot", "parrot", "papagaio_imitar", false},
        {"goat", "goat", "cabra_pulo", false},
        {"cod", "cod", "peixe_cardume", true},
        {"dolphin", "cod", "golfinho_peixe", true},
        {"piglin", "piglin", "piglin_ouro", false},
        {"hoglin", "hoglin", "hoglin_fungo", false},
        {"blaze", "blaze", "blaze_fortaleza", false},
        {"strider", "strider", "strider_lava", false},
        {"zombie", "zombie", "zumbi_horda", false},
        {"wolf", "cat", "cruz_lobo_gato", false},
        {"cow", "wolf", "cruz_vaca_lobo", false},
    };

    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        ServerConfig on = new ServerConfig();
        on.ambientLevel = "muitas";
        ServerConfig.set(on);
        Ambient.reset();
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("difficulty normal");
            cmd.runCommand("time set day");
            cmd.runCommand("weather clear");
            cmd.runCommand("gamerule doDaylightCycle false");
            cmd.runCommand("gamerule doWeatherCycle false");
            cmd.runCommand("gamerule doMobSpawning false");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("fill 4 -61 5 10 -59 9 water"); // poca para os aquaticos
            context.waitTicks(40);
            String falhas = "";
            String so = System.getenv("MOBVOZ_PARES"); // ex.: "goat,dolphin" roda so essas duplas
            for (Object[] d : DUPLAS) {
                if (so != null && !so.isEmpty() && !java.util.Arrays.asList(so.split(",")).contains((String) d[0])) {
                    continue;
                }
                String a = (String) d[0], b = (String) d[1], fam = (String) d[2];
                int y = -60;
                String nbt = "{NoAI:1b,Silent:1b,PersistenceRequired:1b,IsImmuneToZombification:1b,Tags:[\"tm\"]}";
                double x = (boolean) d[3] ? 6.5 : -6.5; // aquaticos na poca; o resto fora dela (blaze e strider sofrem dano na agua)
                cmd.runCommand("summon " + a + " " + x + " " + y + " 6.5 " + nbt);
                cmd.runCommand("summon " + b + " " + (x + 2) + " " + y + " 6.5 " + nbt);
                context.waitTicks(20);
                Ambient.reset();
                Ambient.setOnlyFamily(fam);
                int s0 = Ambient.scenesPlayed();
                int l0 = Ambient.linesSpoken();
                long end = System.currentTimeMillis() + 60_000;
                while ((Ambient.scenesPlayed() <= s0 || Ambient.linesSpoken() - l0 < 2) && System.currentTimeMillis() < end) {
                    context.waitTicks(20);
                    Thread.sleep(100);
                }
                boolean ok = Ambient.scenesPlayed() > s0 && Ambient.linesSpoken() - l0 >= 2;
                System.out.println("[gametest] AMBMUNDO " + a + "+" + b + " (" + fam + "): " + (ok ? "conversou" : "NAO conversou") + " falas=" + (Ambient.linesSpoken() - l0));
                if (!ok) {
                    falhas += " " + a + "+" + b;
                    System.out.println("[gametest] AMBMUNDO diag " + a + "+" + b + ": " + world.getServer().computeOnServer(sv -> {
                        StringBuilder sb = new StringBuilder();
                        for (var l : sv.getAllLevels()) {
                            for (var m : l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new net.minecraft.world.phys.AABB(-40, -70, -40, 40, 40, 40))) {
                                sb.append(m.getType().toShortString()).append(" vivo=").append(m.isAlive()).append(" pos=").append(m.blockPosition().toShortString())
                                        .append(" alvo=").append(m.getTarget()).append(" agua=").append(m.isInWater()).append("; ");
                            }
                        }
                        return sb.toString();
                    }));
                }
                Ambient.setOnlyFamily("__nenhuma__");
                Thread.sleep(6000); // deixa a cena acabar de falar antes de trocar de dupla
                cmd.runCommand("kill @e[tag=tm]");
                context.waitTicks(10);
            }
            check(falhas.isEmpty(), "duplas que nao conversaram:" + falhas);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            Ambient.setOnlyFamily(null);
            Ambient.reset();
            ServerConfig.set(new ServerConfig());
        }
    }
}
