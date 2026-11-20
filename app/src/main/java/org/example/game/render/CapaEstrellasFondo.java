package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SRP: Genera y dibuja el campo procedimental de estrellas titilantes del fondo espacial.
 */
public class CapaEstrellasFondo {

    private record EstrellaFondo(double x, double y, double size, double brilloBase, double velocidadParpadeo) {}

    private final List<EstrellaFondo> estrellas = new ArrayList<>();

    public void generar(double ancho, double alto) {
        estrellas.clear();
        Random rand = new Random(42);
        for (int i = 0; i < 180; i++) {
            double x = rand.nextDouble() * ancho;
            double y = rand.nextDouble() * alto;
            double size = rand.nextDouble() < 0.8 ? 1.5 : (rand.nextDouble() < 0.9 ? 2.5 : 3.5);
            double brillo = 0.2 + rand.nextDouble() * 0.6;
            double vel = 0.5 + rand.nextDouble() * 2.0;
            estrellas.add(new EstrellaFondo(x, y, size, brillo, vel));
        }
    }

    public void dibujar(GraphicsContext gc, long tick, CalculadorParallaxFondo parallax) {
        double offsetX = parallax.getDesplazamientoFondoX();
        double offsetY = parallax.getDesplazamientoFondoY();
        double ancho = parallax.getAnchoCanvas();
        double alto = parallax.getAltoCanvas();
        for (EstrellaFondo ef : estrellas) {
            double alpha = Math.max(0.1, Math.min(1.0, ef.brilloBase() + 0.25 * Math.sin(tick * 0.05 * ef.velocidadParpadeo())));
            double x = ancho > 0 ? parallax.normalizarEnRango(ef.x() + offsetX, ancho) : ef.x() + offsetX;
            double y = alto > 0 ? parallax.normalizarEnRango(ef.y() + offsetY, alto) : ef.y() + offsetY;
            gc.setFill(Color.color(0.91, 0.89, 0.85, alpha));
            gc.fillRect((int) x, (int) y, (int) ef.size(), (int) ef.size());
        }
    }
}
