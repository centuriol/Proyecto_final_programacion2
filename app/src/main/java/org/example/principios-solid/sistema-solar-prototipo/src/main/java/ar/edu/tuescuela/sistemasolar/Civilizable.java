package org.example;

/**
 * ISP: solo la implementan los cuerpos que pueden tener civilización.
 */
public interface Civilizable {
    void desarrollarCivilizacion();
    boolean tieneCivilizacion();
}
