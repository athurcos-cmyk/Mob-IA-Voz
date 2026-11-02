package br.com.mobvoz.target;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Escolhe UM alvo: a caixa mais proxima atingida pelo segmento de mira. */
public final class TargetPicker {
    /** Alcance padrao experimental da mira (blocos), conforme docs/01. */
    public static final double RANGE = 6.0;

    /** Folga angular da mira (graus) para bicho pequeno, e ate onde o alvo anterior ainda vale (blocos). */
    public static final double CONE_DEGREES = 7.0;
    public static final double STICKY_RANGE = 8.0;

    private TargetPicker() {
    }

    /** O centro mais proximo da direcao de mira dentro de {@code maxDegrees}, no alcance; -1 se nenhum. {@code look} e o vetor de mira ja com o comprimento do alcance. */
    public static int nearestInCone(Vec3 from, Vec3 look, List<Vec3> centers, double maxDegrees) {
        double cosMax = Math.cos(Math.toRadians(maxDegrees));
        Vec3 dir = look.normalize();
        int best = -1;
        double bestCos = cosMax;
        for (int i = 0; i < centers.size(); i++) {
            Vec3 toC = centers.get(i).subtract(from);
            double len = toC.length();
            if (len < 1e-6 || len > look.length() + 1.0) {
                continue;
            }
            double c = dir.dot(toC) / len;
            if (c >= bestCos) {
                bestCos = c;
                best = i;
            }
        }
        return best;
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
