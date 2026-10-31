package org.example.game.render.capa;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import org.example.game.render.RepositorioSprites;
import org.example.game.render.UtilidadRender;
/**
 * Capa de renderizado para el fantasma (ghost preview) y guía de colocación en tiempo real (SRP).
 */
public class CapaGhostPreview {

    private final RepositorioSprites repositorioSprites;
    private static final Color COLOR_ACENTO_CYAN = UtilidadRender.colorWeb("#5be3ff");
    private static final Color COLOR_TEXTO_ETIQUETA = UtilidadRender.colorWeb("#e8e4d8");
    private static final Color COLOR_VERDE_EXITO = UtilidadRender.colorWeb("#50fa7b");

    public CapaGhostPreview(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites;
    }

    public void dibujar(GraphicsContext gc, Vector2D posFisica, TipoCuerpo tipo, double factorMasa,
                        String previewNombre, long tick, double tiempoAnimacionSegundos,
                        double anchoCanvas, double altoCanvas) {
        if (posFisica == null || tipo == null) return;

        double x = ConstantesFisicas.fisicaAXJavaFX(posFisica.x(), anchoCanvas);
        double y = ConstantesFisicas.fisicaAYJavaFX(posFisica.y(), altoCanvas);
        double r = Math.max(8.0, Math.min(35.0, 15.0 * factorMasa));

        double pulso = 1.0 + 0.15 * Math.sin(tick * 0.25);
        double rPulsante = r * pulso;

        gc.setStroke(UtilidadRender.colorWeb(tipo.getColorHexString()));
        gc.setLineWidth(2.0);
        gc.setLineDashes(4, 4);
        gc.strokeOval(x - rPulsante, y - rPulsante, rPulsante * 2, rPulsante * 2);
        gc.setLineDashes(null);

        dibujarSpriteFantasma(gc, tipo, r, x, y, tiempoAnimacionSegundos);
        dibujarAreaColapsoYReferencias(gc, tipo, factorMasa, x, y);
        dibujarTagInformacion(gc, tipo, factorMasa, previewNombre, x, y, r);
    }

    private void dibujarSpriteFantasma(GraphicsContext gc, TipoCuerpo tipo, double r, double x, double y, double tiempoAnimacion) {
        Image imgGhost = repositorioSprites != null ? repositorioSprites.obtenerImagen(tipo) : null;
        if (imgGhost != null) {
            gc.setGlobalAlpha(0.65);
            double tamGhost = tipo.getTamanoGhost(r);
            double angulo = 0.0;
            if (tipo.tieneRotacionAnimada()) {
                double velAngular = 57.2958 / Math.max(6.0, r);
                angulo = (tiempoAnimacion * velAngular) % 360.0;
            }

            gc.save();
            gc.translate(x, y);
            if (angulo != 0.0) {
                gc.rotate(angulo);
            }
            gc.drawImage(imgGhost, Math.round(-tamGhost / 2.0), Math.round(-tamGhost / 2.0), tamGhost, tamGhost);
            gc.restore();
            gc.setGlobalAlpha(1.0);
        }
    }

    private void dibujarAreaColapsoYReferencias(GraphicsContext gc, TipoCuerpo tipo, double factorMasa, double x, double y) {
        double rAtraccion = tipo.getRadioAtraccionPreview(factorMasa);

        gc.setStroke(UtilidadRender.colorRgb(255, 255, 255, 0.20));
        gc.setLineWidth(1.0);
        gc.setLineDashes(3, 5);
        gc.strokeOval(x - rAtraccion, y - rAtraccion, rAtraccion * 2.0, rAtraccion * 2.0);

        if (tipo.esEstrella()) {
            double escala = Math.sqrt(Math.max(0.1, factorMasa));
            double[] radiosReferencia = CapaZonasHabitables.getRadiosReferenciaOrbital();
            gc.setStroke(UtilidadRender.colorRgb(91, 227, 255, 0.12));
            for (double rBase : radiosReferencia) {
                double rOrbita = rBase * escala;
                gc.strokeOval(x - rOrbita, y - rOrbita, rOrbita * 2.0, rOrbita * 2.0);
            }
        }
        gc.setLineDashes(null);
    }

    private void dibujarTagInformacion(GraphicsContext gc, TipoCuerpo tipo, double factorMasa, String previewNombre, double x, double y, double r) {
        String textoNombre = (previewNombre != null && !previewNombre.isBlank()) ? previewNombre : tipo.nombre;
        String linea1 = textoNombre + " (" + String.format("%.1fx", factorMasa) + ")";
        double tagWidth = Math.max(130.0, linea1.length() * 8.0 + 20.0);

        gc.setFill(UtilidadRender.colorRgb(26, 28, 38, 0.9));
        gc.fillRect(x + r + 8, y - 20, tagWidth, 42);
        gc.setStroke(COLOR_ACENTO_CYAN);
        gc.setLineWidth(1.0);
        gc.strokeRect(x + r + 8, y - 20, tagWidth, 42);

        gc.setFill(COLOR_TEXTO_ETIQUETA);
        gc.fillText(linea1, x + r + 14, y - 4);
        gc.setFill(COLOR_VERDE_EXITO);
        gc.fillText("Click para colocar", x + r + 14, y + 14);
    }
}
