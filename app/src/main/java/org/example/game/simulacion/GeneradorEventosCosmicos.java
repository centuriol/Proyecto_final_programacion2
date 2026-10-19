package org.example.game.simulacion;

import org.example.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Gestor de generación de eventos cósmicos periódicos (ej. lluvia de meteoritos y bólidos).
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de la frecuencia, conteo y generación de eventos cósmicos naturales.
 * - DIP: Emite los cuerpos y notificaciones a través de callbacks sin acoplarse directamente a la simulación.
 */
public class GeneradorEventosCosmicos {

    private int ticksUltimoMeteorito = 0;

    public void actualizarEventosCosmicos(
            List<CuerpoCeleste> cuerpos,
            double anchoMundo,
            double altoMundo,
            Consumer<CuerpoCeleste> agregadorCuerpo,
            Consumer<MensajeEvento> emisorEventos,
            long tickActual
    ) {
        ticksUltimoMeteorito++;
        if (ticksUltimoMeteorito >= ConfiguracionSimulacion.INTERVALO_METEORITOS_ALEATORIOS) {
            ticksUltimoMeteorito = 0;

            long meteoritosActivos = 0;
            if (cuerpos != null) {
                for (CuerpoCeleste c : cuerpos) {
                    if (c != null && c.getTipoCuerpo() == TipoCuerpo.METEORITO) {
                        meteoritosActivos++;
                    }
                }
            }

            if (meteoritosActivos < ConfiguracionSimulacion.MAX_METEORITOS_SIMULTANEOS) {
                Meteorito m = CuerpoCelesteFactory.crearMeteoritoAleatorio(anchoMundo, altoMundo);
                if (agregadorCuerpo != null) {
                    agregadorCuerpo.accept(m);
                }
                if (emisorEventos != null) {
                    emisorEventos.accept(new MensajeEvento(
                            "Alerta cosmica: Nuevo meteorito detectado en trayectoria.",
                            MensajeEvento.TipoMensaje.ADVERTENCIA,
                            tickActual
                    ));
                }
            }
        }
    }

    public void resetear() {
        ticksUltimoMeteorito = 0;
    }
}
