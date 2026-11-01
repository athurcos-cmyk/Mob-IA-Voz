package br.com.mobvoz.social;

import br.com.mobvoz.config.ServerConfig;
import br.com.mobvoz.memory.MemoryStore;
import br.com.mobvoz.memory.MobMemory;
import br.com.mobvoz.net.SubtitlePayload;
import br.com.mobvoz.profile.Profiles;
import br.com.mobvoz.server.ConversationService;
import br.com.mobvoz.social.AmbientScenes.Line;
import br.com.mobvoz.social.AmbientScenes.Scene;
import br.com.mobvoz.social.AmbientScenes.Situation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;

/**
 * Mundo vivo: perto do jogador, duas criaturas calmas conversam entre si (sobre o tempo, a vila, os bichos e, principalmente, sobre ELE:
 * o que seguram, como esta, o que cada uma realmente sabe dele, inclusive o nome). Falas prontas ({@link AmbientScenes}), sem IA; a voz
 * vem do TTS local. Orcamento do {@link ChatterScheduler} (nunca em cima de uma conversa do jogador). Nao muda o estado do jogo,
 * exceto que ouvir o nome do jogador em voz alta faz a outra criatura aprende-lo (cada uma tem a sua consciencia).
 */
public final class Ambient {
    private static final int CHECK_EVERY = 100;      // ticks entre sorteios por jogador
    private static final double CHANCE = 0.4;
    private static final double PLAYER_RADIUS = 16;
    private static final double PAIR_RADIUS = 6;
    private static final int RECENT = 80;

    private static final Random RND = new Random();
    private static final ArrayDeque<String> recent = new ArrayDeque<>();
    private static ChatterScheduler scheduler;
    private static int schedulerSeconds = -1;
    private static final AtomicInteger scenes = new AtomicInteger();
    private static final AtomicInteger lines = new AtomicInteger();
    private static volatile String onlyFamily;

    private Ambient() {
    }

    public static int scenesPlayed() {
        return scenes.get();
    }

    public static int linesSpoken() {
        return lines.get();
    }

    /** Para testes: so esta familia (null = todas). */
    public static void setOnlyFamily(String id) {
        onlyFamily = id;
    }

