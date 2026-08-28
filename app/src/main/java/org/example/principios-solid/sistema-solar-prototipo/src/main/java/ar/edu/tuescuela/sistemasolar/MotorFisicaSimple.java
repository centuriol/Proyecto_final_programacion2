package org.example;

import java.util.List;

/**
 * SRP: esta clase solo calcula el paso de física (acá, orbitas circulares
 * simples). No sabe nada de JavaFX ni de cómo se dibuja.
 */
public class MotorFisicaSimple implements MotorFisica {
    @Override
    public void avanzarPaso(List<CuerpoCeleste> cuerpos) {
        for (CuerpoCeleste c : cuerpos) {
            c.orbitar();
        }
    }
}
