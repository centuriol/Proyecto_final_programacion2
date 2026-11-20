package org.example.game.render;

/**
 * Contrato compuesto de control de zoom, proyección y navegación de cámara (ISP / DIP).
 */
public interface ControlZoom extends ProyeccionCamara, NavegacionCamara {
    void aplicarRueda(double deltaY);
    void setNivelZoom(double nuevoNivel);
    double getNivelZoom();
    boolean esZoomNeutro();
}
