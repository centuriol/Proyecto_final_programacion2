package org.example;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.example.game.controlador.*;
import org.example.game.controlador.comando.*;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ControlTiempoWidget;
import org.example.game.ui.EspaciadorUI;
import org.example.game.ui.HUDRecursosTop;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ControladorEntradaTest {

    private SimulacionSolar simulacion;
    private RenderizadorPixelArt renderizador;
    private BarraInventarioHotbar hotbar;
    private ControlTiempoWidget controlTiempo;
    private HUDRecursosTop hudRecursos;
    private ControladorMouse controladorMouse;
    private ControladorTeclado controladorTeclado;

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        renderizador = new RenderizadorPixelArt(1920, 1080);
        hotbar = new BarraInventarioHotbar(simulacion);
        controlTiempo = new ControlTiempoWidget(simulacion);
        hudRecursos = new HUDRecursosTop(simulacion);

        controladorMouse = new ControladorMouse(simulacion, hotbar, null, 1920, 1080);
        controladorTeclado = new ControladorTeclado();
        controladorTeclado.configurarAtajosPorDefecto(
                simulacion, controlTiempo, hotbar, renderizador, controladorMouse
        );
    }

    @Test
    void testControladorTecladoRegistroYDespachoComando() {
        AtomicBoolean ejecutado = new AtomicBoolean(false);
        controladorTeclado.registrarComando(KeyCode.A, () -> ejecutado.set(true));

        assertTrue(controladorTeclado.tieneComando(KeyCode.A));
        assertNotNull(controladorTeclado.obtenerComando(KeyCode.A));

        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.A, false, false, false, false);
        controladorTeclado.handle(event);
        assertTrue(ejecutado.get());

        // Desregistrar
        controladorTeclado.desregistrarComando(KeyCode.A);
        assertFalse(controladorTeclado.tieneComando(KeyCode.A));
    }

    @Test
    void testComandoAlternarPausaConEspacio() {
        boolean pausaInicial = simulacion.isEnPausa();
        KeyEvent eventSpace = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false);
        controladorTeclado.handle(eventSpace);

        assertEquals(!pausaInicial, simulacion.isEnPausa());

        controladorTeclado.handle(eventSpace);
        assertEquals(pausaInicial, simulacion.isEnPausa());
    }

    @Test
    void testComandoAvanzarPasoConTeclaS() {
        KeyEvent eventS = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.S, false, false, false, false);
        controladorTeclado.handle(eventS);

        simulacion.avanzarTick();
        assertFalse(simulacion.isEnPausa());
    }

    @Test
    void testComandoCancelarConEscape() {
        simulacion.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "PlanetaTest");
        assertEquals(SimulacionSolar.ModoColocacion.COLOCANDO, simulacion.getModoColocacion());

        KeyEvent eventEsc = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);
        controladorTeclado.handle(eventEsc);

        assertEquals(SimulacionSolar.ModoColocacion.NINGUNO, simulacion.getModoColocacion());
        assertNull(controladorMouse.getCuerpoSeleccionado());
        assertTrue(controladorMouse.getTrayectoriaPreview().isEmpty());
    }

    @Test
    void testComandosSeleccionTipoCuerpoRegistroYEjecucion() throws Exception {
        Comando c1 = controladorTeclado.obtenerComando(KeyCode.DIGIT1);
        Comando c2 = controladorTeclado.obtenerComando(KeyCode.DIGIT2);
        Comando c3 = controladorTeclado.obtenerComando(KeyCode.DIGIT3);

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);
        assertTrue(c1 instanceof ComandoSeleccionarTipoCuerpo);
        assertTrue(c2 instanceof ComandoSeleccionarTipoCuerpo);
        assertTrue(c3 instanceof ComandoSeleccionarTipoCuerpo);

        // Probar ejecución delegada en objeto mockeable
        AtomicBoolean invocado = new AtomicBoolean(false);
        Comando mockComando = new Comando() {
            @Override
            public void ejecutar() {
                invocado.set(true);
            }
        };
        controladorTeclado.registrarComando(KeyCode.DIGIT1, mockComando);

        KeyEvent digit1 = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DIGIT1, false, false, false, false);
        controladorTeclado.handle(digit1);
        assertTrue(invocado.get());
    }

    @Test
    void testComandosAlternarGrillaEstelasZonasHabitables() {
        // Toggle Grilla con G
        KeyEvent keyG = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.G, false, false, false, false);
        controladorTeclado.handle(keyG);

        // Toggle Estelas con T
        KeyEvent keyT = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.T, false, false, false, false);
        controladorTeclado.handle(keyT);

        // Toggle Zonas Habitables con H
        KeyEvent keyH = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.H, false, false, false, false);
        controladorTeclado.handle(keyH);

        assertNotNull(controladorTeclado.obtenerComando(KeyCode.G));
        assertNotNull(controladorTeclado.obtenerComando(KeyCode.T));
        assertNotNull(controladorTeclado.obtenerComando(KeyCode.H));
    }

    @Test
    void testComandosAjustarFactorMasaMasYMenos() {
        double factorInicial = simulacion.getFactorMasaColocacion();

        KeyEvent keyPlus = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PLUS, false, false, false, false);
        controladorTeclado.handle(keyPlus);
        assertTrue(simulacion.getFactorMasaColocacion() > factorInicial);

        KeyEvent keyMinus = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.MINUS, false, false, false, false);
        controladorTeclado.handle(keyMinus);
        assertEquals(factorInicial, simulacion.getFactorMasaColocacion(), 1e-4);
    }

    @Test
    void testControladorMouseScrollAjustaMasa() {
        double masaInicial = simulacion.getFactorMasaColocacion();
        ScrollEvent scrollUp = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, false, false, false, false, false,
                0, 10.0, 0, 10.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        controladorMouse.manejarScroll(scrollUp);
        assertTrue(simulacion.getFactorMasaColocacion() > masaInicial);

        ScrollEvent scrollDown = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, false, false, false, false, false,
                0, -10.0, 0, -10.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        controladorMouse.manejarScroll(scrollDown);
        assertEquals(masaInicial, simulacion.getFactorMasaColocacion(), 1e-2);
    }

    @Test
    void testControladorMouseSeleccionYLimpieza() {
        // Colocar un cuerpo en la simulación
        org.example.Estrella sol = org.example.game.cuerpo.CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        simulacion.getSistemaSolar().agregarCuerpo(sol);

        // Click en el centro de la pantalla (0, 0 en física = 960, 540 en JavaFX para 1920x1080)
        controladorMouse.seleccionarCuerpoBajoCursor(960, 540);
        assertNotNull(controladorMouse.getCuerpoSeleccionado());
        assertEquals(sol, controladorMouse.getCuerpoSeleccionado());

        // Limpieza de selección
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
