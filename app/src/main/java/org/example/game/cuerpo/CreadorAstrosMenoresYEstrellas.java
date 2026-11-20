package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Capacidad segregada para la creación de estrellas, lunas y satélites (ISP).
 */
public interface CreadorAstrosMenoresYEstrellas {
    Estrella crearEstrella(double x, double y, double factorMasa);
    Estrella crearEstrellaPersonalizada(String nombre, double masa, double x, double y);
    Luna crearLuna(double x, double y, double factorMasa, Vector2D velocidadInicial);
    Luna crearLunaPersonalizada(String nombre, double masa, double x, double y, Vector2D velocidadInicial);
    Satelite crearSatelite(double x, double y, double factorMasa, Vector2D velocidadInicial);
}
