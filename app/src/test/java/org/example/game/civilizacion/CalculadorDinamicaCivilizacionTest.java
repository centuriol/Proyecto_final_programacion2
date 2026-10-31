package org.example.game.civilizacion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculadorDinamicaCivilizacionTest {

    @Test
    void calculaPoblacionMaximaYEstadosCorrectamente() {
        CalculadorDinamicaCivilizacion calculador = new CalculadorDinamicaCivilizacion();

        double popMax = calculador.calcularPoblacionMaxima(5.97e24, 1.0, 0);
        assertTrue(popMax > 0);

        // Tech nivel 5 incrementa la capacidad
        double popMaxTech5 = calculador.calcularPoblacionMaxima(5.97e24, 1.0, 5);
        assertTrue(popMaxTech5 > popMax);

        // Estado extinguida si poblacion < 10
        assertEquals(EstadoCivilizacion.EXTINGUIDA, calculador.determinarEstado(5, 1.0, 1.0));

        // Estado en peligro si estabilidad < 0.3
        assertEquals(EstadoCivilizacion.EN_PELIGRO, calculador.determinarEstado(500, 0.2, 0.9));

        // Estado luchando si estabilidad < 0.6 o felicidad < 0.4
        assertEquals(EstadoCivilizacion.LUCHANDO, calculador.determinarEstado(500, 0.5, 0.8));

        // Estado prospera si estabilidad > 0.8 y felicidad > 0.7
        assertEquals(EstadoCivilizacion.PRÓSPERA, calculador.determinarEstado(500, 0.9, 0.85));

        // Multiplicador de produccion es 0 si extinguida
        assertEquals(0.0, calculador.calcularMultiplicadorProduccion(EstadoCivilizacion.EXTINGUIDA, 0.9, 0.9, 0, 0));

        // Multiplicador con estabilidad y felicidad
        double mult = calculador.calcularMultiplicadorProduccion(EstadoCivilizacion.PRÓSPERA, 1.0, 1.0, 0, 0);
        assertTrue(mult > 0.9 && mult <= 1.1);
    }
}
