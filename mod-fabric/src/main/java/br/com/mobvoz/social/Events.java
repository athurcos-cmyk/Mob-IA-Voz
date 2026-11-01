package br.com.mobvoz.social;

import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.orders.Orders;
import br.com.mobvoz.profile.Mentions;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Coisas que ACONTECEM e a criatura comenta sozinha (falas fixas e variadas, sem IA: ver {@link WorldReactions}).
 * Hoje: o jogador pega o ovo que a galinha acabou de botar. Detecta pelo ovo (sem dono) que nasce ao lado de uma galinha e some
 * com um jogador junto; ovos largados pelo jogador ou que ja existiam ao carregar o chunk nao contam.
 */
public final class Events {
    private static final long MAX_AGE_TICKS = 5000; // ovo some sozinho em 5 min; depois disso nao e "pegou o ovo dela"
    private static final double TOUCH_RADIUS = 3.5;

    private record Egg(UUID chicken, ServerLevel level, long born, Vec3 lastPos) {
    }

    private static final Map<UUID, Egg> eggs = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastWarn = new ConcurrentHashMap<>();   // jogador -> tick do ultimo aviso
    private static final Map<String, Long> greeted = new ConcurrentHashMap<>();  // criatura:jogador -> tick do ultimo cumprimento
    private static final Map<UUID, Long> lastGreetOfPlayer = new ConcurrentHashMap<>();
    static final long WARN_COOLDOWN = 600;      // 30 s
    static final long GREET_COOLDOWN = 24000;   // 1 dia de jogo por criatura+jogador
    static final long GREET_PLAYER_COOLDOWN = 1200; // 1 cumprimento por minuto por jogador
    private static final java.util.Set<String> KNOWN_FACTS = java.util.Set.of("GIFT", "TRADE", "THREAT_SPOKEN", "COMFORTED", "HONEST_RETURN",
            "DAMAGED_BY_PLAYER", "PACT_BROKEN", "TAMED");

    /** Puro (testavel): a criatura ja conhece o jogador por algo alem do primeiro encontro? */
    static boolean knows(java.util.Collection<String> factTypes) {
        return factTypes.stream().anyMatch(KNOWN_FACTS::contains);
    }

    /** Puro: cumprimento permitido? (nunca cumprimentou, ou ja passou o intervalo) */
    static boolean greetingDue(Long lastTick, long now, long cooldown) {
        return lastTick == null || now - lastTick >= cooldown;
    }

    private Events() {
    }

    /** Um aliado avisa o dono de um inimigo se aproximando (fala gerada pela IA; no maximo um aviso a cada 30 s por jogador). */
    static void warnOfDanger(ServerPlayer p) {
        long now = p.level().getGameTime();
        if (!greetingDue(lastWarn.get(p.getUUID()), now, WARN_COOLDOWN)) {
            return;
        }
        List<Mob> allies = Orders.alliesOf(p);
        if (allies.isEmpty()) {
            return;
        }
        for (Mob h : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(10), x -> x instanceof Enemy && x.isAlive())) {
            if (Orders.isAlly(h, p) || !(h.getTarget() == p || h.distanceTo(p) < 6)) {
                continue;
            }
            Mob ally = allies.stream().filter(a -> a.distanceTo(p) < 24).findFirst().orElse(null);
            String label = Mentions.labelOf(BuiltInRegistries.ENTITY_TYPE.getKey(h.getType()).toString());
            if (ally == null || label == null) {
                return;
            }
            lastWarn.put(p.getUUID(), now);
            WorldReactions.speakAs(p, ally, "ev:danger", label, 0);
            return;
        }
    }

    /** Cumprimenta quem a criatura ja conhece (presente, troca, ameaca, consolo...) ao reencontra-lo perto; nunca desconhecidos nem hostis. */
    static void greetKnown(ServerPlayer p) {
        long now = p.level().getGameTime();
        if (!greetingDue(lastGreetOfPlayer.get(p.getUUID()), now, GREET_PLAYER_COOLDOWN)) {
            return;
        }
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(6), x -> x.isAlive() && !(x instanceof Enemy) && x.getTarget() != p)) {
            String key = m.getUUID() + ":" + p.getUUID();
            if (!greetingDue(greeted.get(key), now, GREET_COOLDOWN)) {
                continue;
            }
            var facts = MemoryStore.get(m).facts().stream().filter(f -> f.player().equals(p.getUUID())).map(f -> f.type()).toList();
            greeted.put(key, now); // avaliada uma vez por dia de jogo, conheca ou nao
            if (knows(facts)) {
                lastGreetOfPlayer.put(p.getUUID(), now);
                WorldReactions.speakAs(p, m, "ev:greet", "", 0);
                return;
            }
        }
    }

    public static int trackedEggs() {
        return eggs.size();
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ItemEntity it && it.getItem().is(Items.EGG) && it.getOwner() == null && it.tickCount < 5) {
                List<Chicken> near = level.getEntitiesOfClass(Chicken.class, it.getBoundingBox().inflate(2.0), Chicken::isAlive);
                if (!near.isEmpty()) {
                    eggs.put(it.getUUID(), new Egg(near.get(0).getUUID(), level, level.getGameTime(), it.position()));
                }
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (br.com.mobvoz.config.ServerConfig.current().naturalEvents && server.getTickCount() % 20 == 0) {
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    warnOfDanger(p);
                    if (server.getTickCount() % 40 == 0) {
                        greetKnown(p);
                    }
                }
            }
            if (eggs.isEmpty() || server.getTickCount() % 4 != 0) {
                return;
            }
            for (var e : List.copyOf(eggs.entrySet())) {
                Egg egg = e.getValue();
                if (egg.level().getEntity(e.getKey()) instanceof ItemEntity it && it.isAlive()) {
                    eggs.put(e.getKey(), new Egg(egg.chicken(), egg.level(), egg.born(), it.position()));
                    continue;
                }
                eggs.remove(e.getKey()); // sumiu: pegaram, juntou com outro ovo ou venceu o tempo
                if (egg.level().getGameTime() - egg.born() > MAX_AGE_TICKS) {
                    continue;
                }
                ServerPlayer taker = egg.level().getNearestPlayer(egg.lastPos().x, egg.lastPos().y, egg.lastPos().z, TOUCH_RADIUS, false) instanceof ServerPlayer p
                        ? p : null;
                br.com.mobvoz.MobVozMod.LOGGER.info("ovo sumiu: jogador por perto={} galinha viva={}", taker != null, egg.level().getEntity(egg.chicken()) != null);
                if (taker != null && br.com.mobvoz.config.ServerConfig.current().naturalEvents
                        && egg.level().getEntity(egg.chicken()) instanceof Chicken c && c.isAlive()) {
                    WorldReactions.speakAs(taker, c, "ev:egg", "", 0);
                }
            }
        });
    }
}
