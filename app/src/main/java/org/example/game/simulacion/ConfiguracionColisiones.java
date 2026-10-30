package org.example.game.simulacion;

/**
 * SRP: Parámetros numéricos y umbrales para colisiones astronómicas.
 */
public final class ConfiguracionColisiones {
    private ConfiguracionColisiones() {}

    public static final double RATIO_COLISION_COLAPSO = 1.3;
    public static final double RATIO_ABSORCION_MASA = 8.0;
    public static final int FRAGMENTOS_POR_COLAPSO = 3;
    public static final double FRACCION_MASA_DISIPADA = 0.10;
    public static final double VELOCIDAD_EYECCION_FRAGMENTOS = 150.0;
    public static final double FRACCION_MASA_MINIMA_SOBREVIVIENTE = 0.05;
    public static final double PORCENTAJE_GANANCIA_MASA_COLISION = 0.30;
    public static final double DANO_METEORITO_MIN_FRACCION = 0.10;
    public static final double DANO_METEORITO_MAX_FRACCION = 0.25;
    public static final double UMBRAL_EXPLOSION_MASA_FRACCION = 0.30;
}
