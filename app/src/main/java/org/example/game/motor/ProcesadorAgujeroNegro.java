package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Abstracción para procesamiento de horizonte de eventos y absorción de agujeros negros (DIP / ISP).
 */
public interface ProcesadorAgujeroNegro {
    void procesar(List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar, Consumer<String> listenerMensaje);
}
