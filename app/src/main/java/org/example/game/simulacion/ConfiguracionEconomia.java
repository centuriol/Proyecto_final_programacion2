package org.example.game.simulacion;
/**
 * SRP: Configuracion economica centralizada y proporcional de costos de colocacion,
 * presupuesto inicial, produccion pasiva por tick, costos operativos y ciencia.
 *
 * Orden coherente de progresion planetaria en costo y produccion:
 * Planeta de Agua < Planeta Porro < Planeta Lava < Planeta Gaseoso < Sol.
 */
public final class ConfiguracionEconomia {
    private ConfiguracionEconomia() {}

    // Escalado de masa y colocacion
    public static final double FACTOR_MASA_INICIAL = 0.3;
    public static final double FACTOR_MASA_SATELITE_FIJO = 1.0;
    public static final double EXPONENTE_COSTO_MASA = 1.5;

    // ===== Tabla proporcional de costos base de colocacion (a masa estandar 1.0x) =====

    // 1. Planeta de Agua: subido levemente manteniendo proporcion con su produccion
    public static final double COSTO_PLANETA_AGUA_MINERALES = 560.0;
    public static final double COSTO_PLANETA_AGUA_ENERGIA = 280.0;

    // Variante criogenica intermedia proporcional
    public static final double COSTO_PLANETA_HELADO_MINERALES = 670.0;
    public static final double COSTO_PLANETA_HELADO_ENERGIA = 335.0;

    // 2. Planeta Porro (Rocoso): mundo habitable con poblacion, civilizacion y sinergia cientifica
    public static final double COSTO_PLANETA_MINERALES = 800.0;
    public static final double COSTO_PLANETA_ENERGIA = 400.0;

    // 3. Planeta Lava: extraccion industrial intensiva de minerales pesados y ciencia termica
    public static final double COSTO_PLANETA_LAVA_MINERALES = 1180.0;
    public static final double COSTO_PLANETA_LAVA_ENERGIA = 590.0;

    // 4. Planeta Gaseoso: gigante joviano de maximo rendimiento planetario de recursos
    public static final double COSTO_PLANETA_GASEOSO_MINERALES = 1540.0;
    public static final double COSTO_PLANETA_GASEOSO_ENERGIA = 770.0;

    // 5. Sol (Estrella): abaratado un poco mas manteniendose como astro cumbre del sistema
    public static final double COSTO_ESTRELLA_MINERALES = 1700.0;
    public static final double COSTO_ESTRELLA_ENERGIA = 850.0;

    // Cuerpos orbitales especializados
    public static final double COSTO_LUNA_MINERALES = 570.0;
    public static final double COSTO_LUNA_ENERGIA = 285.0;

    public static final double COSTO_SATELITE_MINERALES = 400.0;
    public static final double COSTO_SATELITE_ENERGIA = 200.0;

    // Referencia nominal para singularidades no comprables directamente
    public static final double COSTO_REFERENCIA_AGUJERO_NEGRO = 2500.0;

    // ===== Costos operativos de defensa y colonizacion =====
    public static final double COSTO_ACTIVACION_ESCUDO_ENERGIA = 280.0;
    public static final double COSTO_COLONIZAR_ENERGIA = 120.0;
    public static final double COSTO_COLONIZAR_MINERALES = 180.0;

    public static final double COSTO_ESCUDO_NIVEL_1 = 180.0;
    public static final double COSTO_ESCUDO_NIVEL_2 = 400.0;
    public static final double COSTO_ESCUDO_NIVEL_3 = 750.0;
    public static final double COSTO_ESCUDO_NIVEL_4 = 1200.0;

    // ===== Produccion pasiva proporcional por tick (energia -10%, minerales +5% a +10%) =====

    // Planeta de Agua: minerales +10% (0.033), energia -10% (0.0108) -> 0.0618 total/tick
    public static final double PRODUCCION_PLANETA_AGUA_MINERALES = 0.033;
    public static final double PRODUCCION_PLANETA_AGUA_CIENCIA = 0.018;
    public static final double PRODUCCION_PLANETA_AGUA_ENERGIA = 0.0108;

    // Planeta Helado: minerales +8.3% (0.039), energia -10% (0.0108) -> 0.0738 total/tick
    public static final double PRODUCCION_PLANETA_HELADO_MINERALES = 0.039;
    public static final double PRODUCCION_PLANETA_HELADO_CIENCIA = 0.024;
    public static final double PRODUCCION_PLANETA_HELADO_ENERGIA = 0.0108;

    // Planeta Porro: minerales +9.5% (0.046) -> 0.091 total/tick + multiplicadores de civilizacion
    public static final double PRODUCCION_PLANETA_MINERALES = 0.046;
    public static final double PRODUCCION_PLANETA_CIENCIA = 0.030;
    public static final double PRODUCCION_PLANETA_POBLACION = 0.015;

    // Planeta Lava: minerales +8.3% (0.078) -> 0.130 total/tick
    public static final double PRODUCCION_PLANETA_LAVA_MINERALES = 0.078;
    public static final double PRODUCCION_PLANETA_LAVA_CIENCIA = 0.052;

    // Planeta Gaseoso: minerales +8.2% (0.092), energia -10% (0.0225) -> 0.1695 total/tick
    public static final double PRODUCCION_PLANETA_GASEOSO_MINERALES = 0.092;
    public static final double PRODUCCION_PLANETA_GASEOSO_CIENCIA = 0.055;
    public static final double PRODUCCION_PLANETA_GASEOSO_ENERGIA = 0.0225;

    // Sol: energia -10% (0.0765), minerales +7.9% (0.041) -> 0.1455 total/tick + sosten gravitacional
    public static final double PRODUCCION_ESTRELLA_ENERGIA = 0.0765;
    public static final double PRODUCCION_ESTRELLA_MINERALES = 0.041;
    public static final double PRODUCCION_ESTRELLA_CIENCIA = 0.028;

    // Luna: minerales +7.9% (0.041)
    public static final double PRODUCCION_LUNA_MINERALES = 0.041;

    // Satelite: energia -10% (0.0162)
    public static final double PRODUCCION_SATELITE_CIENCIA = 0.042;
    public static final double PRODUCCION_SATELITE_ENERGIA = 0.0162;

    // ===== Costos escalonados de investigacion cientifica por nivel (1..5) =====
    public static final double COSTO_CIENCIA_NIVEL_1 = 180.0;
    public static final double COSTO_CIENCIA_NIVEL_2 = 650.0;
    public static final double COSTO_CIENCIA_NIVEL_3 = 1800.0;
    public static final double COSTO_CIENCIA_NIVEL_4 = 4200.0;
    public static final double COSTO_CIENCIA_NIVEL_5 = 9500.0;

    private static final double[] COSTOS_PROGRESION_CIENCIA = {
            0.0,
            COSTO_CIENCIA_NIVEL_1,
            COSTO_CIENCIA_NIVEL_2,
            COSTO_CIENCIA_NIVEL_3,
            COSTO_CIENCIA_NIVEL_4,
            COSTO_CIENCIA_NIVEL_5
    };

    public static double costoProgresionNivel(int nivel) {
        if (nivel <= 0) {
            return 0.0;
        }
        if (nivel < COSTOS_PROGRESION_CIENCIA.length) {
            return COSTOS_PROGRESION_CIENCIA[nivel];
        }
        return COSTOS_PROGRESION_CIENCIA[COSTOS_PROGRESION_CIENCIA.length - 1];
    }
}
