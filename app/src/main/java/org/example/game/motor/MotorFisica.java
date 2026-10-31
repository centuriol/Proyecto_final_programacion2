package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.MensajeEvento;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
/**
 * DIP: SistemaSolar y SimulacionSolar dependen de esta interfaz, no de una implementación
 * concreta de física. Se puede cambiar el cálculo sin tocar SistemaSolar ni SimulacionSolar.
 * Interfaz limpia y desacoplada (LSP / ISP / OCP).
 */
public interface MotorFisica {
    void avanzarPaso(List<CuerpoCeleste> cuerpos);

    default void setListenerMensaje(Consumer<String> listener) {}
    default void agregarListenerColision(Runnable listener) {}
    default void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) {}
    default void setListenerExplosion(BiConsumer<Vector2D, Double> listener) {}
    default void setProveedorProgreso(DoubleSupplier proveedorProgreso) {}
}
