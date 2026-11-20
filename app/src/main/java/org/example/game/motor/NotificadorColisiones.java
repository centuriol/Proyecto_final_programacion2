package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.colision.ContextoColision;
import org.example.game.simulacion.MensajeEvento;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * SRP: Administra callbacks de notificación y crea contextos instrumentados para cada resolución de colisión.
 */
public class NotificadorColisiones {

    private Consumer<String> listenerMensaje;
    private BiConsumer<String, MensajeEvento.TipoMensaje> listenerNotificacion;
    private BiConsumer<Vector2D, Double> listenerExplosion;

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
    }

    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) {
        this.listenerNotificacion = listener;
    }

    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) {
        this.listenerExplosion = listener;
    }

    public ContextoColision crearContextoSimple(Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        return new ContextoColision(aEliminar, aAgregar, listenerMensaje, listenerNotificacion, listenerExplosion);
    }

    public ContextoColision crearContextoRastreado(Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar,
                                                  boolean[] explosionEmitida, boolean[] eventoEmitido) {
        Consumer<String> msgConRegistro = msg -> {
            eventoEmitido[0] = true;
            if (listenerMensaje != null) listenerMensaje.accept(msg);
        };
        BiConsumer<String, MensajeEvento.TipoMensaje> notifConRegistro = (msg, tipo) -> {
            eventoEmitido[0] = true;
            if (listenerNotificacion != null) listenerNotificacion.accept(msg, tipo);
        };
        BiConsumer<Vector2D, Double> emisorConRegistro = (pos, radio) -> {
            explosionEmitida[0] = true;
            if (listenerExplosion != null) listenerExplosion.accept(pos, radio);
        };
        return new ContextoColision(aEliminar, aAgregar, msgConRegistro, notifConRegistro, emisorConRegistro);
    }
}
