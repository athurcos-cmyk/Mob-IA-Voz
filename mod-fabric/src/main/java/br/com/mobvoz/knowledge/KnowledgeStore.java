package br.com.mobvoz.knowledge;

import br.com.mobvoz.MobVozMod;
import br.com.mobvoz.knowledge.Knowledge.Event;
import br.com.mobvoz.profile.Profiles;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persistencia (SavedData do Overworld) + ganchos que gravam eventos VERIFICADOS no {@link Knowledge}. */
public final class KnowledgeStore extends SavedData {
    static final SavedDataType<KnowledgeStore> TYPE = new SavedDataType<>(MobVozMod.MOD_ID + "_knowledge", KnowledgeStore::new,
            Knowledge.CODEC.xmap(KnowledgeStore::new, s -> s.k), DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private Knowledge k;

    public KnowledgeStore() {
        this(Knowledge.EMPTY);
    }

    KnowledgeStore(Knowledge k) {
        this.k = k;
    }

    public static Knowledge get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE).k;
    }

    public static void update(MinecraftServer server, UnaryOperator<Knowledge> change) {
        KnowledgeStore s = server.overworld().getDataStorage().computeIfAbsent(TYPE);
        Knowledge next = change.apply(s.k);
        if (!next.equals(s.k)) {
            s.k = next;
            s.setDirty();
        }
    }

    /** Faccao da criatura: quem a ofende, ofende o grupo (enderman -> "enderman", aldeoes -> "aldeia"). */
    public static String factionOf(String profileKey) {
        if (profileKey.startsWith("minecraft:villager") || profileKey.equals("minecraft:iron_golem")) {
            return "aldeia";
        }
        return profileKey.equals("minecraft:enderman") ? "enderman" : "";
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (taken > 0 && entity instanceof Mob m && source.getEntity() instanceof ServerPlayer p) {
                String f = factionOf(Profiles.keyOf(m));
                if (!f.isEmpty()) {
                    long t = p.level().getGameTime();
                    int x = m.getBlockX();
                    int z = m.getBlockZ();
                    update(p.getServer(), k -> k.withFaction(f, new Event("ATTACKED_MEMBER", p.getUUID(), "", t, x, z)));
                }
            }
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            MinecraftServer server = entity.level().getServer();
            if (server == null) {
                return;
            }
            long t = entity.level().getGameTime();
            if (entity instanceof ServerPlayer p) {
                update(server, k -> k.withHistory(new Event("PLAYER_DEATH", p.getUUID(), "", t)));
            } else if (source.getEntity() instanceof ServerPlayer killer) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                String name = switch (id) {
                    case "minecraft:wither" -> "o Wither";
                    case "minecraft:warden" -> "o Warden";
                    case "minecraft:elder_guardian" -> "o Grande Guardião";
                    case "minecraft:ender_dragon" -> "o Ender Dragon";
                    default -> "";
                };
                if (!name.isEmpty()) {
                    update(server, k -> k.withHistory(new Event("BOSS_DEFEATED", killer.getUUID(), name, t)));
                }
            }
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, from, to) -> {
            String dim = to.dimension().location().getPath();
            String name = dim.equals("the_end") ? "o Fim" : dim.equals("the_nether") ? "o Nether" : "";
            if (!name.isEmpty()) {
                long t = to.getGameTime();
                UUID id = player.getUUID();
                update(to.getServer(), k -> k.withHistory(new Event("ENTERED_DIMENSION", id, name, t)));
            }
        });
    }
}
