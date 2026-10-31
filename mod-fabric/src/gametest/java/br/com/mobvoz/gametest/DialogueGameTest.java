package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Conversas de verdade, como jogador, pelo modo chat e o pipeline completo (analise -> decisao do servidor -> fala do LLM real).
 * Nao afirma nada sobre o texto: imprime cada troca para leitura humana ("[dialogo] ...").
 */
public class DialogueGameTest implements FabricClientGameTest {
    private record Scene(String label, String spawn, String setup, String[] lines) {
    }

    private static final Scene[] SCENES = {
        new Scene("Cavalo convencido a ser meu (domar por conversa)", "horse 0.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "EVENT_TAME", new String[] {
            "Oi cavalo! Eu queria muito te conhecer melhor.", "Aceita ser meu cavalo? Eu prometo cuidar de você, te dar um estábulo quentinho e comida todo dia."}),
        new Scene("Troca com o aldeao fazendeiro (falas geradas)", "villager 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,VillagerData:{profession:\"minecraft:farmer\",level:1,type:\"minecraft:plains\"}}", "EVENT_TRADE", new String[] {}),
        new Scene("Esqueleto Wither (estrela do Nether)", "wither_skeleton 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "",
            new String[] {"Esqueleto Wither, você tem uma estrela do Nether para me dar?", "Quanto custa?"}),
        new Scene("Galinha e o ovo (evento)", "chicken 0.5 -60 3.0 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "EVENT_EGG", new String[] {}),
        new Scene("Wither (chefe)", "wither 0.5 -60 5.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "",
            new String[] {"Ei Wither, você pode me dar uma estrela do Nether?", "Então me ajuda a derrotar o Dragão do Fim?",
                "Por que você é tão furioso?"}),
        new Scene("Porco", "pig 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "",
            new String[] {"Oi porco, tudo bem?", "O que você acha das vacas?", "E das galinhas?", "Você tem medo de alguma coisa?", "E por que você tem medo disso?"}),
        new Scene("Vaca", "cow 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "",
            new String[] {"O que você gosta de fazer o dia todo?", "O que você acha dos esqueletos?", "Já teve medo de alguém?"}),
        new Scene("Esqueleto", "skeleton 0.5 -60 4.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "",
            new String[] {"E aí esqueleto, o que você acha das vacas?", "Por que você odeia o sol?"}),
        new Scene("Peixe (bacalhau)", "cod 0.5 -60 3.0 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "setblock 0 -60 2 water",
            new String[] {"Você já saiu da água alguma vez?", "O que tem lá fora, pelo que você viu?"}),
        new Scene("Golem de ferro", "iron_golem 0.5 -60 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "",
            new String[] {"Qual é o seu dever?", "Você sente saudade de alguma coisa?", "O que você acha dos zumbis?", "E dos creepers?"}),
        new Scene("Galinha", "chicken 0.5 -60 3.0 {NoAI:1b,Silent:1b,PersistenceRequired:1b}", "",
            new String[] {"Oi galinha, o que você mais gosta de comer?", "E você tem amigos aqui?"}),
    };

    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
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
        long idle = System.currentTimeMillis() + 60_000;
        while (br.com.mobvoz.client.voice.VoiceInput.status() != br.com.mobvoz.net.StatusPayload.IDLE) {
            check(System.currentTimeMillis() < idle, "a criatura nao terminou de falar");
            c.waitTicks(5);
            Thread.sleep(100);
        }
        Thread.sleep(1600); // cooldown do jogador
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
            cmd.runCommand("time set midnight");
            cmd.runCommand("gamerule doDaylightCycle false");
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            for (Scene s : SCENES) {
                cmd.runCommand("kill @e[type=!player]");
                cmd.runCommand("fill -4 -60 1 4 -57 9 air"); // limpa a agua da cena anterior (ela escorre e empurra a criatura seguinte)
                cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
                if (!s.setup().isEmpty() && !s.setup().startsWith("EVENT_")) {
                    cmd.runCommand(s.setup());
                }
                cmd.runCommand("summon " + s.spawn());
                context.waitTicks(25);
                boolean aimed = context.computeOnClient(mc -> MobVozClient.target() != null);
                if (!aimed) {
                    System.out.println("[dialogo] === " + s.label() + ": criatura nao ficou na mira; cena pulada");
                    continue;
                }
                System.out.println("[dialogo] === " + s.label());
                if (s.setup().equals("EVENT_TAME")) { // rapport minimo (conversa antes) + argumento -> o cavalo aceita e vira do jogador
                    java.util.UUID horse = world.getServer().computeOnServer(sv -> sv.overworld().getEntities(
                            net.minecraft.world.entity.EntityType.HORSE, e -> true).get(0).getUUID());
                    world.getServer().runOnServer(sv -> br.com.mobvoz.memory.MemoryStore.update((net.minecraft.world.entity.Mob) sv.overworld().getEntity(horse),
                            m -> m.withRelation(sv.getPlayerList().getPlayers().get(0).getUUID(), r -> new br.com.mobvoz.memory.MobMemory.Relationship(0.2f, 0.1f, 0))));
                    for (String line : s.lines()) {
                        System.out.println("[dialogo]   JOGADOR : " + line);
                        try {
                            say(context, line);
                            System.out.println("[dialogo]   CRIATURA: " + ChatMode.lastSubtitle);
                        } catch (AssertionError e) {
                            System.out.println("[dialogo]   CRIATURA: (sem resposta: " + e.getMessage() + ")");
                        }
                    }
                    boolean tamed = world.getServer().computeOnServer(sv -> ((net.minecraft.world.entity.animal.horse.AbstractHorse) sv.overworld().getEntity(horse)).isTamed());
                    System.out.println("[dialogo]   RESULTADO NO MUNDO: cavalo domado = " + tamed);
                    continue;
                }
                if (s.setup().equals("EVENT_TRADE")) { // oferta de feno: joga 1 (falta valor), depois 4 (fecha, devolve 1)
                    java.util.UUID farmer = world.getServer().computeOnServer(sv -> sv.overworld().getEntities(
                            net.minecraft.world.entity.EntityType.VILLAGER, e -> true).get(0).getUUID());
                    world.getServer().runOnServer(sv -> br.com.mobvoz.trade.Trades.open((net.minecraft.world.entity.Mob) sv.overworld().getEntity(farmer),
                            sv.getPlayerList().getPlayers().get(0), 0, "", 0));
                    java.util.List<String> heard = new java.util.ArrayList<>();
                    for (int round = 0; round < 2; round++) {
                        int n = round == 0 ? 1 : 4;
                        cmd.runCommand("kill @e[type=item]");
                        final int count = n;
                        world.getServer().runOnServer(sv -> {
                            var pl = sv.getPlayerList().getPlayers().get(0);
                            var it = new net.minecraft.world.entity.item.ItemEntity(sv.overworld(), 0.5, -59.5, 3.5,
                                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.HAY_BLOCK, count));
                            it.setThrower(pl);
                            it.setPickUpDelay(40);
                            sv.overworld().addFreshEntity(it);
                        });
                        long end = System.currentTimeMillis() + 70_000;
                        String before = ChatMode.lastSubtitle;
                        while (System.currentTimeMillis() < end) {
                            context.waitTicks(5);
                            Thread.sleep(100);
                            String now = ChatMode.lastSubtitle;
                            if (now != null && !now.equals(before) && !heard.contains(now)) {
                                heard.add(now);
                                System.out.println("[dialogo]   TROCA   : jogador largou " + n + " feno(s) -> " + now);
                                break;
                            }
                        }
                    }
                    continue;
                }
                if (s.setup().equals("EVENT_EGG")) { // a galinha "bota" um ovo (sem dono) e o jogador o pega: ela comenta sozinha
                    ChatMode.lastSubtitle = null;
                    cmd.runCommand("summon item 0.5 -60 4.0 {Item:{id:\"minecraft:egg\",count:1}}");
                    context.waitTicks(15);
                    cmd.runCommand("tp @p 0.5 -60 4.0");
                    long end = System.currentTimeMillis() + 60_000;
                    while (ChatMode.lastSubtitle == null && System.currentTimeMillis() < end) {
                        context.waitTicks(5);
                        Thread.sleep(100);
                    }
                    System.out.println("[dialogo]   EVENTO  : o jogador pegou o ovo que a galinha acabou de botar");
                    System.out.println("[dialogo]   CRIATURA: " + ChatMode.lastSubtitle);
                    continue;
                }
                for (String line : s.lines()) {
                    System.out.println("[dialogo]   JOGADOR : " + line);
                    try {
                        say(context, line);
                        System.out.println("[dialogo]   CRIATURA: " + ChatMode.lastSubtitle);
                    } catch (AssertionError e) {
                        System.out.println("[dialogo]   CRIATURA: (sem resposta: " + e.getMessage() + ")");
                    }
                }
            }
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
