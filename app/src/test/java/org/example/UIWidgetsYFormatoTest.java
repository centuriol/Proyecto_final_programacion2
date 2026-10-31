package org.example;
import org.example.game.cuerpo.*; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.Vector2D; import org.example.game.simulacion.MensajeEvento; import org.example.game.simulacion.SimulacionSolar; import org.example.game.ui.ControlTiempoWidget; import org.example.game.ui.HUDRecursosTop; import org.example.game.ui.VentanaDescripcionCuerpo; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.ArrayList; import java.util.List; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class UIWidgetsYFormatoTest {

    @Test
    void testNotificacionesDesacopladas() {
        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        List<MensajeEvento> recibidos = new ArrayList<>();

        simulacion.agregarListenerEvento(recibidos::add);

        simulacion.notificarEvento("Prueba de alerta", MensajeEvento.TipoMensaje.ADVERTENCIA);
        assertEquals(1, recibidos.size());
        assertEquals("Prueba de alerta", recibidos.get(0).mensaje());
        assertEquals(MensajeEvento.TipoMensaje.ADVERTENCIA, recibidos.get(0).tipo());

        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().getRecursoObj(TipoRecurso.ENERGIA).setCantidad(0);
        simulacion.getInventario().getRecursoObj(TipoRecurso.MINERALES).setCantidad(0);
        boolean spawneado = simulacion.spawnCuerpo(TipoCuerpo.PLANETA_ROCOSO, 100, 100, 1.0, Vector2D.cero()).isPresent();
        assertFalse(spawneado);
        assertTrue(recibidos.size() >= 2);
        assertTrue(recibidos.get(recibidos.size() - 1).mensaje().contains("Recursos insuficientes"));
    }

    @Test
    void testHUDRecursosTopWidgets() {
        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        HUDRecursosTop hud = new HUDRecursosTop(simulacion);

        var items = hud.getItemsRecursos();
        assertEquals(TipoRecurso.values().length, items.size());

        for (TipoRecurso tipo : TipoRecurso.values()) {
            assertTrue(items.containsKey(tipo));
            var widget = items.get(tipo);
            assertEquals(tipo.nombre.toUpperCase(), widget.getNombreLabelText());
            assertNotNull(widget.getValorLabelText());
        }
    }

    @Test
    void testSinEmojisEnTextos() {
        for (TipoRecurso tr : TipoRecurso.values()) {
            assertFalse(tr.nombre.matches(".*[\\p{So}\\p{Cn}].*"));
            assertFalse(tr.icono.matches(".*[\\p{So}\\p{Cn}].*"));
        }
    }

    @Test
    void testRadioDinamicoPorMasa() {
        Planeta p = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        double radio1x = p.getRadio();
        assertEquals(15.0, radio1x, 1e-6);

        p.setMasa(p.getMasa() * 8.0);
        assertEquals(30.0, p.getRadio(), 1e-6);

        p.setMasa(TipoCuerpo.PLANETA_ROCOSO.masaBase * 0.125);
        assertEquals(7.5, p.getRadio(), 1e-6);
    }

    @Test
    void testNombresPorDefectoVentana() {
        String nombreSol = VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.ESTRELLA);
        assertNotNull(nombreSol);
        assertFalse(nombreSol.isBlank());

        String nombrePlaneta = VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(nombrePlaneta);
        assertFalse(nombrePlaneta.isBlank());

        String nombreLuna = VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.LUNA);
        assertNotNull(nombreLuna);
        assertFalse(nombreLuna.isBlank());
    }

    @Test
    void testFormatoMasaMTEnLugarDeNotacionCientifica1e() {
        String masaTierra = TipoCuerpo.formatearMasa(TipoCuerpo.PLANETA_ROCOSO.masaBase);
        assertTrue(masaTierra.contains("MT"));
        assertFalse(masaTierra.contains("1e"));
        assertTrue(masaTierra.startsWith("5.97"));

        String masaSol = TipoCuerpo.formatearMasa(TipoCuerpo.ESTRELLA.masaBase);
        assertTrue(masaSol.contains("MT"));
        assertTrue(masaSol.contains("1.99M MT"));

        String masaLuna = TipoCuerpo.formatearMasa(TipoCuerpo.LUNA.masaBase);
        assertTrue(masaLuna.contains("MT"));
        assertFalse(masaLuna.contains("1e"));

        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        assertFalse(sol.describir().contains("1e"));
        assertTrue(sol.describir().contains("MT"));

        Luna luna = CuerpoCelesteFactory.crearLuna(0, 0, 1.0, Vector2D.cero());
        assertFalse(luna.describir().contains("1e"));
        assertTrue(luna.describir().contains("MT"));
    }

    @Test
    void testControlTiempoWidgetCompactoYVelocidades() {
        SimulacionSolar sim = new SimulacionSolar(1000, 1000);
        ControlTiempoWidget widget = new ControlTiempoWidget(sim);

        assertEquals(5, widget.getChildren().size());
        for (javafx.scene.Node n : widget.getChildren()) {
            assertTrue(n instanceof javafx.scene.control.Button);
            assertFalse(n instanceof javafx.scene.control.Label);
        }

        assertEquals("||", widget.getBtnPause().getText());
        assertEquals("1x", widget.getBtn1x().getText());
        assertEquals("2x", widget.getBtn2x().getText());
        assertEquals("3x", widget.getBtn3x().getText());
        assertEquals("4x", widget.getBtn4x().getText());

        widget.getBtn2x().fire();
        assertEquals(2.0, sim.getVelocidadSimulacion(), 1e-6);
        assertFalse(sim.isEnPausa());

        widget.getBtn3x().fire();
        assertEquals(3.0, sim.getVelocidadSimulacion(), 1e-6);
        assertFalse(sim.isEnPausa());

        widget.getBtn4x().fire();
        assertEquals(4.0, sim.getVelocidadSimulacion(), 1e-6);
        assertFalse(sim.isEnPausa());

        widget.getBtnPause().fire();
        assertTrue(sim.isEnPausa());
        assertEquals(">", widget.getBtnPause().getText());

        widget.getBtnPause().fire();
        assertFalse(sim.isEnPausa());
        assertEquals("||", widget.getBtnPause().getText());

        widget.getBtnPause().fire();
        assertTrue(sim.isEnPausa());
        widget.getBtn1x().fire();
        assertFalse(sim.isEnPausa());
        assertEquals(1.0, sim.getVelocidadSimulacion(), 1e-6);
    }
}
