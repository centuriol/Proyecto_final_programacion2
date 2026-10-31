package org.example.game.motor;

/**
 * Interfaz para conversión bidireccional entre coordenadas de simulación física y pantalla JavaFX (ISP / DIP).
 */
public interface ConversorCoordenadas {

    double fisicaAYJavaFX(double yFisica, double altoMundo);

    double javaFXAFisica(double yJavaFX, double altoMundo);

    double fisicaAXJavaFX(double xFisica, double anchoMundo);

    double javaFXAFisicaX(double xJavaFX, double anchoMundo);
}
