package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Cuerpos celestes con capacidades de defensa o escudo activo (ISP).
 */
public interface Defensor {
    boolean tieneEscudo();
    int getGolpesEscudoRestantes();
    void activarEscudo(int golpes);
    boolean absorberGolpeEscudo();
    boolean puedeRecibirEscudo();
    double getRadioEscudo();
    double getPulsoVisual();

    double getMasa();
    double getMasaBase();
    double getRadio();
    String getNombre();
    Vector2D getPosicion();
}
