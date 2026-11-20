package org.example.game.controlador;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.SimulacionSolar;
import java.util.List;

/**
 * Abstracción para el cálculo y cacheo de trayectorias preview del cursor durante colocación (DIP / ISP).
 */
public interface PredictorPreview {
    void actualizarConThrottle(double mouseX, double mouseY, SimulacionSolar simulacion);
    void actualizarPrediccion(SimulacionSolar simulacion);
    void limpiar();
    List<Vector2D> getTrayectoriaPreview();
}
