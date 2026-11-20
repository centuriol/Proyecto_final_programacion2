package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstracción para la generación periódica de eventos cósmicos y control de su temporizador (DIP / ISP).
 */
public interface GeneradorEventos {
    void actualizarEventosCosmicos(List<CuerpoCeleste> cuerpos, double anchoMundo, double altoMundo,
                                   Consumer<CuerpoCeleste> agregadorCuerpo,
                                   Consumer<MensajeEvento> emisorEventos, long tickActual);

    void actualizarEventosCosmicos(List<CuerpoCeleste> cuerpos, double anchoMundo, double altoMundo,
                                   Consumer<CuerpoCeleste> agregadorCuerpo,
                                   Consumer<MensajeEvento> emisorEventos, long tickActual, double factorProgreso);

    int getTicksHastaSiguiente();
    void setTicksHastaSiguiente(int ticks);
}
