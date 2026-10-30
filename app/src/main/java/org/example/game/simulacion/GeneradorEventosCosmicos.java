package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;

import java.util.ArrayList;
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

    private int ticksHastaSiguiente;
    private final java.util.Random random = new java.util.Random();

    public GeneradorEventosCosmicos() {
        // Tiempo de gracia inicial generoso para que el jugador pueda construir y desarrollar ciencia con calma
        this.ticksHastaSiguiente = 3600;
    }

    public GeneradorEventosCosmicos(int ticksIniciales) {
        this.ticksHastaSiguiente = ticksIniciales;
    }

    private int calcularSiguienteIntervalo() {
        // ~2400 ticks con ±30% de aleatoriedad (2400 * [0.7 .. 1.3] = 1680 .. 3120 ticks)
        double variacion = 0.7 + (Math.random() * 0.6);
        return (int) (2400 * variacion);
    }

    public void actualizarEventosCosmicos(
            List<CuerpoCeleste> cuerpos,
            double anchoMundo,
            double altoMundo,
            Consumer<CuerpoCeleste> agregadorCuerpo,
            Consumer<MensajeEvento> emisorEventos,
            long tickActual
    ) {
        if (cuerpos == null) return;

        // Filtrar planetas válidos para ser objetivo
        List<CuerpoCeleste> planetas = new ArrayList<>();
        long meteoritosActivos = 0;

        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.esSimulado()) {
                if (c.getTipoCuerpo() == TipoCuerpo.METEORITO) {
                    meteoritosActivos++;
                } else if (c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta()) {
                    planetas.add(c);
                }
            }
        }

        // Sin planetas, no genera
        if (planetas.isEmpty()) {
            return;
        }

        ticksHastaSiguiente--;
        if (ticksHastaSiguiente <= 0) {
            ticksHastaSiguiente = calcularSiguienteIntervalo();

            if (meteoritosActivos < 2) { // Máximo 2 simultáneos
                CuerpoCeleste objetivo = planetas.get(random.nextInt(planetas.size()));
                Meteorito m = CuerpoCelesteFactory.crearMeteoritoHaciaObjetivo(anchoMundo, altoMundo, objetivo);

                if (agregadorCuerpo != null) {
                    agregadorCuerpo.accept(m);
                }
                if (emisorEventos != null) {
                    emisorEventos.accept(new MensajeEvento(
                            "Alerta: Meteorito en camino hacia " + objetivo.getNombre(),
                            MensajeEvento.TipoMensaje.ADVERTENCIA,
                            tickActual
                    ));
                }
            }
        }
    }

    public void resetear() {
        ticksHastaSiguiente = calcularSiguienteIntervalo();
    }
}
