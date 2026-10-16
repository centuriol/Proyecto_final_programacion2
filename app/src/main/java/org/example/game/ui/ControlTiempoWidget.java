package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Widget compacto de control de tiempo (esquina inferior izquierda).
 * Dispone exclusivamente de los controles de pausado/corriendo y velocidades 1x, 2x, 3x y 4x.
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

    private final SimulacionSolar simulacion;
    private final Button btnPause = new Button("||");
    private final Button btn1x = new Button("1x");
    private final Button btn2x = new Button("2x");
    private final Button btn3x = new Button("3x");
    private final Button btn4x = new Button("4x");

    private Boolean ultimaPausa = null;
    private Double ultimaVelocidadEstilo = null;

    public ControlTiempoWidget(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(3, 6, 3, 6));
        setSpacing(3);
        setBackground(BG_WIDGET);
        setBorder(BORDER_WIDGET);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        configurarBoton(btnPause, "Pausar / Reanudar (Espacio)", () -> {
            simulacion.togglePause();
            actualizarSeleccion();
        });

        configurarBoton(btn1x, "Velocidad 1x", () -> {
            simulacion.play();
            simulacion.setVelocidadSimulacion(1.0);
            actualizarSeleccion();
        });

        configurarBoton(btn2x, "Velocidad 2x", () -> {
            simulacion.play();
            simulacion.setVelocidadSimulacion(2.0);
            actualizarSeleccion();
        });

        configurarBoton(btn3x, "Velocidad 3x", () -> {
            simulacion.play();
            simulacion.setVelocidadSimulacion(3.0);
            actualizarSeleccion();
        });

        configurarBoton(btn4x, "Velocidad 4x", () -> {
            simulacion.play();
            simulacion.setVelocidadSimulacion(4.0);
            actualizarSeleccion();
        });

        getChildren().addAll(btnPause, btn1x, btn2x, btn3x, btn4x);

        actualizarSeleccion();
    }

    private void configurarBoton(Button btn, String tooltip, Runnable accion) {
        btn.setPrefSize(28, 22);
        btn.setMinSize(28, 22);
        btn.setMaxSize(28, 22);
        btn.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setPadding(new Insets(2, 4, 2, 4));
        btn.setOnMouseEntered(e -> {
            if (!esBotonActivo(btn)) {
                btn.setBackground(BG_BTN_HOVER);
                btn.setBorder(BORDER_BTN_HOVER);
            }
        });
        btn.setOnMouseExited(e -> {
            aplicarEstiloBoton(btn, esBotonActivo(btn));
        });
        btn.setOnAction(e -> accion.run());
    }

    public void actualizarTick() {
        boolean pausa = simulacion.isEnPausa();
        double v = simulacion.getVelocidadSimulacion();
        if (ultimaPausa == null || ultimaVelocidadEstilo == null || pausa != ultimaPausa || Math.abs(v - ultimaVelocidadEstilo) > 0.001) {
            actualizarSeleccion();
        }
    }

    public void actualizarSeleccion() {
        boolean pausa = simulacion.isEnPausa();
        double v = simulacion.getVelocidadSimulacion();
        this.ultimaPausa = pausa;
        this.ultimaVelocidadEstilo = v;

        btnPause.setText(pausa ? ">" : "||");
        btnPause.setTooltip(new Tooltip(pausa ? "Reanudar Simulación (Espacio)" : "Pausar Simulación (Espacio)"));

        aplicarEstiloBoton(btnPause, pausa);
        aplicarEstiloBoton(btn1x, !pausa && Math.abs(v - 1.0) < 0.2);
        aplicarEstiloBoton(btn2x, !pausa && Math.abs(v - 2.0) < 0.2);
        aplicarEstiloBoton(btn3x, !pausa && Math.abs(v - 3.0) < 0.2);
        aplicarEstiloBoton(btn4x, !pausa && v >= 3.8);
    }

    private boolean esBotonActivo(Button btn) {
        boolean pausa = simulacion.isEnPausa();
        double v = simulacion.getVelocidadSimulacion();
        if (btn == btnPause) return pausa;
        if (pausa) return false;
        if (btn == btn1x) return Math.abs(v - 1.0) < 0.2;
        if (btn == btn2x) return Math.abs(v - 2.0) < 0.2;
        if (btn == btn3x) return Math.abs(v - 3.0) < 0.2;
        if (btn == btn4x) return v >= 3.8;
        return false;
    }

    private void aplicarEstiloBoton(Button btn, boolean activo) {
        if (activo) {
            btn.setBackground(BG_BTN_ACTIVE);
            btn.setBorder(BORDER_BTN_ACTIVE);
            btn.setTextFill(Color.web("#5be3ff"));
        } else {
            btn.setBackground(BG_BTN_NORMAL);
            btn.setBorder(BORDER_BTN_NORMAL);
            btn.setTextFill(Color.web("#e8e4d8"));
        }
    }

    public Button getBtnPause() {
        return btnPause;
    }

    public Button getBtn1x() {
        return btn1x;
    }

    public Button getBtn2x() {
        return btn2x;
    }

    public Button getBtn3x() {
        return btn3x;
    }

    public Button getBtn4x() {
        return btn4x;
    }
}
