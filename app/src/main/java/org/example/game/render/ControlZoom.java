package org.example.game.render;
/**
 * Contrato de control de zoom y vista de camara (ISP / DIP).
 */
public interface ControlZoom {

    void aplicarRueda(double deltaY);

    void setNivelZoom(double nuevoNivel);

    double getNivelZoom();

    boolean esZoomNeutro();

    double pantallaAMundo(double coordenadaPantalla, double dimensionVista);

    double mundoAPantallaX(double xFisica, double anchoVista);

    double mundoAPantallaY(double yFisica, double altoVista);

    void desplazarPorPantalla(double deltaPantallaX, double deltaPantallaY);

    void enfocarPuntoFisico(double xFisica, double yFisica);

    DesplazamientoCamara getDesplazamiento();

    void resetear();
}
