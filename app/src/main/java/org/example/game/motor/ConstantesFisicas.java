package org.example.game.motor;

/**
 * Constantes físicas centralizadas para toda la simulación.
 * Principio: Single Responsibility - un solo lugar para configuración física.
 */
public final class ConstantesFisicas {

    // --- Constantes universales ---
    public static final double G_UNIVERSAL = 6.67430e-11; // m³/kg/s²

    // --- Escalado coordenadas ---
    // 1 pixel = METROS_POR_PIXEL metros
    // Para sistema solar: 1 UA ≈ 150M km = 1.5e11 m
    // En pantalla: 150 px = 1 UA → 1 px = 1e9 m
    public static final double METROS_POR_PIXEL = 1e9;

    // --- Escalado tiempo ---
    // 1 tick = SEGUNDOS_POR_TICK segundos reales
    public static final double SEGUNDOS_POR_TICK = 3600.0; // 1 hora por tick

    // --- G escalado para trabajar en píxeles y ticks ---
    // F = G * m1 * m2 / r²
    // En unidades simuladas: fuerza en (px/tick²) * kg
    // G_sim = G_real * (s/tick)² / (m/px)³
    public static final double G_ESCALADO = G_UNIVERSAL
            * SEGUNDOS_POR_TICK * SEGUNDOS_POR_TICK
            / (METROS_POR_PIXEL * METROS_POR_PIXEL * METROS_POR_PIXEL);

    // --- Estabilidad numérica ---
    // Softening factor para evitar singularidades en r=0
    // ~radio de la Tierra en píxeles = 6371km / 1e9 = 0.006 px
    // Usamos 10px para estabilidad
    public static final double SOFTENING = 10.0;
    public static final double SOFTENING_CUADRADO = SOFTENING * SOFTENING;

    // --- Límites mundo ---
    public static final double RADIO_MUNDO_DEFAULT = 10000.0;
    public static final double LIMITE_ESCAPE_FACTOR = 2.0;

    // --- Conversión coordenadas ---
    // JavaFX: origen arriba-izquierda, Y crece hacia ABAJO
    // Física: origen centro, Y crece hacia ARRIBA
    public static double fisicaAYJavaFX(double yFisica, double altoMundo) {
        return altoMundo / 2.0 - yFisica;
    }

    public static double javaFXAFisica(double yJavaFX, double altoMundo) {
        return altoMundo / 2.0 - yJavaFX;
    }

    public static double fisicaAXJavaFX(double xFisica, double anchoMundo) {
        return anchoMundo / 2.0 + xFisica;
    }

    public static double javaFXAFisicaX(double xJavaFX, double anchoMundo) {
        return xJavaFX - anchoMundo / 2.0;
    }

    private ConstantesFisicas() {}
}