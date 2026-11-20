package org.example.game.simulacion;

import org.example.game.colonizacion.GestorColonizacion;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.SistemaSolar;
import org.example.game.efectos.GestorEfectos;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.progresion.SistemaProgresion;

/**
 * Orquestador principal de la simulación "Órbita" (SRP / DIP).
 */
public class SimulacionSolar {

    private static class EstadoMundo {
        boolean gameOverDisparado = false;
        double anchoMundo;
        double altoMundo;

        EstadoMundo(double ancho, double alto) {
            this.anchoMundo = ancho;
            this.altoMundo = alto;
        }

        void actualizarDimensiones(double ancho, double alto) {
            if (ancho > 0 && alto > 0) {
                this.anchoMundo = ancho;
                this.altoMundo = alto;
            }
        }
    }

    private static final FabricaSimulacion FABRICA_SIM = new FabricaSimulacion();

    private final EstadoMundo mundo;
    private final SubsistemaSimulacion sub;

    public SimulacionSolar(double anchoMundo, double altoMundo) {
        this.mundo = new EstadoMundo(anchoMundo, altoMundo);
        this.sub = FABRICA_SIM.crearSubsistemaPorDefecto(
                anchoMundo, altoMundo,
                () -> this.mundo.anchoMundo,
                () -> this.mundo.altoMundo,
                this::notificarEvento
        );
    }

    public SimulacionSolar(double anchoMundo, double altoMundo, SubsistemaSimulacion subsistema) {
        this.mundo = new EstadoMundo(anchoMundo, altoMundo);
        this.sub = subsistema != null ? subsistema : FABRICA_SIM.crearSubsistemaPorDefecto(
                anchoMundo, altoMundo,
                () -> this.mundo.anchoMundo,
                () -> this.mundo.altoMundo,
                this::notificarEvento
        );
    }

    public void notificarEvento(String mensaje, MensajeEvento.TipoMensaje tipo) {
        sub.busEventos.notificarEvento(mensaje, tipo, sub.reloj.getTickActual());
    }

    public void avanzarTick() {
        if (!sub.reloj.debeAvanzar()) return;
        sub.reloj.avanzarTick();
        sub.coordinadorPaso.ejecutarPaso(
                sub.reloj.getTickActual(), mundo.anchoMundo, mundo.altoMundo,
                sub.busEventos::notificarEvento, this::agregarCuerpo
        );
        verificarDerrota();
        sub.busEventos.notificarCambioEstado();
        sub.reloj.finalizarTick();
    }

    public void avanzarTicks(int cantidad) {
        for (int i = 0; i < cantidad; i++) avanzarTick();
    }

    public void step() {
        sub.reloj.step();
        avanzarTick();
    }

    private void verificarDerrota() {
        if (estaPerdida() && !mundo.gameOverDisparado) {
            mundo.gameOverDisparado = true;
            sub.reloj.pause();
            sub.busEventos.notificarGameOver();
        }
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) { sub.gestorCicloVida.agregarCuerpo(cuerpo); }
    public void limpiarCuerpos() { sub.gestorCicloVida.limpiarCuerpos(); }
    public boolean estaPerdida() { return sub.evaluadorCondicionDerrota.estaPerdida(sub.sistemaSolar.getCuerpos()); }

    public void reiniciar() {
        sub.reiniciarTodo();
        mundo.gameOverDisparado = false;
        notificarEvento("Simulacion reiniciada por completo.", MensajeEvento.TipoMensaje.INFO);
    }

    public void sincronizarTrasCarga() {
        mundo.gameOverDisparado = estaPerdida();
        sub.gestorColocacion.salirModoColocacion();
        sub.gestorEfectos.limpiar();
        sub.busEventos.notificarCambioEstado();
        sub.busEventos.notificarListenersUI();
    }

    public void setDimensionesMundo(double ancho, double alto) { mundo.actualizarDimensiones(ancho, alto); }

    public SistemaSolar getSistemaSolar() { return sub.sistemaSolar; }
    public InventarioJugador getInventario() { return sub.inventario; }
    public SistemaProgresion getProgresion() { return sub.progresion; }
    public GestorColonizacion getGestorColonizacion() { return sub.gestorColonizacion; }
    public GestorEfectos getGestorEfectos() { return sub.gestorEfectos; }
    public TrayectoriaPredictor getPredictor() { return sub.predictor; }
    public TransformadorCuerpos getTransformadorCuerpos() { return sub.transformadorCuerpos; }
    public RelojSimulacion getReloj() { return sub.reloj; }
    public BusEventos getBusEventos() { return sub.busEventos; }
    public GestorColocacionCuerpos getGestorColocacion() { return sub.gestorColocacion; }
    public GestorCicloVidaCuerpos getGestorCicloVida() { return sub.gestorCicloVida; }
    public PresetsSistema getPresetsSistema() { return sub.presetsSistema; }
    public ServicioEscudo getServicioEscudo() { return sub.servicioEscudo; }
    public ServicioReembolso getServicioReembolso() { return sub.servicioReembolso; }
    public CoordinadorPasoSimulacion getCoordinadorPaso() { return sub.coordinadorPaso; }
}
