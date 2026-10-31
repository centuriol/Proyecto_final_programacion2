package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.SistemaSolar;
import org.example.game.motor.MotorFisica;
import org.example.game.colonizacion.GestorColonizacion;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.efectos.GestorEfectos;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.motor.TransformadorCuerpos;

import java.util.Map;
import java.util.Optional;

/**
 * Orquestador principal de la simulacion "Orbita".
 * Principios SOLID:
 * - SRP: Fachada que delega el paso a CoordinadorPasoSimulacion, el ciclo de vida a GestorCicloVidaCuerpos
 *   y la colocacion a GestorColocacionCuerpos.
 * - DIP: Depende de interfaces y componentes modulares inyectados.
 */
public class SimulacionSolar {

    public enum ModoColocacion {
        NINGUNO, COLOCANDO
    }

    private final SistemaSolar sistemaSolar;
    private final MotorFisica motorFisica;
    private final InventarioJugador inventario;
    private final TrayectoriaPredictor predictor;
    private final TransformadorCuerpos transformadorCuerpos;

    // Componentes coordinados (SRP / DIP)
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
    private final org.example.game.cuerpo.FabricaCuerpos fabricaCuerpos;

    // Delegados especializados
    private final CoordinadorPasoSimulacion coordinadorPaso;
    private final GestorCicloVidaCuerpos gestorCicloVida;
    private final GestorColocacionCuerpos gestorColocacion;

    private boolean gameOverDisparado = false;
    private double anchoMundo;
    private double altoMundo;

    public SimulacionSolar(double anchoMundo, double altoMundo,
                           MotorFisica motorFisica,
                           SistemaSolar sistemaSolar,
                           InventarioJugador inventario,
                           TrayectoriaPredictor predictor,
                           TransformadorCuerpos transformadorCuerpos,
                           GestorEconomia gestorEconomia,
                           GeneradorEventosCosmicos generadorEventosCosmicos,
                           SistemaProgresion progresion,
                           GestorColonizacion gestorColonizacion,
                           GestorEfectos gestorEfectos,
                           RelojSimulacion reloj,
                           BusEventos busEventos,
                           ControladorColocacion controladorColocacion,
                           EvaluadorCondicionDerrota evaluadorCondicionDerrota,
                           ValidadorColocacionCuerpos validadorColocacionCuerpos,
                           PresetsSistema presetsSistema,
                           ServicioEscudo servicioEscudo,
                           ServicioReembolso servicioReembolso) {
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
        this.motorFisica = motorFisica != null ? motorFisica : new MotorFisicaPermisiva(Math.max(anchoMundo, altoMundo));
        this.sistemaSolar = sistemaSolar != null ? sistemaSolar : new SistemaSolar(this.motorFisica);
        this.inventario = inventario != null ? inventario : new InventarioJugador();
        this.predictor = predictor != null ? predictor : new TrayectoriaPredictor();
        this.transformadorCuerpos = transformadorCuerpos != null ? transformadorCuerpos : new TransformadorCuerpos();
        this.gestorEconomia = gestorEconomia != null ? gestorEconomia : new GestorEconomia();
        this.generadorEventosCosmicos = generadorEventosCosmicos != null ? generadorEventosCosmicos : new GeneradorEventosCosmicos();
        this.progresion = progresion != null ? progresion : new SistemaProgresion();
        this.gestorColonizacion = gestorColonizacion != null ? gestorColonizacion : new GestorColonizacion();
        this.gestorEfectos = gestorEfectos != null ? gestorEfectos : new GestorEfectos();
        this.reloj = reloj != null ? reloj : new RelojSimulacion();
        this.busEventos = busEventos != null ? busEventos : new BusEventos();
        this.controladorColocacion = controladorColocacion != null ? controladorColocacion : new ControladorColocacion();
        this.evaluadorCondicionDerrota = evaluadorCondicionDerrota != null ? evaluadorCondicionDerrota : new EvaluadorCondicionDerrota();
        this.validadorColocacionCuerpos = validadorColocacionCuerpos != null ? validadorColocacionCuerpos : new ValidadorColocacionCuerpos();
        this.presetsSistema = presetsSistema != null ? presetsSistema : new PresetsSistema();
        this.servicioEscudo = servicioEscudo != null ? servicioEscudo : new ServicioEscudo();
        this.servicioReembolso = servicioReembolso != null ? servicioReembolso : new ServicioReembolso();
        this.escaladorAmenaza = new EscaladorAmenazaMeteorito();
        this.fabricaCuerpos = new org.example.game.cuerpo.CuerpoCelesteFactory(this.escaladorAmenaza);

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
                this.controladorColocacion, this.gestorCicloVida
        );

