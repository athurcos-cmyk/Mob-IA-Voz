package br.com.mobvoz.gametest;

import br.com.mobvoz.client.ChatMode;
import br.com.mobvoz.client.ClientPrefs;
import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.client.voice.VoiceConsent;
import br.com.mobvoz.orders.MobOrders;
import br.com.mobvoz.orders.Orders;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.world.entity.Mob;

/** Modo chat (sem microfone): texto digitado com criatura na mira vira conversa privada (ordem + legenda), nunca chat publico. */
public class ChatModeGameTest implements FabricClientGameTest {
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
        AtomicInteger publicChat = new AtomicInteger();
        ServerMessageEvents.CHAT_MESSAGE.register((msg, sender, params) -> publicChat.incrementAndGet());
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            var cmd = world.getServer();
            cmd.runCommand("gamemode creative @p");
            cmd.runCommand("tp @p 0.5 -60 0.5 0 17");
            cmd.runCommand("summon cow 0.5 -60 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"vaca\"]}");
            context.waitTicks(20);
            context.runOnClient(mc -> VoiceConsent.set(false)); // sem microfone
            context.runOnClient(mc -> ClientPrefs.setChatMode(true));
            UUID cow = context.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
            check(cow != null, "vaca nao esta na mira");

            // 1) com alvo: mensagem privada
            ChatMode.lastSubtitle = null;
            context.runOnClient(mc -> mc.player.connection.sendChat("Me segue até a minha casa."));
            long end = System.currentTimeMillis() + 90_000;
            while (ChatMode.lastSubtitle == null) {
                check(System.currentTimeMillis() < end, "nenhuma legenda: o modo chat nao respondeu");
                context.waitTicks(5);
                Thread.sleep(100);
            }
            MobOrders o = world.getServer().computeOnServer(s -> Orders.get((Mob) s.overworld().getEntity(cow)));
            System.out.println("[gametest] chat: legenda='" + ChatMode.lastSubtitle + "' ordem=" + o.mode() + " chat publico=" + publicChat.get());
            check(o.mode() == MobOrders.Mode.FOLLOW, "o texto digitado nao virou ordem (" + o.mode() + ")");
            check(ChatMode.lastSubtitle.startsWith("vaca: ") && ChatMode.lastSubtitle.length() > 8, "legenda inesperada");
            check(publicChat.get() == 0, "a mensagem vazou para o chat publico");
            context.takeScreenshot("mobvoz_chat_legenda");

            // 2) sem alvo: o chat continua normal
            Thread.sleep(1500);
            cmd.runCommand("tp @p 0.5 -60 0.5 90 17");
            context.waitTicks(10);
            context.runOnClient(mc -> mc.player.connection.sendChat("oi pessoal"));
            context.waitTicks(20);
            check(publicChat.get() == 1, "sem alvo, o chat deveria ser publico normal (" + publicChat.get() + ")");
            System.out.println("[gametest] chat sem alvo segue publico");
            context.runOnClient(mc -> ClientPrefs.setChatMode(false));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
