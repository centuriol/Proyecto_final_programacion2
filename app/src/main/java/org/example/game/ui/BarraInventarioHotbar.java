package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Barra de inventario inferior (Hotbar) estilo RimWorld.
 * SRP: Gestiona la selección rápida de los 8 tipos de cuerpos y el factor de masa.
 */
public class BarraInventarioHotbar extends HBox {

    private static final Background BG_BARRA = new Background(new BackgroundFill(Color.web("#1a1c26"), new CornerRadii(4), Insets.EMPTY));
    private static final Border BORDER_BARRA = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2)));

    private static final Background BG_SLOT_NORMAL = new Background(new BackgroundFill(Color.web("#2b2d3a"), new CornerRadii(4), Insets.EMPTY));
    private static final Background BG_SLOT_HOVER = new Background(new BackgroundFill(Color.web("#3b3d4f"), new CornerRadii(4), Insets.EMPTY));
    private static final Background BG_SLOT_SELECTED = new Background(new BackgroundFill(Color.web("#1a1c26"), new CornerRadii(4), Insets.EMPTY));

    private static final Border BORDER_SLOT_NORMAL = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2)));
    private static final Border BORDER_SLOT_HOVER = new Border(new BorderStroke(Color.web("#5be3ff"), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2)));
    private static final Border BORDER_SLOT_SELECTED = new Border(new BorderStroke(Color.web("#5be3ff"), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2)));

    private static final DropShadow SHADOW_SELECTED = new DropShadow(6, Color.web("#5be3ff"));

    private static final Background BG_BTN_TOGGLE = new Background(new BackgroundFill(Color.web("#1a1c26"), new CornerRadii(4, 4, 0, 0, false), Insets.EMPTY));
    private static final Background BG_BTN_TOGGLE_HOVER = new Background(new BackgroundFill(Color.web("#2b2d3a"), new CornerRadii(4, 4, 0, 0, false), Insets.EMPTY));
    private static final Border BORDER_BTN_TOGGLE = new Border(new BorderStroke(Color.web("#3b3d4f"), BorderStrokeStyle.SOLID, new CornerRadii(4, 4, 0, 0, false), new BorderWidths(1, 1, 0, 1)));
    private static final Border BORDER_BTN_TOGGLE_HOVER = new Border(new BorderStroke(Color.web("#5be3ff"), BorderStrokeStyle.SOLID, new CornerRadii(4, 4, 0, 0, false), new BorderWidths(1, 1, 0, 1)));

    private final SimulacionSolar simulacion;
    private final List<VBox> slotCards = new ArrayList<>();
    private TipoCuerpo tipoSeleccionado = null;
    private boolean retraida = false;
    private Button btnToggleRetraccion;

    private static final TipoCuerpo[] CUERPOS_HOTBAR = {
            TipoCuerpo.ESTRELLA,
            TipoCuerpo.PLANETA_ROCOSO,
            TipoCuerpo.LUNA,
            TipoCuerpo.PLANETA_AGUA,
            TipoCuerpo.PLANETA_LAVA
    };

    private static final String[] ICONOS_HOTBAR = {
            "[SOL]", "[POR]", "[LUN]", "[AGU]", "[LAV]"
    };

    public BarraInventarioHotbar(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(8);
        setBackground(BG_BARRA);
        setBorder(BORDER_BARRA);

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

    public Button getBotonToggle() {
        if (btnToggleRetraccion == null) {
            btnToggleRetraccion = new Button("Ocultar Planetas");
            btnToggleRetraccion.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            actualizarEstiloBotonToggle();
            btnToggleRetraccion.setOnMouseEntered(e -> {
                btnToggleRetraccion.setBackground(BG_BTN_TOGGLE_HOVER);
                btnToggleRetraccion.setBorder(BORDER_BTN_TOGGLE_HOVER);
            });
            btnToggleRetraccion.setOnMouseExited(e -> {
                btnToggleRetraccion.setBackground(BG_BTN_TOGGLE);
                btnToggleRetraccion.setBorder(BORDER_BTN_TOGGLE);
            });
            btnToggleRetraccion.setOnAction(e -> alternarRetraccion());
        }
        return btnToggleRetraccion;
    }

    public void alternarRetraccion() {
        setRetraida(!retraida);
    }

    public void setRetraida(boolean retraida) {
        this.retraida = retraida;
        setVisible(!retraida);
        setManaged(!retraida);
        if (btnToggleRetraccion != null) {
            btnToggleRetraccion.setText(retraida ? "Mostrar Planetas" : "Ocultar Planetas");
            actualizarEstiloBotonToggle();
        }
    }

    public boolean isRetraida() {
        return retraida;
    }

    private void actualizarEstiloBotonToggle() {
        if (btnToggleRetraccion == null) return;
        btnToggleRetraccion.setBackground(BG_BTN_TOGGLE);
        btnToggleRetraccion.setBorder(BORDER_BTN_TOGGLE);
        btnToggleRetraccion.setTextFill(Color.web(retraida ? "#5be3ff" : "#8c92a4"));
        btnToggleRetraccion.setCursor(Cursor.HAND);
        btnToggleRetraccion.setPadding(new Insets(3, 12, 3, 12));
    }

    private VBox crearSlot(TipoCuerpo tipo, String icono, int hotkey) {
        VBox slot = new VBox(4);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefSize(100, 84);
        slot.setPadding(new Insets(5, 8, 5, 8));
        slot.setBackground(BG_SLOT_NORMAL);
        slot.setBorder(BORDER_SLOT_NORMAL);
        slot.setCursor(Cursor.HAND);

        // Tecla de acceso rápido
        Label lblKey = new Label("[" + hotkey + "]");
        lblKey.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        lblKey.setTextFill(Color.web("#8c92a4"));

        // Icono / Imagen del cuerpo celeste
        javafx.scene.layout.StackPane iconBox = org.example.game.render.GestorImagenes.crearContenedorIcono(tipo, 40, 40);

        // Nombre del cuerpo celeste
        Label lblNom = new Label(tipo.nombre.toUpperCase());
        lblNom.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblNom.setTextFill(Color.web(tipo.getColorHexString()));
        lblNom.setAlignment(Pos.CENTER);
        lblNom.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        slot.getChildren().addAll(lblKey, iconBox, lblNom);

        // Hover y Click sin invalidar CSS
        slot.setOnMouseEntered(e -> {
            if (tipoSeleccionado != tipo) {
                slot.setBackground(BG_SLOT_HOVER);
                slot.setBorder(BORDER_SLOT_HOVER);
            }
        });

        slot.setOnMouseExited(e -> {
            if (tipoSeleccionado != tipo) {
                slot.setBackground(BG_SLOT_NORMAL);
                slot.setBorder(BORDER_SLOT_NORMAL);
            }
        });

        slot.setOnMouseClicked(e -> {
            abrirVentanaColocacion(tipo);
        });

        return slot;
    }

    private static String formatearMapaRecursos(String prefijo, String formatoValor, Map<TipoRecurso, Double> mapa, String siVacio) {
        if (mapa == null || mapa.isEmpty()) return siVacio;
        StringBuilder sb = new StringBuilder(prefijo);
        int i = 0;
        for (var e : mapa.entrySet()) {
            if (i > 0) sb.append(", ");
            sb.append(String.format(formatoValor, e.getValue())).append(e.getKey().nombre.substring(0, Math.min(3, e.getKey().nombre.length())));
            i++;
        }
        return sb.toString();
    }

    public static String formatearResumenCosto(TipoCuerpo tipo) {
        Map<TipoRecurso, Double> costos = org.example.game.jugador.InventarioJugador.getCostosBase(tipo);
        return formatearMapaRecursos("Requiere: ", "%.0f ", costos, "Costo: Gratis");
    }

    public static String formatearResumenProduccion(TipoCuerpo tipo) {
        Map<TipoRecurso, Double> prod = org.example.game.jugador.InventarioJugador.getProduccionBasePorTick(tipo);
        return formatearMapaRecursos("Da/t: ", "+%.2f ", prod, "Da/t: Ninguno");
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
                slot.setBackground(BG_SLOT_SELECTED);
                slot.setBorder(BORDER_SLOT_SELECTED);
                slot.setEffect(SHADOW_SELECTED);
            } else {
                slot.setBackground(BG_SLOT_NORMAL);
                slot.setBorder(BORDER_SLOT_NORMAL);
                slot.setEffect(null);
            }
        }
    }
}
