package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;

/**
 * Abstracción para la integración numérica de posición y velocidad de cuerpos celestes (DIP / ISP).
 */
public interface IntegradorFisico {
    void integrar(List<CuerpoCeleste> cuerpos);
}
