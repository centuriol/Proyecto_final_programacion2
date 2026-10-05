package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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

        // 3. Canvas principal de juego redimensionable (ocupa el 100% de la ventana sin recortes ni zoom)
        ResizableCanvas canvasJuego = new ResizableCanvas(anchoInicial, altoInicial);

        // 4. Componentes UI
        hudRecursos = new HUDRecursosTop(simulacion);
        hotbar = new BarraInventarioHotbar(simulacion);
        controlTiempo = new ControlTiempoWidget(simulacion);
        panelNotificaciones = new PanelNotificaciones(simulacion);

        // 5. Controladores de entrada encapsulados según POO y SOLID
        controladorMouse = new ControladorMouse(simulacion, hotbar, stage, anchoInicial, altoInicial);
        controladorMouse.conectar(canvasJuego);

        controladorTeclado = new ControladorTeclado();
        controladorTeclado.configurarAtajosPorDefecto(
                simulacion, controlTiempo, hotbar, renderizador, controladorMouse
        );

        // Sincronizar dimensiones cuando el canvas cambie de tamaño
        canvasJuego.widthProperty().addListener((obs, oldV, newV) -> {
            double w = newV.doubleValue();
            double h = canvasJuego.getHeight();
            if (w > 0 && h > 0) {
                renderizador.setDimensiones(w, h);
                simulacion.setDimensionesMundo(w, h);
                controladorMouse.setDimensiones(w, h);
            }
        });
        canvasJuego.heightProperty().addListener((obs, oldV, newV) -> {
            double w = canvasJuego.getWidth();
            double h = newV.doubleValue();
            if (w > 0 && h > 0) {
                renderizador.setDimensiones(w, h);
                simulacion.setDimensionesMundo(w, h);
                controladorMouse.setDimensiones(w, h);
            }
        });

        // 6. Layout Superpuesto (HUD sobre Canvas)
        BorderPane overlayUI = new BorderPane();
        overlayUI.setPadding(new Insets(12));
        overlayUI.setPickOnBounds(false); // Permite click-through al canvas

        // Barra Superior: Recursos Der
        HBox topBar = new HBox(EspaciadorUI.crearHorizontal(), hudRecursos);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPickOnBounds(false);
        overlayUI.setTop(topBar);

        // Panel lateral izquierdo para notificaciones
        overlayUI.setLeft(panelNotificaciones);

        // Barra Inferior: Control Tiempo (Ticks y velocidades) + Hotbar de Ítems (Todo centrado)
        HBox bottomBar = new HBox(10, controlTiempo, hotbar);
        bottomBar.setAlignment(Pos.BOTTOM_CENTER);
        bottomBar.setPickOnBounds(false);
        overlayUI.setBottom(bottomBar);

        StackPane root = new StackPane(canvasJuego, overlayUI);
        root.setStyle("-fx-background-color: #0e1017;");

        // Vincular el controlador de teclado al root pane
        root.setOnKeyPressed(controladorTeclado);

        // 7. Sincronización de callbacks UI encapsulada
        simulacion.setOnTickCallback(new SincronizadorUI(hudRecursos, controlTiempo));

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