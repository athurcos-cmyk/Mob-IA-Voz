package br.com.mobvoz.orders;

import br.com.mobvoz.MobVozMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

/**
 * Ordens persistentes de UM mob (seguir/ficar/segurar portal/tregua). Anexo da entidade: sobrevive a salvar/carregar e a
 * troca de chunk. So o servidor altera, depois de validar (OrderPolicy). Imutavel.
 */
public record MobOrders(Mode mode, String owner, String anchorDim, double ax, double ay, double az, boolean portalHold, boolean defend,
                        String truceFor, long truceUntil, long truceCooldownUntil, long ownerLastSeen) {
    public enum Mode { NONE, FOLLOW, STAY }

    public static final MobOrders NONE = new MobOrders(Mode.NONE, "", "", 0, 0, 0, false, false, "", 0, 0, 0);

    public static final Codec<MobOrders> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(MobOrders::parseMode, Enum::name).fieldOf("mode").forGetter(MobOrders::mode),
            Codec.STRING.fieldOf("owner").forGetter(MobOrders::owner),
            Codec.STRING.fieldOf("anchorDim").forGetter(MobOrders::anchorDim),
            Codec.DOUBLE.fieldOf("ax").forGetter(MobOrders::ax),
            Codec.DOUBLE.fieldOf("ay").forGetter(MobOrders::ay),
            Codec.DOUBLE.fieldOf("az").forGetter(MobOrders::az),
            Codec.BOOL.fieldOf("portalHold").forGetter(MobOrders::portalHold),
            Codec.BOOL.optionalFieldOf("defend", false).forGetter(MobOrders::defend),
            Codec.STRING.fieldOf("truceFor").forGetter(MobOrders::truceFor),
            Codec.LONG.fieldOf("truceUntil").forGetter(MobOrders::truceUntil),
            Codec.LONG.fieldOf("truceCooldownUntil").forGetter(MobOrders::truceCooldownUntil),
            Codec.LONG.fieldOf("ownerLastSeen").forGetter(MobOrders::ownerLastSeen)).apply(i, MobOrders::new));

    public static final AttachmentType<MobOrders> TYPE = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(MobVozMod.MOD_ID, "orders"), CODEC);

    private static Mode parseMode(String s) {
        try {
            return Mode.valueOf(s);
        } catch (RuntimeException e) {
            return Mode.NONE;
        }
    }

    public boolean active() {
        return mode != Mode.NONE || portalHold || defend || truceUntil > 0;
    }

    public MobOrders follow(String owner, long tick) {
        return new MobOrders(Mode.FOLLOW, owner, anchorDim, ax, ay, az, portalHold, defend, truceFor, truceUntil, truceCooldownUntil, tick);
    }

    public MobOrders stay(String owner, String dim, double x, double y, double z, long tick) {
        return new MobOrders(Mode.STAY, owner, dim, x, y, z, portalHold, defend, truceFor, truceUntil, truceCooldownUntil, tick);
    }

    public MobOrders withDefend(String owner, boolean on) {
        return new MobOrders(mode, on && this.owner.isEmpty() ? owner : this.owner, anchorDim, ax, ay, az, portalHold, on, truceFor,
                truceUntil, truceCooldownUntil, ownerLastSeen);
    }

    /** Pacto rompido: tregua acaba na hora e o cooldown fica longo (nao renegocia de imediato). */
    public MobOrders truceBroken(long cooldownUntil) {
        return new MobOrders(mode, owner, anchorDim, ax, ay, az, portalHold, defend, "", 0, cooldownUntil, ownerLastSeen);
    }

    public MobOrders withPortalHold(boolean hold) {
        return new MobOrders(mode, owner, anchorDim, ax, ay, az, hold, defend, truceFor, truceUntil, truceCooldownUntil, ownerLastSeen);
    }

    public MobOrders withTruce(String player, long until, long cooldownUntil) {
        return new MobOrders(mode, owner, anchorDim, ax, ay, az, portalHold, defend, player, until, cooldownUntil, ownerLastSeen);
    }

    public MobOrders withOwnerSeen(long tick) {
        return new MobOrders(mode, owner, anchorDim, ax, ay, az, portalHold, defend, truceFor, truceUntil, truceCooldownUntil, tick);
    }

    /** Dispensa o aliado: apaga seguir/ficar/portal/tregua; as memorias (MobMemory) ficam intactas. */
    public MobOrders dismissed() {
        return new MobOrders(Mode.NONE, "", "", 0, 0, 0, false, false, "", 0, truceCooldownUntil, 0);
    }
}
