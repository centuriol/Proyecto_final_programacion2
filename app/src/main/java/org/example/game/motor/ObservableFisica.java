package org.example.game.motor;
import java.util.function.Consumer;
/**
 * Interfaz segregada para emisores u observadores de eventos del motor físico (ISP).
 */
public interface ObservableFisica {
    void agregarListenerColision(Runnable listener);
    void setListenerMensaje(Consumer<String> listener);
}
