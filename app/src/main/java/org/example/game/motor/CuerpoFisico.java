package org.example.game.motor;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Interfaz para cuerpos que participan en la simulación física.
 * Define las propiedades dinámicas necesarias para la integración numérica.
 */
public interface CuerpoFisico {
    Vector2D getVelocidad();
    void setVelocidad(Vector2D velocidad);

    Vector2D getFuerza();
    void aplicarFuerza(Vector2D fuerza);

    boolean esSimulado();
    void setSimulado(boolean simulado);

    TipoCuerpo getTipoCuerpo();
    double getRadio();
    double getRadioAtraccion();

    Vector2D getPosicion();
    void setPosicion(Vector2D pos);

    double getMasa();
    void setMasa(double masa);

    String getNombre();
}
