package org.example.game.simulacion;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.progresion.SistemaProgresion;
import java.util.function.BiConsumer;

/**
 * Abstracción para validar requisitos de tecnología, capacidad y recursos antes de colocar un cuerpo (DIP / ISP).
 */
public interface ValidadorColocacion {
    boolean validar(TipoCuerpo tipo, double factorMasa, int cantidadPlanetasActuales,
                    SistemaProgresion progresion, InventarioJugador inventario,
                    BiConsumer<String, MensajeEvento.TipoMensaje> notificador);
}
