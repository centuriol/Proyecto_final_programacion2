package org.example;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.example.game.controlador.*;
import org.example.game.controlador.comando.*;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.ControladorColocacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ControlTiempoWidget;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ControladorTecladoYComandosTest {

    private SimulacionSolar simulacion;
    private ControladorMouse controladorMouse;
    private ControladorTeclado controladorTeclado;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.getProgresion().desbloquearTodo();
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(1920, 1080);
        BarraInventarioHotbar hotbar = new BarraInventarioHotbar(simulacion);
        ControlTiempoWidget controlTiempo = new ControlTiempoWidget(simulacion);

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

        controladorTeclado.desregistrarComando(KeyCode.A);
        assertFalse(controladorTeclado.tieneComando(KeyCode.A));
    }

    @Test
    void testComandoAlternarPausaConEspacio() {
        boolean pausaInicial = simulacion.getReloj().isEnPausa();
        KeyEvent eventSpace = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false);
        controladorTeclado.handle(eventSpace);

        assertEquals(!pausaInicial, simulacion.getReloj().isEnPausa());

        controladorTeclado.handle(eventSpace);
        assertEquals(pausaInicial, simulacion.getReloj().isEnPausa());
    }

    @Test
    void testComandoAvanzarPasoConTeclaS() {
        simulacion.getReloj().pause();
        long tickAntes = simulacion.getReloj().getTickActual();
        KeyEvent eventS = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.S, false, false, false, false);
        controladorTeclado.handle(eventS);

        assertEquals(tickAntes + 1, simulacion.getReloj().getTickActual(), "Debe avanzar 1 tick");
        assertTrue(simulacion.getReloj().isEnPausa(), "Debe permanecer pausado tras avanzar un paso discreto");
    }

    @Test
    void testComandoCancelarConEscape() {
        simulacion.getGestorColocacion().entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "PlanetaTest");
        assertEquals(ControladorColocacion.ModoColocacion.COLOCANDO, simulacion.getGestorColocacion().getModoColocacion());

        KeyEvent eventEsc = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);
        controladorTeclado.handle(eventEsc);

        assertEquals(ControladorColocacion.ModoColocacion.NINGUNO, simulacion.getGestorColocacion().getModoColocacion());
        assertNull(controladorMouse.getCuerpoSeleccionado());
        assertTrue(controladorMouse.getTrayectoriaPreview().isEmpty());
    }

    @Test
    void testComandosSeleccionTipoCuerpoRegistroYEjecucion() {
        Comando c1 = controladorTeclado.obtenerComando(KeyCode.DIGIT1);
        Comando c2 = controladorTeclado.obtenerComando(KeyCode.DIGIT2);
        Comando c3 = controladorTeclado.obtenerComando(KeyCode.DIGIT3);

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotNull(c3);
        assertTrue(c1 instanceof ComandoSeleccionarTipoCuerpo);
        assertTrue(c2 instanceof ComandoSeleccionarTipoCuerpo);
        assertTrue(c3 instanceof ComandoSeleccionarTipoCuerpo);

        AtomicBoolean invocado = new AtomicBoolean(false);
        Comando mockComando = () -> invocado.set(true);
        controladorTeclado.registrarComando(KeyCode.DIGIT1, mockComando);

        KeyEvent digit1 = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DIGIT1, false, false, false, false);
        controladorTeclado.handle(digit1);
        assertTrue(invocado.get());
    }

    @Test
    void testComandosAlternarGrillaEstelasZonasHabitables() {
        KeyEvent keyG = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.G, false, false, false, false);
        controladorTeclado.handle(keyG);

        KeyEvent keyT = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.T, false, false, false, false);
        controladorTeclado.handle(keyT);

        KeyEvent keyH = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.H, false, false, false, false);
        controladorTeclado.handle(keyH);

        assertNotNull(controladorTeclado.obtenerComando(KeyCode.G));
        assertNotNull(controladorTeclado.obtenerComando(KeyCode.T));
        assertNotNull(controladorTeclado.obtenerComando(KeyCode.H));
    }

    @Test
    void testComandosAjustarFactorMasaMasYMenos() {
        double factorInicial = simulacion.getGestorColocacion().getFactorMasaColocacion();

        KeyEvent keyPlus = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PLUS, false, false, false, false);
        controladorTeclado.handle(keyPlus);
        assertTrue(simulacion.getGestorColocacion().getFactorMasaColocacion() > factorInicial);

        KeyEvent keyMinus = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.MINUS, false, false, false, false);
        controladorTeclado.handle(keyMinus);
        assertEquals(factorInicial, simulacion.getGestorColocacion().getFactorMasaColocacion(), 1e-4);
    }
}
