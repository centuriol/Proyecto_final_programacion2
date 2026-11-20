package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * SRP: Dibuja la grilla espacial de coordenadas y ejes centrales adaptados a la cámara y zoom.
 */
public class CapaGrillaEspacial {

    private static final FabricaRender FABRICA = new FabricaRender();
    private static final Color COLOR_GRILLA_LINEA = Color.web("#2b2d3a");
    private static final Color COLOR_GRILLA_EJE = Color.web("#5be3ff");

    public void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas, ProyeccionCamara camara) {
        ProyeccionCamara cam = camara != null ? camara : FABRICA.crearCamaraZoom();
        double minX = cam.pantallaAMundoX(0.0, anchoCanvas);
        double maxX = cam.pantallaAMundoX(anchoCanvas, anchoCanvas);
        double minY = cam.pantallaAMundoY(0.0, altoCanvas);
        double maxY = cam.pantallaAMundoY(altoCanvas, altoCanvas);

        dibujarLineasCeldas(gc, minX, maxX, minY, maxY);
        dibujarEjesCentrales(gc, anchoCanvas, altoCanvas, minX, maxX, minY, maxY);
        gc.setGlobalAlpha(1.0);
    }

    private void dibujarLineasCeldas(GraphicsContext gc, double minX, double maxX, double minY, double maxY) {
        gc.setStroke(COLOR_GRILLA_LINEA);
        gc.setLineWidth(1.0);
        gc.setGlobalAlpha(0.25);

        int tamanoCelda = 32;
        double inicioX = Math.floor(minX / tamanoCelda) * tamanoCelda;
        double inicioY = Math.floor(minY / tamanoCelda) * tamanoCelda;
        for (double x = inicioX; x <= maxX; x += tamanoCelda) {
            gc.strokeLine(x, minY, x, maxY);
        }
        for (double y = inicioY; y <= maxY; y += tamanoCelda) {
            gc.strokeLine(minX, y, maxX, y);
        }
    }

    private void dibujarEjesCentrales(GraphicsContext gc, double anchoCanvas, double altoCanvas,
                                      double minX, double maxX, double minY, double maxY) {
        gc.setStroke(COLOR_GRILLA_EJE);
        gc.setGlobalAlpha(0.18);
        double ejeX = anchoCanvas / 2.0;
        double ejeY = altoCanvas / 2.0;
        if (ejeX >= minX && ejeX <= maxX) {
            gc.strokeLine(ejeX, minY, ejeX, maxY);
        }
        if (ejeY >= minY && ejeY <= maxY) {
            gc.strokeLine(minX, ejeY, maxX, ejeY);
        }
    }
}
