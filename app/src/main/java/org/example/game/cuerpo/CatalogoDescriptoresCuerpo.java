package org.example.game.cuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionEconomia;
import java.util.EnumMap;
import java.util.Map;
/**
 * SRP: Catalogo centralizado de descriptores, lore, costos y produccion para los tipos de cuerpos.
 * Todos los costos y tasas de produccion provienen exclusivamente de ConfiguracionEconomia.
 */
public class CatalogoDescriptoresCuerpo {

    private static final Map<TipoCuerpo, DescriptorTipoCuerpo> DESCRIPTORES = new EnumMap<>(TipoCuerpo.class);

    static {
        // Sol
        DESCRIPTORES.put(TipoCuerpo.ESTRELLA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_ESTRELLA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.PRODUCCION_ESTRELLA_ENERGIA,
                        TipoRecurso.MINERALES, ConfiguracionEconomia.PRODUCCION_ESTRELLA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionEconomia.PRODUCCION_ESTRELLA_CIENCIA
                ),
                65.0, 85.0,
                "Sol", "[SOL]",
                "Sol • Reactor Termonuclear Natural",
                "El Sol: Una colosal masa de plasma autosostenida por equilibrio hidrostatico. En su nucleo ardiente se desarrollan procesos de fusion que emiten inmensos torrentes de radiacion, convirtiendola en la generadora primaria de energia para todo el sistema solar. Sus rafagas y vientos estelares cargados de iones pesados dispersan ademas minerales utiles a traves del espacio.",
                "audio/sol.mp3"
        ));

        // Planeta de Agua
        DESCRIPTORES.put(TipoCuerpo.PLANETA_AGUA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_PLANETA_AGUA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_PLANETA_AGUA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_CIENCIA,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_ENERGIA
                ),
                35.0, 55.0,
                "Planeta de Agua", "[AGU]",
                "Planeta de Agua • Oceano Global Rico en Recursos y Ciencia",
                "Planeta de Agua: Un mundo oceanico cubierto en su totalidad por vastos mares hidrotermales. Aporta minerales, energia geotermica inicial y valiosos datos cientificos marinos para sustentar el desarrollo temprano del sistema solar.",
                "audio/planeta_agua.mp3"
        ));

        // Planeta Porro
        DESCRIPTORES.put(TipoCuerpo.PLANETA_ROCOSO, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_PLANETA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_PLANETA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.PRODUCCION_PLANETA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionEconomia.PRODUCCION_PLANETA_CIENCIA,
                        TipoRecurso.POBLACION, ConfiguracionEconomia.PRODUCCION_PLANETA_POBLACION
                ),
                35.0, 55.0,
                "Planeta Porro", "[POR]",
                "Planeta Porro • Fuente de Poblacion y Ciencia",
                "Planeta Porro: Un mundo geologicamente diferenciado provisto de corteza densa, atmosfera y capas ricas en compuestos pesados. Alberga biomas y condiciones idoneas para el desarrollo de vida organizada. A medida que su poblacion colonizadora prolifera, se establecen academias de ciencia para la investigacion e industrias mineras que extraen minerales valiosos para la expansion.",
                "audio/planeta_porro.mp3"
        ));

        // Planeta Lava
        DESCRIPTORES.put(TipoCuerpo.PLANETA_LAVA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_PLANETA_LAVA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_PLANETA_LAVA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.PRODUCCION_PLANETA_LAVA_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionEconomia.PRODUCCION_PLANETA_LAVA_CIENCIA
                ),
                35.0, 55.0,
                "Planeta Lava", "[LAV]",
                "Planeta Lava • Mundo Volcanico de Extraccion Intensiva",
                "Planeta Lava: Un cuerpo planetario sumergido en una actividad magmatica extrema con rios de roca fundida y erupciones continuas. Exige una elevada inversion de recursos debido a las condiciones termicas extremas, pero genera un altisimo rendimiento cientifico y una abundante extraccion de minerales pesados expulsados del manto, siendo completamente incompatible con el asentamiento de poblacion.",
                "audio/planeta_lava.mp3"
        ));

        // Gigante Gaseoso
        DESCRIPTORES.put(TipoCuerpo.PLANETA_GASEOSO, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_CIENCIA,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_ENERGIA
                ),
                35.0, 55.0,
                "Joviano", "[GAS]",
                "Gigante Gaseoso • Coloso Atmosferico de Alta Extraccion",
                "Gigante Gaseoso: Un mundo masivo de profundas bandas atmosfericas, tormentas ciclonicas perpetuas y nucleo sometido a presiones extremas. Carece de superficie solida habitable, pero concentra enormes reservas de isotopos energeticos, compuestos minerales pesados y fenomenos fisicos de alto valor cientifico.",
                null
        ));

        // Planeta Helado
        DESCRIPTORES.put(TipoCuerpo.PLANETA_HELADO, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_PLANETA_HELADO_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_PLANETA_HELADO_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.PRODUCCION_PLANETA_HELADO_MINERALES,
                        TipoRecurso.CIENCIA, ConfiguracionEconomia.PRODUCCION_PLANETA_HELADO_CIENCIA,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.PRODUCCION_PLANETA_HELADO_ENERGIA
                ),
                35.0, 55.0,
                "Boreas", "[HEL]",
                "Planeta Helado • Mundo Criogenico de Investigacion",
                "Planeta Helado: Un cuerpo planetario cubierto por glaciares perpetuos y oceanos subsuperficiales sellados bajo kilometros de hielo. Aporta extraccion mineral criogenica, observacion cientifica y aprovechamiento termico de sus fisuras internas.",
                null
        ));

        // Luna
        DESCRIPTORES.put(TipoCuerpo.LUNA, new DescriptorTipoCuerpo(
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_LUNA_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_LUNA_ENERGIA
                ),
                Map.of(
                        TipoRecurso.MINERALES, ConfiguracionEconomia.PRODUCCION_LUNA_MINERALES
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
                        TipoRecurso.MINERALES, ConfiguracionEconomia.COSTO_SATELITE_MINERALES,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.COSTO_SATELITE_ENERGIA
                ),
                Map.of(
                        TipoRecurso.CIENCIA, ConfiguracionEconomia.PRODUCCION_SATELITE_CIENCIA,
                        TipoRecurso.ENERGIA, ConfiguracionEconomia.PRODUCCION_SATELITE_ENERGIA
                ),
                18.0, 30.0,
                "Satélite", "[SAT]",
                "Satélite Artificial • Generador Científico y Energético",
                "Satélite Científico: Sonda orbital artificial no tripulada de masa fija (1.0 t). Equipada con paneles fotovoltaicos y sensores de investigación que generan ciencia continua, un suministro constante de energía y defensa activa frente a meteoritos.",
                null
        ));

        // Meteorito (no colocable ni reembolsable por el jugador)
        DESCRIPTORES.put(TipoCuerpo.METEORITO, new DescriptorTipoCuerpo(
                Map.of(),
                Map.of(),
                20.0, 35.0,
                "Bolido", "[MET]",
                "Objeto Astronomico",
                "Cuerpo celeste errante en el sistema.",
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

    private static final CatalogoDescriptoresCuerpo INSTANCIA_POR_DEFECTO = new CatalogoDescriptoresCuerpo();

    public CatalogoDescriptoresCuerpo() {}

    public static CatalogoDescriptoresCuerpo getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public DescriptorTipoCuerpo descriptor(TipoCuerpo tipo) {
        return DESCRIPTORES.getOrDefault(tipo, DescriptorTipoCuerpo.DEFECTO);
    }

    public static DescriptorTipoCuerpo obtenerDescriptor(TipoCuerpo tipo) {
        return INSTANCIA_POR_DEFECTO.descriptor(tipo);
    }
}
