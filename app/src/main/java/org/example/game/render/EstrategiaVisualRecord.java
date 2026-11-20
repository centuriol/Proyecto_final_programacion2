package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;

import java.util.function.DoubleUnaryOperator;

/**
 * SRP: Representación inmutable de una estrategia visual por tipo de cuerpo celeste.
 */
public record EstrategiaVisualRecord(
        DoubleUnaryOperator tamanoGhost,
        DoubleUnaryOperator radioAtraccionPreview,
        boolean rotacionAnimada,
        boolean areaAtraccion,
        Color colorArea,
        boolean lineasReferencia,
        DibujadorSuperficie consumidorDibujo
) implements EstrategiaVisualTipo {

    @FunctionalInterface
    public interface DibujadorSuperficie {
        void dibujar(GraphicsContext gc, CuerpoCeleste c, double r);
    }

    @Override public double calcularTamanoGhost(double radio) { return tamanoGhost.applyAsDouble(radio); }
    @Override public double calcularRadioAtraccionPreview(double factorMasa) { return radioAtraccionPreview.applyAsDouble(factorMasa); }
    @Override public boolean tieneRotacionAnimada() { return rotacionAnimada; }
    @Override public boolean tieneAreaAtraccion() { return areaAtraccion; }
    @Override public Color getColorAreaAtraccion() { return colorArea; }
    @Override public boolean tieneLineasReferenciaOrbital() { return lineasReferencia; }
    @Override public void dibujarSuperficie(GraphicsContext gc, CuerpoCeleste c, double r) {
        if (consumidorDibujo != null) consumidorDibujo.dibujar(gc, c, r);
    }
}
