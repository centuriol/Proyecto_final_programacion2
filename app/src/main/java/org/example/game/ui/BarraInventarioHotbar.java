package org.example.game.ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.SimulacionSolar;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Barra de inventario inferior (Hotbar) estilo RimWorld.
 * SRP: Gestiona la selección rápida de los 8 tipos de cuerpos y el factor de masa.
 */
public class BarraInventarioHotbar extends HBox {

    private final SimulacionSolar simulacion;
    private final VentanaDescripcionCuerpo ventanaDialogos;
    private final List<SlotHotbar> slotCards = new ArrayList<>();
    private TipoCuerpo tipoSeleccionado = null;
    private boolean retraida = false;
    private Button btnToggleRetraccion;
    private Runnable accionAbrirArbol;

    public static final TipoCuerpo[] CUERPOS_HOTBAR = {
            TipoCuerpo.ESTRELLA,
            TipoCuerpo.PLANETA_ROCOSO,
            TipoCuerpo.LUNA,
            TipoCuerpo.PLANETA_AGUA,
            TipoCuerpo.SATELITE,
            TipoCuerpo.PLANETA_LAVA
    };

    public BarraInventarioHotbar(SimulacionSolar simulacion) {
        this(simulacion, new VentanaDescripcionCuerpo());
    }

    public BarraInventarioHotbar(SimulacionSolar simulacion, VentanaDescripcionCuerpo ventanaDialogos) {
        this.simulacion = simulacion;
        this.ventanaDialogos = ventanaDialogos != null ? ventanaDialogos : new VentanaDescripcionCuerpo();

        setAlignment(Pos.CENTER);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(8);
        setBackground(TemaUI.BG_PANEL);
        setBorder(TemaUI.BORDE_PANEL_NEGRO);

        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            TipoCuerpo tipo = CUERPOS_HOTBAR[i];
            int hotkey = i + 1;
            SlotHotbar slot = new SlotHotbar(tipo, hotkey, this::seleccionarTipo);
            slotCards.add(slot);
            getChildren().add(slot);
        }

        actualizarBloqueos();
        this.simulacion.getBusEventos().agregarListenerUI(this::actualizarBloqueos);
    }

    public void setAccionAbrirArbol(Runnable accionAbrirArbol) {
        this.accionAbrirArbol = accionAbrirArbol;
    }

    public Button getBotonToggle() {
        if (btnToggleRetraccion == null) {
            btnToggleRetraccion = new Button("Ocultar Planetas");
            btnToggleRetraccion.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            actualizarEstiloBotonToggle();
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
        btnToggleRetraccion.setStyle(TemaUI.CSS_BTN_SECUNDARIO);
    }

    public void abrirVentanaColocacion(TipoCuerpo tipo) {
        if (tipo == null) return;
        javafx.stage.Window owner = getScene() != null ? getScene().getWindow() : null;
        ventanaDialogos.abrirParaColocar(tipo, simulacion, owner, () -> marcarSeleccionado(tipo));
    }

    public void marcarSeleccionado(TipoCuerpo tipo) {
        this.tipoSeleccionado = tipo;
        actualizarEstilos();
    }

    public void seleccionarTipo(TipoCuerpo tipo) {
        if (simulacion.getProgresion().estaDesbloqueado(tipo)) {
            abrirVentanaColocacion(tipo);
        } else {
            if (accionAbrirArbol != null) {
                accionAbrirArbol.run();
            } else {
                javafx.stage.Window owner = getScene() != null ? getScene().getWindow() : null;
                VentanaProgresion.mostrar(simulacion, owner);
            }
        }
    }

    public void actualizarBloqueos() {
        if (simulacion == null || simulacion.getProgresion() == null) return;
        var prog = simulacion.getProgresion();

        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            TipoCuerpo tipo = CUERPOS_HOTBAR[i];
            SlotHotbar slot = slotCards.get(i);
            Label lblNom = (Label) slot.getChildren().get(2);

            boolean desbloqueado = prog.estaDesbloqueado(tipo);
            slot.setBloqueado(!desbloqueado);
            if (desbloqueado) {
                lblNom.setText(tipo.nombre.toUpperCase());
                lblNom.setTextFill(Color.web(tipo.getColorHexString()));
            } else {
                double costo = obtenerCostoDesbloqueo(tipo);
                lblNom.setText(tipo.nombre.toUpperCase() + " [BLOQ " + (int) costo + " CIE]");
                lblNom.setTextFill(TemaUI.COLOR_TEXTO_MUTED);
            }
        }
    }

    private double obtenerCostoDesbloqueo(TipoCuerpo tipo) {
        return tipo != null ? tipo.getCostoDesbloqueoCiencia() : 0.0;
    }

    public void deseleccionar() {
        this.tipoSeleccionado = null;
        actualizarEstilos();
    }

    private void actualizarEstilos() {
        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            SlotHotbar slot = slotCards.get(i);
            TipoCuerpo tipo = CUERPOS_HOTBAR[i];
            slot.setSeleccionado(tipo == tipoSeleccionado);
        }
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
}
