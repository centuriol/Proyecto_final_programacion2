package org.example.game.motor;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Interfaz para cuerpos que participan en la simulación física.
 * Define las propiedades dinámicas necesarias para la integración numérica:
 * velocidad, fuerza acumulada, posición, masa y estado de simulación.
 */
public interface CuerpoFisico {
    // Estado dinámico
    Vector2D getVelocidad();
    void setVelocidad(Vector2D velocidad);

    Vector2D getFuerza();
    void setFuerza(Vector2D fuerza);
    void aplicarFuerza(Vector2D fuerza);
    void resetearFuerza();

    void confirmarPaso(Vector2D nuevaPosicion, Vector2D nuevaVelocidad);

    // Control de simulación
    boolean esSimulado();
    void setSimulado(boolean simulado);

    // Tipo de cuerpo para lógica especial (agujero negro, estrella, etc.)
    TipoCuerpo getTipoCuerpo();

    // Radio físico (para colisiones, distinto del radio visual)
    double getRadioFisico();

    // Radio de influencia gravitacional / colapso arcade
    double getRadioAtraccion();

    // Posición
    Vector2D getPosicion();
    void setPosicion(Vector2D pos);

    // Masa
    double getMasa();
    void setMasa(double masa);

    // Nombre
    String getNombre();
}
