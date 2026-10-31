package org.example;
import javafx.scene.layout.BorderPane;
import org.example.game.controlador.CoordinadorSesionJuego;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.guardado.*;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.PantallaGameOver;
import org.example.game.ui.PantallaMenuPrincipal;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class MenuPrincipalYDerrotaFlujoTest {

    @TempDir
    Path tempDir;

    private SimulacionSolar simulacion;
    private CamaraZoom camara;
    private GestorGuardadoPartida gestorGuardado;
    private TemporizadorAutoguardado autoguardado;
    private CoordinadorSesionJuego coordinador;
    private PantallaMenuPrincipal menuPrincipal;
    private PantallaGameOver pantallaGameOver;
    private BorderPane overlayJuego;
    private AtomicBoolean salidaInvocada;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1280, 720);
        camara = new CamaraZoom();
        gestorGuardado = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("centuriol_saves")));
        autoguardado = new TemporizadorAutoguardado(gestorGuardado, simulacion, camara, 15.0);
        salidaInvocada = new AtomicBoolean(false);

        coordinador = new CoordinadorSesionJuego(simulacion, camara, gestorGuardado, autoguardado);
        coordinador.configurarCallbacks(() -> {}, () -> salidaInvocada.set(true));

        menuPrincipal = new PantallaMenuPrincipal(
                coordinador::iniciarNuevaPartida,
                gestorGuardado::obtenerRanuras,
                coordinador::cargarRanura,
                coordinador::salir
        );
        pantallaGameOver = new PantallaGameOver(
                coordinador::volverAlUltimoGuardado,
                coordinador::volverAlMenuPrincipal
        );
        overlayJuego = new BorderPane();

        coordinador.vincularPantallas(menuPrincipal, pantallaGameOver, overlayJuego);
        coordinador.mostrarMenuInicial();
    }

    @Test
    void testMenuPrincipalEsPrimeraPantallaConTresOpcionesSinEmojis() {
        assertTrue(menuPrincipal.isVisible(), "El menu principal debe ser la primera pantalla visible");
        assertFalse(overlayJuego.isVisible(), "El HUD de juego debe permanecer oculto en el menu");
        assertFalse(pantallaGameOver.isVisible(), "Game Over debe estar oculto al inicio");
        assertFalse(autoguardado.estaActivo(), "Autoguardado inactivo mientras esta en el menu");
        assertTrue(simulacion.isEnPausa());
        assertFalse(coordinador.esPartidaEnCurso());

        assertEquals("Iniciar partida", menuPrincipal.getBtnIniciarPartida().getText());
        assertEquals("Cargar partida", menuPrincipal.getBtnCargarPartida().getText());
        assertEquals("Salir", menuPrincipal.getBtnSalir().getText());
        assertFalse(menuPrincipal.getBtnIniciarPartida().getText().matches(".*[\\p{So}\\p{Cn}].*"));
        assertFalse(menuPrincipal.getBtnCargarPartida().getText().matches(".*[\\p{So}\\p{Cn}].*"));
        assertFalse(menuPrincipal.getBtnSalir().getText().matches(".*[\\p{So}\\p{Cn}].*"));
    }

    @Test
    void testIniciarPartidaOcultaMenuActivaJuegoYCentraCamara() {
        camara.setNivelZoom(1.4);
        camara.desplazarPorPantalla(300, -200);

        menuPrincipal.getBtnIniciarPartida().fire();

        assertFalse(menuPrincipal.isVisible());
        assertTrue(overlayJuego.isVisible());
        assertTrue(coordinador.esPartidaEnCurso());
        assertTrue(autoguardado.estaActivo());
        assertFalse(simulacion.isEnPausa());
        assertTrue(camara.esVistaNeutra());
        assertFalse(simulacion.estaPerdida());
    }

    @Test
    void testCargarPartidaMuestraTresRanurasYCargaRanuraOcupada() {
        simulacion.cargarPresetEstrellaBinaria();
        simulacion.getReloj().restaurar(144L, false, 2.0);
        gestorGuardado.guardarPartidaRapida(simulacion, camara); // ocupa Ranura 1

        coordinador.mostrarMenuInicial();
        menuPrincipal.getBtnCargarPartida().fire();

        assertTrue(menuPrincipal.isMostrandoRanuras());
        assertEquals(3, menuPrincipal.getBotonesRanuras().size());
        assertFalse(menuPrincipal.getBotonesRanuras().get(0).isDisabled(), "Ranura 1 ocupada debe estar habilitada");
        assertTrue(menuPrincipal.getBotonesRanuras().get(1).isDisabled(), "Ranura 2 vacia debe estar deshabilitada");
        assertTrue(menuPrincipal.getBotonesRanuras().get(2).isDisabled(), "Ranura 3 vacia debe estar deshabilitada");

        simulacion.limpiarCuerpos();
        menuPrincipal.getBotonesRanuras().get(0).fire();

        assertFalse(menuPrincipal.isVisible());
        assertTrue(overlayJuego.isVisible());
        assertEquals(144L, simulacion.getTickActual());
        assertEquals(3, simulacion.getSistemaSolar().getCuerpos().size());
    }

    @Test
    void testOpcionSalirDetieneAutoguardadoEInvocaSalida() {
        menuPrincipal.getBtnIniciarPartida().fire();
        assertTrue(autoguardado.estaActivo());

        coordinador.mostrarMenuInicial();
        menuPrincipal.getBtnSalir().fire();

        assertTrue(salidaInvocada.get());
        assertFalse(autoguardado.estaActivo());
        assertTrue(simulacion.isEnPausa());
    }

    @Test
    void testAlPerderMuestraDosOpcionesYVolverAlUltimoGuardadoRestauraPartida() {
        menuPrincipal.getBtnIniciarPartida().fire();
        simulacion.avanzarTicks(15);
        camara.desplazarPorPantalla(180, -90);
        gestorGuardado.guardarAutoguardado(simulacion, camara);
        long tickGuardado = simulacion.getTickActual();

        // Provocar derrota dejando un agujero negro sin planetas ni lunas
        simulacion.limpiarCuerpos();
        simulacion.agregarCuerpo(CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.0));
        simulacion.avanzarTick();

        assertTrue(simulacion.estaPerdida());
        assertTrue(pantallaGameOver.isVisible(), "Al perder debe mostrarse la pantalla de derrota");
        assertFalse(autoguardado.estaActivo(), "Al perder se detiene el autoguardado");
        assertEquals("Volver al último guardado", pantallaGameOver.getBtnVolverUltimoGuardado().getText());
        assertEquals("Volver al menú", pantallaGameOver.getBtnVolverMenu().getText());

        pantallaGameOver.getBtnVolverUltimoGuardado().fire();

        assertFalse(pantallaGameOver.isVisible(), "Tras restaurar el ultimo guardado se oculta Game Over");
        assertFalse(menuPrincipal.isVisible());
        assertTrue(overlayJuego.isVisible());
        assertFalse(simulacion.estaPerdida());
        assertEquals(tickGuardado, simulacion.getTickActual());
        assertEquals(180.0, camara.getDesplazamiento().getX(), 1e-6);
        assertEquals(-90.0, camara.getDesplazamiento().getY(), 1e-6);
        assertTrue(autoguardado.estaActivo());
    }

    @Test
    void testAlPerderOpcionVolverAlMenuRegresaAPantallaPrincipal() {
        menuPrincipal.getBtnIniciarPartida().fire();
        simulacion.limpiarCuerpos();
        simulacion.agregarCuerpo(CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.0));
        simulacion.avanzarTick();

        assertTrue(pantallaGameOver.isVisible());

        pantallaGameOver.getBtnVolverMenu().fire();

        assertFalse(pantallaGameOver.isVisible());
        assertTrue(menuPrincipal.isVisible());
        assertFalse(overlayJuego.isVisible());
        assertFalse(autoguardado.estaActivo());
    }

    @Test
    void testAlPerderSinGuardadosPreviosInformaEstadoYMantieneOpciones() {
        menuPrincipal.getBtnIniciarPartida().fire();
        simulacion.limpiarCuerpos();
        simulacion.agregarCuerpo(CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.0));
        simulacion.avanzarTick();

        assertTrue(pantallaGameOver.isVisible());
        pantallaGameOver.getBtnVolverUltimoGuardado().fire();

        assertTrue(pantallaGameOver.isVisible(), "Si no hay guardados previos, permanece visible para volver al menu");
        assertTrue(pantallaGameOver.getLblEstadoAccion().isVisible());
        assertFalse(pantallaGameOver.getLblEstadoAccion().getText().isBlank());
    }
}
