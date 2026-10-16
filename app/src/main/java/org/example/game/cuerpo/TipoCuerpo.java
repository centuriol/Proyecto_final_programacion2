package org.example.game.cuerpo;

import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tipos de cuerpos celestes con propiedades fisicas y de juego distintas.
 *
 * Principios SOLID:
 * - SRP: Identifica y tipifica entidades celestes. Los metadatos de presentacion y balance residen en DescriptorTipoCuerpo.
 * - OCP: Categorias polimorficas y registro de descriptores sin bloques switch expansivos.
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
    ESCUDO_DOME(
        "Cupula de Escudo",
        1.0e5,         // Masa virtual
        500.0,         // Radio cobertura
        0,
        false,
        false,
        false,
        0x5BE3FF,      // Cyan barrera
        CategoriaCuerpo.ESTRUCTURA
    ),
    METEORITO(
        "Meteorito",
        1e12,          // 1 billon kg (pequeno asteroide)
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

    public final String nombre;
    public final double masaBase;       // kg
    public final double radioFisicoBase; // metros
    public final double temperaturaBase; // Kelvin
    public final boolean generaGravedadFuerte;
    public final boolean puedeTenerCivilizacion;
    public final boolean esMasivo;       // Fuente principal de gravedad
    public final int colorHex;           // 0xRRGGBB
    public final CategoriaCuerpo categoria;

    private static final Map<TipoCuerpo, DescriptorTipoCuerpo> DESCRIPTORES = new EnumMap<>(TipoCuerpo.class);

    static {
        Map<TipoRecurso, Double> costosPlanetaBase = Map.of(
                TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_PLANETA_MINERALES,
                TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA
        );
        Map<TipoRecurso, Double> prodPlanetaBase = Map.of(
                TipoRecurso.MINERALES, ConfiguracionSimulacion.PRODUCCION_PLANETA_MINERALES,
                TipoRecurso.CIENCIA, ConfiguracionSimulacion.PRODUCCION_PLANETA_CIENCIA,
                TipoRecurso.POBLACION, ConfiguracionSimulacion.PRODUCCION_PLANETA_POBLACION
        );

        // Sol
        DESCRIPTORES.put(ESTRELLA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_ESTRELLA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_ESTRELLA_ENERGIA,
                        TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_ESTRELLA_CIENCIA
                ),
                Map.of(
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA,
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.PRODUCCION_ESTRELLA_MINERALES
                ),
                65.0, 85.0,
                "Sol", "[SOL]",
                "Sol • Reactor Termonuclear Natural",
                "El Sol: Una colosal masa de plasma autosostenida por equilibrio hidrostatico. En su nucleo ardiente se desarrollan procesos de fusion que emiten inmensos torrentes de radiacion, convirtiendola en la generadora primaria de energia para todo el sistema solar. Sus rafagas y vientos estelares cargados de iones pesados dispersan ademas minerales utiles a traves del espacio.",
                "audio/sol.mp3"
        ));

        // Planeta Porro
        DESCRIPTORES.put(PLANETA_ROCOSO, new DescriptorTipoCuerpo(
                costosPlanetaBase,
                prodPlanetaBase,
                35.0, 55.0,
                "Planeta Porro", "[POR]",
                "Planeta Porro • Fuente de Poblacion y Ciencia",
                "Planeta Porro: Un mundo geologicamente diferenciado provisto de corteza densa, atmosfera y capas ricas en compuestos pesados. Alberga biomas y condiciones idoneas para el desarrollo de vida organizada. A medida que su poblacion colonizadora prolifera, se establecen academias de ciencia para la investigacion e industrias mineras que extraen minerales valiosos para la expansion.",
                "audio/planeta_porro.mp3"
        ));

        // Planeta de Agua
        DESCRIPTORES.put(PLANETA_AGUA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_PLANETA_AGUA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_PLANETA_AGUA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_CIENCIA
                ),
                35.0, 55.0,
                "Planeta de Agua", "[AGU]",
                "Planeta de Agua • Oceano Global Rico en Minerales y Ciencia",
                "Planeta de Agua: Un mundo oceanico cubierto en su totalidad por vastos y profundos mares hidrotermales. Su ensamblaje geologico requiere una reducida cantidad de recursos (50 de mineral y 25 de energia), produciendo a cambio una extraccion moderada de minerales y valiosos datos de investigacion marina, sin sustentar una poblacion colonizadora terrestre permanente.",
                "audio/planeta_agua.mp3"
        ));

        // Planeta Lava
        DESCRIPTORES.put(PLANETA_LAVA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_PLANETA_LAVA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_PLANETA_LAVA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.PRODUCCION_PLANETA_LAVA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionSimulacion.PRODUCCION_PLANETA_LAVA_CIENCIA
                ),
                35.0, 55.0,
                "Planeta Lava", "[LAV]",
                "Planeta Lava • Mundo Volcanico de Extraccion Intensiva",
                "Planeta Lava: Un cuerpo planetario sumergido en una actividad magmatica extrema con rios de roca fundida y erupciones continuas. Exige una importante inversion inicial (150 de mineral y 75 de energia) debido a las condiciones termicas extremas, pero genera un altisimo rendimiento cientifico y una abundante extraccion de minerales pesados expulsados del manto, siendo completamente incompatible con el asentamiento de poblacion.",
                "audio/planeta_lava.mp3"
        ));

        // Gigante Gaseoso
        DESCRIPTORES.put(PLANETA_GASEOSO, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_PLANETA_MINERALES * 2,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA * 2
                ),
                prodPlanetaBase,
                35.0, 55.0,
                "Joviano", "[GAS]",
                "Cuerpo Planetario • Fuente de Poblacion y Ciencia",
                "Un mundo geologicamente diferenciado provisto de corteza densa, atmosfera y capas ricas en compuestos pesados. Alberga biomas y condiciones idoneas para el desarrollo de vida organizada. A medida que su poblacion colonizadora prolifera, se establecen academias de ciencia para la investigacion e industrias mineras que extraen minerales valiosos para la expansion.",
                null
        ));

        // Planeta Helado
        DESCRIPTORES.put(PLANETA_HELADO, new DescriptorTipoCuerpo(
                costosPlanetaBase,
                prodPlanetaBase,
                35.0, 55.0,
                "Boreas", "[HEL]",
                "Cuerpo Planetario • Fuente de Poblacion y Ciencia",
                "Un mundo geologicamente diferenciado provisto de corteza densa, atmosfera y capas ricas en compuestos pesados. Alberga biomas y condiciones idoneas para el desarrollo de vida organizada. A medida que su poblacion colonizadora prolifera, se establecen academias de ciencia para la investigacion e industrias mineras que extraen minerales valiosos para la expansion.",
                null
        ));

        // Luna
        DESCRIPTORES.put(LUNA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_LUNA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_LUNA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES
                ),
                25.0, 40.0,
                "Luna", "[LUN]",
                "Luna • Cantera de Extraccion Mineral",
                "La Luna: Un satelite natural solido y esteril que gira en torno a su cuerpo huesped. Su superficie no erosionada conserva el registro intacto de miles de millones de anos de bombardeo astronomico, concentrando depositos superficiales de minerales puros. Constituye una cantera inagotable y segura para la extraccion de minerales brutos sin perturbar biosferas.",
                "audio/luna.mp3"
        ));

        // Satelite
        DESCRIPTORES.put(SATELITE, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, 40.0,
                        TipoRecurso.ENERGIA, 30.0,
                        TipoRecurso.CIENCIA, 10.0
                ),
                Map.of(),
                20.0, 35.0,
                "Sonda-1", "[SAT]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));

        // Cupula de Escudo
        DESCRIPTORES.put(ESCUDO_DOME, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.ENERGIA, 150.0,
                        TipoRecurso.CIENCIA, 50.0
                ),
                Map.of(),
                20.0, 35.0,
                "Aegis", "[ESC]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));

        // Meteorito
        DESCRIPTORES.put(METEORITO, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, 10.0,
                        TipoRecurso.ENERGIA, 5.0
                ),
                Map.of(),
                20.0, 35.0,
                "Bolido", "[MET]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));

        // Agujero Negro
        DESCRIPTORES.put(AGUJERO_NEGRO, new DescriptorTipoCuerpo(
                Map.of(),
                Map.of(),
                90.0, 120.0,
                "Singularidad", "[BH]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));

        // Agujero Negro Supermasivo
        DESCRIPTORES.put(AGUJERO_NEGRO_SUPERMASIVO, new DescriptorTipoCuerpo(
                Map.of(),
                Map.of(),
                90.0, 120.0,
                "Leviatan", "[SMBH]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));

        // Nave Colonizadora
        DESCRIPTORES.put(NAVE_COLONIZADORA, new DescriptorTipoCuerpo(
                Map.of(),
                Map.of(),
                20.0, 35.0,
                "Nave Colonizadora", "[NAV]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));
    }

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

    public String getColorHexString() {
        return String.format("#%06X", colorHex);
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

    public double getMasaMin() { return masaBase * ConfiguracionSimulacion.MASA_FACTOR_MIN; }
    public double getMasaMax() { return masaBase * ConfiguracionSimulacion.MASA_FACTOR_MAX; }

    public static String formatearMasa(double masaKg) {
        double mt = masaKg / 1e24;
        if (mt >= 1_000_000_000.0) {
            return String.format(Locale.US, "%.2fB MT", mt / 1e9);
        } else if (mt >= 1_000_000.0) {
            return String.format(Locale.US, "%.2fM MT", mt / 1e6);
        } else if (mt >= 1_000.0) {
            return String.format(Locale.US, "%.2fk MT", mt / 1e3);
        } else if (mt >= 0.01) {
            return String.format(Locale.US, "%.2f MT", mt);
        } else if (mt >= 0.0001) {
            return String.format(Locale.US, "%.4f MT", mt);
        } else {
            return String.format(Locale.US, "%.6f MT", mt);
        }
    }

    private DescriptorTipoCuerpo getDescriptor() {
        return DESCRIPTORES.getOrDefault(this, DescriptorTipoCuerpo.DEFECTO);
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

    public String getNombrePorDefecto() {
        return getDescriptor().nombrePorDefecto();
    }

    public String getIcono() {
        return getDescriptor().icono();
    }

    public String getSubtitulo() {
        return getDescriptor().subtitulo();
    }

    public String getLore() {
        return getDescriptor().lore();
    }

    public String getRutaAudio() {
        return getDescriptor().rutaAudio();
    }
}