package org.example.game.cuerpo;

/**
 * ISP: solo la implementan los cuerpos que pueden tener civilización.
 */
public interface Civilizable {
    void desarrollarCivilizacion();
    boolean tieneCivilizacion();
}
