package org.example.game.controlador;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;

/**
 * Abstracción para la selección espacial de cuerpos celestes bajo coordenadas del cursor (DIP / ISP).
 */
public interface SeleccionadorCuerpo {
    CuerpoCeleste encontrarCuerpo(double mouseX, double mouseY, double anchoMundo, double altoMundo, List<CuerpoCeleste> cuerpos);
}
