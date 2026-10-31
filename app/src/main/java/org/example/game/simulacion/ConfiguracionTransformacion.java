package org.example.game.simulacion;
/**
 * SRP: Parámetros para eventos de transformación planetaria y búsqueda de órbitas.
 */
public final class ConfiguracionTransformacion {
    private ConfiguracionTransformacion() {}

    public static final double RADIO_TRANSFORMACION_AGUA_PORRO = 120.0;
    public static final double RADIO_TRANSFORMACION_PORRO_LAVA = 70.0;
    public static final int TICKS_ESPERA_TRANSFORMACION_EASTER_EGG = 40;
    public static final int TICKS_MIGRACION_ORBITA = 30;
    public static final double MARGEN_SEGURIDAD_ORBITA_LIBRE = 15.0;
    public static final double PASO_BUSQUEDA_ORBITA_PX = 25.0;
    public static final int MAX_INTENTOS_ORBITA_LIBRE = 8;
}
