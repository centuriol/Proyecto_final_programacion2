package org.example.game.motor;

/**
 * Capacidad segregada para entidades con posición espacial y radios físicos/gravitacionales (ISP).
 */
public interface Posicionable {
    Vector2D getPosicion();
    void setPosicion(Vector2D pos);
    double getRadio();
    double getRadioAtraccion();
}
