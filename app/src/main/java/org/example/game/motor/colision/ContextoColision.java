package org.example.game.motor.colision;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.motor.Vector2D; import org.example.game.simulacion.MensajeEvento; import java.util.List; import java.util.Set; import java.util.function.BiConsumer; import java.util.function.Consumer;
/**
 * Contexto operativo y emisor de eventos para la resolución de colisiones (Parameter Object).
 */
public record ContextoColision( Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar, Consumer<String> emisorMensaje, BiConsumer<String, MensajeEvento.TipoMensaje> emisorEvento, BiConsumer<Vector2D, Double> emisorExplosion ) {
    public void emitirMensaje(String msg) {
        if (emisorMensaje != null) emisorMensaje.accept(msg);
    }

    public void emitirEvento(String msg, MensajeEvento.TipoMensaje tipo) {
        emitirMensaje(msg);
        if (emisorEvento != null) emisorEvento.accept(msg, tipo);
    }

    public void emitirExplosion(Vector2D centro, double radio) {
        if (emisorExplosion != null) emisorExplosion.accept(centro, radio);
    }
}
