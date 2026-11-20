package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.Set;

/**
 * Abstracción para resolución física de colisiones y efectos de impacto entre cuerpos (DIP / ISP).
 */
public interface ResolucionColision {
    void resolverColision(CuerpoCeleste a, CuerpoCeleste b, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar);
    void absorber(CuerpoCeleste absorbente, CuerpoCeleste victima, Set<CuerpoCeleste> aEliminar);
    void generarEscombros(CuerpoCeleste destruido, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar);
    void reboteElastico(CuerpoCeleste a, CuerpoCeleste b);
}
