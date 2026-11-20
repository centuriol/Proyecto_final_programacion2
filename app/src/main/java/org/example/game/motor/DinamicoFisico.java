package org.example.game.motor;

/**
 * Capacidad segregada para entidades sometidas a velocidades y fuerzas dinámicas (ISP).
 */
public interface DinamicoFisico {
    Vector2D getVelocidad();
    void setVelocidad(Vector2D velocidad);
    Vector2D getFuerza();
    void aplicarFuerza(Vector2D fuerza);
}
