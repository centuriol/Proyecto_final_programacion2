package org.example.game.motor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * SRP: Gestiona los listeners de colisiones y mensajes del motor físico.
 */
public class NotificadorEventosFisica {

    private final List<Runnable> listenersColision = new ArrayList<>();
    private Consumer<String> listenerMensaje;

    public void agregarListenerColision(Runnable listener) {
        if (listener != null) {
            listenersColision.add(listener);
        }
    }

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
    }

    public Consumer<String> getListenerMensaje() {
        return listenerMensaje;
    }

    public void notificarColision() {
        for (Runnable l : listenersColision) {
            try {
                l.run();
            } catch (Exception ex) {
                System.err.println("[WARN] Error al ejecutar listener de colision: " + ex.getMessage());
            }
        }
    }
}
