package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;

/**
 * Estrategia de renderizado visual y métricas geométricas por tipo de cuerpo (SRP / OCP).
 */
public interface EstrategiaVisualTipo {

    double calcularTamanoGhost(double radio);

    double calcularRadioAtraccionPreview(double factorMasa);

    boolean tieneRotacionAnimada();

    boolean tieneAreaAtraccion();

    Color getColorAreaAtraccion();

    boolean tieneLineasReferenciaOrbital();

    void dibujarSuperficie(GraphicsContext gc, CuerpoCeleste c, double r);
}
