package org.example.game.render.cuerpo;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.ArcType;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorCuerpo;
import org.example.game.render.RepositorioSprites;
import org.example.game.render.UtilidadRender;
import java.util.EnumMap;
import java.util.Map;
/**
 * Renderizador Pixel Art para planetas (rocoso, agua, lava, gaseoso, helado) (SRP / OCP).
 */
public class RenderizadorPlaneta implements RenderizadorCuerpo {

    @FunctionalInterface
    private interface EstrategiaDibujoSuperficie {
        void dibujar(GraphicsContext gc, CuerpoCeleste c, double r);
    }

    private final RepositorioSprites repositorioSprites;
    private final Map<TipoCuerpo, EstrategiaDibujoSuperficie> estrategiasDibujo = new EnumMap<>(TipoCuerpo.class);

    public RenderizadorPlaneta(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites;
        estrategiasDibujo.put(TipoCuerpo.PLANETA_AGUA, (gc, c, r) -> dibujarPlanetaAgua(gc, r));
        estrategiasDibujo.put(TipoCuerpo.PLANETA_LAVA, (gc, c, r) -> dibujarPlanetaLava(gc, r));
        estrategiasDibujo.put(TipoCuerpo.PLANETA_GASEOSO, (gc, c, r) -> dibujarGiganteGaseoso(gc, r));
        estrategiasDibujo.put(TipoCuerpo.PLANETA_HELADO, (gc, c, r) -> dibujarPlanetaHelado(gc, r));
    }

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste c, TipoCuerpo tipo, double r, long tick) {
        if (intentarDibujarSpritePlaneta(gc, c, tipo)) return;

        EstrategiaDibujoSuperficie estrategia = estrategiasDibujo.getOrDefault(tipo, this::dibujarPlanetaRocoso);
        estrategia.dibujar(gc, c, r);
    }

    private boolean intentarDibujarSpritePlaneta(GraphicsContext gc, CuerpoCeleste c, TipoCuerpo tipo) {
        if (repositorioSprites == null) return false;
        Image imgPlaneta = repositorioSprites.obtenerImagen(tipo);
        if (imgPlaneta == null) return false;

        double r = c != null ? c.getRadio() : 15.0;
        double tam = Math.max(22.0, r * 2.0);
        gc.drawImage(imgPlaneta, Math.round(-tam / 2.0), Math.round(-tam / 2.0), tam, tam);

        if (c != null && org.example.game.civilizacion.SistemaCivilizaciones.getInstancia().obtenerCivilizable(c).map(Civilizable::tieneCivilizacion).orElse(false)) {
            gc.setFill(UtilidadRender.colorWeb("#5be3ff"));
            gc.fillRect(-r * 0.3, -r * 0.2, 2, 2);
            gc.fillRect(r * 0.2, r * 0.1, 2, 2);
        }
        return true;
    }

    private void dibujarPlanetaRocoso(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#2b6cb0"));

        gc.setFill(UtilidadRender.colorWeb("#48bb78"));
        gc.fillRect(-r * 0.5, -r * 0.3, r * 0.7, r * 0.5);
        gc.fillRect(r * 0.1, -r * 0.6, r * 0.5, r * 0.4);
        gc.fillRect(-r * 0.2, r * 0.2, r * 0.6, r * 0.4);

        gc.setFill(UtilidadRender.colorRgb(10, 20, 40, 0.45));
        gc.fillArc(-r, -r, r * 2, r * 2, 90, 180, ArcType.ROUND);

        if (c != null && org.example.game.civilizacion.SistemaCivilizaciones.getInstancia().obtenerCivilizable(c).map(Civilizable::tieneCivilizacion).orElse(false)) {
            gc.setFill(UtilidadRender.colorWeb("#5be3ff"));
            gc.fillRect(-r * 0.4, -r * 0.1, 2, 2);
            gc.fillRect(-r * 0.2, r * 0.3, 2, 2);
            gc.fillRect(r * 0.2, r * 0.1, 3, 2);
        }
    }

    private void dibujarPlanetaAgua(GraphicsContext gc, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#00bfff"));
        gc.setFill(UtilidadRender.colorWeb("#1e90ff"));
        gc.fillRect(-r * 0.4, -r * 0.2, r * 0.8, r * 0.4);
    }

    private void dibujarPlanetaLava(GraphicsContext gc, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#ff4500"));
        gc.setFill(UtilidadRender.colorWeb("#ffd700"));
        gc.fillRect(-r * 0.3, -r * 0.2, r * 0.6, r * 0.4);
    }

    private void dibujarGiganteGaseoso(GraphicsContext gc, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#d69e2e"));

        gc.setFill(UtilidadRender.colorWeb("#dd6b20"));
        gc.fillRect(-r * 0.85, -r * 0.5, r * 1.7, r * 0.3);
        gc.fillRect(-r * 0.95, r * 0.1, r * 1.9, r * 0.35);

        gc.setFill(UtilidadRender.colorWeb("#fbd38d"));
        gc.fillRect(-r * 0.9, -r * 0.15, r * 1.8, r * 0.2);

        gc.setFill(UtilidadRender.colorWeb("#9b2c2c"));
        gc.fillOval(r * 0.2, r * 0.15, r * 0.45, r * 0.3);

        gc.setStroke(UtilidadRender.colorRgb(237, 137, 54, 0.85));
        gc.setLineWidth(3.0);
        gc.strokeOval(-r * 1.7, -r * 0.45, r * 3.4, r * 0.9);

        gc.setStroke(UtilidadRender.colorRgb(254, 235, 200, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeOval(-r * 1.5, -r * 0.38, r * 3.0, r * 0.76);
    }

    private void dibujarPlanetaHelado(GraphicsContext gc, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#e2e8f0"));
        gc.setFill(UtilidadRender.colorWeb("#90cdf4"));
        gc.fillRect(-r * 0.5, -r * 0.2, r * 0.6, r * 0.3);
        gc.fillRect(r * 0.1, r * 0.1, r * 0.4, r * 0.3);
    }
}
