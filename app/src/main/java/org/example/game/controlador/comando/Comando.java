package org.example.game.controlador.comando;

/**
 * DIP + ISP: Abstracción del patrón Command para encapsular acciones ejecutables.
 * Permite que el ControladorTeclado ejecute acciones sin acoplarse a su implementación.
 */
public interface Comando {
    void ejecutar();
}
