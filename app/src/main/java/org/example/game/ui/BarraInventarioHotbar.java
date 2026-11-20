package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;

import java.util.ArrayList;
import java.util.List;

/**
 * Barra de inventario inferior (Hotbar) estilo RimWorld (SRP).
 */
public class BarraInventarioHotbar extends HBox {

    private static final FormateadorHotbar FORMATEADOR = new FormateadorHotbar();
    private static final FabricaUI FABRICA_UI = new FabricaUI();

    public static final TipoCuerpo[] CUERPOS_HOTBAR = {
            TipoCuerpo.ESTRELLA,
            TipoCuerpo.PLANETA_ROCOSO,
            TipoCuerpo.LUNA,
            TipoCuerpo.PLANETA_AGUA,
            TipoCuerpo.SATELITE,
            TipoCuerpo.PLANETA_LAVA
    };

    private static class EstadoHotbar {
        final SimulacionSolar simulacion;
        final VisualizadorDescripcion ventanaDialogos;
        final List<SlotHotbar> slotCards = new ArrayList<>();
        TipoCuerpo tipoSeleccionado = null;
        boolean retraida = false;
        Button btnToggleRetraccion;
        Runnable accionAbrirArbol;

        EstadoHotbar(SimulacionSolar sim, VisualizadorDescripcion dialogos) {
            this.simulacion = sim;
            this.ventanaDialogos = dialogos != null ? dialogos : FABRICA_UI.crearVisualizadorDescripcion();
        }

        void actualizarSeleccionVisual() {
            for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
                slotCards.get(i).setSeleccionado(CUERPOS_HOTBAR[i] == tipoSeleccionado);
            }
        }
    }

    private final EstadoHotbar estado;

    public BarraInventarioHotbar(SimulacionSolar simulacion) {
        this(simulacion, FABRICA_UI.crearVisualizadorDescripcion());
    }

    public BarraInventarioHotbar(SimulacionSolar simulacion, VisualizadorDescripcion ventanaDialogos) {
        this.estado = new EstadoHotbar(simulacion, ventanaDialogos);

        setAlignment(Pos.CENTER);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(8);
        setBackground(TemaUI.BG_PANEL);
        setBorder(TemaUI.BORDE_PANEL_NEGRO);

        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            SlotHotbar slot = new SlotHotbar(CUERPOS_HOTBAR[i], i + 1, this::seleccionarTipo);
            estado.slotCards.add(slot);
            getChildren().add(slot);
        }

        actualizarBloqueos();
        estado.simulacion.getBusEventos().agregarListenerUI(this::actualizarBloqueos);
    }

    public void setAccionAbrirArbol(Runnable accionAbrirArbol) { estado.accionAbrirArbol = accionAbrirArbol; }

    public Button getBotonToggle() {
        if (estado.btnToggleRetraccion == null) {
            estado.btnToggleRetraccion = new Button("Ocultar Planetas");
            estado.btnToggleRetraccion.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            estado.btnToggleRetraccion.setStyle(TemaUI.CSS_BTN_SECUNDARIO);
            estado.btnToggleRetraccion.setOnAction(e -> alternarRetraccion());
        }
        return estado.btnToggleRetraccion;
    }

    public void alternarRetraccion() { setRetraida(!estado.retraida); }

    public void setRetraida(boolean retraida) {
        estado.retraida = retraida;
        setVisible(!retraida);
        setManaged(!retraida);
        if (estado.btnToggleRetraccion != null) {
            estado.btnToggleRetraccion.setText(retraida ? "Mostrar Planetas" : "Ocultar Planetas");
            estado.btnToggleRetraccion.setStyle(TemaUI.CSS_BTN_SECUNDARIO);
        }
    }

    public boolean isRetraida() { return estado.retraida; }

    public void abrirVentanaColocacion(TipoCuerpo tipo) {
        if (tipo == null) return;
        javafx.stage.Window owner = getScene() != null ? getScene().getWindow() : null;
        estado.ventanaDialogos.abrirParaColocar(tipo, estado.simulacion, owner, () -> marcarSeleccionado(tipo));
    }

    public void marcarSeleccionado(TipoCuerpo tipo) {
        estado.tipoSeleccionado = tipo;
        estado.actualizarSeleccionVisual();
    }

    public void seleccionarTipo(TipoCuerpo tipo) {
        if (estado.simulacion.getProgresion().estaDesbloqueado(tipo)) {
            abrirVentanaColocacion(tipo);
        } else if (estado.accionAbrirArbol != null) {
            estado.accionAbrirArbol.run();
        } else {
            javafx.stage.Window owner = getScene() != null ? getScene().getWindow() : null;
            VentanaProgresion.mostrar(estado.simulacion, owner);
        }
    }

    public void actualizarBloqueos() {
        if (estado.simulacion == null || estado.simulacion.getProgresion() == null) return;
        var prog = estado.simulacion.getProgresion();
        for (int i = 0; i < CUERPOS_HOTBAR.length; i++) {
            TipoCuerpo tipo = CUERPOS_HOTBAR[i];
            SlotHotbar slot = estado.slotCards.get(i);
            Label lblNom = (Label) slot.getChildren().get(2);
            boolean desbloqueado = prog.estaDesbloqueado(tipo);
            slot.setBloqueado(!desbloqueado);
            if (desbloqueado) {
                lblNom.setText(tipo.nombre.toUpperCase());
                lblNom.setTextFill(Color.web(tipo.getColorHexString()));
            } else {
                lblNom.setText(tipo.nombre.toUpperCase() + " [BLOQ " + (int) tipo.getCostoDesbloqueoCiencia() + " CIE]");
                lblNom.setTextFill(TemaUI.COLOR_TEXTO_MUTED);
            }
        }
    }

    public void deseleccionar() {
        estado.tipoSeleccionado = null;
        estado.actualizarSeleccionVisual();
    }

    public static String formatearResumenCosto(TipoCuerpo tipo) { return FORMATEADOR.formatearResumenCosto(tipo); }
    public static String formatearResumenProduccion(TipoCuerpo tipo) { return FORMATEADOR.formatearResumenProduccion(tipo); }
    public static String formatearTooltipDetallado(TipoCuerpo tipo, int hotkey) { return FORMATEADOR.formatearTooltipDetallado(tipo, hotkey); }
}
