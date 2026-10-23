package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.game.controlador.BucleJuego;
import org.example.game.controlador.ControladorMouse;
import org.example.game.controlador.ControladorTeclado;
import org.example.game.controlador.SincronizadorUI;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.*;
import org.example.game.controlador.comando.ComandoAbrirProgresion;
import org.example.game.controlador.comando.ComandoCancelar;
import org.example.game.controlador.comando.ComandoDebugMaxRecursos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Punto de entrada principal de "Orbita" - Sandbox espacial Pixel Art.
 *
 * Principios SOLID aplicados:
 * - SRP: Su única responsabilidad es inicializar la aplicación JavaFX, configurar el árbol
 *   de vistas y componer los controladores dedicados. No implementa funciones de teclado,
 *   eventos de ratón ni ciclos de simulación directamente.
 * - DIP: Delega la lógica de negocio a SimulacionSolar/MotorFisica, el dibujo a RenderizadorPixelArt,
 *   la temporización a BucleJuego y el manejo de entradas a ControladorTeclado y ControladorMouse.
 */
public class App extends Application {

    private static final int ANCHO_VENTANA_BASE = 1366;
    private static final int ALTO_VENTANA_BASE = 768;

    private SimulacionSolar simulacion;
    private RenderizadorPixelArt renderizador;
    private BucleJuego bucleJuego;
    private ControladorTeclado controladorTeclado;
    private ControladorMouse controladorMouse;

    // Componentes UI
    private HUDRecursosTop hudRecursos;
    private BarraInventarioHotbar hotbar;
    private ControlTiempoWidget controlTiempo;
    private PanelNotificaciones panelNotificaciones;

