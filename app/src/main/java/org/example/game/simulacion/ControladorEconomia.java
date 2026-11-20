package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.jugador.InventarioJugador;
import org.example.game.progresion.SistemaProgresion;
import java.util.List;

/**
 * Abstracción para el procesamiento de producción económica por tick (DIP / ISP).
 */
public interface ControladorEconomia {
    void actualizarEconomia(List<CuerpoCeleste> cuerpos, InventarioJugador inventario);
    void actualizarEconomia(List<CuerpoCeleste> cuerpos, InventarioJugador inventario, SistemaProgresion prog);
}
