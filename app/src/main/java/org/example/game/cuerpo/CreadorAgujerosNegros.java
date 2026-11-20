package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Capacidad segregada para la creación de agujeros negros estelares y supermasivos (ISP).
 */
public interface CreadorAgujerosNegros {
    AgujeroNegro crearAgujeroNegro(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial);
    AgujeroNegro crearAgujeroNegroPersonalizado(double x, double y, double masa, Vector2D velocidadInicial);
    AgujeroNegro crearAgujeroNegroEstelar(double x, double y, double factorMasa);
    AgujeroNegro crearAgujeroNegroSupermasivo(double x, double y, double factorMasa);
}
