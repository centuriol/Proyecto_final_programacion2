package org.example.game.simulacion;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Configuración centralizada de la simulación para balanceo y tuning.
 * Todos los valores mágicos están aquí para fácil ajuste.
 */
public final class ConfiguracionSimulacion {

    // ===== Límites de masa (colocación) =====
    public static final double MASA_FACTOR_MIN = 1.0;
    public static final double MASA_FACTOR_MAX = 10.0;

    // ===== Física =====
    public static final double G_ESCALADO = 6.67430e-11 * 3600 * 3600 / 1e27; // Ver MotorFisicaNewtoniana
    public static final double SOFTENING = 10.0;
    public static final double RADIO_MUNDO = 10000.0;
    public static final double LIMITE_ESCAPE = RADIO_MUNDO * 2;

    // ===== Umbrales de colisión por proporción de masa =====
    // r = masaMayor / masaMenor (siempre >= 1.0)
    public static final double RATIO_COLISION_COLAPSO = 1.3;   // masas "casi iguales"
    public static final double RATIO_ABSORCION_MASA = 8.0;     // masas muy dispares -> absorción total

    // ===== Escombros (fragmentos de colisión) =====
    public static final int FRAGMENTOS_POR_COLAPSO = 3;         // cantidad de meteoritos generados por cuerpo destruido
    public static final double FRACCION_MASA_DISIPADA = 0.10;   // 10% de la masa se "pierde" en la explosión (arcade, no 100% realista)
    public static final double VELOCIDAD_EYECCION_FRAGMENTOS = 150.0; // px/tick base de dispersión
    public static final double FRACCION_MASA_MINIMA_SOBREVIVIENTE = 0.05; // 5% de la masa base como piso mínimo en colisión asimétrica

    // ===== Ticks y tiempo =====
    public static final int TICKS_POR_SEGUNDO_REAL = 20; // Velocidad base
    public static final double SEGUNDOS_REALES_POR_TICK = 1.0 / TICKS_POR_SEGUNDO_REAL;
    public static final int MAX_TICKS_HISTORIAL = 10000; // Para gráficas futuras

    // ===== Costos Base de Colocación (Materiales requeridos para poner el ítem) =====
    public static final double COSTO_ESTRELLA_MINERALES = 300.0;
    public static final double COSTO_ESTRELLA_ENERGIA = 150.0;
    public static final double COSTO_ESTRELLA_CIENCIA = 50.0;

    public static final double COSTO_PLANETA_MINERALES = 100.0;
    public static final double COSTO_PLANETA_ENERGIA = 50.0;

    public static final double COSTO_LUNA_MINERALES = 50.0;
    public static final double COSTO_LUNA_ENERGIA = 20.0;

    // ===== Producción por Tick (Materiales generados por cada ítem - Crecimiento lento) =====
    // Estrella: da Energía y Minerales
    public static final double PRODUCCION_ESTRELLA_ENERGIA = 0.05;
    public static final double PRODUCCION_ESTRELLA_MINERALES = 0.02;

    // Planeta: da Minerales, Ciencia y Población
    public static final double PRODUCCION_PLANETA_MINERALES = 0.03;
    public static final double PRODUCCION_PLANETA_CIENCIA = 0.02;
    public static final double PRODUCCION_PLANETA_POBLACION = 0.01;

    // Luna: da Minerales
    public static final double PRODUCCION_LUNA_MINERALES = 0.02;

    // Modificadores pasivos legacy mantenidos para compatibilidad interna
    public static final double GANANCIA_PASIVA_POR_TICK = 0.0;
    public static final double GANANCIA_PASIVA_ENERGIA_POR_TICK = 0.0;
    public static final double GANANCIA_PASIVA_MINERALES_POR_TICK = 0.0;
    public static final double GANANCIA_SATELITE_CIENCIA = 0.02;
    public static final double GANANCIA_SATELITE_ENERGIA = 0.03;
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
    public static final double CONSUMO_ENERGIA_ESCUDO_IMPACTO = 20.0;
    public static final double RECOMPENSA_CIENCIA_ESCUDO_IMPACTO = 10.0;

    // ===== Civilizaciones (Crecimiento lento) =====
    public static final double DISTANCIA_ZONA_HABITABLE_MIN = 0.7;
    public static final double DISTANCIA_ZONA_HABITABLE_MAX = 1.5;
    public static final double CRECIMIENTO_POBLACION_BASE = 0.00005; // Crecimiento sustancialmente más lento
    public static final double DECADENCIA_FUERA_ZONA = 0.0001;
    public static final double AMENAZA_AGUJERO_NEGRO_RADIO = 500.0;
    public static final double POBLACION_INICIAL = 1000;
    public static final double POBLACION_MAXIMA_FACTOR = 1e6;

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