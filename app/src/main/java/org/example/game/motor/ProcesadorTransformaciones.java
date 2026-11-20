package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstracción para procesar transformaciones astrofísicas de cuerpos celestes por proximidad o eventos (DIP / ISP).
 */
public interface ProcesadorTransformaciones {
    void procesarTransformaciones(List<CuerpoCeleste> cuerpos);
    void setListenerMensaje(Consumer<String> listener);
}
