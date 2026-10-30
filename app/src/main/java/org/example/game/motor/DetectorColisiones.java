package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;

import java.util.List;
import java.util.Set;

/**
 * Responsable de la detección de contactos físicos y derivación al resolvedor de colisiones (SRP).
 */
public class DetectorColisiones {

    private final ResolvedorColisiones resolvedorColisiones;

    public DetectorColisiones(ResolvedorColisiones resolvedorColisiones) {
        this.resolvedorColisiones = resolvedorColisiones;
    }

    public void detectarYResolver(List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        for (int i = 0; i < simulados.size(); i++) {
            CuerpoCeleste a = simulados.get(i);
            if (aEliminar.contains(a)) continue;

            for (int j = i + 1; j < simulados.size(); j++) {
                CuerpoCeleste b = simulados.get(j);
                if (aEliminar.contains(b)) continue;

                double dist = a.getPosicion().distanciaA(b.getPosicion());
                double radioContacto = a.getRadio() + b.getRadio();

                if (dist < radioContacto) {
                    if (a.estaOrbitando(b)) {
                        separarContactoOrbital(a, b, dist);
                        continue;
                    }
                    if (b.estaOrbitando(a)) {
                        separarContactoOrbital(b, a, dist);
                        continue;
                    }

                    resolvedorColisiones.resolverColision(a, b, aEliminar, aAgregar);
                }
            }
        }
    }

    private void separarContactoOrbital(CuerpoCeleste orbitante, CuerpoCeleste orbitado, double dist) {
        double minOrb = orbitado.getRadio() + orbitante.getRadio() + 4.0;
        Vector2D norm = dist > 0.001 ? orbitante.getPosicion().restar(orbitado.getPosicion()).normalizar() : new Vector2D(1, 0);
        orbitante.setPosicion(orbitado.getPosicion().sumar(norm.multiplicar(minOrb)));
    }
}
