package org.example.game.render.cuerpo;
import javafx.scene.canvas.GraphicsContext; import javafx.scene.image.Image; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.render.RenderizadorCuerpo; import org.example.game.render.RepositorioSprites; import org.example.game.render.UtilidadRender;
/**
 * Renderizador Pixel Art para estrellas (Sol) (SRP / OCP).
 */
public class RenderizadorEstrella implements RenderizadorCuerpo {

    private final RepositorioSprites repositorioSprites;

    public RenderizadorEstrella(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites;
    }

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste cuerpo, TipoCuerpo tipo, double r, long tick) {
        Image imgSol = repositorioSprites != null ? repositorioSprites.obtenerImagen(TipoCuerpo.ESTRELLA) : null;
        if (imgSol != null) {
            double tam = Math.max(32.0, r * 2.2);
            gc.drawImage(imgSol, Math.round(-tam / 2.0), Math.round(-tam / 2.0), tam, tam);
            return;
        }

        double pulso = 1.0 + 0.12 * Math.sin(tick * 0.15);
        double rCorona = r * 1.5 * pulso;

        gc.setFill(UtilidadRender.colorRgb(255, 215, 0, 0.18));
        gc.fillOval(-rCorona, -rCorona, rCorona * 2, rCorona * 2);

        gc.setStroke(UtilidadRender.colorRgb(255, 230, 100, 0.7));
        gc.setLineWidth(2.0);
        double rayoLen = r * 1.8 * pulso;
        gc.strokeLine(-rayoLen, 0, rayoLen, 0);
        gc.strokeLine(0, -rayoLen, 0, rayoLen);

        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#ffaa00"));

        gc.setFill(UtilidadRender.colorWeb("#ffd700"));
        gc.fillOval(-r * 0.75, -r * 0.75, r * 1.5, r * 1.5);

        gc.setFill(UtilidadRender.colorWeb("#ffffff"));
        gc.fillOval(-r * 0.35, -r * 0.45, r * 0.7, r * 0.7);
    }
}
