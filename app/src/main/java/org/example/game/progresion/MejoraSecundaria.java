package org.example.game.progresion;

import org.example.game.simulacion.ConfiguracionEconomia;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mini-mejoras secundarias opcionales que se bifurcan como dendritas en cada nivel
 * de las tres ramas principales (PLANETAS, MASA, CANTIDAD_PLANETAS) sin bloquear el camino principal.
 */
public enum MejoraSecundaria {

    // ===== Rama PLANETAS =====
    PLANETAS_T1_MINERIA(
            "PLANETAS_T1_MINERIA", RamaProgresion.PLANETAS, 1, "+12%P",
            "Sondeo Mineral",
            "+12% producción de minerales y ciencia en planetas.",
            TipoEfecto.PRODUCCION_PLANETAS,
            ConfiguracionEconomia.BONUS_PRODUCCION_PLANETAS_T1,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_1
    ),
    PLANETAS_T2_COSTO(
            "PLANETAS_T2_COSTO", RamaProgresion.PLANETAS, 2, "-12%C",
            "Síntesis Orbital",
            "-12% costo de colocar planetas y lunas.",
            TipoEfecto.COSTO_PLANETAS_LUNAS,
            ConfiguracionEconomia.DESCUENTO_COSTO_PLANETAS_LUNAS_T2,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_2
    ),
    PLANETAS_T3_PRODUCCION(
            "PLANETAS_T3_PRODUCCION", RamaProgresion.PLANETAS, 3, "+18%P",
            "Redes de Extracción",
            "+18% producción de minerales y ciencia en planetas.",
            TipoEfecto.PRODUCCION_PLANETAS,
            ConfiguracionEconomia.BONUS_PRODUCCION_PLANETAS_T3,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_3
    ),
    PLANETAS_T4_ESCUDO(
            "PLANETAS_T4_ESCUDO", RamaProgresion.PLANETAS, 4, "-25%E",
            "Condensador de Escudo",
            "-25% costo de energía al activar escudos planetarios.",
            TipoEfecto.COSTO_ESCUDO,
            ConfiguracionEconomia.DESCUENTO_COSTO_ESCUDO_T4,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_4
    ),
    PLANETAS_T5_PRODUCCION(
            "PLANETAS_T5_PRODUCCION", RamaProgresion.PLANETAS, 5, "+25%P",
            "Núcleo Geotérmico",
            "+25% producción de minerales y ciencia en planetas.",
            TipoEfecto.PRODUCCION_PLANETAS,
            ConfiguracionEconomia.BONUS_PRODUCCION_PLANETAS_T5,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_5
    ),
    PLANETAS_T5_COSTO(
            "PLANETAS_T5_COSTO", RamaProgresion.PLANETAS, 5, "-18%C",
            "Forja Planetaria",
            "-18% costo de colocar planetas y lunas.",
            TipoEfecto.COSTO_PLANETAS_LUNAS,
            ConfiguracionEconomia.DESCUENTO_COSTO_PLANETAS_LUNAS_T5,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_5
    ),

