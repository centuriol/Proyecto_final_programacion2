package org.example.game.simulacion;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Configuración centralizada de la simulación para balanceo y tuning.
 * Todos los valores mágicos están aquí para fácil ajuste.
 */
public final class ConfiguracionSimulacion {

    // ===== Límites de masa (colocación) =====
    public static final double MASA_FACTOR_MIN = 0.3;
    public static final double MASA_FACTOR_MAX = 10.0;
    public static final double[] ESCALONES_MASA_MAX = {0.3, 0.6, 1.0, 2.0, 5.0, 10.0};
    public static final double[] COSTOS_ESCALON_MASA = {0.0, 100.0, 500.0, 1500.0, 3500.0, 8000.0};

    // ===== Costos de desbloqueo de progresión por ciencia (Escalado acelerado: 100, 500, 1500, 3500, 8000) =====
    public static final double COSTO_DESBLOQUEO_LUNA = 100.0;
    public static final double COSTO_DESBLOQUEO_PORRO = 500.0;
    public static final double COSTO_DESBLOQUEO_SATELITE = 1500.0;
    public static final double COSTO_DESBLOQUEO_ESCUDO = 3500.0;
    public static final double COSTO_DESBLOQUEO_LAVA = 8000.0;

    // ===== Capacidad máxima de planetas en simulación =====
    public static final int PLANETAS_INICIALES_MAX = 3;
    public static final int[] ESCALONES_CANTIDAD_PLANETAS = {3, 4, 5, 6, 8, 12};
    public static final double[] COSTOS_ESCALON_CANTIDAD_PLANETAS = {0.0, 100.0, 500.0, 1500.0, 3500.0, 8000.0};

    // ===== Niveles de escudo =====
    public static final int NIVEL_ESCUDO_MAX = 4;
    public static final double[] COSTOS_NIVEL_ESCUDO = {0.0, 100.0, 200.0, 350.0, 500.0};
    public static final double COSTO_ACTIVACION_ESCUDO_ENERGIA = 50.0;

    // ===== Producción de Ciencia =====
    public static final double PRODUCCION_ESTRELLA_CIENCIA = 0.06;

    // ===== Física (Delegada a ConstantesFisicas como única fuente de verdad) =====
    public static final double G_ESCALADO = org.example.game.motor.ConstantesFisicas.G_ESCALADO;
    public static final double SOFTENING = org.example.game.motor.ConstantesFisicas.SOFTENING;
    public static final double RADIO_MUNDO = org.example.game.motor.ConstantesFisicas.RADIO_MUNDO_DEFAULT;
    public static final double LIMITE_ESCAPE = RADIO_MUNDO * org.example.game.motor.ConstantesFisicas.LIMITE_ESCAPE_FACTOR;

    // ===== Umbrales de colisión delegados a ConfiguracionColisiones =====
    public static final double RATIO_COLISION_COLAPSO = ConfiguracionColisiones.RATIO_COLISION_COLAPSO;
    public static final double RATIO_ABSORCION_MASA = ConfiguracionColisiones.RATIO_ABSORCION_MASA;
    public static final int FRAGMENTOS_POR_COLAPSO = ConfiguracionColisiones.FRAGMENTOS_POR_COLAPSO;
    public static final double FRACCION_MASA_DISIPADA = ConfiguracionColisiones.FRACCION_MASA_DISIPADA;
    public static final double VELOCIDAD_EYECCION_FRAGMENTOS = ConfiguracionColisiones.VELOCIDAD_EYECCION_FRAGMENTOS;
    public static final double FRACCION_MASA_MINIMA_SOBREVIVIENTE = ConfiguracionColisiones.FRACCION_MASA_MINIMA_SOBREVIVIENTE;
    public static final double PORCENTAJE_GANANCIA_MASA_COLISION = ConfiguracionColisiones.PORCENTAJE_GANANCIA_MASA_COLISION;

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

    // ===== Producción por Tick delegada a ConfiguracionEconomia =====
    public static final double PRODUCCION_ESTRELLA_ENERGIA = ConfiguracionEconomia.PRODUCCION_ESTRELLA_ENERGIA;
    public static final double PRODUCCION_ESTRELLA_MINERALES = ConfiguracionEconomia.PRODUCCION_ESTRELLA_MINERALES;
    public static final double PRODUCCION_PLANETA_MINERALES = ConfiguracionEconomia.PRODUCCION_PLANETA_MINERALES;
    public static final double PRODUCCION_PLANETA_CIENCIA = ConfiguracionEconomia.PRODUCCION_PLANETA_CIENCIA;
    public static final double PRODUCCION_PLANETA_POBLACION = ConfiguracionEconomia.PRODUCCION_PLANETA_POBLACION;
    public static final double PRODUCCION_PLANETA_AGUA_MINERALES = 0.04;
    public static final double PRODUCCION_PLANETA_AGUA_CIENCIA = 0.03;
    public static final double PRODUCCION_PLANETA_AGUA_ENERGIA = 0.025;
    public static final double PRODUCCION_PLANETA_AGUA_POBLACION = 0.0;
    public static final double PRODUCCION_SATELITE_CIENCIA = 0.08;
    public static final double PRODUCCION_SATELITE_ENERGIA = 0.03;
    public static final double PRODUCCION_PLANETA_LAVA_MINERALES = 0.09;
    public static final double PRODUCCION_PLANETA_LAVA_CIENCIA = 0.06;
    public static final double PRODUCCION_PLANETA_LAVA_POBLACION = 0.0;

    // Luna: da Minerales
    public static final double PRODUCCION_LUNA_MINERALES = 0.035;

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

    // ===== Daño de Meteorito a Planeta (5% a 15% masa inicial, destrucción bajo 30%) =====
    public static final double DANO_METEORITO_MIN_FRACCION = 0.05;
    public static final double DANO_METEORITO_MAX_FRACCION = 0.15;
    public static final double UMBRAL_EXPLOSION_MASA_FRACCION = 0.30;
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
    public static final double COSTO_COLONIZAR_ENERGIA = 40.0;
    public static final double COSTO_COLONIZAR_MINERALES = 50.0;
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

    // Costos base (referencia, los reales están en InventarioJugador)
    public static final double COSTO_BASE_ESTRELLA = 1000;
    public static final double COSTO_BASE_PLANETA_ROCOSO = 100;
    public static final double COSTO_BASE_PLANETA_GASEOSO = 500;
    public static final double COSTO_BASE_AGUJERO_NEGRO = 500;

    private ConfiguracionSimulacion() {} // No instanciar
}