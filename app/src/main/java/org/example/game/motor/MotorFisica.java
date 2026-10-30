package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;

/**
 * DIP: SistemaSolar y SimulacionSolar dependen de esta interfaz, no de una implementación
 * concreta de física. Se puede cambiar el cálculo sin tocar SistemaSolar ni SimulacionSolar.
 * Interfaz limpia sin métodos no-op vacíos (LSP / ISP).
 */
public interface MotorFisica {
    void avanzarPaso(List<CuerpoCeleste> cuerpos);
}
