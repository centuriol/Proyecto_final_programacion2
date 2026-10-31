package org.example;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.example.game.controlador.ControladorMouse;
import org.example.game.controlador.ProveedorEstadoInteraccion;
import org.example.game.controlador.SincronizadorUI;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ControlTiempoWidget;
import org.example.game.ui.EspaciadorUI;
import org.example.game.ui.HUDRecursosTop;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ControladorMouseYHelpersTest {

    private SimulacionSolar simulacion;
    private ControladorMouse controladorMouse;
    private ControlTiempoWidget controlTiempo;
    private HUDRecursosTop hudRecursos;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.getProgresion().desbloquearTodo();
        BarraInventarioHotbar hotbar = new BarraInventarioHotbar(simulacion);
        controlTiempo = new ControlTiempoWidget(simulacion);
        hudRecursos = new HUDRecursosTop(simulacion);
        controladorMouse = new ControladorMouse(simulacion, hotbar, null, 1920, 1080);
    }

    @Test
    void testControladorMouseScrollAjustaZoomComoJuegoNormal() {
        double masaInicial = simulacion.getFactorMasaColocacion();
        double zoomInicial = controladorMouse.getCamaraZoom().getNivelZoom();

        ScrollEvent scrollUp = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, false, false, false, false, false,
                0, 40.0, 0, 40.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        controladorMouse.manejarScroll(scrollUp);
        assertTrue(controladorMouse.getCamaraZoom().getNivelZoom() > zoomInicial);
        assertEquals(masaInicial, simulacion.getFactorMasaColocacion(), 1e-6);

        ScrollEvent scrollDown = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, false, false, false, false, false,
                0, -40.0, 0, -40.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        controladorMouse.manejarScroll(scrollDown);
        assertEquals(zoomInicial, controladorMouse.getCamaraZoom().getNivelZoom(), 1e-6);
    }

    @Test
    void testControladorMouseSeleccionYLimpieza() {
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        simulacion.getSistemaSolar().agregarCuerpo(sol);

        controladorMouse.seleccionarCuerpoBajoCursor(960, 540);
        assertNotNull(controladorMouse.getCuerpoSeleccionado());
        assertEquals(sol, controladorMouse.getCuerpoSeleccionado());

        controladorMouse.limpiarSeleccion();
        assertNull(controladorMouse.getCuerpoSeleccionado());
        assertTrue(controladorMouse.getTrayectoriaPreview().isEmpty());
    }

    @Test
    void testControladorMouseClickSecundarioCancelaColocacion() {
        simulacion.entrarModoColocacion(TipoCuerpo.LUNA, 1.0, "LunaTest");
        assertEquals(SimulacionSolar.ModoColocacion.COLOCANDO, simulacion.getModoColocacion());

        MouseEvent clickSecundario = new MouseEvent(
                MouseEvent.MOUSE_PRESSED, 100, 100, 100, 100, MouseButton.SECONDARY, 1,
                false, false, false, false, false, false, false, false, false, false, null
        );
        controladorMouse.manejarMousePresionado(clickSecundario);

        assertEquals(SimulacionSolar.ModoColocacion.NINGUNO, simulacion.getModoColocacion());
        assertNull(controladorMouse.getCuerpoSeleccionado());
    }

    @Test
    void testSincronizadorUIEjecucion() {
        SincronizadorUI sincronizador = new SincronizadorUI(hudRecursos, controlTiempo);
        assertDoesNotThrow(sincronizador::run);
    }

    @Test
    void testEspaciadorUI() {
        HBox spacer = EspaciadorUI.crearHorizontal();
        assertNotNull(spacer);
        assertTrue(spacer.isMouseTransparent());
        assertEquals(Priority.ALWAYS, HBox.getHgrow(spacer));
    }

    @Test
    void testProveedorEstadoInteraccionDesacoplamiento() {
        ProveedorEstadoInteraccion proveedor = controladorMouse;
        assertNotNull(proveedor.getTrayectoriaPreview());
        assertNull(proveedor.getCuerpoSeleccionado());
    }
}
