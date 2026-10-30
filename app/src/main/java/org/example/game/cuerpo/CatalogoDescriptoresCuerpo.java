package org.example.game.cuerpo;

import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.EnumMap;
import java.util.Map;

/**
 * SRP: Catálogo centralizado de descriptores, lore, costos y producción para los tipos de cuerpos.
 */
public final class CatalogoDescriptoresCuerpo {

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
        DESCRIPTORES.put(TipoCuerpo.ESTRELLA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_ESTRELLA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_ESTRELLA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA,
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.PRODUCCION_ESTRELLA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionSimulacion.PRODUCCION_ESTRELLA_CIENCIA
                ),
                65.0, 85.0,
                "Sol", "[SOL]",
                "Sol • Reactor Termonuclear Natural",
                "El Sol: Una colosal masa de plasma autosostenida por equilibrio hidrostatico. En su nucleo ardiente se desarrollan procesos de fusion que emiten inmensos torrentes de radiacion, convirtiendola en la generadora primaria de energia para todo el sistema solar. Sus rafagas y vientos estelares cargados de iones pesados dispersan ademas minerales utiles a traves del espacio.",
                "audio/sol.mp3"
        ));

        // Planeta Porro
        DESCRIPTORES.put(TipoCuerpo.PLANETA_ROCOSO, new DescriptorTipoCuerpo(
                costosPlanetaBase,
                prodPlanetaBase,
                35.0, 55.0,
                "Planeta Porro", "[POR]",
                "Planeta Porro • Fuente de Poblacion y Ciencia",
                "Planeta Porro: Un mundo geologicamente diferenciado provisto de corteza densa, atmosfera y capas ricas en compuestos pesados. Alberga biomas y condiciones idoneas para el desarrollo de vida organizada. A medida que su poblacion colonizadora prolifera, se establecen academias de ciencia para la investigacion e industrias mineras que extraen minerales valiosos para la expansion.",
                "audio/planeta_porro.mp3"
        ));

        // Planeta de Agua
        DESCRIPTORES.put(TipoCuerpo.PLANETA_AGUA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_PLANETA_AGUA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_PLANETA_AGUA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_CIENCIA,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_ENERGIA
                ),
                35.0, 55.0,
                "Planeta de Agua", "[AGU]",
                "Planeta de Agua • Océano Global Rico en Recursos y Ciencia",
                "Planeta de Agua: Un mundo oceánico cubierto en su totalidad por vastos mares hidrotermales. Aporta minerales, energía geotérmica inicial y valiosos datos científicos marinos para sustentar el desarrollo temprano del sistema solar.",
                "audio/planeta_agua.mp3"
        ));

        // Planeta Lava
        DESCRIPTORES.put(TipoCuerpo.PLANETA_LAVA, new DescriptorTipoCuerpo(
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
        DESCRIPTORES.put(TipoCuerpo.PLANETA_GASEOSO, new DescriptorTipoCuerpo(
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
        DESCRIPTORES.put(TipoCuerpo.PLANETA_HELADO, new DescriptorTipoCuerpo(
                costosPlanetaBase,
                prodPlanetaBase,
                35.0, 55.0,
                "Boreas", "[HEL]",
                "Cuerpo Planetario • Fuente de Poblacion y Ciencia",
                "Un mundo geologicamente diferenciado provisto de corteza densa, atmosfera y capas ricas en compuestos pesados. Alberga biomas y condiciones idoneas para el desarrollo de vida organizada. A medida que su poblacion colonizadora prolifera, se establecen academias de ciencia para la investigacion e industrias mineras que extraen minerales valiosos para la expansion.",
                null
        ));

        // Luna
        DESCRIPTORES.put(TipoCuerpo.LUNA, new DescriptorTipoCuerpo(
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
        DESCRIPTORES.put(TipoCuerpo.SATELITE, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_SATELITE_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_SATELITE_ENERGIA
                ),
                Map.of(
                        TipoRecurso.CIENCIA, ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA,
                        TipoRecurso.ENERGIA, ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA
                ),
                18.0, 30.0,
                "Satélite", "[SAT]",
                "Satélite Artificial • Generador Científico y Energético",
                "Satélite Científico: Sonda orbital artificial no tripulada de masa fija (1.0 t). Equipada con paneles fotovoltaicos y sensores de investigación que generan ciencia continua y un suministro modesto de energía. Su estructura estandarizada no admite modificar su masa.",
                null
        ));

        // Meteorito
        DESCRIPTORES.put(TipoCuerpo.METEORITO, new DescriptorTipoCuerpo(
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
        DESCRIPTORES.put(TipoCuerpo.AGUJERO_NEGRO, new DescriptorTipoCuerpo(
                Map.of(),
                Map.of(),
                90.0, 120.0,
                "Singularidad", "[BH]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));

        // Agujero Negro Supermasivo
        DESCRIPTORES.put(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, new DescriptorTipoCuerpo(
                Map.of(),
                Map.of(),
                90.0, 120.0,
                "Leviatan", "[SMBH]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));

        // Nave Colonizadora
        DESCRIPTORES.put(TipoCuerpo.NAVE_COLONIZADORA, new DescriptorTipoCuerpo(
                Map.of(),
                Map.of(),
                20.0, 35.0,
                "Nave Colonizadora", "[NAV]",
                "Objeto Astronomico",
                "Cuerpo celeste en el sistema.",
                null
        ));
    }

    private CatalogoDescriptoresCuerpo() {}

    public static DescriptorTipoCuerpo obtenerDescriptor(TipoCuerpo tipo) {
        return DESCRIPTORES.getOrDefault(tipo, DescriptorTipoCuerpo.DEFECTO);
    }
}
