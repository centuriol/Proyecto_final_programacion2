package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;

import java.util.List;
import java.util.function.Consumer;

/**
 * DIP: SistemaSolar y SimulacionSolar dependen de esta interfaz, no de una implementación
 * concreta de física. Se puede cambiar el cálculo sin tocar SistemaSolar ni SimulacionSolar.
 */
public interface MotorFisica {
    void avanzarPaso(List<CuerpoCeleste> cuerpos);

    default void agregarListenerColision(Runnable listener) {}
    default void setListenerMensaje(Consumer<String> listener) {}
}
