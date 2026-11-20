package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.Set;

/**
 * Abstracción para la intercepción defensiva de amenazas (meteoritos) mediante satélites (DIP / ISP).
 */
public interface InterceptorAmenazas {
    void interceptar(List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar);
}
