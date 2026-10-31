package org.example.game.simulacion;
/**
 * SRP: Parametros numericos, escalado progresivo de meteoritos y umbrales para colisiones astronomicas.
 * Todos los umbrales de tamano, masa, frecuencia y dano de meteoritos son configurables aqui.
 */
public final class ConfiguracionColisiones {
    private ConfiguracionColisiones() {}

    public static final double RATIO_COLISION_COLAPSO = 1.3;
    public static final double RATIO_ABSORCION_MASA = 8.0;
    public static final int FRAGMENTOS_POR_COLAPSO = 3;
    public static final int MAX_FRAGMENTOS_POR_COLAPSO = 8;
    public static final double FRACCION_MASA_DISIPADA = 0.10;
    public static final double VELOCIDAD_EYECCION_FRAGMENTOS = 150.0;
    public static final double FRACCION_MASA_MINIMA_SOBREVIVIENTE = 0.05;
    public static final double PORCENTAJE_GANANCIA_MASA_COLISION = 0.30;

    // Escalones de progreso del jugador para escalar tamano y dano de meteoritos
    public static final double FACTOR_PROGRESO_METEORITO_INICIAL = ConfiguracionEconomia.FACTOR_MASA_INICIAL;
    public static final double FACTOR_PROGRESO_METEORITO_BAJO = 0.6;
    public static final double FACTOR_PROGRESO_METEORITO_ALTO = 4.0;

    // Fracciones configurables de dano de meteorito sobre la masa inicial del planeta
    // Inicio (0.3x): 5% | Progreso bajo (0.6x): maximo 10% | Progreso alto (>= 4.0x): dano completo (100%)
    public static final double DANO_METEORITO_INICIAL_FRACCION = 0.05;
    public static final double DANO_METEORITO_PROGRESO_BAJO_MAX_FRACCION = 0.10;
    public static final double DANO_METEORITO_PROGRESO_ALTO_FRACCION = 1.00;
    public static final double DANO_METEORITO_MIN_FRACCION = DANO_METEORITO_INICIAL_FRACCION;
    public static final double DANO_METEORITO_MAX_FRACCION = DANO_METEORITO_PROGRESO_ALTO_FRACCION;
    public static final double UMBRAL_EXPLOSION_MASA_FRACCION = 0.30;

    // Topes fisicos y visuales reducidos y escalables para meteoritos y escombros
    public static final double MASA_MINIMA_METEORITO = 1.0e17;
    public static final double MASA_METEORITO_PROGRESO_BAJO = 1.0e18;
    public static final double MASA_MAXIMA_METEORITO = 5.0e20;

    public static final double RADIO_MINIMO_METEORITO = 1.5;
    public static final double RADIO_METEORITO_PROGRESO_BAJO = 2.2;
    public static final double RADIO_MAXIMO_METEORITO = 4.5;
    public static final double RADIO_INTERCEPCION_SATELITE = 60.0;

    // Parametros de aparicion de eventos cosmicos de meteoritos
    public static final int TICKS_GRACIA_INICIAL_METEORITOS = 3600;
    public static final int INTERVALO_BASE_EVENTO_METEORITO = 2400;
    public static final double VARIACION_MIN_INTERVALO_METEORITO = 0.7;
    public static final double RANGO_VARIACION_INTERVALO_METEORITO = 0.6;
    public static final int MAX_METEORITOS_EVENTO_ACTIVOS = 2;
}
