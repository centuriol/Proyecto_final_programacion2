package org.example.game.ui;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;

import java.util.Map;

/**
 * SRP: Formatea textos resumidos y tooltips detallados de costos y producción para la hotbar.
 */
public class FormateadorHotbar {

    public String formatearResumenCosto(TipoCuerpo tipo) {
        return formatearMapaRecursos("Requiere: ", "%.0f ", InventarioJugador.getCostosBase(tipo), "Costo: Gratis");
    }

    public String formatearResumenProduccion(TipoCuerpo tipo) {
        return formatearMapaRecursos("Da/t: ", "+%.2f ", InventarioJugador.getProduccionBasePorTick(tipo), "Da/t: Ninguno");
    }

    public String formatearTooltipDetallado(TipoCuerpo tipo, int hotkey) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append(tipo.nombre.toUpperCase()).append(" [Tecla ").append(hotkey).append("]\n");
        sb.append("========================================\n");
        sb.append("MATERIALES NECESARIOS PARA PONERLO:\n");
        for (var e : InventarioJugador.getCostosBase(tipo).entrySet()) {
            sb.append("  • ").append(e.getKey().nombre).append(": ").append(String.format("%.0f", e.getValue())).append("\n");
        }
        sb.append("\nMATERIALES QUE TE DA POR TICK:\n");
        for (var e : InventarioJugador.getProduccionBasePorTick(tipo).entrySet()) {
            sb.append("  • ").append(e.getKey().nombre).append(": +").append(String.format("%.2f", e.getValue())).append(" / tick\n");
        }
        sb.append("========================================\n");
        sb.append("• Click: Abrir ventana de configuración y colocación\n");
        sb.append("  (Personalizar nombre, masa, costos y producción)\n");
        sb.append("========================================");
        return sb.toString();
    }

    private String formatearMapaRecursos(String prefijo, String formatoValor, Map<TipoRecurso, Double> mapa, String siVacio) {
        if (mapa == null || mapa.isEmpty()) return siVacio;
        StringBuilder sb = new StringBuilder(prefijo);
        int i = 0;
        for (var e : mapa.entrySet()) {
            if (i > 0) sb.append(", ");
            sb.append(String.format(formatoValor, e.getValue()))
                    .append(e.getKey().nombre.substring(0, Math.min(3, e.getKey().nombre.length())));
            i++;
        }
        return sb.toString();
    }
}
