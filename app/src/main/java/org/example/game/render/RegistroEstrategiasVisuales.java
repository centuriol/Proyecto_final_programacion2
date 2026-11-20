package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registro extensible de estrategias visuales desacopladas por TipoCuerpo (SRP / OCP).
 */
public class RegistroEstrategiasVisuales {

    private static final Color COLOR_AREA_ESTRELLA = UtilidadRender.colorRgb(255, 215, 0, 0.16);
    private static final Color COLOR_AREA_LUNA = UtilidadRender.colorRgb(200, 210, 225, 0.15);
    private static final Color COLOR_AREA_PLANETA = UtilidadRender.colorRgb(91, 227, 255, 0.14);
    private static final Color COLOR_AREA_AGUJERO = UtilidadRender.colorRgb(160, 32, 240, 0.18);

    private static final DibujadorSuperficiesPlaneta DIBUJADOR_PLANETAS = new DibujadorSuperficiesPlaneta();
    private static final EstrategiaVisualTipo POR_DEFECTO = new EstrategiaVisualRecord(
            r -> Math.max(16.0, r * 2.0), fm -> 30.0, true, true, COLOR_AREA_PLANETA, false,
            (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#a0aec0"))
    );
    private static final RegistroEstrategiasVisuales INSTANCIA = new RegistroEstrategiasVisuales();

    private final Map<TipoCuerpo, EstrategiaVisualTipo> estrategias = new EnumMap<>(TipoCuerpo.class);

    public RegistroEstrategiasVisuales() {
        registrarPorDefecto();
    }

    private void registrarPorDefecto() {
        registrarInstancia(TipoCuerpo.ESTRELLA, new EstrategiaVisualRecord(
                r -> Math.max(32.0, r * 2.2), fm -> Math.max(65.0, 85.0 * Math.sqrt(fm)), false, true, COLOR_AREA_ESTRELLA, true,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#ffcc00"))
        ));
        registrarPlaneta(TipoCuerpo.PLANETA_ROCOSO, DIBUJADOR_PLANETAS::dibujarPlanetaRocoso);
        registrarPlaneta(TipoCuerpo.PLANETA_AGUA, DIBUJADOR_PLANETAS::dibujarPlanetaAgua);
        registrarPlaneta(TipoCuerpo.PLANETA_LAVA, DIBUJADOR_PLANETAS::dibujarPlanetaLava);
        registrarPlaneta(TipoCuerpo.PLANETA_GASEOSO, DIBUJADOR_PLANETAS::dibujarGiganteGaseoso);
        registrarPlaneta(TipoCuerpo.PLANETA_HELADO, DIBUJADOR_PLANETAS::dibujarPlanetaHelado);
        registrarInstancia(TipoCuerpo.LUNA, new EstrategiaVisualRecord(
                r -> Math.max(16.0, r * 2.0), fm -> Math.max(25.0, 40.0 * Math.sqrt(fm)), true, true, COLOR_AREA_LUNA, false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#cbd5e0"))
        ));
        registrarInstancia(TipoCuerpo.AGUJERO_NEGRO, new EstrategiaVisualRecord(
                r -> Math.max(16.0, r * 2.0), fm -> Math.max(90.0, 120.0 * Math.sqrt(fm)), true, true, COLOR_AREA_AGUJERO, false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#000000"))
        ));
        registrarInstancia(TipoCuerpo.SATELITE, new EstrategiaVisualRecord(
                r -> Math.max(16.0, r * 2.0), fm -> 30.0, true, false, COLOR_AREA_PLANETA, false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#718096"))
        ));
        registrarInstancia(TipoCuerpo.METEORITO, new EstrategiaVisualRecord(
                r -> Math.max(16.0, r * 2.0), fm -> 30.0, true, false, COLOR_AREA_PLANETA, false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#e53e3e"))
        ));
    }

    private void registrarPlaneta(TipoCuerpo tipo, EstrategiaVisualRecord.DibujadorSuperficie dibujador) {
        registrarInstancia(tipo, new EstrategiaVisualRecord(
                r -> Math.max(16.0, r * 2.0), fm -> Math.max(35.0, 55.0 * Math.sqrt(fm)),
                true, true, COLOR_AREA_PLANETA, false, dibujador
        ));
    }

    public void registrarInstancia(TipoCuerpo tipo, EstrategiaVisualTipo estrategia) {
        if (tipo != null && estrategia != null) {
            estrategias.put(tipo, estrategia);
        }
    }

    public EstrategiaVisualTipo obtener(TipoCuerpo tipo) {
        return tipo == null ? POR_DEFECTO : estrategias.getOrDefault(tipo, POR_DEFECTO);
    }

    public static void registrar(TipoCuerpo tipo, EstrategiaVisualTipo estrategia) {
        INSTANCIA.registrarInstancia(tipo, estrategia);
    }

    public static EstrategiaVisualTipo get(TipoCuerpo tipo) {
        return INSTANCIA.obtener(tipo);
    }

    public static void dibujarPlanetaRocoso(GraphicsContext gc, CuerpoCeleste c, double r) { DIBUJADOR_PLANETAS.dibujarPlanetaRocoso(gc, c, r); }
    public static void dibujarPlanetaAgua(GraphicsContext gc, CuerpoCeleste c, double r) { DIBUJADOR_PLANETAS.dibujarPlanetaAgua(gc, c, r); }
    public static void dibujarPlanetaLava(GraphicsContext gc, CuerpoCeleste c, double r) { DIBUJADOR_PLANETAS.dibujarPlanetaLava(gc, c, r); }
    public static void dibujarGiganteGaseoso(GraphicsContext gc, CuerpoCeleste c, double r) { DIBUJADOR_PLANETAS.dibujarGiganteGaseoso(gc, c, r); }
    public static void dibujarPlanetaHelado(GraphicsContext gc, CuerpoCeleste c, double r) { DIBUJADOR_PLANETAS.dibujarPlanetaHelado(gc, c, r); }
}
