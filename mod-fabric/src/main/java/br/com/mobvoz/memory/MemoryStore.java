package br.com.mobvoz.memory;

import br.com.mobvoz.MobVozMod;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Guarda a memoria como anexo persistente da entidade (Fabric Data Attachment): vai junto no NBT do mob, entao
 * salvar/sair/carregar preserva identidade e fatos sem arquivo proprio. Somente a thread do servidor mexe nisso.
 */
public final class MemoryStore {
    public static final AttachmentType<MobMemory> TYPE = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "memory"), MobMemory.CODEC);

    private MemoryStore() {
    }

    public static void init() {
        // forca o carregamento da classe (registro do anexo) durante a inicializacao do mod
    }

    public static MobMemory get(Mob mob) {
        MobMemory m = mob.getAttached(TYPE);
        if (m == null) {
            m = MobMemory.create(UUID.randomUUID());
            mob.setAttached(TYPE, m);
        }
        return m;
    }

    public static MobMemory update(Mob mob, UnaryOperator<MobMemory> change) {
        MobMemory m = change.apply(get(mob));
        mob.setAttached(TYPE, m);
        return m;
    }
}
