package org.example.game.civilizacion;
/**
 * Representa las condiciones instantáneas de habitabilidad y entorno de un planeta civilizable.
 * Implementado como Java 17 record inmutable (SRP / Data record).
 */
public record CondicionesHabitabilidad( double distanciaAEstrella, double distanciaIdeal, double ratioZonaHabitable, boolean enZonaHabitable, double flujoEnergetico, double distanciaAgujeroNegro, boolean bajoAmenazaAgujero, double intensidadAmenaza, double excentricidadOrbital ) {
    public static CondicionesHabitabilidad porDefecto() {
        return new CondicionesHabitabilidad(-1, -1, 0, false, 0, -1, false, 0, 0);
    }
}
