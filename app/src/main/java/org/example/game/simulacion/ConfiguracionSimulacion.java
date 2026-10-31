package org.example.game.simulacion;
import org.example.game.cuerpo.TipoCuerpo;
/**
 * Configuración centralizada de la simulación para balanceo y tuning.
 * Todos los valores mágicos están aquí para fácil ajuste.
 */
public final class ConfiguracionSimulacion {

    // ===== Límites de masa (colocación) =====
    public static final double MASA_FACTOR_MIN = ConfiguracionEconomia.FACTOR_MASA_INICIAL;
    public static final double MASA_FACTOR_MAX = 10.0;
    public static final double[] ESCALONES_MASA_MAX = {MASA_FACTOR_MIN, 0.6, 1.0, 2.0, 5.0, 10.0};
    public static final double[] COSTOS_ESCALON_MASA = {
            0.0,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5
    };

    // ===== Costos de desbloqueo de progresión por ciencia delegados a ConfiguracionEconomia =====
    public static final double COSTO_DESBLOQUEO_LUNA = ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1;
    public static final double COSTO_DESBLOQUEO_PORRO = ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2;
    public static final double COSTO_DESBLOQUEO_SATELITE = ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3;
    public static final double COSTO_DESBLOQUEO_ESCUDO = ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4;
    public static final double COSTO_DESBLOQUEO_LAVA = ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5;

    // ===== Capacidad máxima de planetas en simulación =====
    public static final int PLANETAS_INICIALES_MAX = 3;
    public static final int[] ESCALONES_CANTIDAD_PLANETAS = {3, 4, 5, 6, 8, 12};
    public static final double[] COSTOS_ESCALON_CANTIDAD_PLANETAS = {
            0.0,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4,
            ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5
    };

    // ===== Niveles de escudo =====
    public static final int NIVEL_ESCUDO_MAX = 4;
    public static final double[] COSTOS_NIVEL_ESCUDO = {
            0.0,
            ConfiguracionEconomia.COSTO_ESCUDO_NIVEL_1,
            ConfiguracionEconomia.COSTO_ESCUDO_NIVEL_2,
            ConfiguracionEconomia.COSTO_ESCUDO_NIVEL_3,
            ConfiguracionEconomia.COSTO_ESCUDO_NIVEL_4
    };
    public static final double COSTO_ACTIVACION_ESCUDO_ENERGIA = ConfiguracionEconomia.COSTO_ACTIVACION_ESCUDO_ENERGIA;

    // ===== Producción de Ciencia =====
    public static final double PRODUCCION_ESTRELLA_CIENCIA = ConfiguracionEconomia.PRODUCCION_ESTRELLA_CIENCIA;

    // ===== Física (Delegada a ConstantesFisicas como única fuente de verdad) =====
    public static final double G_ESCALADO = org.example.game.motor.ConstantesFisicas.G_ESCALADO;
    public static final double SOFTENING = org.example.game.motor.ConstantesFisicas.SOFTENING;
    public static final double RADIO_MUNDO = org.example.game.motor.ConstantesFisicas.RADIO_MUNDO_DEFAULT;
    public static final double LIMITE_ESCAPE = RADIO_MUNDO * org.example.game.motor.ConstantesFisicas.LIMITE_ESCAPE_FACTOR;

    // ===== Umbrales de colisión delegados a ConfiguracionColisiones =====
    public static final double RATIO_COLISION_COLAPSO = ConfiguracionColisiones.RATIO_COLISION_COLAPSO;
    public static final double RATIO_ABSORCION_MASA = ConfiguracionColisiones.RATIO_ABSORCION_MASA;
    public static final int FRAGMENTOS_POR_COLAPSO = ConfiguracionColisiones.FRAGMENTOS_POR_COLAPSO;
    public static final int MAX_FRAGMENTOS_POR_COLAPSO = ConfiguracionColisiones.MAX_FRAGMENTOS_POR_COLAPSO;
    public static final double FRACCION_MASA_DISIPADA = ConfiguracionColisiones.FRACCION_MASA_DISIPADA;
    public static final double VELOCIDAD_EYECCION_FRAGMENTOS = ConfiguracionColisiones.VELOCIDAD_EYECCION_FRAGMENTOS;
    public static final double FRACCION_MASA_MINIMA_SOBREVIVIENTE = ConfiguracionColisiones.FRACCION_MASA_MINIMA_SOBREVIVIENTE;
    public static final double PORCENTAJE_GANANCIA_MASA_COLISION = ConfiguracionColisiones.PORCENTAJE_GANANCIA_MASA_COLISION;
    public static final double MASA_MAXIMA_METEORITO = ConfiguracionColisiones.MASA_MAXIMA_METEORITO;
    public static final double RADIO_MINIMO_METEORITO = ConfiguracionColisiones.RADIO_MINIMO_METEORITO;
    public static final double RADIO_MAXIMO_METEORITO = ConfiguracionColisiones.RADIO_MAXIMO_METEORITO;
    public static final double RADIO_INTERCEPCION_SATELITE = ConfiguracionColisiones.RADIO_INTERCEPCION_SATELITE;

