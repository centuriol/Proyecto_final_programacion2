package org.example.game.simulacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadorAmenazaMeteoritoTest {

    private final CalculadorAmenazaMeteorito escalador = new EscaladorAmenazaMeteorito();

    @Test
    @DisplayName("Calcula fracción de daño según progreso inicial y máximo")
    void testFraccionDano() {
        assertEquals(ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION,
                escalador.fraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL), 1e-9);
        assertEquals(1.0, escalador.fraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO), 1e-9);
        assertEquals(1.0, escalador.fraccionDano(10.0), 1e-9);
    }

    @Test
    @DisplayName("Calcula radios según progresión acotada")
    void testRadioPorProgreso() {
        double rIni = escalador.radioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
        double rMax = escalador.radioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO);
        assertTrue(rIni < rMax);
    }

    @Test
    @DisplayName("Calcula radio a partir de masa")
    void testRadioDesdeMasa() {
        double rMin = escalador.radioDesdeMasa(ConfiguracionColisiones.MASA_MINIMA_METEORITO);
        double rMax = escalador.radioDesdeMasa(ConfiguracionColisiones.MASA_MAXIMA_METEORITO);
        assertEquals(ConfiguracionColisiones.RADIO_MINIMO_METEORITO, rMin, 1e-9);
        assertEquals(ConfiguracionColisiones.RADIO_MAXIMO_METEORITO, rMax, 1e-9);
    }
}
