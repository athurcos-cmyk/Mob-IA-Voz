package br.com.mobvoz.social;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.server.ConversationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.Enemy;

/**
 * A criatura PUXA ASSUNTO com o jogador, de vez em quando: uma criatura pacifica, perto e que o enxerga abre uma conversa ("Qual foi o seu maior
 * susto?"). Fala pronta (sem IA, sem custo), de familias {@code ev_puxa_*}; hostis nunca puxam assunto. A resposta do jogador ja e conversa de
 * verdade com a IA, COM contexto: a abertura entra no historico e a IA recebe um aviso para continuar o mesmo assunto
 * ({@link ConversationService#rememberOpening}). Chaves {@code mobsInitiate} e {@code initiativeSeconds}.
 */
public final class Initiative {
    private static final int CHECK_EVERY = 100;           // ticks entre conferencias
    private static final double MIN_DIST = 2.5;
    private static final double MAX_DIST = 10;
    private static final long RETRY_TICKS = 400;          // sem candidata agora: tenta de novo em 20 s
    private static final long QUIET_AFTER_TALK_TICKS = 1800; // nao puxa assunto logo depois de o jogador conversar (90 s)
    private static final Random RND = new Random();
    private static final Map<UUID, Long> next = new ConcurrentHashMap<>();

    private Initiative() {
    }

    public static void reset() {
        next.clear();
    }

    /** Para testes: forca a proxima tentativa de um jogador para agora. */
    public static void dueNow(UUID player) {
        next.put(player, 0L);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerConfig cfg = ServerConfig.current();
            if (!cfg.mobsInitiate || !cfg.naturalEvents || server.getTickCount() % CHECK_EVERY != 0) {
                return;
            }
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                long now = p.level().getGameTime();
                Long due = next.get(p.getUUID());
                if (due == null) { // 1a vez neste jogador: espera um pouco antes da primeira abertura
                    next.put(p.getUUID(), now + interval(cfg) / 2 + RND.nextInt(600));
                    continue;
                }
                if (now < due) {
                    continue;
                }
                next.put(p.getUUID(), tryOpen(p) ? now + interval(cfg) : now + RETRY_TICKS);
            }
        });
    }

    /** Intervalo ate a proxima abertura: o configurado, com variacao de 100% a 160% (nunca certinho). */
    static long interval(ServerConfig cfg) {
        return (long) (cfg.initiativeSeconds * 20L * (1.0 + RND.nextDouble() * 0.6));
    }

    /** Puro: pode esta criatura puxar assunto? (pacifica, nao esta ocupada nem lutando, nem morcego) */
    static boolean eligible(Mob m) {
        return m.isAlive() && !(m instanceof Enemy) && !(m instanceof Bat) && m.getTarget() == null && !m.isSleeping()
                && !ConversationService.isBusy(m.getUUID());
    }

    /** Escolhe uma criatura pacifica perto que enxerga o jogador e abre a conversa. Devolve se alguem falou. */
    public static boolean tryOpen(ServerPlayer p) {
        if (!(p.level() instanceof ServerLevel level) || ConversationService.activeSessions() > 0
                || ConversationService.recentlyTalked(p.getUUID(), QUIET_AFTER_TALK_TICKS, level.getGameTime())) {
            return false;
        }
        List<Mob> near = new ArrayList<>(level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(MAX_DIST), m -> {
            double d = p.distanceTo(m);
            return eligible(m) && d >= MIN_DIST && d <= MAX_DIST && m.hasLineOfSight(p) && (!Ambient.classesOf(m).contains("aquatic") || m.isInWater());
        }));
        if (near.isEmpty()) {
            return false;
        }
        Collections.shuffle(near, RND);
        for (Mob m : near) { // a 1a que tiver uma familia que sirva (especie, hora, clima...)
            if (WorldReactions.opener(p, m)) {
                return true;
            }
        }
        return false;
    }
}
