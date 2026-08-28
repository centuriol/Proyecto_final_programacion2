package org.example;
import java.util.List;

/**
 * DIP: SistemaSolar depende de esta interfaz, no de una implementación
 * concreta de física. Se puede cambiar el cálculo sin tocar SistemaSolar.
 */
public interface MotorFisica {
    void avanzarPaso(List<CuerpoCeleste> cuerpos);
}
