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
    public static final double COSTO_POBLAR_AGUA_CIENCIA = 300.0;
    public static final double COSTO_POBLAR_OTRO_CIENCIA = 450.0;
    public static final double COSTO_POBLAR_LAVA_CIENCIA = 750.0;
    public static final double UMBRAL_POBLACION_PORRO_POBLAR = 1000.0;
    public static final double FACTOR_CRECIMIENTO_OTROS_PLANETAS = 0.25;
    public static final double MULTIPLICADOR_PRODUCCION_PLANETA_POBLADO = 1.3;

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

    // ===== Costos de mini-mejoras secundarias por tier (menores al nodo principal de su tier) =====
    public static final double COSTO_SECUNDARIA_TIER_1 = 90.0;
    public static final double COSTO_SECUNDARIA_TIER_2 = 300.0;
    public static final double COSTO_SECUNDARIA_TIER_3 = 850.0;
    public static final double COSTO_SECUNDARIA_TIER_4 = 2000.0;
    public static final double COSTO_SECUNDARIA_TIER_5 = 4500.0;

    // ===== Valores de efecto de mini-mejoras secundarias por rama =====
    // Rama PLANETAS: +% produccion minerales/ciencia de planetas, -% costo planetas/lunas, -% costo activar escudo
    public static final double BONUS_PRODUCCION_PLANETAS_T1 = 0.12;
    public static final double DESCUENTO_COSTO_PLANETAS_LUNAS_T2 = 0.12;
    public static final double BONUS_PRODUCCION_PLANETAS_T3 = 0.18;
    public static final double DESCUENTO_COSTO_ESCUDO_T4 = 0.25;
    public static final double BONUS_PRODUCCION_PLANETAS_T5 = 0.25;
    public static final double DESCUENTO_COSTO_PLANETAS_LUNAS_T5 = 0.18;

    // Rama MASA: -% costo/exponente por masa extra, +% produccion por masa, -% costo de satelites
    public static final double DESCUENTO_COSTO_MASA_T1 = 0.10;
    public static final double BONUS_PRODUCCION_MASA_T2 = 0.15;
    public static final double DESCUENTO_COSTO_SATELITE_T3 = 0.20;
    public static final double DESCUENTO_COSTO_MASA_T4 = 0.15;
    public static final double BONUS_PRODUCCION_MASA_T5 = 0.25;
    public static final double DESCUENTO_COSTO_SATELITE_T5 = 0.20;

    // Rama CANTIDAD_PLANETAS: +% poblacion maxima, +% crecimiento poblacional, -% costo colonizar/poblar
    public static final double BONUS_POBLACION_MAXIMA_T1 = 0.15;
    public static final double BONUS_CRECIMIENTO_POBLACION_T2 = 0.15;
    public static final double DESCUENTO_COSTO_COLONIZAR_T3 = 0.20;
    public static final double BONUS_POBLACION_MAXIMA_T4 = 0.25;
    public static final double BONUS_CRECIMIENTO_POBLACION_T5 = 0.25;
    public static final double DESCUENTO_COSTO_COLONIZAR_T5 = 0.20;

    private static final double[] COSTOS_PROGRESION_CIENCIA = {
            0.0,
            COSTO_CIENCIA_NIVEL_1,
            COSTO_CIENCIA_NIVEL_2,
            COSTO_CIENCIA_NIVEL_3,
            COSTO_CIENCIA_NIVEL_4,
            COSTO_CIENCIA_NIVEL_5
    };

    private static final double[] COSTOS_SECUNDARIA_CIENCIA = {
            0.0,
            COSTO_SECUNDARIA_TIER_1,
            COSTO_SECUNDARIA_TIER_2,
            COSTO_SECUNDARIA_TIER_3,
            COSTO_SECUNDARIA_TIER_4,
            COSTO_SECUNDARIA_TIER_5
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

    public static double costoSecundariaTier(int tier) {
        if (tier <= 0) {
            return 0.0;
        }
        if (tier < COSTOS_SECUNDARIA_CIENCIA.length) {
            return COSTOS_SECUNDARIA_CIENCIA[tier];
        }
        return COSTOS_SECUNDARIA_CIENCIA[COSTOS_SECUNDARIA_CIENCIA.length - 1];
    }
}
