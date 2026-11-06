package org.example.game.efectos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.motor.Vector2D;
import org.example.game.render.GestorImagenes;
import org.example.game.render.RepositorioSprites;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Gestor de efectos visuales de explosión animada por secuencia de sprites (estilo GIF) en colisiones.
 * SRP: Responsable exclusivo de registrar, avanzar frames y dibujar efectos transitorios visuales.
 */
public class GestorEfectos {

    public static final List<String> SPRITES_EXPLOSION = List.of(
            "explosion1", "explosion2", "explosion3",
            "explosion4", "explosion5", "explosion6",
            "explosion7", "explosion8", "explosion9"
    );
    private static final int TICKS_POR_FRAME_GIF = 4;
    private static final int DURACION_FRAMES_EXPLOSION = SPRITES_EXPLOSION.size() * TICKS_POR_FRAME_GIF;

    private static class EfectoExplosion {
        final Vector2D posicionFisica;
        final double radioInicial;
        final Color color;
        int frameActual = 0;
        final int duracionFrames = DURACION_FRAMES_EXPLOSION;

        EfectoExplosion(Vector2D posicionFisica, double radioInicial, Color color) {
            this.posicionFisica = posicionFisica;
            this.radioInicial = Math.max(14.0, radioInicial);
            this.color = color != null ? color : Color.ORANGE;
        }

        boolean avanzar() {
            frameActual++;
            return frameActual >= duracionFrames;
        }

        int getIndiceFrameSprite() {
            return Math.min(SPRITES_EXPLOSION.size() - 1, (frameActual * SPRITES_EXPLOSION.size()) / duracionFrames);
        }

        void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas,
                     ConversorCoordenadas conversor, RepositorioSprites repositorioSprites) {
            double cx = conversor.fisicaAXJavaFX(posicionFisica.x(), anchoCanvas);
            double cy = conversor.fisicaAYJavaFX(posicionFisica.y(), altoCanvas);

            Image frameImg = obtenerSpriteFrame(repositorioSprites, getIndiceFrameSprite());
            if (frameImg != null) {
                double tamano = Math.max(42.0, radioInicial * 2.4);
                gc.save();
                gc.setImageSmoothing(false);
                gc.drawImage(frameImg, Math.round(cx - tamano / 2.0), Math.round(cy - tamano / 2.0), tamano, tamano);
                gc.restore();
                return;
            }

            double prog = (double) frameActual / duracionFrames;
            double alpha = Math.max(0.0, 1.0 - prog);
            double radio = radioInicial + prog * 50.0;

            gc.save();
            gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * 0.8));
            gc.setLineWidth(2.5 * (1.0 - prog));
            gc.strokeOval(cx - radio, cy - radio, radio * 2, radio * 2);
            gc.restore();
        }

        private Image obtenerSpriteFrame(RepositorioSprites repo, int idx) {
            if (repo == null || idx < 0 || idx >= SPRITES_EXPLOSION.size()) return null;
            Image img = repo.obtenerImagenPorNombre(SPRITES_EXPLOSION.get(idx));
            if (img == null && idx == 8) {
                img = repo.obtenerImagenPorNombre("explosion 9");
            }
            return img;
        }
    }

    private final List<EfectoExplosion> explosiones = new ArrayList<>();
    private final ConversorCoordenadas conversor;
    private RepositorioSprites repositorioSprites;

    public GestorEfectos() {
        this(new TransformadorCoordenadas(), new GestorImagenes());
    }

    public GestorEfectos(ConversorCoordenadas conversor) {
        this(conversor, new GestorImagenes());
    }

    public GestorEfectos(ConversorCoordenadas conversor, RepositorioSprites repositorioSprites) {
        this.conversor = conversor != null ? conversor : new TransformadorCoordenadas();
        this.repositorioSprites = repositorioSprites != null ? repositorioSprites : new GestorImagenes();
    }

    public void setRepositorioSprites(RepositorioSprites repositorioSprites) {
        if (repositorioSprites != null) {
            this.repositorioSprites = repositorioSprites;
        }
    }

    public void agregarExplosion(Vector2D posicionFisica, double radioInicial, Color color) {
        if (posicionFisica != null) {
            explosiones.add(new EfectoExplosion(posicionFisica, radioInicial, color));
        }
    }

    public void agregarExplosion(Vector2D posicionFisica, double radioInicial) {
        agregarExplosion(posicionFisica, radioInicial, Color.ORANGE);
    }

    public void actualizar() {
        Iterator<EfectoExplosion> it = explosiones.iterator();
        while (it.hasNext()) {
            if (it.next().avanzar()) {
                it.remove();
            }
        }
    }

    public void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas) {
        for (EfectoExplosion e : explosiones) {
            e.dibujar(gc, anchoCanvas, altoCanvas, conversor, repositorioSprites);
        }
    }

    public int getCantidadExplosionesActivas() {
        return explosiones.size();
    }

    public void limpiar() {
        explosiones.clear();
    }
}
