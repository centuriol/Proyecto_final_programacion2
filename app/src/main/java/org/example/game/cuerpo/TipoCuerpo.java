package org.example.game.cuerpo;

import org.example.game.simulacion.ConfiguracionSimulacion;

/**
 * Tipos de cuerpos celestes con propiedades físicas y de juego distintas.
 */
public enum TipoCuerpo {
    ESTRELLA(
        "Estrella",
        1.989e30,      // Masa base (kg) - masa solar
        6.96e8,        // Radio físico base (m) - radio solar
        5778,          // Temperatura superficial (K)
        true,          // Genera gravedad fuerte
        false,         // Puede tener civilización
        true,          // Es masivo (fuente de gravedad principal)
        0xFFD700       // Color dorado (hex ARGB)
    ),
    PLANETA_ROCOSO(
        "Planeta Rocoso",
        5.97e24,       // Masa Tierra
        6.37e6,        // Radio Tierra
        288,           // Temp media
        false,
        true,          // Puede tener civilización
        false,
        0x4169E1       // Azul real
    ),
    PLANETA_GASEOSO(
        "Gigante Gaseoso",
        1.90e27,       // Masa Júpiter
        7.00e7,        // Radio Júpiter
        165,
        false,
        false,         // No habitable en superficie
        true,          // Masivo (afecta otros)
        0xCD853F       // Marrón/naranja
    ),
    LUNA(
        "Luna con Cráteres",
        7.34e22,       // Masa Luna
        1.737e6,       // Radio Luna
        220,
        false,
        false,
        false,
        0xA0A5AB       // Gris lunar
    ),
    SATELITE(
        "Satélite Espacial",
        5.0e3,         // 5 toneladas
        10.0,          // 10 metros
        300,
        false,
        false,
        false,
        0x5BE3FF       // Cyan acento
    ),
    ESCUDO_DOME(
        "Cúpula de Escudo",
        1.0e5,         // Masa virtual
        500.0,         // Radio cobertura
        0,
        false,
        false,
        false,
        0x5BE3FF       // Cyan barrera
    ),
    METEORITO(
        "Meteorito",
        1e12,          // 1 billón kg (pequeño asteroide)
        50,            // 50m radio
        200,
        false,
        false,
        false,
        0xFF6B4A       // Naranja fuego
    ),
    AGUJERO_NEGRO(
        "Agujero Negro",
        1.989e31,      // ~10 masas solares (estelar)
        2.95e4,        // Radio Schwarzschild ~30km
        0,             // Hawking temp negligible
        true,          // Gravedad extrema
        false,
        true,
        0x1A1028       // Negro/violeta
    ),
    PLANETA_HELADO(
        "Planeta Helado",
        1.3e22,        // Masa tipo Plutón
        1.19e6,
        44,
        false,
        false,
        false,
        0xB0E0E6       // Azul claro
    ),
    AGUJERO_NEGRO_SUPERMASIVO(
        "Agujero Negro Supermasivo",
        1.989e37,      // ~10^7 masas solares (Sagitario A*)
        2.95e10,       // ~30 millones km
        0,
        true,
        false,
        true,
        0x100010       // Negro púrpura
    ),
    NAVE_COLONIZADORA(
        "Nave Colonizadora",
        1e6,           // 1000 toneladas
        100,           // 100m
        300,
        false,
        false,
        false,
        0x00FF00       // Verde brillante
    );

    public final String nombre;
    public final double masaBase;       // kg
    public final double radioFisicoBase; // metros
    public final double temperaturaBase; // Kelvin
    public final boolean generaGravedadFuerte;
    public final boolean puedeTenerCivilizacion;
    public final boolean esMasivo;       // Fuente principal de gravedad
    public final int colorHex;           // 0xRRGGBB

    TipoCuerpo(String nombre, double masaBase, double radioFisicoBase,
               double temperaturaBase, boolean generaGravedadFuerte,
               boolean puedeTenerCivilizacion, boolean esMasivo, int colorHex) {
        this.nombre = nombre;
        this.masaBase = masaBase;
        this.radioFisicoBase = radioFisicoBase;
        this.temperaturaBase = temperaturaBase;
        this.generaGravedadFuerte = generaGravedadFuerte;
        this.puedeTenerCivilizacion = puedeTenerCivilizacion;
        this.esMasivo = esMasivo;
        this.colorHex = colorHex;
    }

    public String getColorHexString() {
        return String.format("#%06X", colorHex);
    }

    /** Factor de multiplicación de masa (para sliders UI) */
    public double getMasaMin() { return masaBase * ConfiguracionSimulacion.MASA_FACTOR_MIN; }
    public double getMasaMax() { return masaBase * ConfiguracionSimulacion.MASA_FACTOR_MAX; }

    /**
     * Formatea la masa en unidades de Masas Terrestres (MT), donde 1e24 kg = 1 MT,
     * reemplazando la notación científica '1e'.
     */
    public static String formatearMasa(double masaKg) {
        double mt = masaKg / 1e24;
        if (mt >= 1_000_000_000.0) {
            return String.format(java.util.Locale.US, "%.2fB MT", mt / 1e9);
        } else if (mt >= 1_000_000.0) {
            return String.format(java.util.Locale.US, "%.2fM MT", mt / 1e6);
        } else if (mt >= 1_000.0) {
            return String.format(java.util.Locale.US, "%.2fk MT", mt / 1e3);
        } else if (mt >= 0.01) {
            return String.format(java.util.Locale.US, "%.2f MT", mt);
        } else if (mt >= 0.0001) {
            return String.format(java.util.Locale.US, "%.4f MT", mt);
        } else {
            return String.format(java.util.Locale.US, "%.6f MT", mt);
        }
    }
}