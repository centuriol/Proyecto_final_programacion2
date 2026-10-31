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
    default double getRadioEscudo() { return 65.0; }
    default double getPulsoVisual() { return 0.0; }

    double getMasa();
    double getMasaBase();
    double getRadio();
    String getNombre();
    Vector2D getPosicion();
}
