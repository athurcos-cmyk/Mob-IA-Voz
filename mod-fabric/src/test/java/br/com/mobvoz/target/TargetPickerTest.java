package br.com.mobvoz.target;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class TargetPickerTest {
    private static AABB cowAt(double x, double z) {
        return new AABB(x - 0.45, 0, z - 0.45, x + 0.45, 1.4, z + 0.45);
    }

    private static final Vec3 EYE = new Vec3(0, 1, 0);
    private static final Vec3 END = new Vec3(0, 1, TargetPicker.RANGE);

    /** T02: dez vacas em 6 blocos, mira em uma -> exatamente uma, a mais proxima da mira. */
    @Test
    void tenCowsOnlyOneIsPicked() {
        List<AABB> cows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cows.add(cowAt(-4.5 + i, 4.0));
        }
        // mira reta em z: so a vaca em x=0 (indice 4 ou 5 por arredondamento) esta no raio
        int idx = TargetPicker.nearest(new Vec3(0.5, 1, 0), new Vec3(0.5, 1, 6), cows);
        assertEquals(5, idx);
    }

    /** Duas vacas alinhadas: vale a da frente. */
    @Test
    void closestWinsWhenAligned() {
        assertEquals(1, TargetPicker.nearest(EYE, END, List.of(cowAt(0, 5), cowAt(0, 2))));
    }

    /** T03: desviar a mira -> nenhum alvo. */
    @Test
    void lookingAwayPicksNothing() {
        assertEquals(-1, TargetPicker.nearest(EYE, new Vec3(6, 1, 0), List.of(cowAt(0, 3))));
    }

    /** Alem de 6 blocos nao ha alvo. */
    @Test
    void outOfRangePicksNothing() {
        assertEquals(-1, TargetPicker.nearest(EYE, END, List.of(cowAt(0, 7))));
    }
}
