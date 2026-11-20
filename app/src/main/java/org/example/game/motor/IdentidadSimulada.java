package org.example.game.motor;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Capacidad segregada para estado de simulación, clasificación y nombre de un cuerpo (ISP).
 */
public interface IdentidadSimulada {
    boolean esSimulado();
    void setSimulado(boolean simulado);
    TipoCuerpo getTipoCuerpo();
    String getNombre();
}
