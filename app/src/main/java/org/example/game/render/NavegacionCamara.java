package org.example.game.render;

/**
 * Capacidad segregada para desplazamiento, enfoque y reinicio de vista de cámara (ISP).
 */
public interface NavegacionCamara {
    void desplazarPorPantalla(double deltaPantallaX, double deltaPantallaY);
    void enfocarPuntoFisico(double xFisica, double yFisica);
    DesplazamientoCamara getDesplazamiento();
    void resetear();
}
