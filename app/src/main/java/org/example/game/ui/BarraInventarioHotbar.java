package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;

import java.util.ArrayList;
import java.util.List;

/**
 * Barra de inventario inferior (Hotbar) estilo RimWorld.
 * SRP: Gestiona la selección rápida de los 8 tipos de cuerpos y el factor de masa.
 */
public class BarraInventarioHotbar extends HBox {

    private final SimulacionSolar simulacion;
    private final List<VBox> slotCards = new ArrayList<>();
    private final Label lblMasaVal = new Label("1.0x");
    private TipoCuerpo tipoSeleccionado = null;

    private static final TipoCuerpo[] CUERPOS_HOTBAR = {
            TipoCuerpo.ESTRELLA,
            TipoCuerpo.PLANETA_ROCOSO,
            TipoCuerpo.PLANETA_GASEOSO,
            TipoCuerpo.LUNA,
            TipoCuerpo.SATELITE,
            TipoCuerpo.ESCUDO_DOME,
            TipoCuerpo.METEORITO,
            TipoCuerpo.AGUJERO_NEGRO
    };

    private static final String[] ICONOS_HOTBAR = {
            "⭐", "🪨", "🪐", "🌙", "🛰️", "🛡️", "☄️", "🕳️"
    };

    public BarraInventarioHotbar(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(8);
        setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 2px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // 1. Pestaña de control de masa a la izquierda
        VBox tabMasa = crearTabMasa();
        getChildren().add(tabMasa);

        // 2. 8 Slots de inventario
        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            TipoCuerpo tipo = CUERPOS_HOTBAR[i];
            String icono = ICONOS_HOTBAR[i];
            int hotkey = i + 1;

            VBox slot = crearSlot(tipo, icono, hotkey);
            slotCards.add(slot);
            getChildren().add(slot);
        }
    }

    private VBox crearTabMasa() {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(2, 8, 2, 8));
        box.setStyle("-fx-background-color: #2b2d3a; -fx-border-color: #000000; -fx-border-width: 1px;");

        Label lblMasaTitulo = new Label("⚖️ MASA");
        lblMasaTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblMasaTitulo.setTextFill(Color.web("#e8e4d8"));

        lblMasaVal.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lblMasaVal.setTextFill(Color.web("#5be3ff"));

        Slider slider = new Slider(0.1, 5.0, 1.0);
        slider.setPrefWidth(75);
        slider.valueProperty().addListener((obs, old, neu) -> {
            double v = neu.doubleValue();
            simulacion.setFactorMasaColocacion(v);
            lblMasaVal.setText(String.format("%.1fx", v));
        });

        box.getChildren().addAll(lblMasaTitulo, lblMasaVal, slider);
        return box;
    }

    private VBox crearSlot(TipoCuerpo tipo, String icono, int hotkey) {
        VBox slot = new VBox(1);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefSize(68, 54);
        slot.setStyle("-fx-background-color: #2b2d3a; -fx-border-color: #000000; -fx-border-width: 2px; -fx-cursor: hand;");

        Label lblKey = new Label("[" + hotkey + "]");
        lblKey.setFont(Font.font("Monospace", FontWeight.NORMAL, 9));
        lblKey.setTextFill(Color.web("#8c92a4"));

        Label lblIcon = new Label(icono);
        lblIcon.setFont(Font.font(16));

        Label lblNom = new Label(tipo.nombre);
        lblNom.setFont(Font.font("System", FontWeight.BOLD, 9));
        lblNom.setTextFill(Color.web("#e8e4d8"));

        slot.getChildren().addAll(lblKey, lblIcon, lblNom);
        Tooltip.install(slot, new Tooltip(tipo.nombre + " - Tecla " + hotkey + "\nClick para colocar con órbita estable\nArrastrar para lanzar"));

        // Hover y Click
        slot.setOnMouseEntered(e -> {
            if (tipoSeleccionado != tipo) {
                slot.setStyle("-fx-background-color: #3b3d4f; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-cursor: hand;");
            }
        });

        slot.setOnMouseExited(e -> {
            if (tipoSeleccionado != tipo) {
                slot.setStyle("-fx-background-color: #2b2d3a; -fx-border-color: #000000; -fx-border-width: 2px; -fx-cursor: hand;");
            }
        });

        slot.setOnMouseClicked(e -> seleccionarTipo(tipo));

        return slot;
    }

    public void seleccionarTipo(TipoCuerpo tipo) {
        this.tipoSeleccionado = tipo;
        simulacion.entrarModoColocacion(tipo, simulacion.getFactorMasaColocacion());
        actualizarEstilos();
    }

    public void deseleccionar() {
        this.tipoSeleccionado = null;
        actualizarEstilos();
    }

    private void actualizarEstilos() {
        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            VBox slot = slotCards.get(i);
            TipoCuerpo tipo = CUERPOS_HOTBAR[i];
            if (tipo == tipoSeleccionado) {
                slot.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-effect: dropshadow(three-pass-box, #5be3ff, 6, 0, 0, 0);");
            } else {
                slot.setStyle("-fx-background-color: #2b2d3a; -fx-border-color: #000000; -fx-border-width: 2px;");
            }
        }
    }
}
