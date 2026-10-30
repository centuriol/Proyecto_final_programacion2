package org.example.game.cuerpo;

/**
 * Entidades con ciclo de vida finito que expiran tras un período determinado (ISP).
 */
public interface Expirable {
    boolean estaExpirado();
}