    /** Para testes: recomeca o orcamento e o historico. */
    public static void reset() {
        scheduler = null;
        schedulerSeconds = -1;
        recent.clear();
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var cfg = ServerConfig.current();
            if (!cfg.ambientChatter || !cfg.chatterEnabled || server.getTickCount() % CHECK_EVERY != 0) {
                return;
            }
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (RND.nextDouble() < CHANCE) {
                    tryScene(p, cfg);
                }
            }
        });
    }

    private static ChatterScheduler scheduler(ServerConfig cfg) {
        int secs = Math.max(10, cfg.ambientCooldownSeconds);
        if (scheduler == null || schedulerSeconds != secs) {
            scheduler = new ChatterScheduler(new ChatterScheduler.Config(secs * 20L, secs * 60L, 3, 3));
            schedulerSeconds = secs;
        }
        return scheduler;
    }

    static void tryScene(ServerPlayer player, ServerConfig cfg) {
        if (!(player.level() instanceof ServerLevel level) || !level.dimension().location().getPath().equals("overworld")
                || ConversationService.activeSessions() > 0) {
            return;
        }
        List<Mob> near = new ArrayList<>(level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(PLAYER_RADIUS),
                m -> m.isAlive() && (!(m instanceof Enemy) || MONSTERS.contains(typeName(m))) && m.getTarget() == null
                        && !ConversationService.isBusy(m.getUUID())));
        if (near.size() < 2) {
            return;
        }
        Collections.shuffle(near, RND);
        for (Mob x : near) {
            for (Mob y : near) {
                if (x == y || x.distanceTo(y) > PAIR_RADIUS || !canChat(classesOf(x), classesOf(y), x.getType() == y.getType())) {
                    continue;
                }
                if (start(level, player, RND.nextBoolean() ? x : y, RND.nextBoolean() ? y : x, cfg)) {
                    return;
                }
            }
        }
    }

    private static boolean start(ServerLevel level, ServerPlayer player, Mob a, Mob b, ServerConfig cfg) {
        if (a == b) {
            return false;
        }
        Situation sit = situation(level, player, a, b);
        String only = onlyFamily;
        var pick = AmbientScenes.pick(sit, RND, recent::contains, id -> only == null || id.equals(only));
        if (pick.isEmpty()) {
            return false;
        }
        long now = level.getGameTime();
        String region = ChatterScheduler.region(level.dimension().location().toString(), a.getX(), a.getZ());
        if (!scheduler(cfg).tryAcquire(region, a.getUUID(), b.getUUID(), now, false)) {
            return false;
        }
        Scene scene = pick.get();
        recent.addLast(scene.key());
        while (recent.size() > RECENT) {
            recent.pollFirst();
        }
        scenes.incrementAndGet();
        if (!scene.name().isEmpty()) { // ouviu o nome em voz alta: as duas criaturas o aprendem
            for (Mob m : List.of(a, b)) {
                if (!MemoryStore.get(m).nameOf(player.getUUID()).equals(scene.name())) {
                    MemoryStore.update(m, mm -> mm.withName(player.getUUID(), scene.name()));
                }
            }
        }
        play(List.of(a, b), player, scene.lines(), 0, scene.aboutPlayer());
        return true;
    }

    /** Fala as linhas em sequencia (uma por vez); legenda para quem usa so texto. */
    static void play(List<Mob> actors, ServerPlayer player, List<Line> ls, int i, boolean aboutPlayer) {
        if (i >= ls.size()) {
            return;
        }
        Line l = ls.get(i);
        Mob speaker = actors.get(l.speaker());
        Mob listener = actors.get(1 - l.speaker());
        if (!speaker.isAlive() || player.hasDisconnected() || player.distanceTo(speaker) > 24 || ConversationService.isBusy(speaker.getUUID())) {
            return;
        }
        speaker.getLookControl().setLookAt(aboutPlayer && i == 0 ? player : listener, 30f, 30f); // falando DELE, olha para ele
        lines.incrementAndGet();
        if (ServerPlayNetworking.canSend(player, SubtitlePayload.TYPE)) {
            ServerPlayNetworking.send(player, new SubtitlePayload(speaker.getUUID(), Profiles.displayName(speaker), l.text()));
        }
        ConversationService.announce(speaker, player, l.text(), () -> play(actors, player, ls, i + 1, aboutPlayer));
    }

    static Situation situation(ServerLevel level, ServerPlayer player, Mob a, Mob b) {
        Set<String> tags = new HashSet<>();
        long t = ((level.getDayTime() % 24000) + 24000) % 24000;
        tags.add(t < 12000 ? "day" : t < 13000 ? "dusk" : t < 23000 ? "night" : "dawn");
        if (level.isThundering()) {
            tags.add("thunder");
        } else if (level.isRaining()) {
            tags.add("rain");
        } else {
            tags.add("clear");
        }
        if (!a.hasLineOfSight(player) && !b.hasLineOfSight(player)) {
            tags.add("unseen"); // atras de uma parede: nao comentam o que ele segura, veste ou faz (so clima, hora e o que lembram dele)
        }
        if (player.getHealth() <= 12) {
            tags.add("hurt");
        }
        if (player.getFoodData().getFoodLevel() <= 6) {
            tags.add("hungry");
        }
        String item = SceneFacts.heldName(player);
        if (!item.isEmpty()) {
            tags.add("holds");
            var held = player.getMainHandItem();
            if (Profiles.ofMob(a).likeFactor(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem()).toString()) >= 1.0) {
                tags.add("want0"); // quem abre a conversa gosta do que o jogador segura
            }
        }
        String armor = SceneFacts.armorName(player);
        if (!armor.isEmpty()) {
            tags.add("armor");
        }
        addWorldAndPlayerTags(tags, level, player, a, t);
        MobMemory ma = MemoryStore.get(a);
        MobMemory mb = MemoryStore.get(b);
        var pid = player.getUUID();
        if (ma.knows(pid)) {
            tags.add("k0");
        }
        if (mb.knows(pid)) {
            tags.add("k1");
        }
        if (!ma.nameOf(pid).isEmpty()) {
            tags.add("n0");
        }
        if (!mb.nameOf(pid).isEmpty()) {
            tags.add("n1");
        }
        ma.facts().stream().filter(f -> f.player().equals(pid)).forEach(f -> tags.add("f0:" + f.type()));
        return new Situation(tags, classesOf(a), classesOf(b), ma.nameOf(pid), mb.nameOf(pid), item, armor);
    }

    /** Bioma, lua, altura e o que o jogador esta fazendo agora (agachado, correndo, montado, nadando, pegando fogo, voando) ou carregando. */
    private static void addWorldAndPlayerTags(Set<String> tags, ServerLevel level, ServerPlayer player, Mob at, long dayTime) {
        String biome = level.getBiome(at.blockPosition()).unwrapKey().map(k -> k.location().getPath()).orElse("");
        if (biome.contains("snow") || biome.contains("frozen") || biome.contains("ice") || biome.contains("grove")) {
            tags.add("snow");
        } else if (biome.contains("desert") || biome.contains("badlands")) {
            tags.add("desert");
        } else if (biome.contains("jungle") || biome.contains("bamboo")) {
            tags.add("jungle");
        } else if (biome.contains("swamp")) {
            tags.add("swamp");
        } else if (biome.contains("forest") || biome.contains("taiga")) {
            tags.add("forest");
        } else if (biome.contains("windswept") || biome.contains("peaks") || biome.contains("slopes") || biome.contains("meadow")) {
            tags.add("mountain");
        } else if (biome.contains("beach") || biome.contains("shore")) {
            tags.add("beach");
        } else if (biome.contains("savanna")) {
            tags.add("savanna");
        }
        if (dayTime >= 13000 && dayTime < 23000) { // fase da lua: so se fala dela de noite
            int phase = level.getMoonPhase();
            if (phase == 0) {
                tags.add("fullmoon");
            } else if (phase == 4) {
                tags.add("newmoon");
            }
        }
        if (at.getY() < 0) {
            tags.add("deep");
        } else if (at.getY() > 100) {
            tags.add("high");
        }
        if (player.isShiftKeyDown()) {
            tags.add("sneak");
        }
        if (player.isSprinting()) {
            tags.add("run");
        }
        if (player.getVehicle() != null) {
            tags.add("ride");
        }
        if (player.isInWater()) {
            tags.add("swim");
        }
        if (player.isOnFire()) {
            tags.add("burning");
        }
        if (player.isFallFlying()) {
            tags.add("fly");
        }
        if (player.getInventory().hasAnyMatching(st -> st.is(net.minecraft.world.item.Items.EMERALD) || st.is(net.minecraft.world.item.Items.EMERALD_BLOCK))) {
            tags.add("emeralds");
        }
        if (player.getInventory().hasAnyMatching(st -> st.is(net.minecraft.world.item.Items.DIAMOND) || st.is(net.minecraft.world.item.Items.DIAMOND_BLOCK))) {
            tags.add("diamonds");
        }
        int allies = br.com.mobvoz.orders.Orders.alliesOf(player).size();
        if (allies >= 2) {
            tags.add("followers");
        } else if (allies == 1) {
            tags.add("ally");
        }
    }

    /** Monstros que conversam entre si (zumbi com esqueleto, creeper, aranha...; nunca com aldeao nem animal); os demais hostis nunca puxam assunto. */
    static final Set<String> MONSTERS = Set.of("zombie", "skeleton", "spider", "creeper", "enderman", "witch", "warden");

    /** Bichos da mata: a classe "forest" das aberturas de conversa. */
    static final Set<String> FOREST = Set.of("fox", "wolf", "bee", "rabbit", "parrot", "panda", "ocelot");

    private static String typeName(Mob m) {
        String key = Profiles.keyOf(m);
        String type = key.contains("#") ? key.substring(0, key.indexOf('#')) : key;
        return type.substring(type.indexOf(':') + 1);
    }

    /**
     * Duas criaturas so conversam se faz sentido: aldeoes entre si (e o golem da vila), animais de fazenda entre si (o "rebanho": vaca, porco,
     * ovelha, galinha, cavalo), os demais animais (lobo, gato...) so com a MESMA especie, e os monstros entre si (qualquer especie).
     */
    static boolean canChat(Set<String> a, Set<String> b, boolean sameType) {
        return a.contains("villagers") && b.contains("villagers")
                || a.contains("animals") && b.contains("animals") && (a.contains("herd") && b.contains("herd") || sameType)
                || a.contains("monsters") && b.contains("monsters");
    }

    /** Classes de assunto da criatura (ver genambient.py): tipo ("villagers", "animals", "monsters"), "calm" (aldeao ou animal) e a especie. */
    static Set<String> classesOf(Mob m) {
        Set<String> c = new HashSet<>();
        if (m instanceof Enemy) {
            c.add("monsters");
            c.add(typeName(m));
            return c;
        }
        c.add("calm");
        String key = Profiles.keyOf(m);
        String type = key.contains("#") ? key.substring(0, key.indexOf('#')) : key;
        String name = type.substring(type.indexOf(':') + 1);
        if (m instanceof Animal) {
            c.add("animals");
        }
        c.add(name); // a especie tambem e uma classe (fox, bee, dolphin...): as aberturas de conversa tem familias proprias
        if (FOREST.contains(name)) {
            c.add("forest");
        }
        if (m instanceof net.minecraft.world.entity.animal.WaterAnimal || name.equals("turtle") || name.equals("axolotl")) {
            c.add("aquatic");
        }
        if (m instanceof net.minecraft.world.entity.animal.AbstractFish) {
            c.add("fish");
        }
        switch (name) {
            case "cow", "pig", "sheep", "chicken" -> {
                c.add("farm");
                c.add("herd");
                c.add(name);
            }
            case "wolf", "cat" -> {
                c.add("pet");
                c.add(name);
            }
            case "horse", "donkey", "mule" -> {
                c.add("horse");
                c.add("herd");
            }
            case "iron_golem" -> {
                c.add("golem");
                c.add("villagers");
            }
            default -> {
            }
        }
        if (m instanceof net.minecraft.world.entity.npc.AbstractVillager) {
            c.add("villagers");
        }
        if (m instanceof Villager) {
            c.add("villager");
            if (key.contains("#")) {
                c.add("villager:" + key.substring(key.indexOf('#') + 1));
            }
        }
        return c;
    }
}
