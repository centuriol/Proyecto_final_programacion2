package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.Set;

/**
 * Abstracción para detección y derivación de contactos físicos entre cuerpos simulados (DIP / ISP).
 */
public interface DetectorContactoFisico {
    void detectarYResolver(List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar);
}