        configurarNotificadores();
    }

    public SimulacionSolar(double anchoMundo, double altoMundo,
                           MotorFisica motorFisica,
                           SistemaSolar sistemaSolar,
                           InventarioJugador inventario,
                           TrayectoriaPredictor predictor,
                           TransformadorCuerpos transformadorCuerpos,
                           GestorEconomia gestorEconomia,
                           GeneradorEventosCosmicos generadorEventosCosmicos,
                           SistemaProgresion progresion,
                           GestorColonizacion gestorColonizacion,
                           GestorEfectos gestorEfectos,
                           RelojSimulacion reloj,
                           BusEventos busEventos,
                           ControladorColocacion controladorColocacion) {
        this(anchoMundo, altoMundo, motorFisica, sistemaSolar, inventario, predictor, transformadorCuerpos,
                gestorEconomia, generadorEventosCosmicos, progresion, gestorColonizacion, gestorEfectos, reloj, busEventos, controladorColocacion,
                null, null, null, null, null);
    }

    public SimulacionSolar(double anchoMundo, double altoMundo,
                           MotorFisica motorFisica,
                           SistemaSolar sistemaSolar,
                           InventarioJugador inventario,
                           TrayectoriaPredictor predictor,
                           TransformadorCuerpos transformadorCuerpos,
                           GestorEconomia gestorEconomia,
                           GeneradorEventosCosmicos generadorEventosCosmicos,
                           SistemaProgresion progresion,
                           GestorColonizacion gestorColonizacion,
                           GestorEfectos gestorEfectos) {
        this(anchoMundo, altoMundo, motorFisica, sistemaSolar, inventario, predictor, transformadorCuerpos,
                gestorEconomia, generadorEventosCosmicos, progresion, gestorColonizacion, gestorEfectos, null, null, null);
    }

    public SimulacionSolar(double anchoMundo, double altoMundo,
                           MotorFisica motorFisica,
                           SistemaSolar sistemaSolar,
                           InventarioJugador inventario,
                           TrayectoriaPredictor predictor,
                           TransformadorCuerpos transformadorCuerpos,
                           GestorEconomia gestorEconomia,
                           GeneradorEventosCosmicos generadorEventosCosmicos) {
        this(anchoMundo, altoMundo, motorFisica, sistemaSolar, inventario, predictor, transformadorCuerpos,
                gestorEconomia, generadorEventosCosmicos, null, null, null);
    }

    public SimulacionSolar(double anchoMundo, double altoMundo) {
        this(anchoMundo, altoMundo, null, null, null, null, null, null, null, null, null, null);
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

    // ===== Bus de Eventos =====

    public void agregarListenerEvento(java.util.function.Consumer<MensajeEvento> listener) {
        busEventos.agregarListenerEvento(listener);
    }

    public void notificarEvento(MensajeEvento evento) {
        busEventos.notificarEvento(evento);
    }

    public void notificarEvento(String mensaje, MensajeEvento.TipoMensaje tipo) {
        busEventos.notificarEvento(mensaje, tipo, reloj.getTickActual());
    }

    // ===== Avance de Ticks =====

    public void avanzarTick() {
        if (!reloj.debeAvanzar()) return;

        reloj.avanzarTick();
        long tickActual = reloj.getTickActual();

        coordinadorPaso.ejecutarPaso(tickActual, anchoMundo, altoMundo, this::notificarEvento, this::agregarCuerpo);
        verificarDerrota();

        busEventos.notificarCambioEstado();
        reloj.finalizarTick();
    }

    public void avanzarTicks(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            avanzarTick();
        }
    }

    private void verificarDerrota() {
        if (estaPerdida() && !gameOverDisparado) {
            gameOverDisparado = true;
            reloj.pause();
            busEventos.notificarGameOver();
        }
    }

    // ===== Gestion de Cuerpos =====

    public int getCantidadPlanetasActuales() {
        return gestorCicloVida.getCantidadPlanetasActuales();
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return spawnCuerpo(tipo, null, x, y, factorMasa, velocidadInicial);
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, String nombrePersonalizado, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return gestorCicloVida.spawnCuerpo(tipo, nombrePersonalizado, x, y, factorMasa, velocidadInicial, this::notificarEvento, this::salirModoColocacion);
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        gestorCicloVida.agregarCuerpo(cuerpo);
    }

    public void eliminarCuerpo(CuerpoCeleste cuerpo) {
        gestorCicloVida.eliminarCuerpo(cuerpo);
    }

    public void limpiarCuerpos() {
        gestorCicloVida.limpiarCuerpos();
    }

    // ===== Presets =====

    public void cargarPresetSistemaBasico() {
        presetsSistema.cargarPresetSistemaBasico(this);
    }

    public void cargarPresetEstrellaBinaria() {
        presetsSistema.cargarPresetEstrellaBinaria(this);
    }

    // ===== Derrota y Reinicio =====

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
        salirModoColocacion();
        notificarListenersUI();
        notificarEvento("Simulacion reiniciada por completo.", MensajeEvento.TipoMensaje.INFO);
    }

    public void setOnGameOver(Runnable onGameOver) {
        busEventos.setOnGameOver(onGameOver);
    }

    // ===== Modo Colocacion =====

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa, String nombre) {
        double maxPermitido = progresion != null ? progresion.getLimiteMasaActual() : ConfiguracionSimulacion.MASA_FACTOR_MAX;
        controladorColocacion.entrarModoColocacion(tipo, factorMasa, nombre, maxPermitido);
    }

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa) {
        entrarModoColocacion(tipo, factorMasa, null);
    }

    public void actualizarPosicionPreview(double xJavaFX, double yJavaFX) {
        controladorColocacion.actualizarPosicionPreview(xJavaFX, yJavaFX, anchoMundo, altoMundo);
    }

    public Vector2D calcularVelocidadOrbitalAsistida(Vector2D posFisica) {
        return controladorColocacion.calcularVelocidadOrbitalAsistida(posFisica, sistemaSolar.getCuerpos());
    }

    public void confirmarColocacionAutoOrbita() {
        gestorColocacion.confirmarColocacionAutoOrbita(this);
    }

    public void confirmarColocacionConImpulso(Vector2D posFisica, Vector2D velFisica) {
        gestorColocacion.confirmarColocacionConImpulso(this, posFisica, velFisica);
    }

    public void salirModoColocacion() {
        controladorColocacion.salirModoColocacion();
    }

    public String getNombreColocacion() {
        return controladorColocacion.getNombreColocacion();
    }

    public void setNombreColocacion(String nombre) {
        controladorColocacion.setNombreColocacion(nombre);
    }

    public CuerpoCeleste encontrarCuerpoGravitatorioCercano(Vector2D pos) {
        return controladorColocacion.encontrarCuerpoGravitatorioCercano(pos, sistemaSolar.getCuerpos());
    }

    public ModoColocacion getModoColocacion() {
        return controladorColocacion.getModoColocacion() == ControladorColocacion.ModoColocacion.COLOCANDO
                ? ModoColocacion.COLOCANDO
                : ModoColocacion.NINGUNO;
    }

    public TipoCuerpo getTipoColocacion() {
        return controladorColocacion.getTipoColocacion();
    }

    public Vector2D getPosicionPreview() {
        return controladorColocacion.getPosicionPreview();
    }

    public double getFactorMasaColocacion() {
        return controladorColocacion.getFactorMasaColocacion();
    }

    public void setFactorMasaColocacion(double f) {
        double maxPermitido = progresion != null ? progresion.getLimiteMasaActual() : ConfiguracionSimulacion.MASA_FACTOR_MAX;
        controladorColocacion.setFactorMasaColocacion(f, maxPermitido);
    }

    // ===== Control de Simulacion (Reloj) =====

    public void play() { reloj.play(); }
    public void pause() { reloj.pause(); }
    public void togglePause() { reloj.togglePause(); }
    public void step() {
        reloj.step();
        avanzarTick();
    }

    public void setVelocidadSimulacion(double v) { reloj.setVelocidadSimulacion(v); }
    public boolean isEnPausa() { return reloj.isEnPausa(); }
    public double getVelocidadSimulacion() { return reloj.getVelocidadSimulacion(); }
    public long getTickActual() { return reloj.getTickActual(); }

    // ===== Callbacks y Observadores UI =====

    public void agregarListenerUI(Runnable listener) {
        busEventos.agregarListenerUI(listener);
    }

    public void removerListenerUI(Runnable listener) {
        busEventos.removerListenerUI(listener);
    }

    public void notificarListenersUI() {
        busEventos.notificarListenersUI();
    }

    // Getters de colaboradores
    public SistemaSolar getSistemaSolar() { return sistemaSolar; }
    public MotorFisica getMotorFisica() { return motorFisica; }
    public InventarioJugador getInventario() { return inventario; }
    public SistemaProgresion getProgresion() { return progresion; }
    public GestorColonizacion getGestorColonizacion() { return gestorColonizacion; }
    public GestorEfectos getGestorEfectos() { return gestorEfectos; }
    public TrayectoriaPredictor getPredictor() { return predictor; }
    public TransformadorCuerpos getTransformadorCuerpos() { return transformadorCuerpos; }
    public RelojSimulacion getReloj() { return reloj; }
    public BusEventos getBusEventos() { return busEventos; }
    public ControladorColocacion getControladorColocacion() { return controladorColocacion; }
    public GeneradorEventosCosmicos getGeneradorEventosCosmicos() { return generadorEventosCosmicos; }
    public EvaluadorCondicionDerrota getEvaluadorCondicionDerrota() { return evaluadorCondicionDerrota; }
    public ValidadorColocacionCuerpos getValidadorColocacionCuerpos() { return validadorColocacionCuerpos; }
    public PresetsSistema getPresetsSistema() { return presetsSistema; }
    public ServicioEscudo getServicioEscudo() { return servicioEscudo; }
    public ServicioReembolso getServicioReembolso() { return servicioReembolso; }
    public CoordinadorPasoSimulacion getCoordinadorPaso() { return coordinadorPaso; }
    public GestorCicloVidaCuerpos getGestorCicloVida() { return gestorCicloVida; }
    public GestorColocacionCuerpos getGestorColocacion() { return gestorColocacion; }

    public boolean activarEscudo(Planeta planeta) {
        return servicioEscudo.activar(this, (org.example.game.cuerpo.Defensor) planeta);
    }

    public boolean activarEscudo(CuerpoCeleste cuerpo) {
        return servicioEscudo.obtenerDefensor(cuerpo).map(def -> servicioEscudo.activar(this, def)).orElse(false);
    }

    public boolean desmantelarCuerpo(CuerpoCeleste cuerpo) {
        return servicioReembolso.desmantelarCuerpo(this, cuerpo);
    }

    public Map<TipoRecurso, Double> calcularReembolso(CuerpoCeleste cuerpo) {
        return servicioReembolso.calcularReembolso(cuerpo);
    }

    public void sincronizarTrasCarga() {
        this.gameOverDisparado = estaPerdida();
        salirModoColocacion();
        gestorEfectos.limpiar();
        busEventos.notificarCambioEstado();
        notificarListenersUI();
    }

    public void setDimensionesMundo(double ancho, double alto) {
        if (ancho > 0 && alto > 0) {
            this.anchoMundo = ancho;
            this.altoMundo = alto;
        }
    }

    public double getAnchoMundo() { return anchoMundo; }
    public double getAltoMundo() { return altoMundo; }
}
