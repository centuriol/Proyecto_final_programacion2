package org.example.game.render.cuerpo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.example.game.civilizacion.SistemaCivilizaciones;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.EstrategiaVisualTipo;
import org.example.game.render.RegistroEstrategiasVisuales;
import org.example.game.render.RenderizadorCuerpo;
import org.example.game.render.RepositorioSprites;
import org.example.game.render.UtilidadRender;

/**
 * Renderizador Pixel Art para planetas delegando en estrategias visuales por tipo (SRP / OCP).
 */
public class RenderizadorPlaneta implements RenderizadorCuerpo {

    private final RepositorioSprites repositorioSprites;

    public RenderizadorPlaneta(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites;
    }

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste c, TipoCuerpo tipo, double r, long tick) {
        if (intentarDibujarSpritePlaneta(gc, c, tipo)) return;

        EstrategiaVisualTipo estrategia = RegistroEstrategiasVisuales.get(tipo);
        estrategia.dibujarSuperficie(gc, c, r);
    }

    private boolean intentarDibujarSpritePlaneta(GraphicsContext gc, CuerpoCeleste c, TipoCuerpo tipo) {
        if (repositorioSprites == null) return false;
        Image imgPlaneta = repositorioSprites.obtenerImagen(tipo);
        if (imgPlaneta == null) return false;

        double r = c != null ? c.getRadio() : 15.0;
        double tam = Math.max(22.0, r * 2.0);
        gc.drawImage(imgPlaneta, Math.round(-tam / 2.0), Math.round(-tam / 2.0), tam, tam);

        if (c != null && SistemaCivilizaciones.getInstancia().obtenerCivilizable(c).map(Civilizable::tieneCivilizacion).orElse(false)) {
            gc.setFill(UtilidadRender.colorWeb("#5be3ff"));
            gc.fillRect(-r * 0.3, -r * 0.2, 2, 2);
            gc.fillRect(r * 0.2, r * 0.1, 2, 2);
        }
        return true;
    }
}
