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
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.motor.TransformadorCuerpos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * Orquestador principal de la simulación "Órbita".
 * SRP: Coordina el avance de la simulación delegando en componentes especializados (reloj, bus, colocación, física, presets).
 * DIP: Depende de interfaces MotorFisica e Inventario; colaboradores inyectados.
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
            this.motorFisica.setProveedorProgreso(() -> EscaladorAmenazaMeteorito.calcularFactorProgreso(this.progresion));
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

        // 1. Física permisiva con asistencia orbital
        motorFisica.avanzarPaso(sistemaSolar.getCuerpos());

        // 2. Actualizar civilizaciones
        actualizarCivilizaciones();

        // 2.5 Transformaciones de cuerpos por proximidad
        transformadorCuerpos.procesarTransformaciones(sistemaSolar.getCuerpos());

        // 3. Economía pasiva y satélites
        gestorEconomia.actualizarEconomia(sistemaSolar.getCuerpos(), inventario);

        // 4. Misiones de colonización en curso
        gestorColonizacion.actualizar(this::notificarEvento, tickActual);

        // 5. Efectos visuales
        gestorEfectos.actualizar();

        // 6. Eventos cósmicos escalados según el progreso tecnológico / masa desbloqueada
        double factorProgreso = EscaladorAmenazaMeteorito.calcularFactorProgreso(progresion);
        generadorEventosCosmicos.actualizarEventosCosmicos(
                sistemaSolar.getCuerpos(), anchoMundo, altoMundo, this::agregarCuerpo, this::notificarEvento, tickActual, factorProgreso
        );

        // 7. Condición de derrota
        verificarDerrota();

        // 8. Notificaciones de cambio de estado
        busEventos.notificarCambioEstado();
        reloj.finalizarTick();
    }

    public void avanzarTicks(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            avanzarTick();
        }
    }

    private void actualizarCivilizaciones() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>(sistemaSolar.getCuerpos());
        for (CuerpoCeleste c : cuerpos) {
            c.comoCivilizable().ifPresent(civ -> {
                if (civ.tieneCivilizacion()) {
                    if (civ.getCivilizacion() != null && civ.getCivilizacion().getEmisorEventos() == null) {
                        civ.getCivilizacion().setEmisorEventos(this::notificarEvento);
                    }
                    civ.actualizarCivilizacion(cuerpos);
                }
            });
        }
    }

    private void verificarDerrota() {
        if (estaPerdida() && !gameOverDisparado) {
            gameOverDisparado = true;
            reloj.pause();
            busEventos.notificarGameOver();
        }
    }

    // ===== Gestión de Cuerpos =====

    public int getCantidadPlanetasActuales() {
        int count = 0;
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c != null && c.esSimulado() && c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta()) {
                count++;
            }
        }
        return count;
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return spawnCuerpo(tipo, null, x, y, factorMasa, velocidadInicial);
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, String nombrePersonalizado, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double factorEfectivo = (tipo == TipoCuerpo.SATELITE) ? 1.0 : factorMasa;
        if (!validarRequisitosColocacion(tipo, factorEfectivo)) {
            return Optional.empty();
        }

        CuerpoCeleste cuerpo = CuerpoCelesteFactory.crearCuerpo(tipo, x, y, factorEfectivo, velocidadInicial);
        if (cuerpo == null) return Optional.empty();

        if (nombrePersonalizado != null && !nombrePersonalizado.isBlank()) {
            cuerpo.setNombre(nombrePersonalizado.trim());
        }
        if (inventario.gastarParaCrear(tipo, factorEfectivo)) {
            agregarCuerpo(cuerpo);
            salirModoColocacion();
            notificarEvento("Cuerpo colocado: " + cuerpo.getNombre() + " (" + tipo.nombre + ")", MensajeEvento.TipoMensaje.INFO);
            return Optional.of(cuerpo);
        }
        return Optional.empty();
    }

    private boolean validarRequisitosColocacion(TipoCuerpo tipo, double factorMasa) {
        return validadorColocacionCuerpos.validar(
                tipo,
                factorMasa,
                getCantidadPlanetasActuales(),
                progresion,
                inventario,
                this::notificarEvento
        );
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        sistemaSolar.agregarCuerpo(cuerpo);
        busEventos.notificarCambioEstado();
    }

    public void eliminarCuerpo(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return;
        cuerpo.setSimulado(false);
        cuerpo.setMasa(0);
        cuerpo.comoCivilizable().ifPresent(civ -> {
            if (civ.getCivilizacion() != null) {
                civ.getCivilizacion().destruir();
            }
        });
        sistemaSolar.getCuerpos().remove(cuerpo);

        List<CuerpoCeleste> satelitesAEliminar = new ArrayList<>();
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c.getCuerpoOrbitado() == cuerpo) {
                if (c.getTipoCuerpo() == TipoCuerpo.SATELITE) {
                    satelitesAEliminar.add(c);
                }
                c.setCuerpoOrbitado(null);
            }
        }
        for (CuerpoCeleste s : satelitesAEliminar) {
            eliminarCuerpo(s);
        }

        busEventos.notificarCambioEstado();
    }

    public void limpiarCuerpos() {
        sistemaSolar.getCuerpos().clear();
        busEventos.notificarCambioEstado();
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
        CuerpoCelesteFactory.resetearContador();
        salirModoColocacion();
        notificarListenersUI();
        notificarEvento("Simulación reiniciada por completo.", MensajeEvento.TipoMensaje.INFO);
    }

    public void setOnGameOver(Runnable onGameOver) {
        busEventos.setOnGameOver(onGameOver);
    }

    // ===== Modo Colocación =====

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
        if (getModoColocacion() == ModoColocacion.COLOCANDO && getPosicionPreview() != null && getTipoColocacion() != null) {
            Vector2D posPreviewLocal = getPosicionPreview();
            Vector2D velOrbital = calcularVelocidadOrbitalAsistida(posPreviewLocal);
            CuerpoCeleste cuerpoCercano = encontrarCuerpoGravitatorioCercano(posPreviewLocal);
            Optional<CuerpoCeleste> spawnedOpt = spawnCuerpo(getTipoColocacion(), getNombreColocacion(), posPreviewLocal.x(), posPreviewLocal.y(), getFactorMasaColocacion(), velOrbital);
            if (spawnedOpt.isPresent() && cuerpoCercano != null) {
                CuerpoCeleste recienCreado = spawnedOpt.get();
                boolean esMenor = recienCreado.getTipoCuerpo() == TipoCuerpo.LUNA || recienCreado.getTipoCuerpo() == TipoCuerpo.SATELITE;
                boolean cercanoEsPlaneta = cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().esPlaneta();
                boolean recienEsPlaneta = recienCreado.getTipoCuerpo() != null && recienCreado.getTipoCuerpo().esPlaneta();
                boolean cercanoEsMasivo = cuerpoCercano.getTipoCuerpo() != null &&
                        (cuerpoCercano.getTipoCuerpo().esEstrella() || cuerpoCercano.getTipoCuerpo().esAgujeroNegro());

                double dist = posPreviewLocal.distanciaA(cuerpoCercano.getPosicion());
                if ((esMenor && cercanoEsPlaneta) || (recienEsPlaneta && cercanoEsMasivo)) {
                    recienCreado.setCuerpoOrbitado(cuerpoCercano);
                    recienCreado.setRadioOrbita(dist);
                }
            }
        }
    }

    public void confirmarColocacionConImpulso(Vector2D posFisica, Vector2D velFisica) {
        if (getTipoColocacion() != null) {
            spawnCuerpo(getTipoColocacion(), getNombreColocacion(), posFisica.x(), posFisica.y(), getFactorMasaColocacion(), velFisica);
        }
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

    // ===== Control de Simulación (Reloj) =====

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

    public boolean activarEscudo(Planeta planeta) {
        return servicioEscudo.activar(this, planeta);
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
