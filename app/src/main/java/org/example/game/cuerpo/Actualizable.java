package org.example.game.cuerpo;
/**
 * Entidades que ejecutan lógica autónoma cada tick de simulación (ISP).
 */
public interface Actualizable {
    void actualizarEntidad(double dt);
}
