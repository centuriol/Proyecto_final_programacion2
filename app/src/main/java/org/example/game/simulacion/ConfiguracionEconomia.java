package org.example.game.simulacion;

/**
 * SRP: Configuración económica de costos de colocación y producción base de recursos.
 */
public final class ConfiguracionEconomia {
    private ConfiguracionEconomia() {}

    // Costos base de colocación
    public static final double COSTO_ESTRELLA_MINERALES = 300.0;
    public static final double COSTO_ESTRELLA_ENERGIA = 150.0;

    public static final double COSTO_PLANETA_MINERALES = 100.0;
    public static final double COSTO_PLANETA_ENERGIA = 50.0;

    public static final double COSTO_LUNA_MINERALES = 50.0;
    public static final double COSTO_LUNA_ENERGIA = 20.0;

    public static final double COSTO_PLANETA_AGUA_MINERALES = 50.0;
    public static final double COSTO_PLANETA_AGUA_ENERGIA = 25.0;

    public static final double COSTO_SATELITE_MINERALES = 30.0;
    public static final double COSTO_SATELITE_ENERGIA = 20.0;

    public static final double COSTO_PLANETA_LAVA_MINERALES = 150.0;
    public static final double COSTO_PLANETA_LAVA_ENERGIA = 75.0;

    // Producción por tick
    public static final double PRODUCCION_ESTRELLA_ENERGIA = 0.10;
    public static final double PRODUCCION_ESTRELLA_MINERALES = 0.04;
    public static final double PRODUCCION_ESTRELLA_CIENCIA = 0.06;

    public static final double PRODUCCION_PLANETA_MINERALES = 0.045;
    public static final double PRODUCCION_PLANETA_CIENCIA = 0.035;
    public static final double PRODUCCION_PLANETA_POBLACION = 0.015;

    public static final double PRODUCCION_PLANETA_AGUA_MINERALES = 0.03;
    public static final double PRODUCCION_PLANETA_AGUA_CIENCIA = 0.02;
    public static final double PRODUCCION_PLANETA_AGUA_ENERGIA = 0.01;

    public static final double PRODUCCION_PLANETA_LAVA_MINERALES = 0.06;
    public static final double PRODUCCION_PLANETA_LAVA_CIENCIA = 0.04;

    public static final double PRODUCCION_LUNA_MINERALES = 0.025;
    public static final double PRODUCCION_SATELITE_CIENCIA = 0.02;
    public static final double PRODUCCION_SATELITE_ENERGIA = 0.01;
}
