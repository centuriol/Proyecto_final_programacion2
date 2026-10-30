package org.example.game.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.function.DoubleConsumer;

/**
 * Control visual reutilizable para la selección y previsualización del factor de masa.
 *
 * Principios SOLID:
 * - SRP: Encapsula la configuración del slider, etiquetas y cálculo de masa resultante.
 * - Reutilización: Empleado tanto en PanelInventario como en VentanaDescripcionCuerpo.
 */
public class WidgetFactorMasa extends VBox {

    private final Label lblValor;
    private final Slider slider;
    private double masaBase = 0.0;
    private DoubleConsumer onFactorCambiado;

    public WidgetFactorMasa(String tituloPrompt, double factorInicial, double masaBase) {
        this(tituloPrompt, factorInicial, masaBase, ConfiguracionSimulacion.MASA_FACTOR_MAX);
    }

    public WidgetFactorMasa(String tituloPrompt, double factorInicial, double masaBase, double factorMax) {
        super(4);
        this.masaBase = masaBase;

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label lblPrompt = new Label(tituloPrompt != null ? tituloPrompt : "MASA DEL CUERPO:");
        lblPrompt.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblPrompt.setTextFill(Color.web("#5be3ff"));

        this.lblValor = new Label();
        lblValor.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lblValor.setTextFill(Color.web("#ffd700"));

        headerBox.getChildren().addAll(lblPrompt, lblValor);

        double maxEfectivo = Math.max(ConfiguracionSimulacion.MASA_FACTOR_MIN, factorMax);
        double inicialClamped = Math.max(ConfiguracionSimulacion.MASA_FACTOR_MIN,
                Math.min(maxEfectivo, factorInicial));

        this.slider = new Slider(
                ConfiguracionSimulacion.MASA_FACTOR_MIN,
                maxEfectivo,
                inicialClamped
        );
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1.0);
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(0.5);

        actualizarTexto(inicialClamped);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double v = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            actualizarTexto(v);
            if (onFactorCambiado != null) {
                onFactorCambiado.accept(v);
            }
        });

        getChildren().addAll(headerBox, slider);
    }

    public void setMasaBase(double masaBase) {
        this.masaBase = masaBase;
        actualizarTexto(getFactorMasa());
    }

    public double getFactorMasa() {
        return Math.round(slider.getValue() * 10.0) / 10.0;
    }

    public void setFactorMasa(double factor) {
        slider.setValue(factor);
        actualizarTexto(factor);
    }

    public void setOnFactorCambiado(DoubleConsumer callback) {
        this.onFactorCambiado = callback;
    }

    public Slider getSlider() {
        return slider;
    }

    private void actualizarTexto(double factor) {
        if (masaBase > 0) {
            lblValor.setText(String.format("%.1fx (%s)", factor, TipoCuerpo.formatearMasa(masaBase * factor)));
        } else {
            lblValor.setText(String.format("%.1fx", factor));
        }
    }
}
