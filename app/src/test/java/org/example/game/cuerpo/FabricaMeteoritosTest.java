package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionColisiones;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FabricaMeteoritosTest {

    private final AtomicInteger contador = new AtomicInteger();
    private final FabricaMeteoritos fabrica = new FabricaMeteoritos(p -> p + "-" + contador.incrementAndGet());

    @Test
    @DisplayName("Crea meteorito dirigido hacia objetivo con masa escalada")
    void testCrearHaciaObjetivo() {
        Planeta objetivo = new Planeta("Destino", 1e24, 0, 0, TipoCuerpo.PLANETA_ROCOSO);
        Meteorito m = fabrica.crearHaciaObjetivo(1920, 1080, objetivo, ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);

        assertNotNull(m);
        assertTrue(m.getNombre().startsWith("Meteorito-"));
        assertTrue(m.getMasa() >= ConfiguracionColisiones.MASA_MINIMA_METEORITO);
        assertFalse(m.permiteReembolso());
    }

    @Test
    @DisplayName("Crea meteorito lanzado por el jugador")
    void testCrearLanzadoPorJugador() {
        Meteorito m = fabrica.crearLanzadoPorJugador(10, 20, new Vector2D(5, 5));
        assertNotNull(m);
        assertEquals(10.0, m.getPosicion().x(), 1e-9);
        assertEquals(20.0, m.getPosicion().y(), 1e-9);
    }

    @Test
    @DisplayName("Crea fragmento de escombro con masa especificada")
    void testCrearFragmento() {
        Meteorito frag = fabrica.crearFragmento(0, 0, new Vector2D(10, 0), 5000.0);
        assertNotNull(frag);
        assertEquals(5000.0, frag.getMasa(), 1e-9);
        assertTrue(frag.getNombre().startsWith("Escombro-"));
    }
}
