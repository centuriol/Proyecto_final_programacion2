package org.example.game.motor;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Interfaz para cuerpos que participan en la simulación física newtoniana.
 * Define las propiedades dinámicas necesarias para la integración numérica:
 * velocidad, fuerza acumulada, y estado de simulación.
 */
public interface CuerpoFisico {
    // Estado dinámico
    Vector2D getVelocidad();
    void setVelocidad(Vector2D velocidad);

    Vector2D getFuerza();
    void setFuerza(Vector2D fuerza);
    void aplicarFuerza(Vector2D fuerza);
    void resetearFuerza();

    // Para Velocity Verlet (estado intermedio)
    Vector2D getVelocidadMedioPaso();
    void setVelocidadMedioPaso(Vector2D v);

    Vector2D getNuevaPosicion();
    void setNuevaPosicion(Vector2D pos);

    void confirmarPaso(Vector2D nuevaPosicion, Vector2D nuevaVelocidad);

    // Control de simulación
    boolean esSimulado();
    void setSimulado(boolean simulado);

    // Tipo de cuerpo para lógica especial (agujero negro, estrella, etc.)
    TipoCuerpo getTipoCuerpo();

    // Radio físico (para colisiones, distinto del radio visual)
    double getRadioFisico();

    // Posición (común con CuerpoCeleste)
    Vector2D getPosicion();
    void setPosicion(Vector2D pos);

    // Masa (común con CuerpoCeleste/Masivo)
    double getMasa();
    void setMasa(double masa);

    // Nombre
    String getNombre();
}