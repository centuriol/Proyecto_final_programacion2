package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ControlTiempoWidget;
import org.example.game.ui.HUDRecursosTop;
import org.example.game.ui.MenuSuperiorIzquierdo;

import java.util.ArrayList;
import java.util.List;

/**
 * Punto de entrada principal de "Órbita" — Sandbox espacial Pixel Art (Estilo RimWorld UI).
 *
 * Principios SOLID:
 * - SRP: Inicializa la aplicación JavaFX, organiza el árbol de vistas y canaliza eventos de entrada.
 * - DIP: Delega la física a SimulacionSolar/MotorFisica y el dibujo a RenderizadorPixelArt.
 */
public class App extends Application {

    private static final int ANCHO_VENTANA = 1440;
    private static final int ALTO_VENTANA = 900;
    private static final int ANCHO_MUNDO = 1920;
    private static final int ALTO_MUNDO = 1080;

    private SimulacionSolar simulacion;
    private RenderizadorPixelArt renderizador;
    private AnimationTimer gameLoop;

    // Componentes UI
    private HUDRecursosTop hudRecursos;
    private BarraInventarioHotbar hotbar;
    private ControlTiempoWidget controlTiempo;
    private MenuSuperiorIzquierdo menuSuperior;

    // Estado interactivo de mouse
    private Vector2D dragInicioFisica = null;
    private Vector2D dragActualFisica = null;
    private List<Vector2D> trayectoriaPreview = new ArrayList<>();
    private CuerpoCeleste cuerpoSeleccionado = null;

    private long lastTickTime = 0;
    private double tickAcumulado = 0;

    @Override
    public void start(Stage stage) {
        // 1. Simulación & Motor de física permisiva
        simulacion = new SimulacionSolar(ANCHO_MUNDO, ALTO_MUNDO);

        // 2. Renderizador Pixel Art
        renderizador = new RenderizadorPixelArt(ANCHO_MUNDO, ALTO_MUNDO);

        // 3. Canvas principal de juego
        Canvas canvasJuego = new Canvas(ANCHO_MUNDO, ALTO_MUNDO);

        // 4. Componentes UI RimWorld Style
        hudRecursos = new HUDRecursosTop(simulacion);
        hotbar = new BarraInventarioHotbar(simulacion);
        controlTiempo = new ControlTiempoWidget(simulacion);
        menuSuperior = new MenuSuperiorIzquierdo(simulacion, renderizador, () -> {
            cuerpoSeleccionado = null;
            hudRecursos.actualizar();
            controlTiempo.actualizarTick();
        });

        // 5. Layout Superpuesto (HUD sobre Canvas)
        BorderPane overlayUI = new BorderPane();
        overlayUI.setPadding(new Insets(12));
        overlayUI.setPickOnBounds(false); // Permite click-through al canvas

        // Barra Superior: Menú Izq + Spacer + Recursos Der
        HBox topBar = new HBox(menuSuperior, crearSpacer(), hudRecursos);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPickOnBounds(false);
        overlayUI.setTop(topBar);

        // Barra Inferior: Control Tiempo Izq + Spacer + Hotbar Centro
        HBox bottomBar = new HBox(controlTiempo, crearSpacer(), hotbar, crearSpacer());
        bottomBar.setAlignment(Pos.BOTTOM_CENTER);
        bottomBar.setPickOnBounds(false);
        overlayUI.setBottom(bottomBar);

        StackPane root = new StackPane(canvasJuego, overlayUI);
        root.setStyle("-fx-background-color: #0e1017;");

        // 6. Configurar eventos de mouse y teclado
        configurarEventos(root, canvasJuego);

        // 7. Sincronización de callbacks
        simulacion.setOnTickCallback(() -> {
            hudRecursos.actualizar();
            controlTiempo.actualizarTick();
        });

        // 8. Bucle principal de animación (Ticks fijos + Render variable)
        iniciarGameLoop(canvasJuego);

        // 9. Mostrar ventana
        Scene scene = new Scene(root, ANCHO_VENTANA, ALTO_VENTANA);
        scene.setFill(Color.web("#0e1017"));

        stage.setScene(scene);
        stage.setTitle("Órbita — Sistema Solar Pixel Art Sandbox");
        stage.show();

        root.requestFocus();
    }

