package org.example.game.motor;

/**
 * Conversor concreto de coordenadas plano estándar (SRP / DIP).
 * Convierte el origen centrado cartesiano físico al origen superior-izquierdo de JavaFX.
 */
public class TransformadorCoordenadas implements ConversorCoordenadas {

    @Override
    public double fisicaAYJavaFX(double yFisica, double altoMundo) {
        return altoMundo / 2.0 - yFisica;
    }

    @Override
    public double javaFXAFisica(double yJavaFX, double altoMundo) {
        return altoMundo / 2.0 - yJavaFX;
    }

    @Override
    public double fisicaAXJavaFX(double xFisica, double anchoMundo) {
        return anchoMundo / 2.0 + xFisica;
    }

    @Override
    public double javaFXAFisicaX(double xJavaFX, double anchoMundo) {
        return xJavaFX - anchoMundo / 2.0;
    }
}