    @Override
    public void start(Stage stage) {
        // Obtener dimensiones ideales de la pantalla del usuario
        double anchoInicial = ANCHO_VENTANA_BASE;
        double altoInicial = ALTO_VENTANA_BASE;
        try {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            anchoInicial = bounds.getWidth();
            altoInicial = bounds.getHeight();
        } catch (Exception ignored) {}

        // 1. Simulación & Motor de física permisiva
        simulacion = new SimulacionSolar(anchoInicial, altoInicial);

        // 2. Renderizador Pixel Art
        renderizador = new RenderizadorPixelArt(anchoInicial, altoInicial);
        renderizador.setProveedorNaves(() -> simulacion.getGestorColonizacion().getNaves());
        renderizador.setGestorEfectos(simulacion.getGestorEfectos());

        // 3. Canvas principal de juego redimensionable (ocupa el 100% de la ventana sin recortes ni zoom)
        ResizableCanvas canvasJuego = new ResizableCanvas(anchoInicial, altoInicial);

        // 4. Componentes UI
        hudRecursos = new HUDRecursosTop(simulacion);
        hotbar = new BarraInventarioHotbar(simulacion);
        controlTiempo = new ControlTiempoWidget(simulacion);
        panelNotificaciones = new PanelNotificaciones(simulacion);

        // 5. Controladores de entrada encapsulados según POO y SOLID
        controladorMouse = new ControladorMouse(
                simulacion,
                hotbar,
                cuerpo -> VentanaDescripcionCuerpo.mostrarParaInspeccionar(cuerpo, simulacion, stage, controladorMouse),
                anchoInicial,
                altoInicial
        );
        controladorMouse.conectar(canvasJuego);
        controladorMouse.setCamaraZoom(renderizador.getCamaraZoom());

        controladorTeclado = new ControladorTeclado();
        controladorTeclado.configurarAtajosPorDefecto(
                simulacion, controlTiempo, hotbar, renderizador, controladorMouse
        );

        // [TEMPORAL - Remover antes de la entrega final]
        controladorTeclado.setComandoCtrlM(new ComandoDebugMaxRecursos(simulacion, renderizador));

        // Sincronizar dimensiones cuando el canvas cambie de tamaño
        Runnable sincronizarDimensiones = () -> {
            double w = canvasJuego.getWidth();
            double h = canvasJuego.getHeight();
            if (w > 0 && h > 0) {
                renderizador.setDimensiones(w, h);
                simulacion.setDimensionesMundo(w, h);
                controladorMouse.setDimensiones(w, h);
            }
        };
        canvasJuego.widthProperty().addListener((obs, oldV, newV) -> sincronizarDimensiones.run());
        canvasJuego.heightProperty().addListener((obs, oldV, newV) -> sincronizarDimensiones.run());

        // 6. Layout Superpuesto (HUD sobre Canvas)
        BorderPane overlayUI = new BorderPane();
        overlayUI.setPadding(new Insets(12));
        overlayUI.setPickOnBounds(false); // Permite click-through al canvas

        // 6.2 Pantalla del Árbol de Investigación (Apartado dedicado)
        PantallaArbolProgresion pantallaArbol = new PantallaArbolProgresion(simulacion, null);

        // Botones de Navegación de Vistas Principales (Simulador vs Árbol de Investigación)
        HBox navTabs = new HBox(8);
        navTabs.setAlignment(Pos.CENTER_LEFT);
        navTabs.setPickOnBounds(true);

        Button btnTabSimulador = new Button("[SIMULADOR]");
        btnTabSimulador.setFont(Font.font("Monospace", FontWeight.BOLD, 11));

        Button btnTabArbol = new Button("[ÁRBOL DE INVESTIGACIÓN (P)]");
        btnTabArbol.setFont(Font.font("Monospace", FontWeight.BOLD, 11));

        String estiloTabActiva = "-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-padding: 6 14 6 14; -fx-cursor: hand;";
        String estiloTabInactiva = "-fx-background-color: #1a1c26; -fx-text-fill: #8c92a4; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-padding: 6 14 6 14; -fx-cursor: hand;";

        btnTabSimulador.setStyle(estiloTabActiva);
        btnTabArbol.setStyle(estiloTabInactiva);

        Runnable sincronizarTabs = () -> {
            boolean arbolVisible = pantallaArbol.isVisible();
            btnTabSimulador.setStyle(arbolVisible ? estiloTabInactiva : estiloTabActiva);
            btnTabArbol.setStyle(arbolVisible ? estiloTabActiva : estiloTabInactiva);
        };

        pantallaArbol.visibleProperty().addListener((obs, oldV, newV) -> sincronizarTabs.run());

        btnTabSimulador.setOnAction(e -> pantallaArbol.ocultar());
        btnTabArbol.setOnAction(e -> pantallaArbol.mostrar());

        navTabs.getChildren().addAll(btnTabSimulador, btnTabArbol);

        // Barra Superior: Navegación Izq, Recursos Der
        HBox topBar = new HBox(navTabs, EspaciadorUI.crearHorizontal(), hudRecursos);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPickOnBounds(false);
        overlayUI.setTop(topBar);

        // Conectar el comando de atajo P y selección de hotbar a la pantalla del árbol
        controladorTeclado.registrarComando(KeyCode.P, new ComandoAbrirProgresion(simulacion, pantallaArbol::alternar));
        hotbar.setAccionAbrirArbol(pantallaArbol::mostrar);

        ComandoCancelar comandoCancelar = new ComandoCancelar(simulacion, hotbar, controladorMouse);
        comandoCancelar.setAccionExtra(() -> {
            if (pantallaArbol.isVisible()) {
                pantallaArbol.ocultar();
            }
        });
        controladorTeclado.registrarComando(KeyCode.ESCAPE, comandoCancelar);

        // Panel lateral izquierdo para notificaciones
        overlayUI.setLeft(panelNotificaciones);

        // Barra Inferior: Control Tiempo fijado en esquina inferior izquierda, Hotbar centrada
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

        // Espaciador derecho invisible para balancear horizontalmente el centro de la hotbar
        Region balanceadorDerecho = new Region();
        balanceadorDerecho.setPickOnBounds(false);
        balanceadorDerecho.setMouseTransparent(true);
        balanceadorDerecho.minWidthProperty().bind(controlTiempo.widthProperty());
        balanceadorDerecho.prefWidthProperty().bind(controlTiempo.widthProperty());
        bottomBar.setRight(balanceadorDerecho);

        overlayUI.setBottom(bottomBar);

        // 6.5 Pantalla de Game Over
        PantallaGameOver pantallaGameOver = new PantallaGameOver(simulacion);
        simulacion.setOnGameOver(pantallaGameOver::mostrar);

        StackPane root = new StackPane(canvasJuego, overlayUI, pantallaArbol, pantallaGameOver);
        root.setStyle("-fx-background-color: #0e1017;");

        // Vincular el controlador de teclado al root pane
        root.setOnKeyPressed(controladorTeclado);

        // 7. Sincronización de callbacks UI encapsulada (Observer desacoplado)
        simulacion.agregarListenerUI(new SincronizadorUI(hudRecursos, controlTiempo));

        // 8. Bucle principal de animación (Ticks fijos + Render desacoplado)
        bucleJuego = new BucleJuego(simulacion, renderizador, canvasJuego, controladorMouse);
        bucleJuego.iniciar();

        // 9. Mostrar ventana maximizada aprovechando toda la resolución de pantalla
        Scene scene = new Scene(root, anchoInicial, altoInicial);
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

    public SimulacionSolar getSimulacion() {
        return simulacion;
    }

    public ControladorTeclado getControladorTeclado() {
        return controladorTeclado;
    }

    public ControladorMouse getControladorMouse() {
        return controladorMouse;
    }

    public BucleJuego getBucleJuego() {
        return bucleJuego;
    }

    public RenderizadorPixelArt getRenderizador() {
        return renderizador;
    }

    public static void main(String[] args) {
        launch(args);
    }
}