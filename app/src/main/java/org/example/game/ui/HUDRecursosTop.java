package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.Planeta;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Contador de recursos (arriba-derecha) estilo RimWorld UI.
 * SRP: Muestra readouts compactos de los recursos del jugador y el estado general.
 */
public class HUDRecursosTop extends HBox {

    private final SimulacionSolar simulacion;
    private final InventarioJugador inventario;

    private final Label lblMasa = new Label("⭐ 0");
    private final Label lblMateria = new Label("🪨 0");
    private final Label lblMinerales = new Label("⛏ 0");
    private final Label lblEnergia = new Label("⚡ 0");
    private final Label lblCiencia = new Label("🔬 0");
    private final Label lblPop = new Label("👥 0");

    public HUDRecursosTop(SimulacionSolar simulacion) {
        this.simulacion = simulacion;
        this.inventario = simulacion.getInventario();

        setAlignment(Pos.CENTER_RIGHT);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(12);
        setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 2px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        configurarItem(lblMasa, "#ffd700");
        configurarItem(lblMateria, "#63b3ed");
        configurarItem(lblMinerales, "#ffb86c");
        configurarItem(lblEnergia, "#5be3ff");
        configurarItem(lblCiencia, "#ff79c6");
        configurarItem(lblPop, "#50fa7b");

        getChildren().addAll(lblMasa, lblMateria, lblMinerales, lblEnergia, lblCiencia, lblPop);
        actualizar();
    }

    private void configurarItem(Label lbl, String colorHex) {
        lbl.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(colorHex));
    }

    public void actualizar() {
        lblMasa.setText(String.format("⭐ %.0f", inventario.getRecurso(TipoRecurso.MASA_ESTELAR)));
        lblMateria.setText(String.format("🪨 %.0f", inventario.getRecurso(TipoRecurso.MATERIA_PLANETARIA)));
        lblMinerales.setText(String.format("⛏ %.0f", inventario.getRecurso(TipoRecurso.MINERALES)));
        lblEnergia.setText(String.format("⚡ %.0f", inventario.getRecurso(TipoRecurso.ENERGIA)));
        lblCiencia.setText(String.format("🔬 %.0f", inventario.getRecurso(TipoRecurso.CIENCIA)));

        long popTotal = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Planeta)
                .map(c -> (Planeta) c)
                .filter(Planeta::tieneCivilizacion)
                .mapToLong(p -> (long) p.getCivilizacion().getPoblacion())
                .sum();

        lblPop.setText("👥 " + formatPop(popTotal));
    }

    private String formatPop(long n) {
        if (n >= 1_000_000_000) return String.format("%.1fB", n / 1e9);
        if (n >= 1_000_000) return String.format("%.1fM", n / 1e6);
        if (n >= 1_000) return String.format("%.1fK", n / 1e3);
        return String.valueOf(n);
    }
}
