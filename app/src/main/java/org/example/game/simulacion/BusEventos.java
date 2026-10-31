package org.example.game.simulacion;
import java.util.ArrayList; import java.util.List; import java.util.concurrent.CopyOnWriteArrayList; import java.util.function.Consumer;
/**
 * Bus de eventos desacoplado para notificaciones de simulación y sincronización con UI (SRP / Observer).
 */
public class BusEventos {

    private final List<Consumer<MensajeEvento>> listenersEvento = new ArrayList<>();
    private final List<Runnable> listenersUI = new CopyOnWriteArrayList<>();
    private Runnable onCambioEstado;
    private Runnable onGameOver;

    public void agregarListenerEvento(Consumer<MensajeEvento> listener) {
        if (listener != null) {
            listenersEvento.add(listener);
        }
    }

    public void notificarEvento(MensajeEvento evento) {
        for (Consumer<MensajeEvento> listener : listenersEvento) {
            try {
                listener.accept(evento);
            } catch (Exception ignored) {}
        }
        System.out.println("[Tick " + evento.tick() + "] [" + evento.tipo().name() + "] " + evento.mensaje());
    }

    public void notificarEvento(String mensaje, MensajeEvento.TipoMensaje tipo, long tick) {
        notificarEvento(new MensajeEvento(mensaje, tipo, tick));
    }

    public void agregarListenerUI(Runnable listener) {
        if (listener != null && !listenersUI.contains(listener)) {
            listenersUI.add(listener);
        }
    }

    public void removerListenerUI(Runnable listener) {
        if (listener != null) {
            listenersUI.remove(listener);
        }
    }

    public void notificarListenersUI() {
        for (Runnable r : listenersUI) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    public void notificarCambioEstado() {
        if (onCambioEstado != null) {
            onCambioEstado.run();
        }
    }

    public void notificarGameOver() {
        if (onGameOver != null) {
            onGameOver.run();
        }
    }

    public void setOnCambioEstado(Runnable onCambioEstado) {
        this.onCambioEstado = onCambioEstado;
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }
}
