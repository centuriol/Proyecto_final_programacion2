package org.example;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.guardado.RepositorioPartidasArchivo;
import org.example.game.ui.PantallaArbolProgresion;
import org.example.game.ui.PantallaMenuPausa;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class MenuPausaYDisposicionUITest {

    @TempDir
    Path tempDir;

    private App app;
    private StackPane root;

    @BeforeEach
    void setUp() {
        app = new App();
        GestorGuardadoPartida gestor = new GestorGuardadoPartida(
                new RepositorioPartidasArchivo(tempDir.resolve("saves_ui_pausa"))
        );
        root = app.inicializarInterfaz(null, 1280.0, 720.0, gestor);
        app.getCoordinadorSesion().iniciarNuevaPartida();
    }

    @Test
    void testBarraSuperiorSinBotonesIzquierdosYBotonArbolDiscretoAbajoDerecha() {
        List<Button> botonesTopBar = recolectarBotones(app.getTopBar());
        assertTrue(botonesTopBar.isEmpty(), "La barra superior no debe contener botones de navegacion ni guardado");

        Node esquinaDerecha = app.getBottomBar().getRight();
        assertNotNull(esquinaDerecha);
        List<Button> botonesDerecha = recolectarBotones(esquinaDerecha);
        assertEquals(1, botonesDerecha.size());
        assertSame(app.getBtnArbolInferior(), botonesDerecha.get(0));
        assertEquals("Árbol", app.getBtnArbolInferior().getText());
        assertNotNull(app.getBottomBar().getCenter(), "La barra de objetos debe permanecer centrada");
    }

    @Test
    void testBotonArbolInferiorAbreArbolYXArribaDerechaVuelveAlSimulador() {
        PantallaArbolProgresion arbol = app.getPantallaArbol();
        assertFalse(arbol.isVisible());

        app.getBtnArbolInferior().fire();
        assertTrue(arbol.isVisible(), "El boton discreto Arbol abajo a la derecha debe abrir el arbol");

        Button btnCerrar = arbol.getBtnCerrar();
        assertNotNull(btnCerrar);
        assertEquals("X", btnCerrar.getText(), "Dentro del arbol debe existir una X arriba a la derecha");

        List<Button> todosBotonesArbol = recolectarBotones(arbol);
        assertTrue(todosBotonesArbol.stream().noneMatch(b -> b.getText().contains("[SIMULADOR]")));

        btnCerrar.fire();
        assertFalse(arbol.isVisible(), "Hacer clic en la X del arbol debe volver al simulador");
    }

    @Test
    void testEscapeAbreMenuPausaConCuatroOpcionesYContinuarReanuda() {
        PantallaMenuPausa menuPausa = app.getPantallaMenuPausa();
        assertFalse(menuPausa.isVisible());
        assertFalse(app.getSimulacion().getReloj().isEnPausa());

        enviarTecla(KeyCode.ESCAPE);

        assertTrue(menuPausa.isVisible(), "Presionar Esc debe abrir el menu de pausa");
        assertTrue(app.getSimulacion().getReloj().isEnPausa(), "El menu de pausa debe pausar la simulacion");
        assertEquals("Continuar", menuPausa.getBtnContinuar().getText());
        assertEquals("Guardar", menuPausa.getBtnGuardar().getText());
        assertEquals("Cargar", menuPausa.getBtnCargar().getText());
        assertEquals("Salir al menú principal", menuPausa.getBtnSalirMenu().getText());

        menuPausa.getBtnContinuar().fire();
        assertFalse(menuPausa.isVisible());
        assertFalse(app.getSimulacion().getReloj().isEnPausa());

        enviarTecla(KeyCode.ESCAPE);
        assertTrue(menuPausa.isVisible());
        enviarTecla(KeyCode.ESCAPE);
        assertFalse(menuPausa.isVisible(), "Presionar Esc nuevamente debe reanudar la partida");
        assertFalse(app.getSimulacion().getReloj().isEnPausa());
    }

    @Test
    void testGuardarCargarYSalirAlMenuPrincipalDesdeMenuPausa() {
        app.getSimulacion().getPresetsSistema().cargarPresetEstrellaBinaria(app.getSimulacion());
        app.getSimulacion().avanzarTicks(12);
        long tickGuardado = app.getSimulacion().getReloj().getTickActual();

        enviarTecla(KeyCode.ESCAPE);
        PantallaMenuPausa menuPausa = app.getPantallaMenuPausa();
        menuPausa.getBtnGuardar().fire();

        assertTrue(menuPausa.isVisible());
        assertTrue(menuPausa.getLblEstado().isVisible());
        assertFalse(menuPausa.getLblEstado().getText().isBlank());

        app.getSimulacion().limpiarCuerpos();
        assertEquals(0, app.getSimulacion().getSistemaSolar().getCuerpos().size());

        menuPausa.getBtnCargar().fire();
        assertFalse(menuPausa.isVisible(), "Al cargar partida desde pausa se vuelve al simulador activo");
        assertEquals(3, app.getSimulacion().getSistemaSolar().getCuerpos().size());
        assertEquals(tickGuardado, app.getSimulacion().getReloj().getTickActual());
        assertFalse(app.getSimulacion().getReloj().isEnPausa());

        enviarTecla(KeyCode.ESCAPE);
        menuPausa.getBtnSalirMenu().fire();
        assertFalse(menuPausa.isVisible());
        assertTrue(app.getPantallaMenuPrincipal().isVisible());
    }

    @Test
    void testAtajosF5F9YPSiguenFuncionandoYEscEnArbolCierraArbolPrimero() {
        enviarTecla(KeyCode.P);
        assertTrue(app.getPantallaArbol().isVisible(), "El atajo P debe seguir abriendo el arbol");

        enviarTecla(KeyCode.ESCAPE);
        assertFalse(app.getPantallaArbol().isVisible(), "Esc dentro del arbol debe cerrarlo y volver al simulador");
        assertFalse(app.getPantallaMenuPausa().isVisible(), "Esc al cerrar el arbol no debe abrir la pausa en el mismo pulso");

        app.getSimulacion().getPresetsSistema().cargarPresetSistemaBasico(app.getSimulacion());
        int cantidadGuardada = app.getSimulacion().getSistemaSolar().getCuerpos().size();
        assertTrue(cantidadGuardada > 0);

        enviarTecla(KeyCode.F5);
        app.getSimulacion().limpiarCuerpos();
        assertEquals(0, app.getSimulacion().getSistemaSolar().getCuerpos().size());

        enviarTecla(KeyCode.F9);
        assertEquals(cantidadGuardada, app.getSimulacion().getSistemaSolar().getCuerpos().size());
    }

    private void enviarTecla(KeyCode codigo) {
        KeyEvent evento = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                codigo.getName(),
                codigo,
                false,
                false,
                false,
                false
        );
        root.getOnKeyPressed().handle(evento);
    }

    private List<Button> recolectarBotones(Node nodo) {
        List<Button> encontrados = new ArrayList<>();
        if (nodo instanceof Button boton) {
            encontrados.add(boton);
        }
        if (nodo instanceof Parent padre) {
            for (Node hijo : padre.getChildrenUnmodifiable()) {
                encontrados.addAll(recolectarBotones(hijo));
            }
        }
        return encontrados;
    }
}
