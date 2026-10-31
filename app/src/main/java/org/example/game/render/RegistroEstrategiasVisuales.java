package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import org.example.game.civilizacion.SistemaCivilizaciones;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registro y mapa de estrategias visuales desacopladas por TipoCuerpo (OCP).
 * Permite registrar nuevos tipos sin modificar las capas de vista.
 */
public class RegistroEstrategiasVisuales {

    private static final Color COLOR_AREA_ESTRELLA = UtilidadRender.colorRgb(255, 215, 0, 0.16);
    private static final Color COLOR_AREA_LUNA = UtilidadRender.colorRgb(200, 210, 225, 0.15);
    private static final Color COLOR_AREA_PLANETA = UtilidadRender.colorRgb(91, 227, 255, 0.14);
    private static final Color COLOR_AREA_AGUJERO = UtilidadRender.colorRgb(160, 32, 240, 0.18);

    private static final Map<TipoCuerpo, EstrategiaVisualTipo> ESTRATEGIAS = new EnumMap<>(TipoCuerpo.class);
    private static final EstrategiaVisualTipo POR_DEFECTO = new EstrategiaBase(
            r -> Math.max(16.0, r * 2.0),
            fm -> 30.0,
            true,
            true,
            COLOR_AREA_PLANETA,
            false,
            (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#a0aec0"))
    );

    static {
        // Estrella
        registrar(TipoCuerpo.ESTRELLA, new EstrategiaBase(
                r -> Math.max(32.0, r * 2.2),
                fm -> Math.max(65.0, 85.0 * Math.sqrt(fm)),
                false,
                true,
                COLOR_AREA_ESTRELLA,
                true,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#ffcc00"))
        ));

        // Planeta Rocoso
        registrar(TipoCuerpo.PLANETA_ROCOSO, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> Math.max(35.0, 55.0 * Math.sqrt(fm)),
                true,
                true,
                COLOR_AREA_PLANETA,
                false,
                RegistroEstrategiasVisuales::dibujarPlanetaRocoso
        ));

        // Planeta Agua
        registrar(TipoCuerpo.PLANETA_AGUA, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> Math.max(35.0, 55.0 * Math.sqrt(fm)),
                true,
                true,
                COLOR_AREA_PLANETA,
                false,
                RegistroEstrategiasVisuales::dibujarPlanetaAgua
        ));

