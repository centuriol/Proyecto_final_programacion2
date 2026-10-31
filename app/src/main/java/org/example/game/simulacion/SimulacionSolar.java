package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.FabricaCuerpos;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.SistemaSolar;
import org.example.game.motor.MotorFisica;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.colonizacion.GestorColonizacion;
import org.example.game.efectos.GestorEfectos;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.progresion.SistemaProgresion;

/**
 * Orquestador principal de la simulacion "Orbita".
 * Principios SOLID:
 * - SRP: Delega paso a CoordinadorPasoSimulacion, ciclo de vida a GestorCicloVidaCuerpos,
 *   colocacion a GestorColocacionCuerpos y tiempo a RelojSimulacion.
 * - DIP: Depende de subsistemas modulares inyectados o creados con responsabilidades unicas.
 */
public class SimulacionSolar {

    private final SistemaSolar sistemaSolar;
    private final MotorFisica motorFisica;
    private final InventarioJugador inventario;
    private final TrayectoriaPredictor predictor;
    private final TransformadorCuerpos transformadorCuerpos;
    private final GestorEconomia gestorEconomia;
    private final GeneradorEventosCosmicos generadorEventosCosmicos;
    private final SistemaProgresion progresion;
    private final GestorColonizacion gestorColonizacion;
    private final GestorEfectos gestorEfectos;
    private final RelojSimulacion reloj;
    private final BusEventos busEventos;
    private final ControladorColocacion controladorColocacion;
    private final EvaluadorCondicionDerrota evaluadorCondicionDerrota;
    private final ValidadorColocacionCuerpos validadorColocacionCuerpos;
    private final PresetsSistema presetsSistema;
    private final ServicioEscudo servicioEscudo;
    private final ServicioReembolso servicioReembolso;
    private final CalculadorAmenazaMeteorito escaladorAmenaza;
    private final FabricaCuerpos fabricaCuerpos;

    private final CoordinadorPasoSimulacion coordinadorPaso;
    private final GestorCicloVidaCuerpos gestorCicloVida;
    private final GestorColocacionCuerpos gestorColocacion;

    private boolean gameOverDisparado = false;
    private double anchoMundo;
    private double altoMundo;

    public SimulacionSolar(double anchoMundo, double altoMundo) {
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
        this.motorFisica = new MotorFisicaPermisiva(Math.max(anchoMundo, altoMundo));
        this.sistemaSolar = new SistemaSolar(this.motorFisica);
        this.inventario = new InventarioJugador();
        this.predictor = new TrayectoriaPredictor();
        this.transformadorCuerpos = new TransformadorCuerpos();
        this.gestorEconomia = new GestorEconomia();
        this.generadorEventosCosmicos = new GeneradorEventosCosmicos();
        this.progresion = new SistemaProgresion();
        this.gestorColonizacion = new GestorColonizacion();
        this.gestorEfectos = new GestorEfectos();
        this.reloj = new RelojSimulacion();
        this.busEventos = new BusEventos();
        this.controladorColocacion = new ControladorColocacion();
        this.evaluadorCondicionDerrota = new EvaluadorCondicionDerrota();
        this.validadorColocacionCuerpos = new ValidadorColocacionCuerpos();
        this.presetsSistema = new PresetsSistema();
        this.servicioEscudo = new ServicioEscudo();
        this.servicioReembolso = new ServicioReembolso();
        this.escaladorAmenaza = new EscaladorAmenazaMeteorito();
        this.fabricaCuerpos = new CuerpoCelesteFactory(this.escaladorAmenaza);

        this.coordinadorPaso = new CoordinadorPasoSimulacion(
                this.motorFisica, this.sistemaSolar, this.transformadorCuerpos,
                this.gestorEconomia, this.inventario, this.gestorColonizacion,
                this.gestorEfectos, this.generadorEventosCosmicos, this.progresion,
                this.escaladorAmenaza
        );
        this.gestorCicloVida = new GestorCicloVidaCuerpos(
                this.sistemaSolar, this.validadorColocacionCuerpos,
                this.progresion, this.inventario, this.busEventos, this.fabricaCuerpos
        );
        this.gestorColocacion = new GestorColocacionCuerpos(
                this.controladorColocacion, this.gestorCicloVida,
                () -> this.anchoMundo, () -> this.altoMundo
        );

        configurarNotificadores();
    }

