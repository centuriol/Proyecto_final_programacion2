package org.example.game.guardado;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.MensajeEvento;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstracción para la reconstrucción de cuerpos celestes a partir de datos persistidos (DIP / ISP).
 */
public interface ReconstructorCuerpo {
    List<CuerpoCeleste> reconstruirTodos(List<DatosCuerpo> datosCuerpos, Consumer<MensajeEvento> emisorEventos);
}
