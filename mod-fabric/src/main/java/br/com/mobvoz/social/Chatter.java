package br.com.mobvoz.social;

import br.com.mobvoz.MobVozMod;
import br.com.mobvoz.ai.SidecarClient;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.server.ConversationService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * Conversa mob-mob guiada por EVENTO (fase 9): o jogador golpeia uma criatura hostil que tem um parceiro proximo. Nada de
 * tick por mob nem Ollama em loop: no maximo uma cena curta (2-3 falas) por aprovacao do {@link ChatterScheduler}.
 * A cena e so FALA: nenhuma acao e executada a partir dela.
 */
public final class Chatter {
    private static final double PARTNER_RADIUS = 8.0;
    private static final double AUDIENCE_RADIUS = 20.0;

    private static ChatterScheduler scheduler = new ChatterScheduler(ChatterScheduler.Config.DEFAULT);
    private static SidecarClient sidecar = SidecarClient.local();
    private static final AtomicInteger linesSpoken = new AtomicInteger();
    private static volatile boolean enabled = true;

    private Chatter() {
    }

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static void setScheduler(ChatterScheduler s) {
        scheduler = s;
    }

    public static int linesSpoken() {
        return linesSpoken.get();
    }

    public static ChatterScheduler scheduler() {
        return scheduler;
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (enabled && br.com.mobvoz.config.ServerConfig.current().chatterEnabled && taken > 0 && entity instanceof Mob hit && hit instanceof Enemy && source.getEntity() instanceof ServerPlayer p) {
                onCombat(hit, p);
            }
        });
    }

    static void onCombat(Mob hit, ServerPlayer player) {
        if (!(hit.level() instanceof ServerLevel level)) {
            return;
        }
        Mob partner = level.getEntitiesOfClass(Mob.class, hit.getBoundingBox().inflate(PARTNER_RADIUS),
                m -> m != hit && m.isAlive() && m instanceof Enemy && m.getType() == hit.getType()).stream().findFirst().orElse(null);
        if (partner == null || player.distanceTo(hit) > AUDIENCE_RADIUS) {
            return;
        }
        long now = level.getGameTime();
        String region = ChatterScheduler.region(level.dimension().location().toString(), hit.getX(), hit.getZ());
        if (!scheduler.tryAcquire(region, hit.getUUID(), partner.getUUID(), now, ConversationService.activeSessions() > 0)) {
            return;
        }
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(hit.getType()).toString();
        Profiles.Profile prof = Profiles.of(type);
        sidecar.scene(type, prof.label(), prof.style(), scheduler.config().maxTurns()).thenAccept(lines -> level.getServer().execute(() -> {
            List<SidecarClient.SceneLine> use = lines.isEmpty() ? fallback(type) : lines;
            play(List.of(hit, partner), player, use, 0);
        }));
    }

    /** Fala as linhas em sequencia (uma por vez, sem sobrepor). */
    private static void play(List<Mob> actors, ServerPlayer player, List<SidecarClient.SceneLine> lines, int i) {
        if (i >= lines.size() || i >= scheduler.config().maxTurns()) {
            return;
        }
        SidecarClient.SceneLine l = lines.get(i);
        Mob speaker = actors.get(Math.floorMod(l.speaker(), 2));
        if (!speaker.isAlive() || player.hasDisconnected()) {
            return;
        }
        linesSpoken.incrementAndGet();
        MobVozMod.LOGGER.info("cena mob-mob: fala {} de {}", i + 1, lines.size());
        ConversationService.announce(speaker, player, l.text(), () -> play(actors, player, lines, i + 1));
    }

    /** Sidecar fora do ar: falas curtas e genericas coerentes com o combate (sem IA). */
    static List<SidecarClient.SceneLine> fallback(String type) {
        List<SidecarClient.SceneLine> out = new ArrayList<>();
        out.add(new SidecarClient.SceneLine(0, "Ele está aqui! Vamos pegá-lo!"));
        out.add(new SidecarClient.SceneLine(1, "Eu vou pela esquerda, você vai pela direita."));
        return out;
    }
}
