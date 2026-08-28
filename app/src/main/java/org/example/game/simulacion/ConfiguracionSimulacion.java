package org.example.game.simulacion;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Configuración centralizada de la simulación para balanceo y tuning.
 * Todos los valores mágicos están aquí para fácil ajuste.
 */
public final class ConfiguracionSimulacion {

    // ===== Física =====
    public static final double G_ESCALADO = 6.67430e-11 * 3600 * 3600 / 1e27; // Ver MotorFisicaNewtoniana
    public static final double SOFTENING = 10.0;
    public static final double RADIO_MUNDO = 10000.0;
    public static final double LIMITE_ESCAPE = RADIO_MUNDO * 2;

    // ===== Ticks y tiempo =====
    public static final int TICKS_POR_SEGUNDO_REAL = 20; // Velocidad base
    public static final double SEGUNDOS_REALES_POR_TICK = 1.0 / TICKS_POR_SEGUNDO_REAL;
    public static final int MAX_TICKS_HISTORIAL = 10000; // Para gráficas futuras

    // ===== Economía =====
    public static final double GANANCIA_PASIVA_POR_TICK = 0.1; // Materia planetaria/tick
    public static final double BONUS_CIV_PROSPERA = 5.0; // Bonus por civilización próspera/tick
    public static final double BONUS_CIV_LUCHANDO = 1.0; // Civilización en peligro
    public static final double COSTO_MANTENIMIENTO_ESTRELLA = 0.5; // Energía/tick por estrella

    // ===== Civilizaciones =====
    public static final double DISTANCIA_ZONA_HABITABLE_MIN = 0.7; // Factor relativo a Tierra
    public static final double DISTANCIA_ZONA_HABITABLE_MAX = 1.5;
    public static final double CRECIMIENTO_POBLACION_BASE = 0.001; // % por tick en zona ideal
    public static final double DECADENCIA_FUERA_ZONA = 0.005; // % por tick fuera de zona
    public static final double AMENAZA_AGUJERO_NEGRO_RADIO = 500.0; // px - radio de influencia negativa
    public static final double POBLACION_INICIAL = 1_000_000;
    public static final double POBLACION_MAXIMA_FACTOR = 1e10; // Por masa planeta

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