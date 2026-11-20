package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionColisiones;
import java.util.List;
import java.util.Set;

/**
 * SRP: Intercepta meteoritos dentro del radio defensivo de satélites activos.
 */
public class InterceptorMeteoritosSatelite implements InterceptorAmenazas {

    private final ResolucionColision resolvedorColisiones;

    public InterceptorMeteoritosSatelite(ResolucionColision resolvedorColisiones) {
        this.resolvedorColisiones = resolvedorColisiones;
    }

    public void interceptar(List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        for (CuerpoCeleste satelite : simulados) {
            if (estaActivo(satelite, aEliminar) && satelite.getTipoCuerpo() == TipoCuerpo.SATELITE) {
                interceptarAmenazasParaSatelite(satelite, simulados, aEliminar, aAgregar);
            }
        }
    }

    private void interceptarAmenazasParaSatelite(CuerpoCeleste satelite, List<CuerpoCeleste> simulados,
                                                 Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        for (CuerpoCeleste amenaza : simulados) {
            if (!estaActivo(amenaza, aEliminar) || amenaza.getTipoCuerpo() != TipoCuerpo.METEORITO) continue;
            if (estaEnContactoConEscudoActivo(amenaza, simulados, aEliminar)) continue;
            double dist = satelite.getPosicion().distanciaA(amenaza.getPosicion());
            double radioDefensa = Math.max(satelite.getRadio() + amenaza.getRadio(), ConfiguracionColisiones.RADIO_INTERCEPCION_SATELITE);
            if (dist <= radioDefensa) {
                resolvedorColisiones.resolverColision(satelite, amenaza, aEliminar, aAgregar);
                if (!estaActivo(satelite, aEliminar)) break;
            }
        }
    }

    private boolean estaEnContactoConEscudoActivo(CuerpoCeleste amenaza, List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar) {
        for (CuerpoCeleste candidato : simulados) {
            if (candidato == null || aEliminar.contains(candidato)) continue;
            if (ServicioEscudo.getInstanciaPorDefecto().tieneEscudo(candidato)
                    && amenaza.getPosicion().distanciaA(candidato.getPosicion()) < candidato.getRadio() + amenaza.getRadio()) {
                return true;
            }
        }
        return false;
    }

    private boolean estaActivo(CuerpoCeleste c, Set<CuerpoCeleste> aEliminar) {
        return c != null && c.esSimulado() && !aEliminar.contains(c);
    }
}
