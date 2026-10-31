package org.example.game.render.cuerpo;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorCuerpo;
import org.example.game.render.RepositorioSprites;
import org.example.game.render.UtilidadRender;
/**
 * Renderizador Pixel Art para lunas y satélites naturales (SRP / OCP).
 */
public class RenderizadorLuna implements RenderizadorCuerpo {

    private final RepositorioSprites repositorioSprites;

    public RenderizadorLuna(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites;
    }

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste cuerpo, TipoCuerpo tipo, double r, long tick) {
        Image imgLuna = repositorioSprites != null ? repositorioSprites.obtenerImagen(TipoCuerpo.LUNA) : null;
        if (imgLuna != null) {
            double tam = Math.max(14.0, r * 2.0);
            gc.drawImage(imgLuna, Math.round(-tam / 2.0), Math.round(-tam / 2.0), tam, tam);
            return;
        }

        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#a0aec0"));

        gc.setFill(UtilidadRender.colorWeb("#4a5568"));
        gc.fillOval(-r * 0.4, -r * 0.3, r * 0.4, r * 0.4);
        gc.fillOval(r * 0.1, r * 0.2, r * 0.5, r * 0.5);
        gc.fillOval(-r * 0.2, r * 0.3, r * 0.3, r * 0.3);
    }
}
