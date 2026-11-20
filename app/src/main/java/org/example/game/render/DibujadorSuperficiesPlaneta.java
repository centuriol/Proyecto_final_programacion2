package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import org.example.game.civilizacion.SistemaCivilizaciones;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;

/**
 * SRP: Renderizado procedimental de respaldo para superficies planetarias cuando no hay sprite cargado.
 */
public class DibujadorSuperficiesPlaneta {

    public void dibujarPlanetaRocoso(GraphicsContext gc, CuerpoCeleste c, double r) {
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

    public void dibujarPlanetaAgua(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#00bfff"));
        gc.setFill(UtilidadRender.colorWeb("#1e90ff"));
        gc.fillRect(-r * 0.4, -r * 0.2, r * 0.8, r * 0.4);
    }

    public void dibujarPlanetaLava(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#ff4500"));
        gc.setFill(UtilidadRender.colorWeb("#ffd700"));
        gc.fillRect(-r * 0.3, -r * 0.2, r * 0.6, r * 0.4);
    }

    public void dibujarGiganteGaseoso(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#d69e2e"));
        gc.setFill(UtilidadRender.colorWeb("#dd6b20"));
        gc.fillRect(-r * 0.85, -r * 0.5, r * 1.7, r * 0.3);
        gc.fillRect(-r * 0.95, r * 0.1, r * 1.9, r * 0.35);

        gc.setFill(UtilidadRender.colorWeb("#fbd38d"));
        gc.fillRect(-r * 0.9, -r * 0.15, r * 1.8, r * 0.2);

        gc.setFill(UtilidadRender.colorWeb("#9b2c2c"));
        gc.fillOval(r * 0.2, r * 0.15, r * 0.45, r * 0.3);
    }

    public void dibujarPlanetaHelado(GraphicsContext gc, CuerpoCeleste c, double r) {
        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#e2e8f0"));
        gc.setFill(UtilidadRender.colorWeb("#bee3f8"));
        gc.fillRect(-r * 0.6, -r * 0.3, r * 1.2, r * 0.6);
        gc.setFill(UtilidadRender.colorWeb("#90cdf4"));
        gc.fillRect(-r * 0.3, -r * 0.1, r * 0.6, r * 0.2);
    }
}
