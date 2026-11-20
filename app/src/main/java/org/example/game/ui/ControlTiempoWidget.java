package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Widget compacto de control de tiempo (pausa y velocidades 1x, 2x, 3x y 4x) (SRP).
 */
public class ControlTiempoWidget extends HBox {

    private static final Background BG_WIDGET = new Background(new BackgroundFill(Color.web("#1a1c26"), new CornerRadii(4), Insets.EMPTY));
    private static final Border BORDER_WIDGET = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1.5)));
    private static final Background BG_BTN_NORMAL = new Background(new BackgroundFill(Color.web("#2b2d3a"), new CornerRadii(3), Insets.EMPTY));
    private static final Background BG_BTN_ACTIVE = new Background(new BackgroundFill(Color.web("#1a1c26"), new CornerRadii(3), Insets.EMPTY));
    private static final Background BG_BTN_HOVER = new Background(new BackgroundFill(Color.web("#3b3d4f"), new CornerRadii(3), Insets.EMPTY));
    private static final Border BORDER_BTN_NORMAL = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1)));
    private static final Border BORDER_BTN_ACTIVE = new Border(new BorderStroke(Color.web("#5be3ff"), BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1.5)));
    private static final Border BORDER_BTN_HOVER = new Border(new BorderStroke(Color.web("#5be3ff"), BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1)));

    private static class EstadoBotonesTiempo {
        final Button btnPause = new Button("||");
        final Button btn1x = new Button("1x");
        final Button btn2x = new Button("2x");
        final Button btn3x = new Button("3x");
        final Button btn4x = new Button("4x");
        Boolean ultimaPausa = null;
        Double ultimaVelocidadEstilo = null;

        boolean cambioEstado(boolean pausa, double velocidad) {
            return ultimaPausa == null || ultimaVelocidadEstilo == null
                    || pausa != ultimaPausa || Math.abs(velocidad - ultimaVelocidadEstilo) > 0.001;
        }

        boolean esActivo(Button btn, boolean pausa, double v) {
            if (btn == btnPause) return pausa;
            if (pausa) return false;
            if (btn == btn1x) return Math.abs(v - 1.0) < 0.2;
            if (btn == btn2x) return Math.abs(v - 2.0) < 0.2;
            if (btn == btn3x) return Math.abs(v - 3.0) < 0.2;
            return btn == btn4x && v >= 3.8;
        }
    }

    private final SimulacionSolar simulacion;
    private final EstadoBotonesTiempo botones = new EstadoBotonesTiempo();

    public ControlTiempoWidget(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(3, 6, 3, 6));
        setSpacing(3);
        setBackground(BG_WIDGET);
        setBorder(BORDER_WIDGET);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        configurarBoton(botones.btnPause, "Pausar / Reanudar (Espacio)", () -> {
            simulacion.getReloj().togglePause();
            actualizarSeleccion();
        });
        configurarBotonVelocidad(botones.btn1x, 1.0);
        configurarBotonVelocidad(botones.btn2x, 2.0);
        configurarBotonVelocidad(botones.btn3x, 3.0);
        configurarBotonVelocidad(botones.btn4x, 4.0);

        getChildren().addAll(botones.btnPause, botones.btn1x, botones.btn2x, botones.btn3x, botones.btn4x);
        actualizarSeleccion();
    }

    private void configurarBotonVelocidad(Button btn, double velocidad) {
        configurarBoton(btn, "Velocidad " + (int) velocidad + "x", () -> {
            simulacion.getReloj().play();
            simulacion.getReloj().setVelocidadSimulacion(velocidad);
            actualizarSeleccion();
        });
    }

    private void configurarBoton(Button btn, String tooltip, Runnable accion) {
        btn.setPrefSize(28, 22);
        btn.setMinSize(28, 22);
        btn.setMaxSize(28, 22);
        btn.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        btn.setCursor(Cursor.HAND);
        btn.setPadding(new Insets(2, 4, 2, 4));
        btn.setOnMouseEntered(e -> {
            if (!esBotonActivo(btn)) {
                btn.setBackground(BG_BTN_HOVER);
                btn.setBorder(BORDER_BTN_HOVER);
            }
        });
        btn.setOnMouseExited(e -> aplicarEstiloBoton(btn, esBotonActivo(btn)));
        btn.setOnAction(e -> accion.run());
    }

    public void actualizarTick() {
        if (botones.cambioEstado(simulacion.getReloj().isEnPausa(), simulacion.getReloj().getVelocidadSimulacion())) {
            actualizarSeleccion();
        }
    }

    public void actualizarSeleccion() {
        boolean pausa = simulacion.getReloj().isEnPausa();
        double v = simulacion.getReloj().getVelocidadSimulacion();
        botones.ultimaPausa = pausa;
        botones.ultimaVelocidadEstilo = v;

        botones.btnPause.setText(pausa ? ">" : "||");
        botones.btnPause.setTooltip(new Tooltip(pausa ? "Reanudar Simulación (Espacio)" : "Pausar Simulación (Espacio)"));

        aplicarEstiloBoton(botones.btnPause, pausa);
        aplicarEstiloBoton(botones.btn1x, !pausa && Math.abs(v - 1.0) < 0.2);
        aplicarEstiloBoton(botones.btn2x, !pausa && Math.abs(v - 2.0) < 0.2);
        aplicarEstiloBoton(botones.btn3x, !pausa && Math.abs(v - 3.0) < 0.2);
        aplicarEstiloBoton(botones.btn4x, !pausa && v >= 3.8);
    }

    private boolean esBotonActivo(Button btn) {
        return botones.esActivo(btn, simulacion.getReloj().isEnPausa(), simulacion.getReloj().getVelocidadSimulacion());
    }

    private void aplicarEstiloBoton(Button btn, boolean activo) {
        btn.setBackground(activo ? BG_BTN_ACTIVE : BG_BTN_NORMAL);
        btn.setBorder(activo ? BORDER_BTN_ACTIVE : BORDER_BTN_NORMAL);
        btn.setTextFill(activo ? Color.web("#5be3ff") : Color.web("#e8e4d8"));
    }

    public Button getBtnPause() { return botones.btnPause; }
    public Button getBtn1x() { return botones.btn1x; }
    public Button getBtn2x() { return botones.btn2x; }
    public Button getBtn3x() { return botones.btn3x; }
    public Button getBtn4x() { return botones.btn4x; }
}