    private void configurarNotificadores() {
        this.transformadorCuerpos.setListenerMensaje(msg -> notificarEvento(msg, MensajeEvento.TipoMensaje.EXITO));
        if (this.motorFisica != null) {
            this.motorFisica.setListenerMensaje(msg -> notificarEvento(msg, MensajeEvento.TipoMensaje.EXITO));
            this.motorFisica.agregarListenerColision(busEventos::notificarCambioEstado);
            this.motorFisica.setListenerNotificacion(this::notificarEvento);
            this.motorFisica.setListenerExplosion(this.gestorEfectos::agregarExplosion);
            this.motorFisica.setProveedorProgreso(() -> this.escaladorAmenaza.factorProgreso(this.progresion));
        }
    }

    public void notificarEvento(String mensaje, MensajeEvento.TipoMensaje tipo) {
        busEventos.notificarEvento(mensaje, tipo, reloj.getTickActual());
    }

    public void avanzarTick() {
        if (!reloj.debeAvanzar()) return;

        reloj.avanzarTick();
        long tickActual = reloj.getTickActual();

        coordinadorPaso.ejecutarPaso(tickActual, anchoMundo, altoMundo, busEventos::notificarEvento, this::agregarCuerpo);
        verificarDerrota();

        busEventos.notificarCambioEstado();
        reloj.finalizarTick();
    }

    public void avanzarTicks(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            avanzarTick();
        }
    }

    public void step() {
        reloj.step();
        avanzarTick();
    }

    private void verificarDerrota() {
        if (estaPerdida() && !gameOverDisparado) {
            gameOverDisparado = true;
            reloj.pause();
            busEventos.notificarGameOver();
        }
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        gestorCicloVida.agregarCuerpo(cuerpo);
    }

    public void limpiarCuerpos() {
        gestorCicloVida.limpiarCuerpos();
    }

    public boolean estaPerdida() {
        return evaluadorCondicionDerrota.estaPerdida(sistemaSolar.getCuerpos());
    }

    public void reiniciar() {
        limpiarCuerpos();
        gestorColonizacion.limpiar();
        gestorEfectos.limpiar();
        inventario.reiniciar();
        progresion.reiniciar();
        reloj.reiniciar();
        gameOverDisparado = false;
        this.fabricaCuerpos.resetearContador();
        gestorColocacion.salirModoColocacion();
        busEventos.notificarListenersUI();
        notificarEvento("Simulacion reiniciada por completo.", MensajeEvento.TipoMensaje.INFO);
    }

    public void sincronizarTrasCarga() {
        this.gameOverDisparado = estaPerdida();
        gestorColocacion.salirModoColocacion();
        gestorEfectos.limpiar();
        busEventos.notificarCambioEstado();
        busEventos.notificarListenersUI();
    }

    public void setDimensionesMundo(double ancho, double alto) {
        if (ancho > 0 && alto > 0) {
            this.anchoMundo = ancho;
            this.altoMundo = alto;
        }
    }

    public SistemaSolar getSistemaSolar() { return sistemaSolar; }
    public InventarioJugador getInventario() { return inventario; }
    public SistemaProgresion getProgresion() { return progresion; }
    public GestorColonizacion getGestorColonizacion() { return gestorColonizacion; }
    public GestorEfectos getGestorEfectos() { return gestorEfectos; }
    public TrayectoriaPredictor getPredictor() { return predictor; }
    public TransformadorCuerpos getTransformadorCuerpos() { return transformadorCuerpos; }
    public RelojSimulacion getReloj() { return reloj; }
    public BusEventos getBusEventos() { return busEventos; }
    public GestorColocacionCuerpos getGestorColocacion() { return gestorColocacion; }
    public GestorCicloVidaCuerpos getGestorCicloVida() { return gestorCicloVida; }
    public PresetsSistema getPresetsSistema() { return presetsSistema; }
    public ServicioEscudo getServicioEscudo() { return servicioEscudo; }
    public ServicioReembolso getServicioReembolso() { return servicioReembolso; }
    public CoordinadorPasoSimulacion getCoordinadorPaso() { return coordinadorPaso; }
}
