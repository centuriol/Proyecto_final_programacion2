package org.example.game.civilizacion;

import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.ArrayList;
import java.util.List;

/**
 * SRP: Registra y acota el historial temporal de población y estabilidad de una civilización.
 */
public class HistorialCivilizacion {

    private final List<Double> historialPoblacion = new ArrayList<>();
    private final List<Double> historialEstabilidad = new ArrayList<>();
    private int contadorTicksHistorial = 0;

    public void registrarMuestra(double poblacion, double estabilidad) {
        contadorTicksHistorial++;
        if (contadorTicksHistorial % 10 == 0) {
            historialPoblacion.add(poblacion);
            historialEstabilidad.add(estabilidad);
            if (historialPoblacion.size() > ConfiguracionSimulacion.MAX_TICKS_HISTORIAL / 10) {
                historialPoblacion.remove(0);
                historialEstabilidad.remove(0);
            }
        }
    }

    public List<Double> getHistorialPoblacion() { return List.copyOf(historialPoblacion); }
    public List<Double> getHistorialEstabilidad() { return List.copyOf(historialEstabilidad); }
}
