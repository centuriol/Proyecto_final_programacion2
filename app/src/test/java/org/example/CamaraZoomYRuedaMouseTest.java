package org.example;
import javafx.scene.canvas.Canvas; import javafx.scene.input.ScrollEvent; import org.example.game.controlador.ControladorMouse; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.render.CamaraZoom; import org.example.game.render.ControlZoom; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class CamaraZoomYRuedaMouseTest {

    @Test
    void testRuedaAdelanteAcercaHastaMaximo1Punto5x() {
        CamaraZoom zoom = new CamaraZoom();
        assertEquals(1.0, zoom.getNivelZoom(), 1e-6);

        zoom.aplicarRueda(40.0);
        assertEquals(1.1, zoom.getNivelZoom(), 1e-6);

        for (int i = 0; i < 25; i++) {
            zoom.aplicarRueda(40.0);
        }
        assertEquals(1.5, zoom.getNivelZoom(), 1e-6);
        assertFalse(zoom.esZoomNeutro());
    }

    @Test
    void testRuedaAtrasAlejaHastaMinimo0Punto5x() {
        CamaraZoom zoom = new CamaraZoom();

        zoom.aplicarRueda(-40.0);
        assertEquals(0.9, zoom.getNivelZoom(), 1e-6);

        for (int i = 0; i < 25; i++) {
            zoom.aplicarRueda(-40.0);
        }
        assertEquals(0.5, zoom.getNivelZoom(), 1e-6);
        assertFalse(zoom.esZoomNeutro());
    }

    @Test
    void testRuedaConDeltaCeroONoFinitoNoAlteraZoom() {
        CamaraZoom zoom = new CamaraZoom();
        zoom.aplicarRueda(40.0);
        assertEquals(1.1, zoom.getNivelZoom(), 1e-6);

        zoom.aplicarRueda(0.0);
        zoom.aplicarRueda(Double.NaN);
        zoom.aplicarRueda(Double.POSITIVE_INFINITY);

        assertEquals(1.1, zoom.getNivelZoom(), 1e-6);
    }

    @Test
    void testConversionCoordenadasPantallaAMundoEncapsulada() {
        CamaraZoom zoom = new CamaraZoom();
        assertEquals(750.0, zoom.pantallaAMundo(750.0, 1000.0), 1e-6);
        assertEquals(0.0, zoom.calcularDesplazamientoCentro(1000.0), 1e-6);

        zoom.setNivelZoom(1.5);
        assertEquals(600.0, zoom.pantallaAMundo(650.0, 1000.0), 1e-6);
        assertEquals(-250.0, zoom.calcularDesplazamientoCentro(1000.0), 1e-6);

        zoom.setNivelZoom(0.5);
        assertEquals(800.0, zoom.pantallaAMundo(650.0, 1000.0), 1e-6);
        assertEquals(250.0, zoom.calcularDesplazamientoCentro(1000.0), 1e-6);
    }

    @Test
    void testControladorMouseRuedaNormalAcercaYAlejaConLimites() {
        SimulacionSolar sim = new SimulacionSolar(1000, 800);
        sim.getProgresion().desbloquearTodo();
        sim.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "PlanetaZoom");

        ControladorMouse mouse = new ControladorMouse(sim, null, 1000, 800);
        Canvas canvas = new Canvas(1000, 800);
        mouse.conectar(canvas);
        assertNotNull(canvas.getOnScroll());

        for (int i = 0; i < 15; i++) {
            mouse.manejarScroll(crearScroll(700, 400, 40.0));
        }
        assertEquals(1.5, mouse.getCamaraZoom().getNivelZoom(), 1e-6);
        assertNotNull(sim.getPosicionPreview());

        for (int i = 0; i < 30; i++) {
            mouse.manejarScroll(crearScroll(700, 400, -40.0));
        }
        assertEquals(0.5, mouse.getCamaraZoom().getNivelZoom(), 1e-6);
    }

    @Test
    void testSetNivelZoomAcotaLimitesYResetearRestauraNeutro() {
        CamaraZoom zoom = new CamaraZoom();

        zoom.setNivelZoom(5.0);
        assertEquals(1.5, zoom.getNivelZoom(), 1e-6);

        zoom.setNivelZoom(0.1);
        assertEquals(0.5, zoom.getNivelZoom(), 1e-6);

        zoom.resetear();
        assertEquals(1.0, zoom.getNivelZoom(), 1e-6);
        assertTrue(zoom.esZoomNeutro());
    }

    @Test
    void testPolimorfismoControlZoomDIP() {
        ControlZoom control = new CamaraZoom();
        control.aplicarRueda(32.0);
        assertEquals(1.1, control.getNivelZoom(), 1e-6);
        assertFalse(control.esZoomNeutro());

        control.setNivelZoom(1.0);
        assertTrue(control.esZoomNeutro());
    }

    private ScrollEvent crearScroll(double x, double y, double deltaY) {
        return new ScrollEvent(
                ScrollEvent.SCROLL, x, y, x, y,
                false, false, false, false, false, false,
                0.0, deltaY, 0.0, deltaY,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0.0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0.0,
                0, null
        );
    }
}
