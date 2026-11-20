package org.example.game.render;

/**
 * Capacidad segregada de métricas y banderas geométricas visuales para un tipo de cuerpo (ISP / OCP).
 */
public interface MetricasVisualesTipo {
    double calcularTamanoGhost(double radio);
    double calcularRadioAtraccionPreview(double factorMasa);
    boolean tieneRotacionAnimada();
    boolean tieneLineasReferenciaOrbital();
}
