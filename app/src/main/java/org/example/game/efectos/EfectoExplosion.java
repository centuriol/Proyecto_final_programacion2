package org.example.game.efectos;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.Vector2D;
import org.example.game.render.CatalogoSprites;

/**
 * SRP / ISP: Representa una explosión animada individual (avance de frames y renderizado).
 */
public class EfectoExplosion {

    private record ParametrosExplosion(Vector2D posicionFisica, double radioInicial, Color color, int duracionFrames) {}

    private final ParametrosExplosion params;
    private int frameActual = 0;

    public EfectoExplosion(Vector2D posicionFisica, double radioInicial, Color color, int duracionFrames) {
        this.params = new ParametrosExplosion(
                posicionFisica,
                Math.max(14.0, radioInicial),
                color != null ? color : Color.ORANGE,
                duracionFrames
        );
    }

    public boolean avanzar() {
        frameActual++;
        return frameActual >= params.duracionFrames();
    }

    public int getIndiceFrameSprite() {
        int total = GestorEfectos.SPRITES_EXPLOSION.size();
        return Math.min(total - 1, (frameActual * total) / params.duracionFrames());
    }

    public void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas,
                        ConversorCoordenadas conversor, CatalogoSprites catalogoSprites) {
        double cx = conversor.fisicaAXJavaFX(params.posicionFisica().x(), anchoCanvas);
        double cy = conversor.fisicaAYJavaFX(params.posicionFisica().y(), altoCanvas);

        Image frameImg = obtenerSpriteFrame(catalogoSprites, getIndiceFrameSprite());
        if (frameImg != null) {
            double tamano = Math.max(42.0, params.radioInicial() * 2.4);
            gc.save();
            gc.setImageSmoothing(false);
            gc.drawImage(frameImg, Math.round(cx - tamano / 2.0), Math.round(cy - tamano / 2.0), tamano, tamano);
            gc.restore();
            return;
        }

        double prog = (double) frameActual / params.duracionFrames();
        double alpha = Math.max(0.0, 1.0 - prog);
        double radio = params.radioInicial() + prog * 50.0;
        Color color = params.color();

        gc.save();
        gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * 0.8));
        gc.setLineWidth(2.5 * (1.0 - prog));
        gc.strokeOval(cx - radio, cy - radio, radio * 2, radio * 2);
        gc.restore();
    }

    private Image obtenerSpriteFrame(CatalogoSprites repo, int idx) {
        if (repo == null || idx < 0 || idx >= GestorEfectos.SPRITES_EXPLOSION.size()) return null;
        Image img = repo.obtenerImagenPorNombre(GestorEfectos.SPRITES_EXPLOSION.get(idx));
        if (img == null && idx == 8) {
            img = repo.obtenerImagenPorNombre("explosion 9");
        }
        return img;
    }
}
