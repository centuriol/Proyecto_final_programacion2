package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Contrato compuesto de fábrica desacoplada de cuerpos celestes, segregado por capacidades (ISP / DIP).
 */
public interface FabricaCuerpos extends
        CreadorPlanetas,
        CreadorAstrosMenoresYEstrellas,
        CreadorAgujerosNegros,
        CreadorMeteoritos,
        GeneradorIdentidadCuerpos {

    CuerpoCeleste crearCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial);

    void registrarCreador(TipoCuerpo tipo, CreadorCuerpo creador);
}
