package org.example.game.simulacion;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GestorColocacionCuerposTest {

    @Test
    void gestionaCicloDeColocacion() {
        ControladorColocacion controlador = new ControladorColocacion();
        GestorColocacionCuerpos gestor = new GestorColocacionCuerpos(controlador, null);

        assertEquals(ControladorColocacion.ModoColocacion.NINGUNO, gestor.getModoColocacion());

        gestor.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.5, "TierraNueva", 5.0);
        assertEquals(ControladorColocacion.ModoColocacion.COLOCANDO, gestor.getModoColocacion());
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, gestor.getTipoColocacion());
        assertEquals("TierraNueva", gestor.getNombreColocacion());
        assertEquals(1.5, gestor.getFactorMasaColocacion(), 0.001);

        gestor.actualizarPosicionPreview(400, 300, 800, 600);
        assertNotNull(gestor.getPosicionPreview());

        gestor.salirModoColocacion();
        assertEquals(ControladorColocacion.ModoColocacion.NINGUNO, gestor.getModoColocacion());
    }
}
