package org.example.game.simulacion;

/**
 * Representa un evento o notificacion del juego emitido por la simulacion.
 */
public record MensajeEvento(String mensaje, TipoMensaje tipo, long tick) {

    public enum TipoMensaje {
        INFO("#5be3ff"),
        ADVERTENCIA("#ffd700"),
        PELIGRO("#ff4444"),
        EXITO("#50fa7b");

        public final String colorHex;

        TipoMensaje(String colorHex) {
            this.colorHex = colorHex;
        }
    }
}
