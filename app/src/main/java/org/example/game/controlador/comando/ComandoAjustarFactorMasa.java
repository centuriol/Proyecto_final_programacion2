package org.example.game.controlador.comando;

import org.example.game.simulacion.SimulacionSolar;

/**
 * SRP: Encapsula la acción de escalar el factor de masa de colocación actual de la simulación.
 */
public class ComandoAjustarFactorMasa implements Comando {

    private final SimulacionSolar simulacion;
    private final double multiplicador;

    public ComandoAjustarFactorMasa(SimulacionSolar simulacion, double multiplicador) {
        this.simulacion = simulacion;
        this.multiplicador = multiplicador;
    }

    @Override
    public void ejecutar() {
        if (simulacion != null) {
            simulacion.setFactorMasaColocacion(simulacion.getFactorMasaColocacion() * multiplicador);
        }
    }
}
