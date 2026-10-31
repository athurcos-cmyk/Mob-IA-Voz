package br.com.mobvoz.profile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * "Consciencia" da criatura (lore.json, pesquisada no wiki e resumida): de onde veio, como PERCEBE o mundo (o Warden e cego e
 * escuta vibracoes), por que faz o que faz, o que teme e como reage a uma ameaca. Entra no prompt como fatos sobre si mesma;
 * lendas ficam marcadas como "dizem". Criaturas sem entrada recebem uma consciencia derivada da categoria.
 */
public final class Lore {
    public enum Threat { FLEE, FIGHT, UNMOVED }

    public record Entry(String origem, String sentidos, String motivacao, String medos, Threat ameaca, String reacao) {
    }

    private static final Map<String, Entry> BY_ID = new HashMap<>();

    static {
        try (InputStream in = Lore.class.getResourceAsStream("/mobvoz/lore.json")) {
            JsonObject lore = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("lore");
            for (var e : lore.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                BY_ID.put(e.getKey(), new Entry(o.get("origem").getAsString(), o.get("sentidos").getAsString(), o.get("motivacao").getAsString(),
                        o.get("medos").getAsString(), Threat.valueOf(o.get("ameaca").getAsString().toUpperCase()), o.get("reacao").getAsString()));
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("lore.json invalido", e);
        }
    }

    private Lore() {
    }

    public static boolean has(String typeId) {
        return BY_ID.containsKey(typeId);
    }

    public static int count() {
        return BY_ID.size();
    }

    public static Entry of(String typeId) {
        return BY_ID.get(typeId);
    }

    /** Consciencia da criatura; sem lore proprio, deriva da categoria (monstro luta, animal foge, aquatico foge, resto nao se abala). */
    public static Entry forMob(Mob mob) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        Entry e = BY_ID.get(id);
        if (e != null) {
            return e;
        }
        String name = id.substring(id.indexOf(':') + 1).replace('_', ' ');
        boolean monster = mob instanceof Enemy;
        boolean water = mob instanceof net.minecraft.world.entity.animal.WaterAnimal;
        return new Entry("É um(a) " + name + " e não conhece a própria origem além do lugar onde vive.",
                "Percebe o mundo como a sua espécie percebe.", monster ? "Persegue o que encontra porque é da sua natureza." : "Vive o dia como a sua espécie vive.",
                monster ? "Luz, fogo e quem é mais forte." : "Perigos em geral e quem a machuca.",
                monster ? Threat.FIGHT : water || mob.getType().getCategory().isFriendly() ? Threat.FLEE : Threat.UNMOVED,
                monster ? "ao ouvir ameaça, reage com agressividade" : "ao ouvir ameaça, se assusta");
    }
}
