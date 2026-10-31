package org.example.game.cuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import java.util.EnumMap;
import java.util.Map;
/**
 * Tipos de cuerpos celestes con propiedades fisicas y de juego distintas.
 *
 * Principios SOLID:
 * - SRP: Identifica y tipifica entidades celestes. Descriptores y lore residen en CatalogoDescriptoresCuerpo.
 * - OCP: Categorias polimorficas y metadatos accesibles via getters y helpers semanticos.
 */
public enum TipoCuerpo {
    ESTRELLA(
        "Sol",
        1.989e30,      // Masa base (kg) - masa solar
        6.96e8,        // Radio fisico base (m) - radio solar
        5778,          // Temperatura superficial (K)
        true,          // Genera gravedad fuerte
        false,         // Puede tener civilizacion
        true,          // Es masivo (fuente de gravedad principal)
        0xFFD700,      // Color dorado (hex ARGB)
        CategoriaCuerpo.ESTRELLA
    ),
    PLANETA_ROCOSO(
        "Planeta Porro",
        5.97e24,       // Masa Tierra
        6.37e6,        // Radio Tierra
        288,           // Temp media
        false,
        true,          // Puede tener civilizacion
        false,
        0x4169E1,      // Azul real
        CategoriaCuerpo.PLANETA
    ),
    PLANETA_AGUA(
        "Planeta de Agua",
        5.97e24,       // Masa similar a Planeta Porro
        6.37e6,        // Radio similar a Planeta Porro
        285,           // Temp media acuatica
        false,
        false,         // No genera poblacion
        false,
        0x00BFFF,      // Deep Sky Blue
        CategoriaCuerpo.PLANETA
    ),
    PLANETA_LAVA(
        "Planeta Lava",
        5.97e24,       // Masa similar a Planeta Porro
        6.37e6,        // Radio similar a Planeta Porro
        1400,          // Temp media magmatica
        false,
        false,         // No genera poblacion
        false,
        0xFF4500,      // OrangeRed / Rojo lava
        CategoriaCuerpo.PLANETA
    ),
    PLANETA_GASEOSO(
        "Gigante Gaseoso",
        1.90e27,       // Masa Jupiter
        7.00e7,        // Radio Jupiter
        165,
        false,
        false,         // No habitable en superficie
        true,          // Masivo (afecta otros)
        0xCD853F,      // Marron/naranja
        CategoriaCuerpo.PLANETA
    ),
    LUNA(
        "Luna",
        7.34e22,       // Masa Luna
        1.737e6,       // Radio Luna
        220,
        false,
        false,
        false,
        0xA0A5AB,      // Gris lunar
        CategoriaCuerpo.LUNA
    ),
    SATELITE(
        "Satelite Espacial",
        5.0e3,         // 5 toneladas
        10.0,          // 10 metros
        300,
        false,
        false,
        false,
        0x5BE3FF,      // Cyan acento
        CategoriaCuerpo.ESTRUCTURA
    ),
    METEORITO(
        "Meteorito",
        1e20,          // Asteroide masivo de alta densidad y capacidad destructiva
        50,            // 50m radio
        200,
        false,
        false,
        false,
        0xFF6B4A,      // Naranja fuego
        CategoriaCuerpo.ESCOMBRO
    ),
    AGUJERO_NEGRO(
        "Agujero Negro",
        1.989e31,      // ~10 masas solares (estelar)
        2.95e4,        // Radio Schwarzschild ~30km
        0,             // Hawking temp negligible
        true,          // Gravedad extrema
        false,
        true,
        0x1A1028,      // Negro/violeta
        CategoriaCuerpo.AGUJERO_NEGRO
    ),
    PLANETA_HELADO(
        "Planeta Helado",
        1.3e22,        // Masa tipo Pluton
        1.19e6,
        44,
        false,
        false,
        false,
        0xB0E0E6,      // Azul claro
        CategoriaCuerpo.PLANETA
    ),
    AGUJERO_NEGRO_SUPERMASIVO(
        "Agujero Negro Supermasivo",
        1.989e37,      // ~10^7 masas solares (Sagitario A*)
        2.95e10,       // ~30 millones km
        0,
        true,
        false,
        true,
        0x100010,      // Negro purpura
        CategoriaCuerpo.AGUJERO_NEGRO
    ),
    NAVE_COLONIZADORA(
        "Nave Colonizadora",
        1e6,           // 1000 toneladas
        100,           // 100m
        300,
        false,
        false,
        false,
        0x00FF00,      // Verde brillante
        CategoriaCuerpo.ESTRUCTURA
    );

    public enum CategoriaCuerpo {
        ESTRELLA,
        PLANETA,
        LUNA,
        AGUJERO_NEGRO,
        ESTRUCTURA,
        ESCOMBRO,
        OTRO
    }

