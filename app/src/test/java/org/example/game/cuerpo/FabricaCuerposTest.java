package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricaCuerposTest {

    private final FabricaCuerpos fabrica = new CuerpoCelesteFactory();

    @Test
    @DisplayName("Crea cuerpos mediante fabricar() según TipoCuerpo")
    void testFabricarPorTipo() {
        CuerpoCeleste estrella = fabrica.crearCuerpo(TipoCuerpo.ESTRELLA, 0, 0, 1.0, Vector2D.cero());
        assertNotNull(estrella);
        assertTrue(estrella.getNombre().startsWith("Sol-"));

        CuerpoCeleste planeta = fabrica.crearCuerpo(TipoCuerpo.PLANETA_ROCOSO, 100, 0, 1.0, Vector2D.cero());
        assertNotNull(planeta);
        assertTrue(planeta.getNombre().startsWith("Planeta-"));
    }

    @Test
    @DisplayName("Resetear contador reinicia la secuencia de identificadores")
    void testResetearContador() {
        fabrica.resetearContador();
        String nom1 = fabrica.generarNombre("Test");
        assertEquals("Test-1", nom1);
    }
}