    // ===== Transformacion de cuerpos delegada a ConfiguracionTransformacion =====
    public static final double RADIO_TRANSFORMACION_AGUA_PORRO = ConfiguracionTransformacion.RADIO_TRANSFORMACION_AGUA_PORRO;
    public static final double RADIO_TRANSFORMACION_PORRO_LAVA = ConfiguracionTransformacion.RADIO_TRANSFORMACION_PORRO_LAVA;
    public static final int TICKS_ESPERA_TRANSFORMACION_EASTER_EGG = ConfiguracionTransformacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG;
    public static final int TICKS_MIGRACION_ORBITA = ConfiguracionTransformacion.TICKS_MIGRACION_ORBITA;
    public static final double MARGEN_SEGURIDAD_ORBITA_LIBRE = ConfiguracionTransformacion.MARGEN_SEGURIDAD_ORBITA_LIBRE;
    public static final double PASO_BUSQUEDA_ORBITA_PX = ConfiguracionTransformacion.PASO_BUSQUEDA_ORBITA_PX;
    public static final int MAX_INTENTOS_ORBITA_LIBRE = ConfiguracionTransformacion.MAX_INTENTOS_ORBITA_LIBRE;

    // ===== Ticks y tiempo =====
    public static final int TICKS_POR_SEGUNDO_REAL = 20; // Velocidad base
    public static final double SEGUNDOS_REALES_POR_TICK = 1.0 / TICKS_POR_SEGUNDO_REAL;
    public static final int MAX_TICKS_HISTORIAL = 10000; // Para gráficas futuras

    // ===== Costos Base de Colocación delegados a ConfiguracionEconomia =====
    public static final double COSTO_ESTRELLA_MINERALES = ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES;
    public static final double COSTO_ESTRELLA_ENERGIA = ConfiguracionEconomia.COSTO_ESTRELLA_ENERGIA;
    public static final double COSTO_PLANETA_MINERALES = ConfiguracionEconomia.COSTO_PLANETA_MINERALES;
    public static final double COSTO_PLANETA_ENERGIA = ConfiguracionEconomia.COSTO_PLANETA_ENERGIA;
    public static final double COSTO_LUNA_MINERALES = ConfiguracionEconomia.COSTO_LUNA_MINERALES;
    public static final double COSTO_LUNA_ENERGIA = ConfiguracionEconomia.COSTO_LUNA_ENERGIA;
    public static final double COSTO_PLANETA_AGUA_MINERALES = ConfiguracionEconomia.COSTO_PLANETA_AGUA_MINERALES;
    public static final double COSTO_PLANETA_AGUA_ENERGIA = ConfiguracionEconomia.COSTO_PLANETA_AGUA_ENERGIA;
    public static final double COSTO_SATELITE_MINERALES = ConfiguracionEconomia.COSTO_SATELITE_MINERALES;
    public static final double COSTO_SATELITE_ENERGIA = ConfiguracionEconomia.COSTO_SATELITE_ENERGIA;
    public static final double COSTO_PLANETA_LAVA_MINERALES = ConfiguracionEconomia.COSTO_PLANETA_LAVA_MINERALES;
    public static final double COSTO_PLANETA_LAVA_ENERGIA = ConfiguracionEconomia.COSTO_PLANETA_LAVA_ENERGIA;
    public static final double COSTO_PLANETA_GASEOSO_MINERALES = ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES;
    public static final double COSTO_PLANETA_GASEOSO_ENERGIA = ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_ENERGIA;
    public static final double COSTO_PLANETA_HELADO_MINERALES = ConfiguracionEconomia.COSTO_PLANETA_HELADO_MINERALES;
    public static final double COSTO_PLANETA_HELADO_ENERGIA = ConfiguracionEconomia.COSTO_PLANETA_HELADO_ENERGIA;

