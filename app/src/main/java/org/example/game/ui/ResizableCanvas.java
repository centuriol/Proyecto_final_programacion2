package org.example.game.ui;

import javafx.scene.canvas.Canvas;

/**
 * Canvas con redimensionamiento automático para layouts de JavaFX (como StackPane).
 * Permite que el lienzo se ajuste al tamaño exacto de la ventana/pantalla
 * sin recortar el fondo ni generar scroll o zoom involuntario.
 */
public class ResizableCanvas extends Canvas {

    public ResizableCanvas() {
        super();
    }

    public ResizableCanvas(double width, double height) {
        super(width, height);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    @Override
    public double minWidth(double height) {
        return 1.0;
    }

    @Override
    public double minHeight(double width) {
        return 1.0;
    }

    @Override
    public double maxWidth(double height) {
        return Double.MAX_VALUE;
    }

    @Override
    public double maxHeight(double width) {
        return Double.MAX_VALUE;
    }

    @Override
    public void resize(double width, double height) {
        super.setWidth(width);
        super.setHeight(height);
    }
}
