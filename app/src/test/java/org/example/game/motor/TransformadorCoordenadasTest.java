package org.example.game.motor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformadorCoordenadasTest {

    private final ConversorCoordenadas conversor = new TransformadorCoordenadas();

    @Test
    @DisplayName("Convierte origen físico al centro de pantalla JavaFX y viceversa")
    void testOrigenCentrado() {
        double ancho = 1920.0;
        double alto = 1080.0;

        assertEquals(960.0, conversor.fisicaAXJavaFX(0.0, ancho), 1e-9);
        assertEquals(540.0, conversor.fisicaAYJavaFX(0.0, alto), 1e-9);

        assertEquals(0.0, conversor.javaFXAFisicaX(960.0, ancho), 1e-9);
        assertEquals(0.0, conversor.javaFXAFisica(540.0, alto), 1e-9);
    }

    @Test
    @DisplayName("Convierte desplazamientos positivos y negativos simétricamente")
    void testDesplazamientos() {
        double ancho = 1000.0;
        double alto = 800.0;

        // X: +100 física -> 600 JavaFX
        assertEquals(600.0, conversor.fisicaAXJavaFX(100.0, ancho), 1e-9);
        assertEquals(100.0, conversor.javaFXAFisicaX(600.0, ancho), 1e-9);

        // Y: +50 física -> 350 JavaFX (Y invertido)
        assertEquals(350.0, conversor.fisicaAYJavaFX(50.0, alto), 1e-9);
        assertEquals(50.0, conversor.javaFXAFisica(350.0, alto), 1e-9);

        // Y: -50 física -> 450 JavaFX
        assertEquals(450.0, conversor.fisicaAYJavaFX(-50.0, alto), 1e-9);
        assertEquals(-50.0, conversor.javaFXAFisica(450.0, alto), 1e-9);
    }
}
