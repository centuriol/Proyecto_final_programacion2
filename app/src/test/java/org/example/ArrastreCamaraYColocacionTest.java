package org.example;
import javafx.scene.canvas.Canvas; import javafx.scene.input.MouseButton; import javafx.scene.input.MouseEvent; import org.example.game.controlador.ControladorMouse; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.CuerpoCelesteFactory; import org.example.game.cuerpo.Estrella; import org.example.game.cuerpo.Planeta; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.Vector2D; import org.example.game.render.CamaraZoom; import org.example.game.render.RenderizadorPixelArt; import org.example.game.simulacion.SimulacionSolar; import org.example.game.ui.BarraInventarioHotbar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.Collections; import java.util.concurrent.atomic.AtomicReference; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ArrastreCamaraYColocacionTest {

    private SimulacionSolar simulacion;
    private BarraInventarioHotbar hotbar;
    private CamaraZoom camara;
    private AtomicReference<CuerpoCeleste> inspeccionado;
    private ControladorMouse mouse;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1000, 800);
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 5000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 5000);
        hotbar = new BarraInventarioHotbar(simulacion);
        camara = new CamaraZoom();
        inspeccionado = new AtomicReference<>();
        mouse = new ControladorMouse(simulacion, hotbar, inspeccionado::set, 1000, 800);
        mouse.setCamaraZoom(camara);
    }

    @Test
    void testMantenerClicIzquierdoYArrastrarMueveCamaraPorTodoElMundo() {
        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 500, 400, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 750, 200, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, -1500, 1400, MouseButton.PRIMARY));
        mouse.manejarMouseLiberado(eventoMouse(MouseEvent.MOUSE_RELEASED, -1500, 1400, MouseButton.PRIMARY));

        assertEquals(-2000.0, camara.getDesplazamiento().getX(), 1e-6);
        assertEquals(1000.0, camara.getDesplazamiento().getY(), 1e-6);
        assertFalse(camara.esVistaNeutra());
    }

    @Test
    void testArrastreRespetaZoomActualManteniendoPuntoBajoCursor() {
        camara.setNivelZoom(0.5);
        double mundoInicialX = mouse.transformarX(300);
        double mundoInicialY = mouse.transformarY(250);

        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 300, 250, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 400, 190, MouseButton.PRIMARY));
        mouse.manejarMouseLiberado(eventoMouse(MouseEvent.MOUSE_RELEASED, 400, 190, MouseButton.PRIMARY));

        assertEquals(200.0, camara.getDesplazamiento().getX(), 1e-6, "A zoom 0.5x, +100px pantalla equivalen a +200 mundo");
        assertEquals(-120.0, camara.getDesplazamiento().getY(), 1e-6, "A zoom 0.5x, -60px pantalla equivalen a -120 mundo");
        assertEquals(mundoInicialX, mouse.transformarX(400), 1e-6, "El punto del mundo bajo el cursor se conserva");
        assertEquals(mundoInicialY, mouse.transformarY(190), 1e-6);

        camara.resetear();
        camara.setNivelZoom(1.5);
        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 500, 400, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 650, 550, MouseButton.PRIMARY));
        mouse.manejarMouseLiberado(eventoMouse(MouseEvent.MOUSE_RELEASED, 650, 550, MouseButton.PRIMARY));

        assertEquals(100.0, camara.getDesplazamiento().getX(), 1e-6, "A zoom 1.5x, +150px pantalla equivalen a +100 mundo");
        assertEquals(100.0, camara.getDesplazamiento().getY(), 1e-6);
    }

    @Test
    void testArrastreNoInterfiereConColocarCuerposEnCamaraDesplazada() {
        camara.setNivelZoom(0.5);
        camara.desplazarPorPantalla(-200.0, 100.0); // desplazamiento mundo = (-400, +200)
        double despAntesX = camara.getDesplazamiento().getX();
        double despAntesY = camara.getDesplazamiento().getY();

        simulacion.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "PlanetaLejano");

        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 600, 300, MouseButton.PRIMARY));
        assertEquals(despAntesX, camara.getDesplazamiento().getX(), 1e-6, "Arrastrar en modo colocacion no debe mover camara");
        assertEquals(despAntesY, camara.getDesplazamiento().getY(), 1e-6);

        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 600, 300, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 800, 500, MouseButton.PRIMARY));
        mouse.manejarMouseLiberado(eventoMouse(MouseEvent.MOUSE_RELEASED, 800, 500, MouseButton.PRIMARY));

        assertEquals(despAntesX, camara.getDesplazamiento().getX(), 1e-6, "Confirmar colocacion y mover mouse no debe arrastrar camara");
        assertEquals(despAntesY, camara.getDesplazamiento().getY(), 1e-6);
        assertEquals(1, simulacion.getSistemaSolar().getCuerpos().size());

        CuerpoCeleste creado = simulacion.getSistemaSolar().getCuerpos().get(0);
        assertEquals("PlanetaLejano", creado.getNombre());
        // pantalla 600 -> zoom 0.5 -> 500 + 200 = 700, menos despX (-400) = 1100 JavaFX -> xFisica = 1100 - 500 = 600
        assertEquals(600.0, creado.getPosicionX(), 1e-4);
    }

    @Test
    void testClickSimpleInspeccionaPeroArrastreNoAbreInspeccion() {
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 500, 400, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 540, 430, MouseButton.PRIMARY));
        mouse.manejarMouseLiberado(eventoMouse(MouseEvent.MOUSE_RELEASED, 540, 430, MouseButton.PRIMARY));
        assertNull(inspeccionado.get(), "Un arrastre de camara sobre un cuerpo no debe disparar inspeccion");

        // El sol ahora se ve en pantalla (540, 430)
        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 540, 430, MouseButton.PRIMARY));
        mouse.manejarMouseLiberado(eventoMouse(MouseEvent.MOUSE_RELEASED, 540, 430, MouseButton.PRIMARY));
        assertEquals(sol, inspeccionado.get(), "Un clic sin arrastrar en la nueva posicion visual debe inspeccionar");
    }

    @Test
    void testEntrarModoColocacionDuranteArrastreCancelaMovimientoCamara() {
        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 400, 400, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 450, 400, MouseButton.PRIMARY));
        assertEquals(50.0, camara.getDesplazamiento().getX(), 1e-6);

        simulacion.entrarModoColocacion(TipoCuerpo.LUNA, 1.0, "Luna");
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 600, 500, MouseButton.PRIMARY));
        assertEquals(50.0, camara.getDesplazamiento().getX(), 1e-6);
    }

    @Test
    void testModoColonizacionNoMueveCamaraAlArrastrar() {
        Planeta origen = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        origen.desarrollarCivilizacion();
        mouse.iniciarModoColonizacion(origen);

        mouse.manejarMousePresionado(eventoMouse(MouseEvent.MOUSE_PRESSED, 500, 400, MouseButton.PRIMARY));
        mouse.manejarMouseArrastrado(eventoMouse(MouseEvent.MOUSE_DRAGGED, 700, 600, MouseButton.PRIMARY));

        assertTrue(camara.getDesplazamiento().esCentrada());
    }

    @Test
    void testRenderizadorDibujaConDesplazamientoYGrillaSinErroresYResetearCentra() {
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(1000, 800);
        renderizador.toggleGrilla();
        renderizador.getCamaraZoom().setNivelZoom(0.6);
        renderizador.getCamaraZoom().desplazarPorPantalla(1200, -900);

        Canvas canvas = new Canvas(1000, 800);
        simulacion.cargarPresetSistemaBasico();
        assertDoesNotThrow(() -> renderizador.renderizarTodo(
                canvas.getGraphicsContext2D(),
                simulacion.getSistemaSolar().getCuerpos(),
                10L, null, null, 1.0, null, Collections.emptyList(), null
        ));

        renderizador.getCamaraZoom().resetear();
        assertTrue(renderizador.getCamaraZoom().esVistaNeutra());
        assertEquals(0.0, renderizador.getCamaraZoom().getDesplazamiento().getX(), 1e-6);
        assertEquals(0.0, renderizador.getCamaraZoom().getDesplazamiento().getY(), 1e-6);
    }

    private MouseEvent eventoMouse(javafx.event.EventType<MouseEvent> tipo, double x, double y, MouseButton boton) {
        return new MouseEvent(
                tipo, x, y, x, y, boton, 1,
                false, false, false, false,
                boton == MouseButton.PRIMARY, false, boton == MouseButton.SECONDARY,
                false, false, false, null
        );
    }
}
