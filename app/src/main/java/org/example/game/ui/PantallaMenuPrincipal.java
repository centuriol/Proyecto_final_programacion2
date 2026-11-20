package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.guardado.InfoRanuraGuardado;
import org.example.game.render.EscenaFondoMenu;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Primera pantalla de Centuriol con fondo espacial animado permanente y opciones principales (SRP / DIP).
 */
public class PantallaMenuPrincipal extends StackPane {

    private static class VistasMenu {
        final VBox vistaOpciones = new VBox(14);
        final PanelRanurasMenu panelRanuras;
        final Button btnIniciarPartida;
        final Button btnCargarPartida;
        final Button btnSalir;

        VistasMenu(Runnable onIniciar, Runnable onMostrarRanuras, Runnable onSalir,
                   Supplier<List<InfoRanuraGuardado>> proveedorRanuras,
                   IntConsumer onCargarRanura, Runnable onVolver) {
            this.vistaOpciones.setAlignment(Pos.CENTER);
            this.btnIniciarPartida = PanelRanurasMenu.crearBotonAccion("Iniciar partida", () -> {
                if (onIniciar != null) onIniciar.run();
            });
            this.btnCargarPartida = PanelRanurasMenu.crearBotonAccion("Cargar partida", onMostrarRanuras);
            this.btnSalir = PanelRanurasMenu.crearBotonAccion("Salir", () -> {
                if (onSalir != null) onSalir.run();
            });
            this.btnSalir.setStyle(TemaUI.CSS_BTN_PELIGRO + " -fx-padding: 12 24 12 24;");
            this.vistaOpciones.getChildren().addAll(btnIniciarPartida, btnCargarPartida, btnSalir);
            this.panelRanuras = new PanelRanurasMenu(proveedorRanuras, onCargarRanura, onVolver);
        }

        void mostrarOpciones() {
            panelRanuras.setVisible(false);
            panelRanuras.setManaged(false);
            vistaOpciones.setVisible(true);
            vistaOpciones.setManaged(true);
        }

        void mostrarRanuras() {
            panelRanuras.refrescar();
            vistaOpciones.setVisible(false);
            vistaOpciones.setManaged(false);
            panelRanuras.setVisible(true);
            panelRanuras.setManaged(true);
        }
    }

    private final ControladorFondoMenu controladorFondo = new ControladorFondoMenu();
    private final VistasMenu vistas;

    public PantallaMenuPrincipal(Runnable onIniciarPartida,
                                 Supplier<List<InfoRanuraGuardado>> proveedorRanuras,
                                 IntConsumer onCargarRanura,
                                 Runnable onSalir) {
        this.vistas = new VistasMenu(
                onIniciarPartida, this::mostrarVistaRanuras, onSalir,
                proveedorRanuras, onCargarRanura, this::mostrarVistaOpciones
        );

        setBackground(new Background(new BackgroundFill(Color.web("#080a12"), CornerRadii.EMPTY, Insets.EMPTY)));
        setAlignment(Pos.CENTER);
        setPickOnBounds(true);

        getChildren().addAll(controladorFondo.getCanvasFondo(), construirTarjetaCentral());
        sceneProperty().addListener((obs, anterior, actual) -> sincronizarCicloAnimacion());
        visibleProperty().addListener((obs, anterior, visible) -> sincronizarCicloAnimacion());
        mostrarMenuPrincipal();
    }

    private VBox construirTarjetaCentral() {
        VBox tarjeta = new VBox(20);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setMaxSize(470, Region.USE_PREF_SIZE);
        tarjeta.setPadding(new Insets(34, 42, 34, 42));
        tarjeta.setBackground(new Background(new BackgroundFill(Color.rgb(22, 25, 36, 0.85), new CornerRadii(6), Insets.EMPTY)));
        tarjeta.setBorder(new Border(new BorderStroke(TemaUI.COLOR_ACENTO_CYAN, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(2))));

        Label titulo = new Label("CENTURIOL");
        titulo.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
        titulo.setTextFill(TemaUI.COLOR_ACENTO_CYAN);

        Label subtitulo = new Label("Simulador de Sistemas Solares y Civilizaciones");
        subtitulo.setFont(Font.font("Monospace", FontWeight.NORMAL, 13));
        subtitulo.setTextFill(TemaUI.COLOR_TEXTO_MUTED);

        tarjeta.getChildren().addAll(titulo, subtitulo, new StackPane(vistas.vistaOpciones, vistas.panelRanuras));
        return tarjeta;
    }

    private void sincronizarCicloAnimacion() {
        controladorFondo.sincronizarAnimacion(isVisible() && getScene() != null);
    }

    public void avanzarYRenderizarFondo(double deltaSegundos) { controladorFondo.avanzarYRenderizar(deltaSegundos); }
    public boolean manejarClicSatelite(double x, double y) { return controladorFondo.manejarClicSatelite(x, y); }
    public void refrescarRanuras() { vistas.panelRanuras.refrescar(); }

    public void mostrarMenuPrincipal() {
        controladorFondo.reiniciarSatelites();
        vistas.mostrarOpciones();
        setVisible(true);
        setManaged(true);
        toFront();
        sincronizarCicloAnimacion();
    }

    public void mostrarVistaRanuras() { vistas.mostrarRanuras(); }
    private void mostrarVistaOpciones() { vistas.mostrarOpciones(); }

    public void ocultar() {
        setVisible(false);
        setManaged(false);
        sincronizarCicloAnimacion();
    }

    public boolean isMostrandoRanuras() { return vistas.panelRanuras.isVisible(); }
    public boolean isAnimacionFondoActiva() { return controladorFondo.isAnimacionActiva(); }
    public EscenaFondoMenu getEscenaFondo() { return controladorFondo.getEscenaFondo(); }
    public CampoSatelitesMenu getCampoSatelites() { return controladorFondo.getCampoSatelites(); }
    public ResizableCanvas getCanvasFondo() { return controladorFondo.getCanvasFondo(); }
    public Button getBtnIniciarPartida() { return vistas.btnIniciarPartida; }
    public Button getBtnCargarPartida() { return vistas.btnCargarPartida; }
    public Button getBtnSalir() { return vistas.btnSalir; }
    public Button getBtnVolver() { return vistas.panelRanuras.getBtnVolver(); }
    public List<Button> getBotonesRanuras() { return vistas.panelRanuras.getBotonesRanuras(); }
}
