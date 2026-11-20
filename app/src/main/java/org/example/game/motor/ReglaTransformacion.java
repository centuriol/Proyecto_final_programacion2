package org.example.game.motor;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * SRP: Regla declarativa que define cuándo y hacia qué tipo se transforma un planeta cerca de una estrella.
 */
public record ReglaTransformacion(
        TipoCuerpo tipoOrigen,
        TipoCuerpo tipoDestino,
        double radioMinimoActivacion,
        double radioActivacion,
        boolean migrarSiguienteOrbita,
        String nombreResultante,
        String mensajeEvento
) {
    public ReglaTransformacion(TipoCuerpo tipoOrigen, TipoCuerpo tipoDestino, double radioActivacion,
                               String nombreResultante, String mensajeEvento) {
        this(tipoOrigen, tipoDestino, 0.0, radioActivacion, true, nombreResultante, mensajeEvento);
    }
}
