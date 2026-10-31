package org.example;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.game.controlador.*;
import org.example.game.controlador.comando.*;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.guardado.TemporizadorAutoguardado;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.*;
/**
 * Punto de entrada principal de la aplicacion JavaFX "Centuriol".
 * Principios SOLID:
 * - SRP: Metodos de inicializacion breves y modulares delegando el flujo de sesion a CoordinadorSesionJuego.
 * - DIP: Inyeccion de componentes e interfaces.
 */
public class App extends Application {

    public static final double ANCHO_VENTANA_BASE = 1280.0;
    public static final double ALTO_VENTANA_BASE = 720.0;

    private SimulacionSolar simulacion;
    private RenderizadorPixelArt renderizador;
    private ResizableCanvas canvasJuego;
    private BucleJuego bucleJuego;

    private ControladorTeclado controladorTeclado;
    private ControladorMouse controladorMouse;

    private HUDRecursosTop hudRecursos;
    private BarraInventarioHotbar hotbar;
    private ControlTiempoWidget controlTiempo;
    private PanelNotificaciones panelNotificaciones;
    private CapaIndicadoresSol capaIndicadoresSol;
    private HBox topBar;
    private BorderPane bottomBar;
    private Button btnArbolInferior;

    private GestorGuardadoPartida gestorGuardado;
    private TemporizadorAutoguardado autoguardado;
    private CoordinadorSesionJuego coordinadorSesion;
    private PantallaArbolProgresion pantallaArbol;
    private PantallaMenuPausa pantallaMenuPausa;
    private final VisualizadorDescripcion visualizadorDescripcion = new VentanaDescripcionCuerpo();
    private PantallaMenuPrincipal pantallaMenuPrincipal;
    private PantallaGameOver pantallaGameOver;

    @Override
    public void start(Stage stage) {
        double[] dims = obtenerDimensionesIniciales();
        double ancho = dims[0];
        double alto = dims[1];

        StackPane root = inicializarInterfaz(stage, ancho, alto, null);
        iniciarBucle(canvasJuego);
        mostrarVentana(stage, root, ancho, alto);
    }

    public StackPane inicializarInterfaz(Stage stage,
                                         double ancho,
                                         double alto,
                                         GestorGuardadoPartida gestorPersonalizado) {
        inicializarSimulacionYRender(ancho, alto);
        canvasJuego = new ResizableCanvas(ancho, alto);
        inicializarComponentesUI();
        configurarControladores(stage, canvasJuego, ancho, alto);

        pantallaArbol = new PantallaArbolProgresion(simulacion, null);
        configurarSistemaGuardadoYSesion(pantallaArbol, gestorPersonalizado);

        ConfiguradorAtajosSesion configuradorAtajos = new ConfiguradorAtajosSesion();
        configuradorAtajos.configurarAtajos(
                controladorTeclado,
                simulacion,
                renderizador.getCamaraZoom(),
                gestorGuardado,
                coordinadorSesion,
                controladorMouse,
                hotbar,
                pantallaArbol
        );

        ConstructorOverlayUI constructorOverlay = new ConstructorOverlayUI();
        ConstructorOverlayUI.EstructuraOverlay estructuraOverlay = constructorOverlay.construir(
                hudRecursos,
                hotbar,
                controlTiempo,
                panelNotificaciones,
                pantallaArbol,
                coordinadorSesion
        );
        this.topBar = estructuraOverlay.topBar();
        this.bottomBar = estructuraOverlay.bottomBar();
        this.btnArbolInferior = estructuraOverlay.btnArbolInferior();
        BorderPane overlayUI = estructuraOverlay.overlay();

        capaIndicadoresSol.visibleProperty().bind(overlayUI.visibleProperty());
        capaIndicadoresSol.managedProperty().bind(overlayUI.managedProperty());

        coordinadorSesion.vincularPantallas(pantallaMenuPrincipal, pantallaGameOver, overlayUI);
        coordinadorSesion.vincularMenuPausa(pantallaMenuPausa);
        coordinadorSesion.mostrarMenuInicial();

        simulacion.agregarListenerUI(new SincronizadorUI(hudRecursos, controlTiempo));
        return ensamblarEscenaRaiz(overlayUI);
    }

    private StackPane ensamblarEscenaRaiz(BorderPane overlayUI) {
        StackPane root = new StackPane(
                canvasJuego,
                capaIndicadoresSol,
                overlayUI,
                pantallaArbol,
                pantallaMenuPausa,
                pantallaGameOver,
                pantallaMenuPrincipal
        );
        root.setStyle("-fx-background-color: #0e1017;");
        root.setOnKeyPressed(evento -> {
            if (coordinadorSesion.esPartidaEnCurso()) {
                controladorTeclado.handle(evento);
            }
        });
        return root;
    }

    private double[] obtenerDimensionesIniciales() {
        double ancho = ANCHO_VENTANA_BASE;
        double alto = ALTO_VENTANA_BASE;
        try {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            ancho = bounds.getWidth();
            alto = bounds.getHeight();
        } catch (Exception ex) {
            System.err.println("[INFO] Dimensiones por defecto aplicadas en entorno sin pantalla: " + ex.getMessage());
        }
        return new double[]{ancho, alto};
    }

    private void inicializarSimulacionYRender(double ancho, double alto) {
        simulacion = new SimulacionSolar(ancho, alto);
        renderizador = new RenderizadorPixelArt(ancho, alto);
        renderizador.setProveedorNaves(() -> simulacion.getGestorColonizacion().getNaves());
        renderizador.setGestorEfectos(simulacion.getGestorEfectos());
    }

