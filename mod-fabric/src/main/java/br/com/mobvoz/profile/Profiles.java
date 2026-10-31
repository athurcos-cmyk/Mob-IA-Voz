package br.com.mobvoz.profile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Perfis por especie/profissao, de {@code mobvoz/profiles.json}. Alem de personalidade/voz, cada perfil sabe QUEM e a criatura:
 * dieta, habitat ({@code diet}/{@code about}), o que aceita em troca ({@code wants}) e o UNICO que pode dar ({@code drops}).
 * Criaturas sem perfil proprio recebem um perfil derivado da categoria (monstro, animal, aquatico...), nunca um generico solto.
 */
public final class Profiles {
    public record Profile(String id, String label, String style, Set<String> likes, Set<String> dislikes, Set<String> wants,
                          Set<String> drops, String diet, String about, String address, String voice, double pitch, double speed,
                          int credulity, double follow) {
        /** Reacao a um presente: 1 = gosta, 0.15 = indiferente, -0.3 = nao gosta. */
        public double likeFactor(String itemId) {
            if (likes.contains(itemId) || wants.contains(itemId)) {
                return 1.0;
            }
            return dislikes.contains(itemId) ? -0.3 : 0.15;
        }

        public String reaction(String itemId) {
            double f = likeFactor(itemId);
            return f >= 1.0 ? "gostou" : f < 0 ? "não gostou" : "indiferente";
        }
    }

    private static final Map<String, Profile> BY_ID = new HashMap<>();
    private static Profile fallback;

    static {
        try (InputStream in = Profiles.class.getResourceAsStream("/mobvoz/profiles.json")) {
            JsonObject root = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            fallback = parse(root.getAsJsonObject("fallback"));
            root.getAsJsonArray("profiles").forEach(e -> {
                Profile p = parse(e.getAsJsonObject());
                BY_ID.put(p.id(), p);
            });
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("profiles.json invalido", e);
        }
    }

    private Profiles() {
    }

    private static Profile parse(JsonObject o) {
        return new Profile(o.get("id").getAsString(), o.get("label").getAsString(), o.get("style").getAsString(),
                set(o, "likes"), set(o, "dislikes"), set(o, "wants"), set(o, "drops"), str(o, "diet"), str(o, "about"),
                o.has("address") ? o.get("address").getAsString() : "Humano", o.get("voice").getAsString(), o.get("pitch").getAsDouble(),
                o.get("speed").getAsDouble(), o.get("credulity").getAsInt(), o.get("follow").getAsDouble());
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) ? o.get(k).getAsString() : "";
    }

    @SuppressWarnings("unchecked")
    private static Set<String> set(JsonObject o, String key) {
        return o.has(key) ? Set.copyOf(new Gson().fromJson(o.get(key), List.class)) : Set.of();
    }

    /** @param key "minecraft:villager#farmer" ou "minecraft:cow"; sem perfil = universal. */
    public static Profile of(String key) {
        Profile p = BY_ID.get(key);
        if (p == null && key.contains("#")) {
            p = BY_ID.get(key.substring(0, key.indexOf('#'))); // profissao sem perfil: cai na especie
        }
        return p != null ? p : fallback;
    }

    public static boolean has(String key) {
        return BY_ID.containsKey(key);
    }

    public static java.util.Collection<Profile> all() {
        return BY_ID.values();
    }

    /** Perfil da criatura; se a especie nao tem perfil, deriva um da CATEGORIA do mob (coerencia minima de especie). */
    public static Profile ofMob(net.minecraft.world.entity.Mob mob) {
        String key = keyOf(mob);
        if (has(key) || BY_ID.containsKey(key.contains("#") ? key.substring(0, key.indexOf('#')) : key)) {
            return of(key);
        }
        String name = key.substring(key.indexOf(':') + 1).replace('_', ' ');
        String[] cat = switch (mob.getType().getCategory()) {
            case MONSTER -> new String[] {"monstro hostil", "vive em lugares escuros e ataca quem encontra", "ameaçador e direto"};
            case WATER_CREATURE, WATER_AMBIENT, UNDERGROUND_WATER_CREATURE, AXOLOTLS ->
                    new String[] {"criatura aquática", "vive na água e se alimenta do que encontra nela", "calmo e fluido"};
            case AMBIENT -> new String[] {"animalzinho", "vive pelas cavernas e arredores", "tímido e curioso"};
            default -> new String[] {"animal", "vive na natureza e se alimenta do que combina com a sua espécie", "natural e curioso"};
        };
        return new Profile("*" + key, name, cat[2], Set.of(), Set.of(), Set.of(), Set.of(), "o que combina com um(a) " + name,
                "é um(a) " + name + ": " + cat[0] + "; " + cat[1], "Humano", fallback.voice(), fallback.pitch(), fallback.speed(),
                fallback.credulity(), mob instanceof net.minecraft.world.entity.monster.Enemy ? 0.6 : 0.1);
    }

    /** "minecraft:villager#farmer" para aldeoes com profissao; senao o id da especie. */
    public static String keyOf(net.minecraft.world.entity.Mob mob) {
        String type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        if (mob instanceof net.minecraft.world.entity.npc.Villager v) {
            return v.getVillagerData().profession().unwrapKey().map(k -> type + "#" + k.location().getPath()).orElse(type);
        }
        return type;
    }

    public static int count() {
        return BY_ID.size();
    }

    /** Variacao individual estavel (identidade -> +-7% no tom), para duas vacas nao soarem identicas. */
    public static double individualPitch(Profile p, UUID identity) {
        int h = (int) ((identity.getMostSignificantBits() ^ identity.getLeastSignificantBits()) & 0xFF);
        return p.pitch() * (0.93 + 0.14 * (h / 255.0));
    }
}
