package org.example.game.cuerpo;

/**
 * Capacidad segregada para cuerpos celestes que pueden activar y gestionar un escudo defensivo (ISP).
 */
public interface Escudable {
    boolean tieneEscudo();
    int getGolpesEscudoRestantes();
    void activarEscudo(int golpes);
    boolean absorberGolpeEscudo();
    boolean puedeRecibirEscudo();
}
