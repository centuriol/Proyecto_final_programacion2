package org.example.game.cuerpo;

/**
 * Capacidad segregada para generación y sincronización de nombres/identificadores de cuerpos (ISP).
 */
public interface GeneradorIdentidadCuerpos {
    String generarNombre(String prefijo);
    void resetearContador();
    void asegurarContadorMinimo(long minimo);
}