    // ===== Rama MASA =====
    MASA_T1_COSTO(
            "MASA_T1_COSTO", RamaProgresion.MASA, 1, "-10%M",
            "Compresión de Masa",
            "-10% costo adicional por factor de masa.",
            TipoEfecto.COSTO_MASA,
            ConfiguracionEconomia.DESCUENTO_COSTO_MASA_T1,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_1
    ),
    MASA_T2_PRODUCCION(
            "MASA_T2_PRODUCCION", RamaProgresion.MASA, 2, "+15%M",
            "Densidad Productiva",
            "+15% producción de recursos escalada por masa.",
            TipoEfecto.PRODUCCION_MASA,
            ConfiguracionEconomia.BONUS_PRODUCCION_MASA_T2,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_2
    ),
    MASA_T3_SATELITE(
            "MASA_T3_SATELITE", RamaProgresion.MASA, 3, "-20%S",
            "Micro-Ensamblaje",
            "-20% costo de colocar satélites espaciales.",
            TipoEfecto.COSTO_SATELITE,
            ConfiguracionEconomia.DESCUENTO_COSTO_SATELITE_T3,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_3
    ),
    MASA_T4_COSTO(
            "MASA_T4_COSTO", RamaProgresion.MASA, 4, "-15%M",
            "Estabilizador Gravitacional",
            "-15% costo adicional por factor de masa.",
            TipoEfecto.COSTO_MASA,
            ConfiguracionEconomia.DESCUENTO_COSTO_MASA_T4,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_4
    ),
    MASA_T5_PRODUCCION(
            "MASA_T5_PRODUCCION", RamaProgresion.MASA, 5, "+25%M",
            "Singularidad Industrial",
            "+25% producción de recursos escalada por masa.",
            TipoEfecto.PRODUCCION_MASA,
            ConfiguracionEconomia.BONUS_PRODUCCION_MASA_T5,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_5
    ),
    MASA_T5_SATELITE(
            "MASA_T5_SATELITE", RamaProgresion.MASA, 5, "-20%S",
            "Enjambre Orbital",
            "-20% costo de colocar satélites espaciales.",
            TipoEfecto.COSTO_SATELITE,
            ConfiguracionEconomia.DESCUENTO_COSTO_SATELITE_T5,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_5
    ),

    // ===== Rama CANTIDAD_PLANETAS =====
    CANTIDAD_T1_POBLACION(
            "CANTIDAD_T1_POBLACION", RamaProgresion.CANTIDAD_PLANETAS, 1, "+15%H",
            "Domos Habitacionales",
            "+15% capacidad de población máxima en planetas.",
            TipoEfecto.POBLACION_MAXIMA,
            ConfiguracionEconomia.BONUS_POBLACION_MAXIMA_T1,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_1
    ),
    CANTIDAD_T2_CRECIMIENTO(
            "CANTIDAD_T2_CRECIMIENTO", RamaProgresion.CANTIDAD_PLANETAS, 2, "+15%R",
            "Biosferas Aceleradas",
            "+15% velocidad de crecimiento poblacional.",
            TipoEfecto.CRECIMIENTO_POBLACION,
            ConfiguracionEconomia.BONUS_CRECIMIENTO_POBLACION_T2,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_2
    ),
    CANTIDAD_T3_COLONIZAR(
            "CANTIDAD_T3_COLONIZAR", RamaProgresion.CANTIDAD_PLANETAS, 3, "-20%N",
            "Propulsión Colonial",
            "-20% costo de colonizar y poblar nuevos mundos.",
            TipoEfecto.COSTO_COLONIZACION,
            ConfiguracionEconomia.DESCUENTO_COSTO_COLONIZAR_T3,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_3
    ),
    CANTIDAD_T4_POBLACION(
            "CANTIDAD_T4_POBLACION", RamaProgresion.CANTIDAD_PLANETAS, 4, "+25%H",
            "Ecumenópolis Orbital",
            "+25% capacidad de población máxima en planetas.",
            TipoEfecto.POBLACION_MAXIMA,
            ConfiguracionEconomia.BONUS_POBLACION_MAXIMA_T4,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_4
    ),
    CANTIDAD_T5_CRECIMIENTO(
            "CANTIDAD_T5_CRECIMIENTO", RamaProgresion.CANTIDAD_PLANETAS, 5, "+25%R",
            "Red Demográfica Solar",
            "+25% velocidad de crecimiento poblacional.",
            TipoEfecto.CRECIMIENTO_POBLACION,
            ConfiguracionEconomia.BONUS_CRECIMIENTO_POBLACION_T5,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_5
    ),
    CANTIDAD_T5_COLONIZAR(
            "CANTIDAD_T5_COLONIZAR", RamaProgresion.CANTIDAD_PLANETAS, 5, "-20%N",
            "Flota Interestelar",
            "-20% costo de colonizar y poblar nuevos mundos.",
            TipoEfecto.COSTO_COLONIZACION,
            ConfiguracionEconomia.DESCUENTO_COSTO_COLONIZAR_T5,
            ConfiguracionEconomia.COSTO_SECUNDARIA_TIER_5
    );

