package org.example.game.progresion;

import org.example.game.simulacion.ConfiguracionSimulacion;

/**
 * Elementos y tecnologías que se pueden desbloquear en la rama planetaria.
 *
 * Principios SOLID:
 * - SRP: Identifica los hitos tecnológicos de la rama de cuerpos celestes.
 */
public enum ElementoDesbloqueable {
    LUNA("Luna", ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA),
    PLANETA_PORRO("Planeta Porro", ConfiguracionSimulacion.COSTO_DESBLOQUEO_PORRO),
    SATELITE("Satélite", ConfiguracionSimulacion.COSTO_DESBLOQUEO_SATELITE),
    ESCUDO("Escudo", ConfiguracionSimulacion.COSTO_DESBLOQUEO_ESCUDO),
    PLANETA_LAVA("Planeta Lava", ConfiguracionSimulacion.COSTO_DESBLOQUEO_LAVA);

    public final String nombre;
    public final double costoCiencia;

    ElementoDesbloqueable(String nombre, double costoCiencia) {
        this.nombre = nombre;
        this.costoCiencia = costoCiencia;
    }
}
