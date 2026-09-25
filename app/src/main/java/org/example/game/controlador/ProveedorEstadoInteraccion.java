package org.example.game.controlador;

import org.example.CuerpoCeleste;
import org.example.game.motor.Vector2D;

import java.util.List;

/**
 * ISP + DIP: Contrato que expone los datos de interacción visual (órbita sugerida y cuerpo seleccionado)
 * requeridos por el renderizador en el bucle principal de juego. Desacopla BucleJuego del ControladorMouse concreto.
 */
public interface ProveedorEstadoInteraccion {
    List<Vector2D> getTrayectoriaPreview();
    CuerpoCeleste getCuerpoSeleccionado();
}
