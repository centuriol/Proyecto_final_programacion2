package org.example.game.render;

/**
 * Capacidad segregada para conversión y proyección de coordenadas entre pantalla y mundo físico (ISP).
 */
public interface ProyeccionCamara {
    double pantallaAMundo(double coordenadaPantalla, double dimensionVista);
    double pantallaAMundoX(double xPantalla, double anchoVista);
    double pantallaAMundoY(double yPantalla, double altoVista);
    double mundoAPantallaX(double xFisica, double anchoVista);
    double mundoAPantallaY(double yFisica, double altoVista);
}
