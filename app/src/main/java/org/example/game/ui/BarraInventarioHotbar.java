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
import org.example.game.simulacion.ConfiguracionSimulacion;
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
    private TipoCuerpo tipoSeleccionado = null;

    private static final TipoCuerpo[] CUERPOS_HOTBAR = {
            TipoCuerpo.ESTRELLA,
            TipoCuerpo.PLANETA_ROCOSO,
            TipoCuerpo.LUNA
    };

    private static final String[] ICONOS_HOTBAR = {
            "[EST]", "[ROC]", "[LUN]"
    };

    public BarraInventarioHotbar(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(8);
        setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 2px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // Slots de inventario
        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            TipoCuerpo tipo = CUERPOS_HOTBAR[i];
            String icono = ICONOS_HOTBAR[i];
            int hotkey = i + 1;

            VBox slot = crearSlot(tipo, icono, hotkey);
            slotCards.add(slot);
            getChildren().add(slot);
        }
    }

    private VBox crearSlot(TipoCuerpo tipo, String icono, int hotkey) {
        VBox slot = new VBox(2);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefSize(145, 78);
        slot.setPadding(new Insets(3, 6, 3, 6));
        slot.setStyle("-fx-background-color: #2b2d3a; -fx-border-color: #000000; -fx-border-width: 2px; -fx-cursor: hand;");

        // Cabecera: [1] ESTRELLA
        HBox header = new HBox(4);
        header.setAlignment(Pos.CENTER);

        Label lblKey = new Label("[" + hotkey + "]");
        lblKey.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        lblKey.setTextFill(Color.web("#8c92a4"));

        Label lblNom = new Label(tipo.nombre.toUpperCase());
        lblNom.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblNom.setTextFill(Color.web(tipo.getColorHexString()));

        header.getChildren().addAll(lblKey, lblNom);

        // Necesitas (Costo para colocarlo)
        String textoCosto = formatearResumenCosto(tipo);
        Label lblCosto = new Label(textoCosto);
        lblCosto.setFont(Font.font("System", FontWeight.NORMAL, 9));
        lblCosto.setTextFill(Color.web("#ffd700"));

        // Da por tick (Producción)
        String textoProd = formatearResumenProduccion(tipo);
        Label lblProd = new Label(textoProd);
        lblProd.setFont(Font.font("System", FontWeight.NORMAL, 9));
        lblProd.setTextFill(Color.web("#50fa7b"));

        // Hint click
        Label lblInfo = new Label("[Click: Configurar]");
        lblInfo.setFont(Font.font("Monospace", FontWeight.NORMAL, 8));
        lblInfo.setTextFill(Color.web("#718096"));

        slot.getChildren().addAll(header, lblCosto, lblProd, lblInfo);

        // Sin tooltip al pasar el cursor (solo se abre la descripción al hacer click)

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

        slot.setOnMouseClicked(e -> {
            abrirVentanaColocacion(tipo);
        });

        return slot;
    }

    public static String formatearResumenCosto(TipoCuerpo tipo) {
        java.util.Map<org.example.game.jugador.TipoRecurso, Double> costos = org.example.game.jugador.InventarioJugador.getCostosBase(tipo);
        if (costos.isEmpty()) return "Costo: Gratis";
        StringBuilder sb = new StringBuilder("Requiere: ");
        int i = 0;
        for (var e : costos.entrySet()) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.0f ", e.getValue())).append(e.getKey().nombre.substring(0, Math.min(3, e.getKey().nombre.length())));
            i++;
        }
        return sb.toString();
    }

    public static String formatearResumenProduccion(TipoCuerpo tipo) {
        java.util.Map<org.example.game.jugador.TipoRecurso, Double> prod = org.example.game.jugador.InventarioJugador.getProduccionBasePorTick(tipo);
        if (prod.isEmpty()) return "Da/t: Ninguno";
        StringBuilder sb = new StringBuilder("Da/t: ");
        int i = 0;
        for (var e : prod.entrySet()) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("+%.2f ", e.getValue())).append(e.getKey().nombre.substring(0, Math.min(3, e.getKey().nombre.length())));
            i++;
        }
        return sb.toString();
    }

    public static String formatearTooltipDetallado(TipoCuerpo tipo, int hotkey) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append(tipo.nombre.toUpperCase()).append(" [Tecla ").append(hotkey).append("]\n");
        sb.append("========================================\n");
        sb.append("MATERIALES NECESARIOS PARA PONERLO:\n");
        var costos = org.example.game.jugador.InventarioJugador.getCostosBase(tipo);
        for (var e : costos.entrySet()) {
            sb.append("  • ").append(e.getKey().nombre).append(": ").append(String.format("%.0f", e.getValue())).append("\n");
        }
        sb.append("\nMATERIALES QUE TE DA POR TICK:\n");
        var prod = org.example.game.jugador.InventarioJugador.getProduccionBasePorTick(tipo);
        for (var e : prod.entrySet()) {
            sb.append("  • ").append(e.getKey().nombre).append(": +").append(String.format("%.2f", e.getValue())).append(" / tick\n");
        }
        sb.append("========================================\n");
        sb.append("• Click: Abrir ventana de configuración y colocación\n");
        sb.append("  (Personalizar nombre, masa, costos y producción)\n");
        sb.append("========================================");
        return sb.toString();
    }

    public void abrirVentanaColocacion(TipoCuerpo tipo) {
        if (tipo == null) return;
        javafx.stage.Window owner = getScene() != null ? getScene().getWindow() : null;
        VentanaDescripcionCuerpo.mostrarParaColocar(tipo, simulacion, owner, () -> {
            marcarSeleccionado(tipo);
        });
    }

    public void marcarSeleccionado(TipoCuerpo tipo) {
        this.tipoSeleccionado = tipo;
        actualizarEstilos();
    }

    public void seleccionarTipo(TipoCuerpo tipo) {
        abrirVentanaColocacion(tipo);
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
