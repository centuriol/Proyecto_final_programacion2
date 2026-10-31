package org.example.game.cuerpo;
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
}