    // Campos públicos finales mantenidos para compatibilidad
    public final String nombre;
    public final double masaBase;       // kg
    public final double radioFisicoBase; // metros
    public final double temperaturaBase; // Kelvin
    public final boolean generaGravedadFuerte;
    public final boolean puedeTenerCivilizacion;
    public final boolean esMasivo;       // Fuente principal de gravedad
    public final int colorHex;           // 0xRRGGBB
    public final CategoriaCuerpo categoria;

    TipoCuerpo(String nombre, double masaBase, double radioFisicoBase,
               double temperaturaBase, boolean generaGravedadFuerte,
               boolean puedeTenerCivilizacion, boolean esMasivo, int colorHex,
               CategoriaCuerpo categoria) {
        this.nombre = nombre;
        this.masaBase = masaBase;
        this.radioFisicoBase = radioFisicoBase;
        this.temperaturaBase = temperaturaBase;
        this.generaGravedadFuerte = generaGravedadFuerte;
        this.puedeTenerCivilizacion = puedeTenerCivilizacion;
        this.esMasivo = esMasivo;
        this.colorHex = colorHex;
        this.categoria = categoria;
    }

    TipoCuerpo(String nombre, double masaBase, double radioFisicoBase,
               double temperaturaBase, boolean generaGravedadFuerte,
               boolean puedeTenerCivilizacion, boolean esMasivo, int colorHex) {
        this(nombre, masaBase, radioFisicoBase, temperaturaBase, generaGravedadFuerte, puedeTenerCivilizacion, esMasivo, colorHex, CategoriaCuerpo.OTRO);
    }

    public String getNombre() { return nombre; }
    public double getMasaBase() { return masaBase; }
    public double getRadioFisicoBase() { return radioFisicoBase; }
    public double getTemperaturaBase() { return temperaturaBase; }
    public boolean isGeneraGravedadFuerte() { return generaGravedadFuerte; }
    public boolean isPuedeTenerCivilizacion() { return puedeTenerCivilizacion; }
    public boolean isEsMasivo() { return esMasivo; }
    public int getColorHex() { return colorHex; }
    public CategoriaCuerpo getCategoria() { return categoria; }

    public String getColorHexString() {
        return String.format("#%06X", colorHex);
    }

    private static final Map<TipoCuerpo, Double> FACTORES_CAPACIDAD_POBLACION = new EnumMap<>(TipoCuerpo.class);

    static {
        FACTORES_CAPACIDAD_POBLACION.put(PLANETA_ROCOSO, 1.0);
        FACTORES_CAPACIDAD_POBLACION.put(PLANETA_AGUA, 0.6);
        FACTORES_CAPACIDAD_POBLACION.put(PLANETA_LAVA, 0.3);
    }

    public double getFactorCapacidadPoblacion() {
        return FACTORES_CAPACIDAD_POBLACION.getOrDefault(this, 0.0);
    }

    public boolean esPlaneta() {
        return categoria == CategoriaCuerpo.PLANETA;
    }

    public boolean esAgujeroNegro() {
        return categoria == CategoriaCuerpo.AGUJERO_NEGRO;
    }

    public boolean esEstrella() {
        return categoria == CategoriaCuerpo.ESTRELLA;
    }

    public boolean esLuna() {
        return categoria == CategoriaCuerpo.LUNA;
    }

    public boolean esAtractorOrbital() {
        return esEstrella() || esAgujeroNegro();
    }

    public boolean esMenorOrbital() {
        return this == LUNA || this == SATELITE;
    }

    public int getNivelRequerido() {
        return switch (this) {
            case LUNA -> 1;
            case PLANETA_ROCOSO -> 2;
            case SATELITE -> 3;
            case PLANETA_LAVA -> 5;
            default -> 0;
        };
    }

    public double getCostoDesbloqueoCiencia() {
        return switch (this) {
            case LUNA -> 100.0;
            case PLANETA_ROCOSO -> 200.0;
            case SATELITE -> 350.0;
            case PLANETA_LAVA -> 800.0;
            default -> 0.0;
        };
    }

    public boolean permiteAjusteMasa() {
        return this != SATELITE;
    }

    public double ajustarFactorMasaEfectivo(double factorMasa) {
        return permiteAjusteMasa() ? factorMasa : 1.0;
    }

    public double getMasaMin() { return masaBase * ConfiguracionSimulacion.MASA_FACTOR_MIN; }
    public double getMasaMax() { return masaBase * ConfiguracionSimulacion.MASA_FACTOR_MAX; }

    public static String formatearMasa(double masaKg) {
        return FormateadorMasa.formatearMasa(masaKg);
    }

    private DescriptorTipoCuerpo getDescriptor() {
        return CatalogoDescriptoresCuerpo.obtenerDescriptor(this);
    }

    public Map<TipoRecurso, Double> getCostosBase() {
        return getDescriptor().costosBase();
    }

    public Map<TipoRecurso, Double> getProduccionBase() {
        return getDescriptor().produccionBase();
    }

    public double getRadioAtraccionMin() {
        return getDescriptor().radioAtraccionMin();
    }

    public double getRadioAtraccionMax() {
        return getDescriptor().radioAtraccionMax();
    }
}
