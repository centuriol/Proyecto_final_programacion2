package org.example.game.render;

/**
 * Componente de camara que gestiona el nivel de zoom de la vista.
 *
 * Principios SOLID:
 * - SRP: Encapsula exclusivamente el estado y logica de zoom, sin mezclar
 *   responsabilidades de renderizado ni de simulacion.
 * - OCP: Se puede extender con pan/scroll sin modificar la clase existente.
 */
public class CamaraZoom {

    private static final double ZOOM_MINIMO = 0.5;
    private static final double ZOOM_MAXIMO = 1.5;
    private static final double ZOOM_INICIAL = 1.0;
    private static final double INCREMENTO_ZOOM = 0.1;

    private double nivelZoom = ZOOM_INICIAL;

    /**
     * Acerca el zoom un paso (incrementa el nivel).
     */
    public void acercar() {
        nivelZoom = Math.min(ZOOM_MAXIMO, nivelZoom + INCREMENTO_ZOOM);
    }

    /**
     * Aleja el zoom un paso (decrementa el nivel).
     */
    public void alejar() {
        nivelZoom = Math.max(ZOOM_MINIMO, nivelZoom - INCREMENTO_ZOOM);
    }

    /**
     * Resetea el zoom al nivel por defecto (1.0x).
     */
    public void resetear() {
        nivelZoom = ZOOM_INICIAL;
    }

    /**
     * Ajusta el zoom por un delta arbitrario (positivo acerca, negativo aleja).
     * @param delta Cantidad a sumar al nivel actual de zoom.
     */
    public void ajustarPorDelta(double delta) {
        nivelZoom = Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, nivelZoom + delta));
    }

    public double getNivelZoom() {
        return nivelZoom;
    }

    public double getZoomMinimo() {
        return ZOOM_MINIMO;
    }

    public double getZoomMaximo() {
        return ZOOM_MAXIMO;
    }

    /**
     * Indica si el zoom esta en el nivel por defecto (sin escala aplicada).
     */
    public boolean esZoomNeutro() {
        return Math.abs(nivelZoom - ZOOM_INICIAL) < 0.001;
    }
}
