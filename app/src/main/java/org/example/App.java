package org.example;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.game.controlador.*;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.*;

/**
 * Punto de entrada principal de la aplicación JavaFX "Centuriol" (SRP / DIP).
 */
public class App extends Application {

    public static final double ANCHO_VENTANA_BASE = 1280.0;
    public static final double ALTO_VENTANA_BASE = 720.0;

    private final EnsambladorAplicacion ensamblador;

    public App() {
        this(new EnsambladorAplicacion());
    }

    public App(EnsambladorAplicacion ensamblador) {
        this.ensamblador = ensamblador != null ? ensamblador : new EnsambladorAplicacion();
    }

    @Override
    public void start(Stage stage) {
        double[] dims = obtenerDimensionesIniciales();
        StackPane root = inicializarInterfaz(stage, dims[0], dims[1], null);
        ensamblador.iniciarBucle();
        mostrarVentana(stage, root, dims[0], dims[1]);
    }

    public StackPane inicializarInterfaz(Stage stage, double ancho, double alto, GestorGuardadoPartida gestorPersonalizado) {
        return ensamblador.inicializar(stage, ancho, alto, gestorPersonalizado);
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
        ensamblador.detener();
        super.stop();
    }

    public SimulacionSolar getSimulacion() { return ensamblador.getSimulacion(); }
    public ControladorTeclado getControladorTeclado() { return ensamblador.getControladorTeclado(); }
    public ControladorMouse getControladorMouse() { return ensamblador.getControladorMouse(); }
    public BucleJuego getBucleJuego() { return ensamblador.getBucleJuego(); }
    public RenderizadorPixelArt getRenderizador() { return ensamblador.getRenderizador(); }
    public GestorGuardadoPartida getGestorGuardado() { return ensamblador.getGestorGuardado(); }
    public CoordinadorSesionJuego getCoordinadorSesion() { return ensamblador.getCoordinadorSesion(); }
    public PantallaMenuPrincipal getPantallaMenuPrincipal() { return ensamblador.getPantallaMenuPrincipal(); }
    public PantallaMenuPausa getPantallaMenuPausa() { return ensamblador.getPantallaMenuPausa(); }
    public PantallaArbolProgresion getPantallaArbol() { return ensamblador.getPantallaArbol(); }
    public PantallaGameOver getPantallaGameOver() { return ensamblador.getPantallaGameOver(); }
    public CapaIndicadoresSol getCapaIndicadoresSol() { return ensamblador.getCapaIndicadoresSol(); }
    public HBox getTopBar() { return ensamblador.getTopBar(); }
    public BorderPane getBottomBar() { return ensamblador.getBottomBar(); }
    public Button getBtnArbolInferior() { return ensamblador.getBtnArbolInferior(); }

    public static void main(String[] args) {
        launch(args);
    }
}
