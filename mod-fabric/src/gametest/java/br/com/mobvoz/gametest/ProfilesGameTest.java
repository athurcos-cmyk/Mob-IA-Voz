package br.com.mobvoz.gametest;

import br.com.mobvoz.client.MobVozClient;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.profile.Profiles;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;

/** Fase 8 em jogo real (T16/T17): fazendeiro gosta de feno, ferreiro nao; chave de perfil vem da profissao real do aldeao. */
public class ProfilesGameTest implements FabricClientGameTest {
    private static void check(boolean ok, String msg) {
        if (!ok) {
            throw new AssertionError(msg);
        }
    }

    private static float gift(ClientGameTestContext c, TestSingleplayerContext w, String profession, int n) {
        w.getServer().runCommand("kill @e[type=villager]");
        w.getServer().runCommand("summon villager 0.5 -60 3.5 {NoAI:1b,Silent:1b,VillagerData:{profession:\"minecraft:" + profession
                + "\",level:1,type:\"minecraft:plains\"}}");
        c.waitTicks(20);
        UUID id = c.computeOnClient(mc -> MobVozClient.target() == null ? null : MobVozClient.target().getUUID());
        check(id != null, "aldeao nao esta na mira");
        String key = w.getServer().computeOnServer(s -> Profiles.keyOf((Mob) s.overworld().getEntity(id)));
        check(key.equals("minecraft:villager#" + profession), "chave de perfil errada: " + key);
        c.getInput().holdKey(o -> o.keyShift);
        c.waitTicks(3);
        for (int i = 0; i < n; i++) {
            c.runOnClient(mc -> mc.gameMode.interact(mc.player, MobVozClient.target(), InteractionHand.MAIN_HAND));
            c.waitTicks(2);
        }
        c.getInput().releaseKey(o -> o.keyShift);
        c.waitTicks(5);
        return w.getServer().computeOnServer(s -> MemoryStore.get((Mob) s.overworld().getEntity(id))
                .relationOf(s.getPlayerList().getPlayers().get(0).getUUID()).affinity());
    }

    @Override
    public void runTest(ClientGameTestContext context) {
        if (Only.skip(getClass())) {
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            world.getClientWorld().waitForChunksRender();
            world.getServer().runCommand("gamemode survival @p");
            world.getServer().runCommand("give @p minecraft:hay_block 64");
            world.getServer().runCommand("tp @p 0.5 -60 0.5 0 17");
            float farmer = gift(context, world, "farmer", 10);
            float smith = gift(context, world, "toolsmith", 10);
            int hay = context.computeOnClient(mc -> mc.player.getInventory().countItem(Items.HAY_BLOCK));
            System.out.println("[gametest] T16/T17: afinidade fazendeiro=" + farmer + " ferreiro=" + smith + " feno restante=" + hay);
            check(farmer > 0.1f, "o fazendeiro deveria gostar de feno");
            check(smith < 0f, "o ferreiro nao deveria gostar de feno");
            check(hay == 44, "cada presente consome exatamente 1 feno (restaram " + hay + ")");
        }
    }
}
