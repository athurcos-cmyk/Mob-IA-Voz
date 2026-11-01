package br.com.mobvoz.gametest;

import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.client.voice.VoiceInput;
import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.net.StatusPayload;
import br.com.mobvoz.net.UtterancePayload;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * Chamar pelo apelido, SEM mirar, com a IA real: "Mimosa, voce esta bem?" (digitado e FALADO) faz a vaca que esta atras do jogador responder;
 * falar DE alguem no meio da frase, chamar um nome errado ou chamar quem esta longe demais NAO faz nada (o servidor revalida).
 */
public class NameCallGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static void waitIdle(ClientGameTestContext c) throws InterruptedException {
        long idle = System.currentTimeMillis() + 60_000;
        while (VoiceInput.status() != StatusPayload.IDLE) {
            check(System.currentTimeMillis() < idle, "a criatura nao terminou de falar");
            c.waitTicks(5);
            Thread.sleep(100);
        }
    }

    /** Espera uma legenda; devolve-a (ou null se nenhuma chegou no prazo). */
    private static String awaitSubtitle(ClientGameTestContext c, long ms) throws InterruptedException {
        long end = System.currentTimeMillis() + ms;
        while (ChatMode.lastSubtitle == null && System.currentTimeMillis() < end) {
            c.waitTicks(5);
            Thread.sleep(100);
        }
        return ChatMode.lastSubtitle;
    }

    private static Thread speakAsync(short[] pcm) {
        Thread t = new Thread(() -> {
            try {
                for (int i = 0; i + 960 <= pcm.length; i += 960) {
                    short[] f = new short[960];
                    System.arraycopy(pcm, i, f, 0, 960);
                    VoiceInput.onFrame(f);
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.start();
        return t;
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        var cfg = new ServerConfig();
        cfg.ambientChatter = false;
        cfg.mobsInitiate = false; // a criatura nao puxa assunto no meio do teste
        cfg.naturalEvents = false; // nada de fala espontanea ocupando a vaca
        ServerConfig.set(cfg);
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("time set day");
            cmd.runCommand("forceload add 0 -48");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17"); // olhando para +z; as vacas ficam ATRAS (z negativo)
            cmd.runCommand("summon cow 0.5 -60 -8.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,CustomName:\"Mimosa\",Tags:[\"mimosa\"]}");
            cmd.runCommand("summon cow 0.5 -60 -46.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,CustomName:\"Longe\",Tags:[\"longe\"]}");
            context.waitTicks(60); // o cliente varre as criaturas com nome a cada 10 ticks
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            check(context.computeOnClient(mc -> br.com.mobvoz.client.MobVozClient.target()) == null, "ninguem deveria estar na mira");
            UUID mimosa = world.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(Mob.class, new AABB(-5, -70, -20, 5, 40, 5),
                    x -> x.getTags().contains("mimosa")).get(0).getUUID());
            UUID longe = world.getServer().computeOnServer(s -> s.overworld().getEntitiesOfClass(Mob.class, new AABB(-5, -70, -60, 5, 40, -30),
                    x -> x.getTags().contains("longe")).get(0).getUUID());
            int named = context.computeOnClient(mc -> br.com.mobvoz.client.MobVozClient.namedNearby().size());
            check(named == 1, "o cliente deveria ver 1 criatura com nome ao alcance (a outra esta longe): " + named);

            // 1) digitado: chamou a Mimosa de costas para ela
            ChatMode.lastSubtitle = null;
            context.runOnClient(mc -> mc.player.connection.sendChat("Mimosa, você está bem?"));
            String s1 = awaitSubtitle(context, 90_000);
            System.out.println("[gametest] NAME1 digitado: " + s1);
            check(s1 != null && s1.startsWith("Mimosa:"), "a Mimosa deveria responder (legenda): " + s1);
            waitIdle(context);
            Thread.sleep(1600);

            // 2) falar DE alguem no meio da frase nao e chamar
            ChatMode.lastSubtitle = null;
            context.runOnClient(mc -> mc.player.connection.sendChat("Ontem eu vi a Mimosa na colina perto do rio com a Bia e o Rex"));
            String s2 = awaitSubtitle(context, 12_000);
            System.out.println("[gametest] NAME2 falando DE ela: " + s2);
            check(s2 == null, "falar DE alguem nao deveria acionar a criatura: " + s2);

            // 3) o servidor revalida: nome errado e criatura longe demais sao recusados, mesmo com o pacote "por nome"
            ChatMode.lastSubtitle = null;
            context.runOnClient(mc -> ClientPlayNetworking.send(new UtterancePayload(mimosa, "Oi Rex, tudo bem com você?", true, true)));
            check(awaitSubtitle(context, 12_000) == null, "nome errado deveria ser recusado pelo servidor");
            context.runOnClient(mc -> ClientPlayNetworking.send(new UtterancePayload(longe, "Longe, vem aqui por favor", true, true)));
            check(awaitSubtitle(context, 12_000) == null, "criatura a mais de 32 blocos deveria ser recusada pelo servidor");
            context.runOnClient(mc -> ClientPlayNetworking.send(new UtterancePayload(mimosa, "Mimosa, tudo bem?", true, false)));
            check(awaitSubtitle(context, 12_000) == null, "sem 'por nome' e sem mirar, o servidor recusa");
            System.out.println("[gametest] NAME3 servidor recusou: nome errado, longe demais e sem mirar");

            // 4) falado: a mesma chamada pelo microfone (TTS no lugar da voz), sem ninguem na mira
            context.runOnClient(mc -> {
                ClientPrefs.setChatMode(false);
                VoiceConsent.set(true);
            });
            var pcm = SidecarClient.local().synthesize("Mimosa, você está bem?", "pt_BR-faber-medium", 1.0, 1.0).join();
            check(pcm.isPresent(), "TTS indisponivel (sidecar ligado?)");
            short[] voice = new short[pcm.get().length + 48000];
            System.arraycopy(pcm.get(), 0, voice, 0, pcm.get().length); // 1 s de silencio no fim fecha a fala
            int before = VoiceInput.sentCount();
            ChatMode.lastSubtitle = null;
            Thread talker = speakAsync(voice);
            long end = System.currentTimeMillis() + 60_000;
            while (VoiceInput.sentCount() == before && System.currentTimeMillis() < end) {
                context.waitTicks(2);
                Thread.sleep(50);
            }
            talker.join();
            check(VoiceInput.sentCount() == before + 1, "a fala chamando a Mimosa deveria ser enviada ao servidor");
            String s4 = awaitSubtitle(context, 90_000);
            System.out.println("[gametest] NAME4 falado: " + s4);
            check(s4 != null && s4.startsWith("Mimosa:"), "a Mimosa deveria responder a chamada falada: " + s4);
            waitIdle(context);

            // 5) falou perto de uma criatura com nome, mas sem chamar ninguem: descarta (nada e enviado)
            int sent = VoiceInput.sentCount();
            var talk = SidecarClient.local().synthesize("Que dia bonito para construir uma casa", "pt_BR-faber-medium", 1.0, 1.0).join();
            short[] other = new short[talk.get().length + 48000];
            System.arraycopy(talk.get(), 0, other, 0, talk.get().length);
            speakAsync(other).join();
            Thread.sleep(6000);
            context.waitTicks(10);
            check(VoiceInput.sentCount() == sent, "fala sem chamar ninguem pelo nome nao deveria ir ao servidor");
            System.out.println("[gametest] NAME5 fala sem chamar ninguem: descartada");
            context.runOnClient(mc -> VoiceConsent.set(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            ServerConfig.set(new ServerConfig());
            ClientPrefs.setChatMode(false);
        }
    }
}
