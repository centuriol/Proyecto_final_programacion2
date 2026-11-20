package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Capacidad segregada para la creación de planetas (ISP).
 */
public interface CreadorPlanetas {
    Planeta crearPlaneta(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial);
    Planeta crearPlanetaPersonalizado(String nombre, double masa, double x, double y, TipoCuerpo tipo, Vector2D velocidadInicial);
    Planeta crearPlanetaEnOrbita(CuerpoCeleste central, double distancia, double factorMasa, TipoCuerpo tipo);
}
