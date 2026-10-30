package org.example.game.motor.colision;

import org.example.game.cuerpo.CuerpoCeleste;

/**
 * Estrategia extensible para resolución de colisiones físicas o destructivas (OCP / Strategy).
 */
public interface ReglaColision {
    boolean aplica(CuerpoCeleste a, CuerpoCeleste b);
    void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision contexto);
}
