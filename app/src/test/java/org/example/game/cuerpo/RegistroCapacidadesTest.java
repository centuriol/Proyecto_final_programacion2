package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistroCapacidadesTest {

    @Test
    @DisplayName("RegistroCapacidades registra y despacha capacidades de Planeta sin casteo")
    void testCapacidadesPlaneta() {
        Planeta planeta = new Planeta("Marte", 6.4e23, 100, 0, TipoCuerpo.PLANETA_ROCOSO);

        assertTrue(RegistroCapacidades.obtenerCivilizable(planeta).isPresent());
        assertTrue(RegistroCapacidades.obtenerDefensor(planeta).isPresent());
        assertTrue(RegistroCapacidades.obtenerColonizable(planeta).isPresent());
        assertTrue(RegistroCapacidades.obtenerTransformable(planeta).isPresent());

        RegistroCapacidades.desregistrar(planeta);

        assertFalse(RegistroCapacidades.obtenerCivilizable(planeta).isPresent());
        assertFalse(RegistroCapacidades.obtenerDefensor(planeta).isPresent());
        assertFalse(RegistroCapacidades.obtenerColonizable(planeta).isPresent());
        assertFalse(RegistroCapacidades.obtenerTransformable(planeta).isPresent());
    }

    @Test
    @DisplayName("RegistroCapacidades registra satelite y meteorito en subsistema fisico")
    void testSateliteYMeteorito() {
        Satelite satelite = new Satelite("Sat1", 10.0, 50, 50);
        Meteorito meteorito = new Meteorito("Met1", 50.0, 100, 100, Vector2D.cero(), 50.0, false);

        assertFalse(RegistroCapacidades.obtenerCivilizable(satelite).isPresent());
        assertFalse(RegistroCapacidades.obtenerCivilizable(meteorito).isPresent());

        RegistroCapacidades.desregistrar(satelite);
        RegistroCapacidades.desregistrar(meteorito);
    }
}
