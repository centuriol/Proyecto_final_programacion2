package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FabricaPlanetasTest {

    private final AtomicInteger contador = new AtomicInteger();
    private final FabricaPlanetas fabrica = new FabricaPlanetas(p -> p + "-" + contador.incrementAndGet());

    @Test
    @DisplayName("Crea planeta con tipo y masa escalada")
    void testCrearPlaneta() {
        Planeta p = fabrica.crearPlaneta(TipoCuerpo.PLANETA_AGUA, 100, 200, 1.5, Vector2D.cero());
        assertNotNull(p);
        assertEquals(TipoCuerpo.PLANETA_AGUA, p.getSubTipo());
        assertEquals(100.0, p.getPosicion().x(), 1e-9);
        assertEquals(200.0, p.getPosicion().y(), 1e-9);
        assertEquals(TipoCuerpo.PLANETA_AGUA.masaBase * 1.5, p.getMasa(), 1e-6);
    }

    @Test
    @DisplayName("Crea planeta personalizado con nombre dado")
    void testCrearPlanetaPersonalizado() {
        Planeta p = fabrica.crearPlanetaPersonalizado("MiTierra", 5.97e24, 50, 60, TipoCuerpo.PLANETA_ROCOSO, new Vector2D(1, 2));
        assertNotNull(p);
        assertEquals("MiTierra", p.getNombre());
        assertEquals(5.97e24, p.getMasa(), 1e-9);
    }

    @Test
    @DisplayName("Crea planeta en órbita calculada")
    void testCrearPlanetaEnOrbita() {
        Estrella sol = new Estrella("Sol", 1.989e30, 0, 0);
        Planeta p = fabrica.crearPlanetaEnOrbita(sol, 200, 1.0, TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(p);
        assertEquals(200.0, p.getPosicion().x(), 1e-9);
        assertTrue(p.getVelocidad().magnitud() > 0);
    }
}