    public enum TipoEfecto {
        PRODUCCION_PLANETAS,
        COSTO_PLANETAS_LUNAS,
        COSTO_ESCUDO,
        COSTO_MASA,
        PRODUCCION_MASA,
        COSTO_SATELITE,
        POBLACION_MAXIMA,
        CRECIMIENTO_POBLACION,
        COSTO_COLONIZACION
    }

    private final String id;
    private final RamaProgresion rama;
    private final int tier;
    private final String tag;
    private final String nombre;
    private final String descripcion;
    private final TipoEfecto efecto;
    private final double valor;
    private final double costo;

    MejoraSecundaria(String id,
                     RamaProgresion rama,
                     int tier,
                     String tag,
                     String nombre,
                     String descripcion,
                     TipoEfecto efecto,
                     double valor,
                     double costo) {
        this.id = id;
        this.rama = rama;
        this.tier = tier;
        this.tag = tag;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.efecto = efecto;
        this.valor = valor;
        this.costo = costo;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public RamaProgresion rama() { return rama; }
    public RamaProgresion getRama() { return rama; }

    public int tier() { return tier; }
    public int getTier() { return tier; }

    public String tag() { return tag; }
    public String getTag() { return tag; }

    public String nombre() { return nombre; }
    public String getNombre() { return nombre; }

    public String descripcion() { return descripcion; }
    public String getDescripcion() { return descripcion; }

    public TipoEfecto efecto() { return efecto; }
    public TipoEfecto getEfecto() { return efecto; }

    public double valor() { return valor; }
    public double getValor() { return valor; }

    public double costo() { return costo; }
    public double getCosto() { return costo; }

    public String formatearEfecto() {
        int pct = (int) Math.round(valor * 100.0);
        return switch (efecto) {
            case PRODUCCION_PLANETAS -> "+" + pct + "% producción minerales/ciencia planetaria";
            case COSTO_PLANETAS_LUNAS -> "-" + pct + "% costo colocación planetas/lunas";
            case COSTO_ESCUDO -> "-" + pct + "% costo activación de escudo";
            case COSTO_MASA -> "-" + pct + "% costo por factor de masa";
            case PRODUCCION_MASA -> "+" + pct + "% producción escalada por masa";
            case COSTO_SATELITE -> "-" + pct + "% costo de satélites";
            case POBLACION_MAXIMA -> "+" + pct + "% población máxima";
            case CRECIMIENTO_POBLACION -> "+" + pct + "% crecimiento poblacional";
            case COSTO_COLONIZACION -> "-" + pct + "% costo colonizar/poblar";
        };
    }

    public static Optional<MejoraSecundaria> porId(String idBuscado) {
        if (idBuscado == null || idBuscado.isBlank()) {
            return Optional.empty();
        }
        String limpio = idBuscado.trim();
        for (MejoraSecundaria m : values()) {
            if (m.id.equalsIgnoreCase(limpio) || m.name().equalsIgnoreCase(limpio)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public static List<MejoraSecundaria> deRama(RamaProgresion rama) {
        if (rama == null) return List.of();
        List<MejoraSecundaria> lista = new ArrayList<>();
        for (MejoraSecundaria m : values()) {
            if (m.rama == rama) {
                lista.add(m);
            }
        }
        return List.copyOf(lista);
    }

    public static List<MejoraSecundaria> deRamaYTier(RamaProgresion rama, int tier) {
        if (rama == null || tier <= 0) return List.of();
        List<MejoraSecundaria> lista = new ArrayList<>();
        for (MejoraSecundaria m : values()) {
            if (m.rama == rama && m.tier == tier) {
                lista.add(m);
            }
        }
        return List.copyOf(lista);
    }
}
