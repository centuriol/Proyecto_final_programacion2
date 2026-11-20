package org.example.game.cuerpo;

/**
 * Contrato compuesto para cuerpos celestes con capacidades de defensa y escudo visual activo (ISP).
 */
public interface Defensor extends Escudable, IdentidadFisicaDefensor {
    default double getRadioEscudo() { return 65.0; }
    default double getPulsoVisual() { return 0.0; }
}
