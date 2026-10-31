package br.com.mobvoz.target;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Escolhe UM alvo: a caixa mais proxima atingida pelo segmento de mira. */
public final class TargetPicker {
    /** Alcance padrao experimental da mira (blocos), conforme docs/01. */
    public static final double RANGE = 6.0;

    private TargetPicker() {
    }

    /** @return indice da caixa mais proxima de {@code from} atingida pelo segmento, ou -1. */
    public static int nearest(Vec3 from, Vec3 to, List<AABB> boxes) {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < boxes.size(); i++) {
            AABB box = boxes.get(i);
            Optional<Vec3> hit = box.contains(from) ? Optional.of(from) : box.clip(from, to);
            if (hit.isEmpty()) {
                continue;
            }
            double dist = hit.get().distanceToSqr(from);
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }
}
