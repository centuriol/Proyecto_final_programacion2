package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;

/**
 * Abstracción para búsqueda y validación de posiciones orbitales libres alrededor de una estrella (DIP / ISP).
 */
public interface BuscadorOrbita {
    Vector2D buscarPosicionLibre(CuerpoCeleste cuerpo, CuerpoCeleste estrella, double radioActivacion, List<CuerpoCeleste> cuerpos);
    boolean esPosicionLibre(Vector2D pos, CuerpoCeleste cuerpo, List<CuerpoCeleste> cuerpos);
}
