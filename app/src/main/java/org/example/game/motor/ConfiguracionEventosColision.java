package org.example.game.motor;

import org.example.game.simulacion.MensajeEvento;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Contrato segregado para configurar listeners de eventos y progreso en la resolución de colisiones (DIP / ISP).
 */
public interface ConfiguracionEventosColision {
    void setListenerMensaje(Consumer<String> listener);
    void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener);
    void setListenerExplosion(BiConsumer<Vector2D, Double> listener);
    void setProveedorProgreso(DoubleSupplier proveedorProgreso);
}
