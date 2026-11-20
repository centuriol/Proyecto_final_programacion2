package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;

/**
 * Abstracción para evaluar si el estado actual del sistema solar cumple la condición de derrota (DIP / ISP).
 */
public interface EvaluadorDerrota {
    boolean estaPerdida(List<CuerpoCeleste> cuerpos);
}
