package org.example;

import javafx.application.Application;
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
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.*;

/**
 * Punto de entrada principal de la aplicación JavaFX "Órbita".
 * Principios SOLID:
 * - SRP: Métodos de inicialización breves y modulares para simulación, controladores, interfaz y bucle.
 * - DIP: Inyección de componentes e interfaces.
 */
public class App extends Application {

    public static final double ANCHO_VENTANA_BASE = 1280.0;
    public static final double ALTO_VENTANA_BASE = 720.0;

    private SimulacionSolar simulacion;
    private RenderizadorPixelArt renderizador;
    private BucleJuego bucleJuego;

    private ControladorTeclado controladorTeclado;
    private ControladorMouse controladorMouse;

    private HUDRecursosTop hudRecursos;
    private BarraInventarioHotbar hotbar;
    private ControlTiempoWidget controlTiempo;
    private PanelNotificaciones panelNotificaciones;

    @Override
    public void start(Stage stage) {
        double[] dims = obtenerDimensionesIniciales();
        double ancho = dims[0];
        double alto = dims[1];

        inicializarSimulacionYRender(ancho, alto);
        ResizableCanvas canvasJuego = new ResizableCanvas(ancho, alto);
        inicializarComponentesUI();
        configurarControladores(stage, canvasJuego, ancho, alto);

        PantallaArbolProgresion pantallaArbol = new PantallaArbolProgresion(simulacion, null);
        configurarAtajosArbol(pantallaArbol);

        BorderPane overlayUI = construirOverlayUI(pantallaArbol);
        PantallaGameOver pantallaGameOver = new PantallaGameOver(simulacion);
        simulacion.setOnGameOver(pantallaGameOver::mostrar);

        StackPane root = new StackPane(canvasJuego, overlayUI, pantallaArbol, pantallaGameOver);
        root.setStyle("-fx-background-color: #0e1017;");
        root.setOnKeyPressed(controladorTeclado);

        simulacion.agregarListenerUI(new SincronizadorUI(hudRecursos, controlTiempo));
        iniciarBucle(canvasJuego);
        mostrarVentana(stage, root, ancho, alto);
    }

    private double[] obtenerDimensionesIniciales() {
        double ancho = ANCHO_VENTANA_BASE;
        double alto = ALTO_VENTANA_BASE;
        try {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            ancho = bounds.getWidth();
            alto = bounds.getHeight();
        } catch (Exception ignored) {}
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
    }

    private void configurarControladores(Stage stage, ResizableCanvas canvas, double ancho, double alto) {
        controladorMouse = new ControladorMouse(
                simulacion,
                hotbar,
                cuerpo -> VentanaDescripcionCuerpo.mostrarParaInspeccionar(cuerpo, simulacion, stage, controladorMouse),
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

    private void configurarAtajosArbol(PantallaArbolProgresion pantallaArbol) {
        controladorTeclado.registrarComando(KeyCode.P, new ComandoAbrirProgresion(simulacion, pantallaArbol::alternar));
        hotbar.setAccionAbrirArbol(pantallaArbol::mostrar);

        ComandoCancelar comandoCancelar = new ComandoCancelar(simulacion, hotbar, controladorMouse);
        comandoCancelar.setAccionExtra(() -> {
            if (pantallaArbol.isVisible()) {
                pantallaArbol.ocultar();
            }
        });
        controladorTeclado.registrarComando(KeyCode.ESCAPE, comandoCancelar);
    }

    private BorderPane construirOverlayUI(PantallaArbolProgresion pantallaArbol) {
        BorderPane overlay = new BorderPane();
        overlay.setPadding(new Insets(12));
        overlay.setPickOnBounds(false);

        overlay.setTop(construirTopBar(pantallaArbol));
        overlay.setLeft(panelNotificaciones);
        overlay.setBottom(construirBottomBar());
        return overlay;
    }

    private HBox construirTopBar(PantallaArbolProgresion pantallaArbol) {
        HBox navTabs = new HBox(8);
        navTabs.setAlignment(Pos.CENTER_LEFT);
        navTabs.setPickOnBounds(true);

        Button btnTabSim = new Button("[SIMULADOR]");
        btnTabSim.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        Button btnTabArbol = new Button("[ÁRBOL DE INVESTIGACIÓN (P)]");
        btnTabArbol.setFont(Font.font("Monospace", FontWeight.BOLD, 11));

        String tabActiva = "-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-padding: 6 14 6 14; -fx-cursor: hand;";
        String tabInactiva = "-fx-background-color: #1a1c26; -fx-text-fill: #8c92a4; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-padding: 6 14 6 14; -fx-cursor: hand;";

        btnTabSim.setStyle(tabActiva);
        btnTabArbol.setStyle(tabInactiva);

        pantallaArbol.visibleProperty().addListener((obs, oldV, arbolVisible) -> {
            btnTabSim.setStyle(arbolVisible ? tabInactiva : tabActiva);
            btnTabArbol.setStyle(arbolVisible ? tabActiva : tabInactiva);
        });

        btnTabSim.setOnAction(e -> pantallaArbol.ocultar());
        btnTabArbol.setOnAction(e -> pantallaArbol.mostrar());
        navTabs.getChildren().addAll(btnTabSim, btnTabArbol);

        HBox topBar = new HBox(navTabs, EspaciadorUI.crearHorizontal(), hudRecursos);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPickOnBounds(false);
        return topBar;
    }

    private BorderPane construirBottomBar() {
        BorderPane bottomBar = new BorderPane();
        bottomBar.setPickOnBounds(false);

        BorderPane.setAlignment(controlTiempo, Pos.BOTTOM_LEFT);
        bottomBar.setLeft(controlTiempo);

        VBox contenedorHotbar = new VBox(2, hotbar.getBotonToggle(), hotbar);
        contenedorHotbar.setAlignment(Pos.BOTTOM_CENTER);
        contenedorHotbar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        contenedorHotbar.setPickOnBounds(false);
        BorderPane.setAlignment(contenedorHotbar, Pos.BOTTOM_CENTER);
        bottomBar.setCenter(contenedorHotbar);

        Region balanceadorDerecho = new Region();
        balanceadorDerecho.setPickOnBounds(false);
        balanceadorDerecho.setMouseTransparent(true);
        balanceadorDerecho.minWidthProperty().bind(controlTiempo.widthProperty());
        balanceadorDerecho.prefWidthProperty().bind(controlTiempo.widthProperty());
        bottomBar.setRight(balanceadorDerecho);

        return bottomBar;
    }

    private void iniciarBucle(ResizableCanvas canvas) {
        bucleJuego = new BucleJuego(simulacion, renderizador, canvas, controladorMouse);
        bucleJuego.iniciar();
    }

    private void mostrarVentana(Stage stage, StackPane root, double ancho, double alto) {
        Scene scene = new Scene(root, ancho, alto);
        scene.setFill(Color.web("#0e1017"));

        stage.setScene(scene);
        stage.setTitle("Orbita - Simulador de Sistemas Solares");
        stage.setMaximized(true);
        stage.show();
        root.requestFocus();
    }

    @Override
    public void stop() throws Exception {
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

    public static void main(String[] args) {
        launch(args);
    }
}
