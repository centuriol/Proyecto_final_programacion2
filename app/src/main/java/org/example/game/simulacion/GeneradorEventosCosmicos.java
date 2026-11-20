package org.example.game.simulacion;

import org.example.game.cuerpo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Gestor de generación de eventos cósmicos periódicos escalados según el progreso del jugador (SRP / DIP).
 */
public class GeneradorEventosCosmicos implements GeneradorEventos {

    private static final FabricaSimulacion FABRICA_SIM = new FabricaSimulacion();

    private record EstadoCuerposEvento(List<CuerpoCeleste> planetas, long meteoritosActivos, double masaPlanetaMax) {}

    private static class ProgramadorIntervalos {
        int ticksHastaSiguiente;
        final Random random = new Random();

        ProgramadorIntervalos(int ticksIniciales) {
            this.ticksHastaSiguiente = ticksIniciales;
        }

        boolean avanzarYTocaDisparar() {
            ticksHastaSiguiente--;
            if (ticksHastaSiguiente > 0) return false;
            resetear();
            return true;
        }

        void resetear() {
            double variacion = ConfiguracionColisiones.VARIACION_MIN_INTERVALO_METEORITO
                    + (Math.random() * ConfiguracionColisiones.RANGO_VARIACION_INTERVALO_METEORITO);
            ticksHastaSiguiente = (int) (ConfiguracionColisiones.INTERVALO_BASE_EVENTO_METEORITO * variacion);
        }

        CuerpoCeleste elegirObjetivo(List<CuerpoCeleste> planetas) {
            return planetas.get(random.nextInt(planetas.size()));
        }
    }

    private final ProgramadorIntervalos programador;
    private final CreadorMeteoritos creadorMeteoritos;

    public GeneradorEventosCosmicos() {
        this(ConfiguracionColisiones.TICKS_GRACIA_INICIAL_METEORITOS, FABRICA_SIM.crearCreadorMeteoritos());
    }

    public GeneradorEventosCosmicos(int ticksIniciales) {
        this(ticksIniciales, FABRICA_SIM.crearCreadorMeteoritos());
    }

    public GeneradorEventosCosmicos(int ticksIniciales, CreadorMeteoritos creadorMeteoritos) {
        this.programador = new ProgramadorIntervalos(ticksIniciales);
        this.creadorMeteoritos = creadorMeteoritos != null ? creadorMeteoritos : FABRICA_SIM.crearCreadorMeteoritos();
    }

    public void actualizarEventosCosmicos(List<CuerpoCeleste> cuerpos, double anchoMundo, double altoMundo,
                                          Consumer<CuerpoCeleste> agregadorCuerpo,
                                          Consumer<MensajeEvento> emisorEventos, long tickActual) {
        actualizarEventosCosmicos(cuerpos, anchoMundo, altoMundo, agregadorCuerpo, emisorEventos, tickActual,
                ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
    }

    public void actualizarEventosCosmicos(List<CuerpoCeleste> cuerpos, double anchoMundo, double altoMundo,
                                          Consumer<CuerpoCeleste> agregadorCuerpo,
                                          Consumer<MensajeEvento> emisorEventos, long tickActual, double factorProgreso) {
        if (cuerpos == null) return;
        EstadoCuerposEvento estado = analizarEscenario(cuerpos);
        if (estado.planetas().isEmpty() || !programador.avanzarYTocaDisparar()) return;
        if (estado.meteoritosActivos() >= ConfiguracionColisiones.MAX_METEORITOS_EVENTO_ACTIVOS) return;

        lanzarMeteorito(estado, anchoMundo, altoMundo, factorProgreso, agregadorCuerpo, emisorEventos, tickActual);
    }

    private EstadoCuerposEvento analizarEscenario(List<CuerpoCeleste> cuerpos) {
        List<CuerpoCeleste> planetas = new ArrayList<>();
        long meteoritosActivos = 0;
        double masaPlanetaMax = 0.0;
        for (CuerpoCeleste c : cuerpos) {
            if (c == null || !c.esSimulado()) continue;
            if (c.getTipoCuerpo() == TipoCuerpo.METEORITO) {
                meteoritosActivos++;
            } else if (RegistroCapacidades.obtenerCivilizable(c).isPresent()) {
                planetas.add(c);
                masaPlanetaMax = Math.max(masaPlanetaMax, c.getMasa());
            }
        }
        return new EstadoCuerposEvento(planetas, meteoritosActivos, masaPlanetaMax);
    }

    private void lanzarMeteorito(EstadoCuerposEvento estado, double anchoMundo, double altoMundo, double factorProgreso,
                                 Consumer<CuerpoCeleste> agregadorCuerpo, Consumer<MensajeEvento> emisorEventos, long tickActual) {
        CuerpoCeleste objetivo = programador.elegirObjetivo(estado.planetas());
        Meteorito m = creadorMeteoritos.crearMeteoritoHaciaObjetivo(anchoMundo, altoMundo, objetivo, factorProgreso);
        if (estado.masaPlanetaMax() > 0.0) {
            m.setMasa(estado.masaPlanetaMax() * ConfiguracionColisiones.FRACCION_MASA_METEORITO_PLANETA_MAX);
        }
        if (agregadorCuerpo != null) agregadorCuerpo.accept(m);
        if (emisorEventos != null) {
            emisorEventos.accept(new MensajeEvento(
                    "Alerta: Meteorito en camino hacia " + objetivo.getNombre(),
                    MensajeEvento.TipoMensaje.ADVERTENCIA, tickActual
            ));
        }
    }

    public void resetear() { programador.resetear(); }
    public int getTicksHastaSiguiente() { return programador.ticksHastaSiguiente; }
    public void setTicksHastaSiguiente(int ticks) { programador.ticksHastaSiguiente = Math.max(1, ticks); }
}
