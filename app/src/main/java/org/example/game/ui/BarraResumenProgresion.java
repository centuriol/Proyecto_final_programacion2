package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.SimulacionSolar;

/**
 * SRP: Barra reutilizable de indicadores resumidos de ciencia, capacidad de planetas y límite de masa.
 */
public class BarraResumenProgresion extends HBox {

    private final Label lblCienciaPill = new Label("Ciencia: 0.0 CIE");
    private final Label lblPlanetasPill = new Label("Planetas: 0 / 3");
    private final Label lblMasaPill = new Label("Masa Máx: 0.3x");
    private final boolean formatoCompacto;

    public BarraResumenProgresion(boolean formatoCompacto) {
        super(formatoCompacto ? 8 : 12);
        this.formatoCompacto = formatoCompacto;
        setAlignment(formatoCompacto ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        double fontSize = formatoCompacto ? 10.5 : 12.0;
        Insets padding = formatoCompacto ? new Insets(4, 9, 4, 9) : new Insets(4, 10, 4, 10);
        configurarPill(lblCienciaPill, "#172a38", "#5be3ff", fontSize, padding);
        configurarPill(lblPlanetasPill, "#2b2617", "#ffd700", fontSize, padding);
        configurarPill(lblMasaPill, "#261a38", "#d2a8ff", fontSize, padding);

        getChildren().addAll(lblCienciaPill, lblPlanetasPill, lblMasaPill);
    }

    private void configurarPill(Label label, String fondo, String colorBorde, double fontSize, Insets padding) {
        label.setFont(Font.font("Monospace", FontWeight.BOLD, fontSize));
        label.setPadding(padding);
        label.setStyle("-fx-background-color: " + fondo + "; -fx-text-fill: " + colorBorde
                + "; -fx-border-color: " + colorBorde + "; -fx-border-width: 1px; -fx-background-radius: 4px;");
    }

    public double actualizar(SimulacionSolar simulacion) {
        var inv = simulacion.getInventario();
        var prog = simulacion.getProgresion();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;
        lblCienciaPill.setText(String.format("Ciencia: %.1f CIE", ciencia));
        lblPlanetasPill.setText(String.format("Planetas: %d / %d",
                simulacion.getGestorCicloVida().getCantidadPlanetasActuales(), prog.getLimitePlanetas()));
        String prefijoMasa = formatoCompacto ? "Masa Máx: " : "Limite Masa: ";
        lblMasaPill.setText(prefijoMasa + String.format("%.1fx", prog.getLimiteMasaActual()));
        return ciencia;
    }
}
