package org.example.game.civilizacion;

import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SistemaCivilizacionesTest {

    private SistemaCivilizaciones sistema;

    @BeforeEach
    void setUp() {
        sistema = new SistemaCivilizaciones();
    }

    @Test
    @DisplayName("Registrar y desregistrar entidades Civilizable")
    void testRegistrarYDesregistrar() {
        Planeta planeta = new Planeta("Tierra", 5.97e24, 0, 0, TipoCuerpo.PLANETA_ROCOSO);
        sistema.registrar(planeta, planeta);

        assertEquals(1, sistema.getCantidadCivilizablesActivos());
        assertTrue(sistema.obtenerCivilizable(planeta).isPresent());
        assertSame(planeta, sistema.obtenerCivilizable(planeta).get());

        sistema.desregistrar(planeta);
        assertEquals(0, sistema.getCantidadCivilizablesActivos());
        assertFalse(sistema.obtenerCivilizable(planeta).isPresent());
    }

    @Test
    @DisplayName("Actualizar civilizaciones avanza el estado de las civilizaciones activas")
    void testActualizarCivilizaciones() {
        Planeta planeta = new Planeta("Tierra", 5.97e24, 0, 0, TipoCuerpo.PLANETA_ROCOSO);
        sistema.registrar(planeta, planeta);

        AtomicInteger eventosRecibidos = new AtomicInteger(0);
        sistema.actualizarCivilizaciones(List.of(planeta), ev -> eventosRecibidos.incrementAndGet());

        assertTrue(planeta.tieneCivilizacion());
        assertNotNull(planeta.getCivilizacion());
    }

    @Test
    @DisplayName("Limpiar vacia la coleccion de civilizables")
    void testLimpiar() {
        Planeta p1 = new Planeta("P1", 1e24, 0, 0, TipoCuerpo.PLANETA_ROCOSO);
        Planeta p2 = new Planeta("P2", 1e24, 100, 0, TipoCuerpo.PLANETA_ROCOSO);
        sistema.registrar(p1, p1);
        sistema.registrar(p2, p2);

        assertEquals(2, sistema.getCantidadCivilizablesActivos());
        sistema.limpiar();
        assertEquals(0, sistema.getCantidadCivilizablesActivos());
    }
}
