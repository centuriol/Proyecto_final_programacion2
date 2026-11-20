package org.example.game.render.capa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.example.game.render.CatalogoSprites;
import org.example.game.render.FabricaRender;
import org.example.game.render.UtilidadRender;

/**
 * Responsable de renderizar la cúpula visual y pulso de escudos planetarios (SRP / ISP / DIP).
 */
public class CapaEscudoPlanetario {

    public static final String NOMBRE_SPRITE_ESCUDO = "escudo_planeta_posta";
    private static final FabricaRender FABRICA = new FabricaRender();

    private final CatalogoSprites catalogoSprites;

    public CapaEscudoPlanetario(CatalogoSprites catalogoSprites) {
        this.catalogoSprites = catalogoSprites != null ? catalogoSprites : FABRICA.crearRepositorioSprites();
    }

    public CapaEscudoPlanetario() {
        this(FABRICA.crearRepositorioSprites());
    }

    public void dibujarCupula(GraphicsContext gc, double cx, double cy, double r, long tick) {
        if (gc == null) return;
        double rEfectivo = Math.max(16.0, r * 1.5);
        double pulso = 0.85 + 0.15 * Math.sin(tick * 0.2);

        Image imgEscudo = catalogoSprites.obtenerImagenPorNombre(NOMBRE_SPRITE_ESCUDO);
        if (imgEscudo != null) {
            gc.setFill(UtilidadRender.colorRgb(91, 227, 255, 0.12 * pulso));
            gc.fillOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);

            gc.save();
            gc.setImageSmoothing(false);
            gc.setGlobalAlpha(pulso);
            gc.drawImage(
                    imgEscudo,
                    Math.round(cx - rEfectivo),
                    Math.round(cy - rEfectivo),
                    rEfectivo * 2,
                    rEfectivo * 2
            );
            gc.restore();
            return;
        }

        gc.setFill(UtilidadRender.colorRgb(91, 227, 255, 0.20 * pulso));
        gc.fillOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);

        gc.setStroke(UtilidadRender.colorWeb("#5be3ff"));
        gc.setLineWidth(1.8);
        gc.setLineDashes(6, 4);
        gc.strokeOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);
        gc.setLineDashes(null);
    }
}
