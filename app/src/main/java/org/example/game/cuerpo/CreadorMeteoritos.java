package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Capacidad segregada para la creación de meteoritos y fragmentos de colisión (ISP).
 */
public interface CreadorMeteoritos {
    Meteorito crearMeteoritoHaciaObjetivo(double anchoMundo, double altoMundo, CuerpoCeleste objetivo, double factorProgreso);
    Meteorito crearMeteoritoJugadorFisica(double x, double y, Vector2D vel);
    Meteorito crearFragmentoColision(double x, double y, Vector2D velocidadBase, double masaFragmento);
}
