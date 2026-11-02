package br.com.mobvoz.target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class StickyTargetTest {
    private final UUID cow = UUID.randomUUID();
    private final UUID pig = UUID.randomUUID();

    /** A mira escapa (o bicho anda ou o mouse mexe): o alvo fica por 5 s se ainda estiver perto e visivel, e ninguem mais entra no lugar dele. */
    @Test
    void keepsTheLastTargetForAWhileAfterTheAimLeaves() {
        StickyTarget s = new StickyTarget();
        assertEquals(cow, s.choose(cow, 0, true));
        assertEquals(cow, s.choose(null, 30, true), "1,5 s depois ainda e a vaca");
        assertEquals(cow, s.choose(null, StickyTarget.KEEP_TICKS, true));
        assertNull(s.choose(null, StickyTarget.KEEP_TICKS + 5, true), "passou da folga");
    }

    @Test
    void dropsItWhenItIsTooFarOrHidden() {
        StickyTarget s = new StickyTarget();
        s.choose(cow, 0, true);
        assertNull(s.choose(null, 10, false), "longe ou atras de uma parede: solta");
        assertNull(s.choose(null, 11, true), "e nao volta sozinho");
    }

    @Test
    void aNewAimedTargetAlwaysWins() {
        StickyTarget s = new StickyTarget();
        s.choose(cow, 0, true);
        assertEquals(pig, s.choose(pig, 5, true));
        assertEquals(pig, s.choose(null, 20, true));
    }

    /** Cone de mira: bicho pequeno ao lado do raio exato ainda e pego; o fora do cone nao; escolhe o mais centrado. */
    @Test
    void smallConeHelpsWithTinyCreatures() {
        Vec3 eye = new Vec3(0, 1, 0);
        Vec3 look = new Vec3(0, 0, TargetPicker.RANGE);
        List<Vec3> centers = List.of(new Vec3(0.5, 1, 4), new Vec3(0.1, 1, 4), new Vec3(3, 1, 4));
        assertEquals(1, TargetPicker.nearestInCone(eye, look, centers, TargetPicker.CONE_DEGREES), "o mais centrado dentro do cone");
        assertEquals(-1, TargetPicker.nearestInCone(eye, look, List.of(new Vec3(3, 1, 4)), TargetPicker.CONE_DEGREES), "fora do cone");
        assertEquals(-1, TargetPicker.nearestInCone(eye, look, List.of(new Vec3(0, 1, 12)), TargetPicker.CONE_DEGREES), "alem do alcance");
    }
}
