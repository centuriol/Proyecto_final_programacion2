package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Widget de control de tiempo (abajo-izquierda) estilo RimWorld UI.
 * 4 botones cuadrados (pause / 1x / 2x / 4x), selected = accent outline #5be3ff, others dim.
 */
public class ControlTiempoWidget extends VBox {

    private final SimulacionSolar simulacion;
    private final Button btnPause = new Button("||");
    private final Button btn1x = new Button("1x");
    private final Button btn2x = new Button("2x");
    private final Button btn4x = new Button("4x");
    private final Button btnStep = new Button(">|");
    private final Label lblTick = new Label("Tick: 0");

    private double velocidadActual = 1.0;

    public ControlTiempoWidget(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(6, 10, 6, 10));
        setSpacing(4);
        setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 2px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        lblTick.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblTick.setTextFill(Color.web("#5be3ff"));

        HBox btnBox = new HBox(4);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        configurarBoton(btnPause, "Pausa (Espacio)", () -> {
            simulacion.pause();
            actualizarSeleccion();
        });

        configurarBoton(btn1x, "Velocidad Normal 1x", () -> {
            simulacion.play();
            simulacion.setVelocidadSimulacion(1.0);
            this.velocidadActual = 1.0;
            actualizarSeleccion();
        });

        configurarBoton(btn2x, "Velocidad Rápida 2x", () -> {
            simulacion.play();
            simulacion.setVelocidadSimulacion(2.0);
            this.velocidadActual = 2.0;
            actualizarSeleccion();
        });

        configurarBoton(btn4x, "Velocidad Ultra 4x", () -> {
            simulacion.play();
            simulacion.setVelocidadSimulacion(4.0);
            this.velocidadActual = 4.0;
            actualizarSeleccion();
        });

        configurarBoton(btnStep, "Avanzar 1 Paso (S)", () -> {
            simulacion.step();
            actualizarSeleccion();
        });

        btnBox.getChildren().addAll(btnPause, btn1x, btn2x, btn4x, btnStep);
        getChildren().addAll(lblTick, btnBox);

        actualizarSeleccion();
    }

    private void configurarBoton(Button btn, String tooltip, Runnable accion) {
        btn.setPrefSize(32, 28);
        btn.setFont(Font.font("System", FontWeight.BOLD, 11));
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        btn.setOnAction(e -> accion.run());
    }

    public void actualizarTick() {
        lblTick.setText("Tick: " + simulacion.getTickActual() + (simulacion.isEnPausa() ? " [PAUSA]" : ""));
        actualizarSeleccion();
    }

    public void actualizarSeleccion() {
        boolean pausa = simulacion.isEnPausa();
        double v = simulacion.getVelocidadSimulacion();

        aplicarEstiloBoton(btnPause, pausa);
        aplicarEstiloBoton(btn1x, !pausa && Math.abs(v - 1.0) < 0.2);
        aplicarEstiloBoton(btn2x, !pausa && Math.abs(v - 2.0) < 0.2);
        aplicarEstiloBoton(btn4x, !pausa && v >= 3.5);
        aplicarEstiloBoton(btnStep, false);
    }

    private void aplicarEstiloBoton(Button btn, boolean activo) {
        if (activo) {
            btn.setStyle("-fx-background-color: #1a1c26; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #000000; -fx-border-width: 1px; -fx-cursor: hand;");
        }
    }
}
