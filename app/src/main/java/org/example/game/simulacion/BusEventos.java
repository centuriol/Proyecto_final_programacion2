package org.example.game.simulacion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Bus de eventos desacoplado para notificaciones de simulación y sincronización con UI (SRP / Observer).
 */
public class BusEventos {

    private static class CanalObservadoresUI {
        final List<Runnable> listenersUI = new CopyOnWriteArrayList<>();
        Runnable onCambioEstado;
        Runnable onGameOver;

        void agregarUI(Runnable listener) {
            if (listener != null && !listenersUI.contains(listener)) listenersUI.add(listener);
        }

        void removerUI(Runnable listener) {
            if (listener != null) listenersUI.remove(listener);
        }

        void notificarUI() {
            for (Runnable r : listenersUI) {
                try {
                    r.run();
                } catch (Exception ex) {
                    System.err.println("[WARN] Error al ejecutar listener de UI: " + ex.getMessage());
                }
            }
        }
    }

    private final List<Consumer<MensajeEvento>> listenersEvento = new ArrayList<>();
    private final CanalObservadoresUI canalUI = new CanalObservadoresUI();

    public void agregarListenerEvento(Consumer<MensajeEvento> listener) {
        if (listener != null) listenersEvento.add(listener);
    }

    public void notificarEvento(MensajeEvento evento) {
        for (Consumer<MensajeEvento> listener : listenersEvento) {
            try {
                listener.accept(evento);
            } catch (Exception ex) {
                System.err.println("[WARN] Error al despachar evento de bus a listener: " + ex.getMessage());
            }
        }
        System.out.println("[Tick " + evento.tick() + "] [" + evento.tipo().name() + "] " + evento.mensaje());
    }

    public void notificarEvento(String mensaje, MensajeEvento.TipoMensaje tipo, long tick) {
        notificarEvento(new MensajeEvento(mensaje, tipo, tick));
    }

    public void agregarListenerUI(Runnable listener) { canalUI.agregarUI(listener); }
    public void removerListenerUI(Runnable listener) { canalUI.removerUI(listener); }
    public void notificarListenersUI() { canalUI.notificarUI(); }
    public void notificarCambioEstado() { if (canalUI.onCambioEstado != null) canalUI.onCambioEstado.run(); }
    public void notificarGameOver() { if (canalUI.onGameOver != null) canalUI.onGameOver.run(); }
    public void setOnCambioEstado(Runnable onCambioEstado) { canalUI.onCambioEstado = onCambioEstado; }
    public void setOnGameOver(Runnable onGameOver) { canalUI.onGameOver = onGameOver; }
}
