package org.example.game.simulacion;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
/**
 * Gestor de generacion de eventos cosmicos periodicos escalados segun el progreso del jugador.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de la frecuencia, conteo y generacion de eventos cosmicos naturales.
 * - DIP: Emite los cuerpos y notificaciones a traves de callbacks sin acoplarse directamente a la simulacion.
 */
public class GeneradorEventosCosmicos {

    private int ticksHastaSiguiente;
    private final Random random = new Random();

    public GeneradorEventosCosmicos() {
        this.ticksHastaSiguiente = ConfiguracionColisiones.TICKS_GRACIA_INICIAL_METEORITOS;
    }

    public GeneradorEventosCosmicos(int ticksIniciales) {
        this.ticksHastaSiguiente = ticksIniciales;
    }

    private int calcularSiguienteIntervalo() {
        double variacion = ConfiguracionColisiones.VARIACION_MIN_INTERVALO_METEORITO
                + (Math.random() * ConfiguracionColisiones.RANGO_VARIACION_INTERVALO_METEORITO);
        return (int) (ConfiguracionColisiones.INTERVALO_BASE_EVENTO_METEORITO * variacion);
    }

    public void actualizarEventosCosmicos(
            List<CuerpoCeleste> cuerpos,
            double anchoMundo,
            double altoMundo,
            Consumer<CuerpoCeleste> agregadorCuerpo,
            Consumer<MensajeEvento> emisorEventos,
            long tickActual
    ) {
        actualizarEventosCosmicos(
                cuerpos,
                anchoMundo,
                altoMundo,
                agregadorCuerpo,
                emisorEventos,
                tickActual,
                ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL
        );
    }

    public void actualizarEventosCosmicos(
            List<CuerpoCeleste> cuerpos,
            double anchoMundo,
            double altoMundo,
            Consumer<CuerpoCeleste> agregadorCuerpo,
            Consumer<MensajeEvento> emisorEventos,
            long tickActual,
            double factorProgreso
    ) {
        if (cuerpos == null) return;

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

        if (planetas.isEmpty()) {
            return;
        }

        ticksHastaSiguiente--;
        if (ticksHastaSiguiente <= 0) {
            ticksHastaSiguiente = calcularSiguienteIntervalo();

            if (meteoritosActivos < ConfiguracionColisiones.MAX_METEORITOS_EVENTO_ACTIVOS) {
                CuerpoCeleste objetivo = planetas.get(random.nextInt(planetas.size()));
                Meteorito m = CuerpoCelesteFactory.crearMeteoritoHaciaObjetivo(anchoMundo, altoMundo, objetivo, factorProgreso);

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

    public int getTicksHastaSiguiente() {
        return ticksHastaSiguiente;
    }

    public void setTicksHastaSiguiente(int ticksHastaSiguiente) {
        this.ticksHastaSiguiente = Math.max(1, ticksHastaSiguiente);
    }
}