    // ===== Producción por Tick delegada a ConfiguracionEconomia =====
    public static final double PRODUCCION_ESTRELLA_ENERGIA = ConfiguracionEconomia.PRODUCCION_ESTRELLA_ENERGIA;
    public static final double PRODUCCION_ESTRELLA_MINERALES = ConfiguracionEconomia.PRODUCCION_ESTRELLA_MINERALES;
    public static final double PRODUCCION_PLANETA_MINERALES = ConfiguracionEconomia.PRODUCCION_PLANETA_MINERALES;
    public static final double PRODUCCION_PLANETA_CIENCIA = ConfiguracionEconomia.PRODUCCION_PLANETA_CIENCIA;
    public static final double PRODUCCION_PLANETA_POBLACION = ConfiguracionEconomia.PRODUCCION_PLANETA_POBLACION;
    public static final double PRODUCCION_PLANETA_AGUA_MINERALES = ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_MINERALES;
    public static final double PRODUCCION_PLANETA_AGUA_CIENCIA = ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_CIENCIA;
    public static final double PRODUCCION_PLANETA_AGUA_ENERGIA = ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_ENERGIA;
    public static final double PRODUCCION_PLANETA_AGUA_POBLACION = 0.0;
    public static final double PRODUCCION_SATELITE_CIENCIA = ConfiguracionEconomia.PRODUCCION_SATELITE_CIENCIA;
    public static final double PRODUCCION_SATELITE_ENERGIA = ConfiguracionEconomia.PRODUCCION_SATELITE_ENERGIA;
    public static final double PRODUCCION_PLANETA_LAVA_MINERALES = ConfiguracionEconomia.PRODUCCION_PLANETA_LAVA_MINERALES;
    public static final double PRODUCCION_PLANETA_LAVA_CIENCIA = ConfiguracionEconomia.PRODUCCION_PLANETA_LAVA_CIENCIA;
    public static final double PRODUCCION_PLANETA_LAVA_POBLACION = 0.0;
    public static final double PRODUCCION_PLANETA_GASEOSO_MINERALES = ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_MINERALES;
    public static final double PRODUCCION_PLANETA_GASEOSO_CIENCIA = ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_CIENCIA;
    public static final double PRODUCCION_PLANETA_GASEOSO_ENERGIA = ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_ENERGIA;
    public static final double PRODUCCION_PLANETA_HELADO_MINERALES = ConfiguracionEconomia.PRODUCCION_PLANETA_HELADO_MINERALES;
    public static final double PRODUCCION_PLANETA_HELADO_CIENCIA = ConfiguracionEconomia.PRODUCCION_PLANETA_HELADO_CIENCIA;
    public static final double PRODUCCION_PLANETA_HELADO_ENERGIA = ConfiguracionEconomia.PRODUCCION_PLANETA_HELADO_ENERGIA;

    // Luna: da Minerales
    public static final double PRODUCCION_LUNA_MINERALES = ConfiguracionEconomia.PRODUCCION_LUNA_MINERALES;

    // Modificadores pasivos legacy mantenidos para compatibilidad interna
    public static final double GANANCIA_PASIVA_POR_TICK = 0.0;
    public static final double GANANCIA_PASIVA_ENERGIA_POR_TICK = 0.0;
    public static final double GANANCIA_PASIVA_MINERALES_POR_TICK = 0.0;
    public static final double GANANCIA_SATELITE_CIENCIA = PRODUCCION_SATELITE_CIENCIA;
    public static final double GANANCIA_SATELITE_ENERGIA = 0.0;
    public static final double BONUS_CIV_PROSPERA = 1.0;
    public static final double BONUS_CIV_LUCHANDO = 0.5;
    public static final double BONUS_CIV_MINERALES_FACTOR = 0.10;
    public static final double BONUS_CIV_CIENCIA_FACTOR = 0.10;
    public static final double BONUS_CIV_ENERGIA_FACTOR = 0.0;
    public static final double COSTO_MANTENIMIENTO_ESTRELLA = 0.0;
    public static final double GENERACION_PASIVA_POR_UNIDAD_MASA = 0.000001;

