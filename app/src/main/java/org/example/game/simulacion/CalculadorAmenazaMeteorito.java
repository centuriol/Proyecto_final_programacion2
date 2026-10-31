package org.example.game.simulacion;

import org.example.game.progresion.SistemaProgresion;

/**
 * Interfaz para el escalado de amenaza de meteoritos según progresión y masa (ISP / DIP).
 */
public interface CalculadorAmenazaMeteorito {

    double factorProgreso(SistemaProgresion progresion);

    double fraccionDano(double factorProgreso);

    double radioPorProgreso(double factorProgreso);

    double masaPorProgreso(double factorProgreso);

    double radioDesdeMasa(double masa);
}
