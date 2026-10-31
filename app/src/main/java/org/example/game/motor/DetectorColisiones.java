package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.simulacion.ConfiguracionColisiones; import java.util.List; import java.util.Set;
/**
 * Responsable de la detección de contactos físicos y derivación al resolvedor de colisiones (SRP).
 */
public class DetectorColisiones {

    private final ResolvedorColisiones resolvedorColisiones;

    public DetectorColisiones(ResolvedorColisiones resolvedorColisiones) {
        this.resolvedorColisiones = resolvedorColisiones;
    }

    public void detectarYResolver(List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        interceptarMeteoritosConSatelites(simulados, aEliminar, aAgregar);

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

    private void interceptarMeteoritosConSatelites(List<CuerpoCeleste> simulados,
                                                   Set<CuerpoCeleste> aEliminar,
                                                   List<CuerpoCeleste> aAgregar) {
        for (CuerpoCeleste satelite : simulados) {
            if (satelite == null || aEliminar.contains(satelite) || satelite.getTipoCuerpo() != TipoCuerpo.SATELITE) {
                continue;
            }
            for (CuerpoCeleste amenaza : simulados) {
                if (amenaza == null || aEliminar.contains(amenaza) || amenaza.getTipoCuerpo() != TipoCuerpo.METEORITO) {
                    continue;
                }
                if (estaEnContactoConEscudoActivo(amenaza, simulados, aEliminar)) {
                    continue;
                }
                double dist = satelite.getPosicion().distanciaA(amenaza.getPosicion());
                double radioDefensa = Math.max(
                        satelite.getRadio() + amenaza.getRadio(),
                        ConfiguracionColisiones.RADIO_INTERCEPCION_SATELITE
                );
                if (dist <= radioDefensa) {
                    resolvedorColisiones.resolverColision(satelite, amenaza, aEliminar, aAgregar);
                }
            }
        }
    }

    private boolean estaEnContactoConEscudoActivo(CuerpoCeleste amenaza,
                                                  List<CuerpoCeleste> simulados,
                                                  Set<CuerpoCeleste> aEliminar) {
        for (CuerpoCeleste candidato : simulados) {
            if (candidato == null || aEliminar.contains(candidato)) continue;
            if (candidato.tieneEscudo()) {
                double dist = amenaza.getPosicion().distanciaA(candidato.getPosicion());
                if (dist < candidato.getRadio() + amenaza.getRadio()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void separarContactoOrbital(CuerpoCeleste orbitante, CuerpoCeleste orbitado, double dist) {
        double minOrb = orbitado.getRadio() + orbitante.getRadio() + 4.0;
        Vector2D norm = dist > 0.001 ? orbitante.getPosicion().restar(orbitado.getPosicion()).normalizar() : new Vector2D(1, 0);
        orbitante.setPosicion(orbitado.getPosicion().sumar(norm.multiplicar(minOrb)));
    }
}
