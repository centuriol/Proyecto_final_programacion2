package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Propiedades físicas e identificatorias necesarias para calcular costos y ubicar defensas planetarias (ISP).
 */
public interface IdentidadFisicaDefensor {
    double getMasa();
    double getMasaBase();
    double getRadio();
    String getNombre();
    Vector2D getPosicion();
}
