package org.example.game.motor;

/**
 * Interfaz compuesta para cuerpos que participan completamente en la simulación física (ISP).
 */
public interface CuerpoFisico extends Posicionable, DinamicoFisico, IdentidadSimulada {
    double getMasa();
    void setMasa(double masa);
}