    private void inicializarComponentesUI() {
        hudRecursos = new HUDRecursosTop(simulacion);
        hotbar = new BarraInventarioHotbar(simulacion);
        controlTiempo = new ControlTiempoWidget(simulacion);
        panelNotificaciones = new PanelNotificaciones(simulacion);
        capaIndicadoresSol = new CapaIndicadoresSol(renderizador.getCamaraZoom());
    }

    private void configurarControladores(Stage stage, ResizableCanvas canvas, double ancho, double alto) {
        controladorMouse = new ControladorMouse(
                simulacion,
                hotbar,
                cuerpo -> {
                    if (stage != null) {
                        visualizadorDescripcion.abrirParaInspeccionar(cuerpo, simulacion, stage, controladorMouse);
                    }
                },
                ancho,
                alto
        );
        controladorMouse.conectar(canvas);
        controladorMouse.setCamaraZoom(renderizador.getCamaraZoom());

        controladorTeclado = new ControladorTeclado();
        controladorTeclado.configurarAtajosPorDefecto(
                simulacion, controlTiempo, hotbar, renderizador, controladorMouse
        );

        canvas.widthProperty().addListener((obs, oldV, newV) -> sincronizarCanvas(canvas));
        canvas.heightProperty().addListener((obs, oldV, newV) -> sincronizarCanvas(canvas));
    }

    private void sincronizarCanvas(ResizableCanvas canvas) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w > 0 && h > 0) {
            renderizador.setDimensiones(w, h);
            simulacion.setDimensionesMundo(w, h);
            controladorMouse.setDimensiones(w, h);
        }
    }

    private void configurarSistemaGuardadoYSesion(PantallaArbolProgresion arbol,
                                                  GestorGuardadoPartida gestorPersonalizado) {
        gestorGuardado = gestorPersonalizado != null ? gestorPersonalizado : new GestorGuardadoPartida();
        autoguardado = new TemporizadorAutoguardado(gestorGuardado, simulacion, renderizador.getCamaraZoom());

        Runnable limpiarVista = () -> {
            renderizador.limpiarEstelas();
            controladorMouse.limpiarSeleccion();
            hotbar.deseleccionar();
            arbol.ocultar();
        };

        coordinadorSesion = new CoordinadorSesionJuego(simulacion, renderizador.getCamaraZoom(), gestorGuardado, autoguardado);
        coordinadorSesion.configurarCallbacks(limpiarVista, () -> {
            if (bucleJuego != null) {
                bucleJuego.detener();
            }
            if (Platform.isFxApplicationThread()) {
                Platform.exit();
            }
        });

        gestorGuardado.agregarListenerPostCarga(() -> {
            limpiarVista.run();
            if (!simulacion.estaPerdida() && pantallaGameOver != null) {
                pantallaGameOver.ocultar();
            }
        });

        inicializarPantallasModales();
    }

    private void inicializarPantallasModales() {
        pantallaMenuPrincipal = new PantallaMenuPrincipal(
                coordinadorSesion::iniciarNuevaPartida,
                gestorGuardado::obtenerRanuras,
                coordinadorSesion::cargarRanura,
                coordinadorSesion::salir
        );
        pantallaMenuPausa = new PantallaMenuPausa(
                coordinadorSesion::reanudarDesdePausa,
                coordinadorSesion::guardarDesdePausa,
                coordinadorSesion::cargarDesdePausa,
                coordinadorSesion::volverAlMenuPrincipal
        );
        pantallaGameOver = new PantallaGameOver(
                coordinadorSesion::volverAlUltimoGuardado,
                coordinadorSesion::volverAlMenuPrincipal
        );
    }

    private void iniciarBucle(ResizableCanvas canvas) {
        bucleJuego = new BucleJuego(simulacion, renderizador, canvas, controladorMouse, autoguardado);
        bucleJuego.setListenerFrame(() -> capaIndicadoresSol.actualizar(
                simulacion.getSistemaSolar().getCuerpos(),
                canvas.getWidth(),
                canvas.getHeight()
        ));
        bucleJuego.iniciar();
    }

    private void mostrarVentana(Stage stage, StackPane root, double ancho, double alto) {
        Scene scene = new Scene(root, ancho, alto);
        scene.setFill(Color.web("#0e1017"));

        stage.setScene(scene);
        stage.setTitle("Centuriol - Simulador de Sistemas Solares");
        stage.setMaximized(true);
        stage.show();
        root.requestFocus();
    }

    @Override
    public void stop() throws Exception {
        if (autoguardado != null) {
            autoguardado.desactivar();
        }
        if (bucleJuego != null) {
            bucleJuego.detener();
        }
        super.stop();
    }

    public SimulacionSolar getSimulacion() { return simulacion; }
    public ControladorTeclado getControladorTeclado() { return controladorTeclado; }
    public ControladorMouse getControladorMouse() { return controladorMouse; }
    public BucleJuego getBucleJuego() { return bucleJuego; }
    public RenderizadorPixelArt getRenderizador() { return renderizador; }
    public GestorGuardadoPartida getGestorGuardado() { return gestorGuardado; }
    public CoordinadorSesionJuego getCoordinadorSesion() { return coordinadorSesion; }
    public PantallaMenuPrincipal getPantallaMenuPrincipal() { return pantallaMenuPrincipal; }
    public PantallaMenuPausa getPantallaMenuPausa() { return pantallaMenuPausa; }
    public PantallaArbolProgresion getPantallaArbol() { return pantallaArbol; }
    public PantallaGameOver getPantallaGameOver() { return pantallaGameOver; }
    public CapaIndicadoresSol getCapaIndicadoresSol() { return capaIndicadoresSol; }
    public HBox getTopBar() { return topBar; }
    public BorderPane getBottomBar() { return bottomBar; }
    public Button getBtnArbolInferior() { return btnArbolInferior; }

    public static void main(String[] args) {
        launch(args);
    }
}
