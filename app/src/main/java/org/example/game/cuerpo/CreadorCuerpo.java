package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Estrategia funcional para la creación dinámica de cuerpos celestes específicos (OCP).
 */
@FunctionalInterface
public interface CreadorCuerpo {

    CuerpoCeleste crear(double x, double y, double factorMasa, Vector2D velocidadInicial);
}
