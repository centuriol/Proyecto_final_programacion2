package org.example.game.efectos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Gestor de efectos visuales (anillos y partículas de explosión que se desvanecen en ~40 frames).
 * SRP: Responsable exclusivo de registrar y dibujar efectos transitorios visuales.
 */
public class GestorEfectos {

    private static class EfectoExplosion {
        final Vector2D posicionFisica;
        final double radioInicial;
        final Color color;
        int frameActual = 0;
        final int duracionFrames = 40;

        EfectoExplosion(Vector2D posicionFisica, double radioInicial, Color color) {
            this.posicionFisica = posicionFisica;
            this.radioInicial = Math.max(10.0, radioInicial);
            this.color = color != null ? color : Color.ORANGE;
        }

        boolean avanzar() {
            frameActual++;
            return frameActual >= duracionFrames;
        }

        void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas) {
            double prog = (double) frameActual / duracionFrames;
            double alpha = Math.max(0.0, 1.0 - prog);
            double radio = radioInicial + prog * 50.0;

            double cx = ConstantesFisicas.fisicaAXJavaFX(posicionFisica.x(), anchoCanvas);
            double cy = ConstantesFisicas.fisicaAYJavaFX(posicionFisica.y(), altoCanvas);

            // Anillo exterior
            gc.save();
            gc.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), alpha * 0.8));
            gc.setLineWidth(2.5 * (1.0 - prog));
            gc.strokeOval(cx - radio, cy - radio, radio * 2, radio * 2);

            // Anillo interior secundario
            double radioInt = radio * 0.6;
            gc.setStroke(Color.color(1.0, 0.9, 0.4, alpha * 0.9));
            gc.setLineWidth(1.5);
            gc.strokeOval(cx - radioInt, cy - radioInt, radioInt * 2, radioInt * 2);
            gc.restore();
        }
    }

    private final List<EfectoExplosion> explosiones = new ArrayList<>();

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
            e.dibujar(gc, anchoCanvas, altoCanvas);
        }
    }

    public void limpiar() {
        explosiones.clear();
    }
}