    private HBox crearSpacer() {
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMouseTransparent(true);
        return spacer;
    }

    private void configurarEventos(StackPane root, Canvas canvas) {
        // --- TECLADO ---
        root.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case SPACE -> {
                    simulacion.togglePause();
                    controlTiempo.actualizarSeleccion();
                }
                case S -> {
                    simulacion.step();
                    controlTiempo.actualizarTick();
                }
                case ESCAPE -> {
                    simulacion.salirModoColocacion();
                    hotbar.deseleccionar();
                    cuerpoSeleccionado = null;
                }
                case DIGIT1 -> hotbar.seleccionarTipo(TipoCuerpo.ESTRELLA);
                case DIGIT2 -> hotbar.seleccionarTipo(TipoCuerpo.PLANETA_ROCOSO);
                case DIGIT3 -> hotbar.seleccionarTipo(TipoCuerpo.PLANETA_GASEOSO);
                case DIGIT4 -> hotbar.seleccionarTipo(TipoCuerpo.LUNA);
                case DIGIT5 -> hotbar.seleccionarTipo(TipoCuerpo.SATELITE);
                case DIGIT6 -> hotbar.seleccionarTipo(TipoCuerpo.ESCUDO_DOME);
                case DIGIT7 -> hotbar.seleccionarTipo(TipoCuerpo.METEORITO);
                case DIGIT8 -> hotbar.seleccionarTipo(TipoCuerpo.AGUJERO_NEGRO);
                case G -> renderizador.toggleGrilla();
                case T -> renderizador.toggleEstelas();
                case H -> renderizador.toggleZonasHabitables();
                case PLUS, EQUALS -> simulacion.setFactorMasaColocacion(simulacion.getFactorMasaColocacion() * 1.15);
                case MINUS -> simulacion.setFactorMasaColocacion(simulacion.getFactorMasaColocacion() / 1.15);
                default -> {}
            }
        });

        // --- MOUSE MOVED: Actualiza preview y calcula órbita sugerida ---
        canvas.setOnMouseMoved(e -> {
            if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
                simulacion.actualizarPosicionPreview(e.getX(), e.getY());
                actualizarPrediccionTrayectoriaAuto();
            }
        });

        // --- MOUSE PRESSED: Inicia colocación o Slingshot Drag ---
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
                    double xFisica = ConstantesFisicas.javaFXAFisicaX(e.getX(), ANCHO_MUNDO);
                    double yFisica = ConstantesFisicas.javaFXAFisica(e.getY(), ALTO_MUNDO);
                    dragInicioFisica = new Vector2D(xFisica, yFisica);
                    dragActualFisica = new Vector2D(xFisica, yFisica);
                } else {
                    // Seleccionar cuerpo bajo el cursor
                    seleccionarCuerpoBajoCursor(e.getX(), e.getY());
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                // Click derecho cancela colocación o deselecciona
                simulacion.salirModoColocacion();
                hotbar.deseleccionar();
                cuerpoSeleccionado = null;
                dragInicioFisica = null;
                dragActualFisica = null;
                trayectoriaPreview.clear();
            }
        });

        // --- MOUSE DRAGGED: Arrastre para vector de impulso (Slingshot) ---
        canvas.setOnMouseDragged(e -> {
            if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO && dragInicioFisica != null) {
                double xFisica = ConstantesFisicas.javaFXAFisicaX(e.getX(), ANCHO_MUNDO);
                double yFisica = ConstantesFisicas.javaFXAFisica(e.getY(), ALTO_MUNDO);
                dragActualFisica = new Vector2D(xFisica, yFisica);

                // Calcular vector impulso hacia adelante
                Vector2D delta = dragInicioFisica.restar(dragActualFisica);
                Vector2D velImpulso = delta.multiplicar(1.8);

                // Predecir trayectoria del lanzamiento
                TipoCuerpo tipo = simulacion.getTipoColocacion();
                double masa = tipo != null ? tipo.masaBase * simulacion.getFactorMasaColocacion() : 1e24;

                trayectoriaPreview = simulacion.getPredictor().predecirTrayectoria(
                        dragInicioFisica, velImpulso, masa,
                        simulacion.getSistemaSolar().getCuerpos(), 80, 1.0
                );
            }
        });

        // --- MOUSE RELEASED: Confirma colocación con Slingshot o Auto-Órbita ---
        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY && simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
                if (dragInicioFisica != null) {
                    double distArrastre = dragInicioFisica.distanciaA(dragActualFisica != null ? dragActualFisica : dragInicioFisica);

                    if (distArrastre > 12.0) {
                        // Lanzamiento por Slingshot (impulso manual)
                        Vector2D delta = dragInicioFisica.restar(dragActualFisica);
                        Vector2D velImpulso = delta.multiplicar(1.8);
                        simulacion.confirmarColocacionConImpulso(dragInicioFisica, velImpulso);
                    } else {
                        // Colocación asistida con órbita circular estable
                        simulacion.confirmarColocacionAutoOrbita();
                    }
                }

                dragInicioFisica = null;
                dragActualFisica = null;
                trayectoriaPreview.clear();
                hotbar.deseleccionar();
            }
        });

        // --- SCROLL: Ajuste de masa ---
        canvas.setOnScroll(e -> {
            double delta = e.getDeltaY() > 0 ? 1.15 : 0.87;
            simulacion.setFactorMasaColocacion(simulacion.getFactorMasaColocacion() * delta);
            e.consume();
        });
    }

    private void actualizarPrediccionTrayectoriaAuto() {
        Vector2D pos = simulacion.getPosicionPreview();
        if (pos == null) return;

        Vector2D velOrbital = simulacion.calcularVelocidadOrbitalAsistida(pos);
        TipoCuerpo tipo = simulacion.getTipoColocacion();
        double masa = tipo != null ? tipo.masaBase * simulacion.getFactorMasaColocacion() : 1e24;

        trayectoriaPreview = simulacion.getPredictor().predecirTrayectoria(
                pos, velOrbital, masa,
                simulacion.getSistemaSolar().getCuerpos(), 75, 1.0
        );
    }

    private void seleccionarCuerpoBajoCursor(double mouseX, double mouseY) {
        double xFisica = ConstantesFisicas.javaFXAFisicaX(mouseX, ANCHO_MUNDO);
        double yFisica = ConstantesFisicas.javaFXAFisica(mouseY, ALTO_MUNDO);
        Vector2D clickPos = new Vector2D(xFisica, yFisica);

        CuerpoCeleste masCercano = null;
        double distMin = 35.0; // Umbral de selección en px

        for (CuerpoCeleste c : simulacion.getSistemaSolar().getCuerpos()) {
            double d = clickPos.distanciaA(c.getPosicion());
            if (d < distMin) {
                distMin = d;
                masCercano = c;
            }
        }
        this.cuerpoSeleccionado = masCercano;
    }

    private void iniciarGameLoop(Canvas canvas) {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTickTime == 0) {
                    lastTickTime = now;
                    return;
                }

                double deltaTime = (now - lastTickTime) / 1_000_000_000.0;
                lastTickTime = now;

                // Ticks fijos a 20 TPS base * multiplicador de velocidad
                double ticksPorSegundo = 20.0 * simulacion.getVelocidadSimulacion();
                double tiempoPorTick = 1.0 / ticksPorSegundo;

                tickAcumulado += deltaTime;
                int ticksAEjecutar = (int) (tickAcumulado / tiempoPorTick);

                if (ticksAEjecutar > 0) {
                    tickAcumulado -= ticksAEjecutar * tiempoPorTick;
                    ticksAEjecutar = Math.min(ticksAEjecutar, 8); // Evitar espiral de retraso
                    for (int i = 0; i < ticksAEjecutar; i++) {
                        simulacion.avanzarTick();
                    }
                }

                // Renderizar frame completo en Pixel Art
                renderizador.renderizarTodo(
                        canvas.getGraphicsContext2D(),
                        simulacion.getSistemaSolar().getCuerpos(),
                        simulacion.getTickActual(),
                        simulacion.getPosicionPreview(),
                        simulacion.getTipoColocacion(),
                        simulacion.getFactorMasaColocacion(),
                        dragInicioFisica,
                        trayectoriaPreview,
                        cuerpoSeleccionado
                );
            }
        };
        gameLoop.start();
    }

    @Override
    public void stop() throws Exception {
        if (gameLoop != null) gameLoop.stop();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}