package org.example.game.guardado;
import org.example.game.render.ControlZoom;
import org.example.game.simulacion.SimulacionSolar;
/**
 * Contrato de mapeo bidireccional entre el modelo vivo de simulacion y DatosPartida (DIP / ISP).
 */
public interface MapeadorPartida {

    DatosPartida capturar(String nombrePartida, SimulacionSolar simulacion, ControlZoom zoom);

    void restaurar(DatosPartida datos, SimulacionSolar simulacion, ControlZoom zoom);
}