    // ===== Modificadores Dinamicos de Economia =====
    public static final double MULTIPLICADOR_SINERGIA_CIVILIZACIONES = 0.05;
    public static final double MULTIPLICADOR_TECNOLOGIA_CIV = 0.02;
    public static final double PENALIZACION_EVENTO_CATASTROFE = 0.50;
    public static final int DURACION_TICKS_EFECTO_EVENTO = 100;

    // ===== Daño progresivo de Meteorito a Planeta delegado a ConfiguracionColisiones =====
    public static final double DANO_METEORITO_MIN_FRACCION = ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION;
    public static final double DANO_METEORITO_PROGRESO_BAJO_MAX_FRACCION = ConfiguracionColisiones.DANO_METEORITO_PROGRESO_BAJO_MAX_FRACCION;
    public static final double DANO_METEORITO_MAX_FRACCION = ConfiguracionColisiones.DANO_METEORITO_PROGRESO_ALTO_FRACCION;
    public static final double UMBRAL_EXPLOSION_MASA_FRACCION = ConfiguracionColisiones.UMBRAL_EXPLOSION_MASA_FRACCION;
    public static final double GUIADO_METEORITO = 0.05;
    public static final double VELOCIDAD_METEORITO_PX_TICK = 3.0;

    // ===== Civilizaciones (Crecimiento logístico lento) =====
    public static final double DISTANCIA_ZONA_HABITABLE_MIN = 0.7;
    public static final double DISTANCIA_ZONA_HABITABLE_MAX = 1.5;
    // Calibración logística: r = 0.0008, con K=1e6 y x0=1000, alcanzar 80% (800.000) toma ~10366 ticks (~8.6 min a 1x/20 tps)
    public static final double CRECIMIENTO_POBLACION_BASE = 0.0008;
    public static final double DECADENCIA_FUERA_ZONA = 0.0001;
    public static final double AMENAZA_AGUJERO_NEGRO_RADIO = 500.0;
    public static final double POBLACION_INICIAL = 1000;
    public static final double POBLACION_MAXIMA_FACTOR = 1e6;

    // Costo de lanzamiento de nave colonizadora
    public static final double COSTO_COLONIZAR_ENERGIA = ConfiguracionEconomia.COSTO_COLONIZAR_ENERGIA;
    public static final double COSTO_COLONIZAR_MINERALES = ConfiguracionEconomia.COSTO_COLONIZAR_MINERALES;
    public static final double PORCENTAJE_POBLACION_COLONIZADORA = 0.10; // 10%
    public static final double UMBRAL_COLONIZAR_PORCENTAJE = 80.0; // 80% de capacidad
    public static final double VELOCIDAD_NAVE_COLONIZADORA = 4.0; // px/tick

    // ===== Eventos cósmicos =====
    public static final int INTERVALO_METEORITOS_ALEATORIOS = 500; // ticks
    public static final double PROB_METEORITO_POR_TICK = 1.0 / INTERVALO_METEORITOS_ALEATORIOS;
    public static final int MAX_METEORITOS_SIMULTANEOS = 20;

    // ===== Spawn/Colocación =====
    public static final double DISTANCIA_MINIMA_SPAWN = 50.0; // px entre cuerpos al spawnear
    public static final double PREVIEW_ORBITA_PUNTOS = 100; // Puntos para dibujar órbita preview

    // ===== UI =====
    public static final int PANEL_LATERAL_ANCHO = 280;
    public static final int HUD_ALTO = 60;

    // Costos base delegados a ConfiguracionEconomia (sin numeros sueltos)
    public static final double COSTO_BASE_ESTRELLA = ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES;
    public static final double COSTO_BASE_PLANETA_ROCOSO = ConfiguracionEconomia.COSTO_PLANETA_MINERALES;
    public static final double COSTO_BASE_PLANETA_GASEOSO = ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES;
    public static final double COSTO_BASE_AGUJERO_NEGRO = ConfiguracionEconomia.COSTO_REFERENCIA_AGUJERO_NEGRO;

    private ConfiguracionSimulacion() {} // No instanciar
}