        // Planeta Lava
        registrar(TipoCuerpo.PLANETA_LAVA, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> Math.max(35.0, 55.0 * Math.sqrt(fm)),
                true,
                true,
                COLOR_AREA_PLANETA,
                false,
                RegistroEstrategiasVisuales::dibujarPlanetaLava
        ));

        // Planeta Gaseoso
        registrar(TipoCuerpo.PLANETA_GASEOSO, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> Math.max(35.0, 55.0 * Math.sqrt(fm)),
                true,
                true,
                COLOR_AREA_PLANETA,
                false,
                RegistroEstrategiasVisuales::dibujarGiganteGaseoso
        ));

        // Planeta Helado
        registrar(TipoCuerpo.PLANETA_HELADO, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> Math.max(35.0, 55.0 * Math.sqrt(fm)),
                true,
                true,
                COLOR_AREA_PLANETA,
                false,
                RegistroEstrategiasVisuales::dibujarPlanetaHelado
        ));

        // Luna
        registrar(TipoCuerpo.LUNA, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> Math.max(25.0, 40.0 * Math.sqrt(fm)),
                true,
                true,
                COLOR_AREA_LUNA,
                false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#cbd5e0"))
        ));

        // Agujero Negro
        registrar(TipoCuerpo.AGUJERO_NEGRO, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> Math.max(90.0, 120.0 * Math.sqrt(fm)),
                true,
                true,
                COLOR_AREA_AGUJERO,
                false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#000000"))
        ));

        // Satelite
        registrar(TipoCuerpo.SATELITE, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> 30.0,
                true,
                false,
                COLOR_AREA_PLANETA,
                false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#718096"))
        ));

        // Meteorito
        registrar(TipoCuerpo.METEORITO, new EstrategiaBase(
                r -> Math.max(16.0, r * 2.0),
                fm -> 30.0,
                true,
                false,
                COLOR_AREA_PLANETA,
                false,
                (gc, c, r) -> UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#e53e3e"))
        ));
    }

    public static void registrar(TipoCuerpo tipo, EstrategiaVisualTipo estrategia) {
        if (tipo != null && estrategia != null) {
            ESTRATEGIAS.put(tipo, estrategia);
        }
    }

    public static EstrategiaVisualTipo get(TipoCuerpo tipo) {
        if (tipo == null) return POR_DEFECTO;
        return ESTRATEGIAS.getOrDefault(tipo, POR_DEFECTO);
    }

    public static void dibujarPlanetaRocoso(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#2b6cb0"));

        gc.setFill(UtilidadRender.colorWeb("#48bb78"));
        gc.fillRect(-r * 0.5, -r * 0.3, r * 0.7, r * 0.5);
        gc.fillRect(r * 0.1, -r * 0.6, r * 0.5, r * 0.4);
        gc.fillRect(-r * 0.2, r * 0.2, r * 0.6, r * 0.4);

        gc.setFill(UtilidadRender.colorRgb(10, 20, 40, 0.45));
        gc.fillArc(-r, -r, r * 2, r * 2, 90, 180, ArcType.ROUND);

        if (c != null && SistemaCivilizaciones.getInstancia().obtenerCivilizable(c).map(Civilizable::tieneCivilizacion).orElse(false)) {
            gc.setFill(UtilidadRender.colorWeb("#5be3ff"));
            gc.fillRect(-r * 0.4, -r * 0.1, 2, 2);
            gc.fillRect(-r * 0.2, r * 0.3, 2, 2);
            gc.fillRect(r * 0.2, r * 0.1, 3, 2);
        }
    }

    public static void dibujarPlanetaAgua(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#00bfff"));
        gc.setFill(UtilidadRender.colorWeb("#1e90ff"));
        gc.fillRect(-r * 0.4, -r * 0.2, r * 0.8, r * 0.4);
    }

    public static void dibujarPlanetaLava(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#ff4500"));
        gc.setFill(UtilidadRender.colorWeb("#ffd700"));
        gc.fillRect(-r * 0.3, -r * 0.2, r * 0.6, r * 0.4);
    }

    public static void dibujarGiganteGaseoso(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#d69e2e"));
        gc.setFill(UtilidadRender.colorWeb("#dd6b20"));
        gc.fillRect(-r * 0.85, -r * 0.5, r * 1.7, r * 0.3);
        gc.fillRect(-r * 0.95, r * 0.1, r * 1.9, r * 0.35);

        gc.setFill(UtilidadRender.colorWeb("#fbd38d"));
        gc.fillRect(-r * 0.9, -r * 0.15, r * 1.8, r * 0.2);

        gc.setFill(UtilidadRender.colorWeb("#9b2c2c"));
        gc.fillOval(r * 0.2, r * 0.15, r * 0.45, r * 0.3);
    }

    public static void dibujarPlanetaHelado(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#e2e8f0"));
        gc.setFill(UtilidadRender.colorWeb("#bee3f8"));
        gc.fillRect(-r * 0.6, -r * 0.3, r * 1.2, r * 0.6);
        gc.setFill(UtilidadRender.colorWeb("#90cdf4"));
        gc.fillRect(-r * 0.3, -r * 0.1, r * 0.6, r * 0.2);
    }

    private static class EstrategiaBase implements EstrategiaVisualTipo {
        private final java.util.function.DoubleUnaryOperator tamanoGhost;
        private final java.util.function.DoubleUnaryOperator radioAtraccionPreview;
        private final boolean rotacionAnimada;
        private final boolean areaAtraccion;
        private final Color colorArea;
        private final boolean lineasReferencia;
        private final java.util.function.Consumer<TriConsumerArgs> dibujador;

        @FunctionalInterface
        interface TriConsumer {
            void accept(GraphicsContext gc, CuerpoCeleste c, double r);
        }

        private final TriConsumer consumidorDibujo;

        EstrategiaBase(java.util.function.DoubleUnaryOperator tamanoGhost,
                       java.util.function.DoubleUnaryOperator radioAtraccionPreview,
                       boolean rotacionAnimada,
                       boolean areaAtraccion,
                       Color colorArea,
                       boolean lineasReferencia,
                       TriConsumer consumidorDibujo) {
            this.tamanoGhost = tamanoGhost;
            this.radioAtraccionPreview = radioAtraccionPreview;
            this.rotacionAnimada = rotacionAnimada;
            this.areaAtraccion = areaAtraccion;
            this.colorArea = colorArea;
            this.lineasReferencia = lineasReferencia;
            this.dibujador = null;
            this.consumidorDibujo = consumidorDibujo;
        }

        @Override public double calcularTamanoGhost(double radio) { return tamanoGhost.applyAsDouble(radio); }
        @Override public double calcularRadioAtraccionPreview(double factorMasa) { return radioAtraccionPreview.applyAsDouble(factorMasa); }
        @Override public boolean tieneRotacionAnimada() { return rotacionAnimada; }
        @Override public boolean tieneAreaAtraccion() { return areaAtraccion; }
        @Override public Color getColorAreaAtraccion() { return colorArea; }
        @Override public boolean tieneLineasReferenciaOrbital() { return lineasReferencia; }
        @Override public void dibujarSuperficie(GraphicsContext gc, CuerpoCeleste c, double r) {
            if (consumidorDibujo != null) consumidorDibujo.accept(gc, c, r);
        }
    }

    private static class TriConsumerArgs {}
}
