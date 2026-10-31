package org.example.game.cuerpo;
/**
 * Cuerpos celestes habitables que pueden ser colonizados por naves de colonización (ISP).
 */
public interface Colonizable {
    boolean puedeSerColonizado();
    void recibirColonos(double poblacionColonos);
}
