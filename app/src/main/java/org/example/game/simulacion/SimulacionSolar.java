package org.example.game.simulacion;

import org.example.CuerpoCeleste;
import org.example.Estrella;
import org.example.Planeta;
import org.example.SistemaSolar;
import org.example.MotorFisica;
import org.example.game.colonizacion.GestorColonizacion;
import org.example.game.cuerpo.Luna;
import org.example.game.cuerpo.Satelite;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.efectos.GestorEfectos;
import org.example.game.jugador.InventarioJugador;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.SistemaProgresion;
import org.example.RenderizadorCircular;
import org.example.VistaSistemaSolar;
import org.example.game.motor.TransformadorCuerpos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Orquestador principal de la simulación "Órbita".
 * SRP: Coordina el bucle de simulación, gestión de cuerpos, progresión tecnológica, economía y presets.
 * DIP: Depende de interfaces MotorFisica e Inventario; colaboradores inyectados con valores por defecto.
 */
public class SimulacionSolar {
    private final SistemaSolar sistemaSolar;
    private final MotorFisica motorFisica;
    private final InventarioJugador inventario;
    private final VistaSistemaSolar vista;
    private final TrayectoriaPredictor predictor;
    private final TransformadorCuerpos transformadorCuerpos;
    private final Random random = new Random();

    // Estado de simulación
    private long tickActual = 0;
    private boolean enPausa = false;
    private boolean pasoUnico = false;
    private double velocidadSimulacion = 1.0;
    private boolean gameOverDisparado = false;
    private Runnable onGameOver;

    // Modo de colocación
    private ModoColocacion modoColocacion = ModoColocacion.NINGUNO;
    private TipoCuerpo tipoColocacion = null;
    private double factorMasaColocacion = 1.0;
    private String nombreColocacion = null;
    private Vector2D posicionPreview = null;

    // Componentes delegados (SRP / DIP)
    private final GestorEconomia gestorEconomia;
    private final GeneradorEventosCosmicos generadorEventosCosmicos;
    private final SistemaProgresion progresion;
    private final GestorColonizacion gestorColonizacion;
    private final GestorEfectos gestorEfectos;

    // Callbacks y listeners para UI y eventos (Patrón Observer / SRP / ISP)
    private final List<Runnable> listenersUI = new java.util.concurrent.CopyOnWriteArrayList<>();
    private Runnable onTickCallback;
    private Runnable onCambioEstadoCallback;
    private final List<java.util.function.Consumer<MensajeEvento>> listenersEvento = new ArrayList<>();

    private double anchoMundo;
    private double altoMundo;

    public SimulacionSolar(double anchoMundo, double altoMundo,
                           MotorFisica motorFisica,
                           SistemaSolar sistemaSolar,
                           InventarioJugador inventario,
                           VistaSistemaSolar vista,
                           TrayectoriaPredictor predictor,
                           TransformadorCuerpos transformadorCuerpos,
                           GestorEconomia gestorEconomia,
                           GeneradorEventosCosmicos generadorEventosCosmicos,
                           SistemaProgresion progresion,
                           GestorColonizacion gestorColonizacion,
                           GestorEfectos gestorEfectos) {
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
        this.motorFisica = motorFisica != null ? motorFisica : new MotorFisicaPermisiva(Math.max(anchoMundo, altoMundo));
        this.sistemaSolar = sistemaSolar != null ? sistemaSolar : new SistemaSolar(this.motorFisica);
        this.inventario = inventario != null ? inventario : new InventarioJugador();
        this.vista = vista != null ? vista : new VistaSistemaSolar(new RenderizadorCircular(anchoMundo, altoMundo));
        this.predictor = predictor != null ? predictor : new TrayectoriaPredictor();
        this.transformadorCuerpos = transformadorCuerpos != null ? transformadorCuerpos : new TransformadorCuerpos();
        this.gestorEconomia = gestorEconomia != null ? gestorEconomia : new GestorEconomia();
        this.generadorEventosCosmicos = generadorEventosCosmicos != null ? generadorEventosCosmicos : new GeneradorEventosCosmicos();
        this.progresion = progresion != null ? progresion : new SistemaProgresion();
        this.gestorColonizacion = gestorColonizacion != null ? gestorColonizacion : new GestorColonizacion();
        this.gestorEfectos = gestorEfectos != null ? gestorEfectos : new GestorEfectos();

        this.transformadorCuerpos.setListenerMensaje(msg -> notificarEvento(msg, MensajeEvento.TipoMensaje.EXITO));
        this.motorFisica.setListenerMensaje(msg -> notificarEvento(msg, MensajeEvento.TipoMensaje.EXITO));
        if (this.motorFisica instanceof MotorFisicaPermisiva mfp) {
            mfp.setListenerNotificacion(this::notificarEvento);
            mfp.setListenerExplosion(this.gestorEfectos::agregarExplosion);
        }

        this.motorFisica.agregarListenerColision(() -> {
            if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();
        });
    }

    public SimulacionSolar(double anchoMundo, double altoMundo,
                           MotorFisica motorFisica,
                           SistemaSolar sistemaSolar,
                           InventarioJugador inventario,
                           VistaSistemaSolar vista,
                           TrayectoriaPredictor predictor,
                           TransformadorCuerpos transformadorCuerpos,
                           GestorEconomia gestorEconomia,
                           GeneradorEventosCosmicos generadorEventosCosmicos) {
        this(anchoMundo, altoMundo, motorFisica, sistemaSolar, inventario, vista, predictor, transformadorCuerpos, gestorEconomia, generadorEventosCosmicos, null, null, null);
    }

    public SimulacionSolar(double anchoMundo, double altoMundo) {
        this(anchoMundo, altoMundo, null, null, null, null, null, null, null, null, null, null, null);
    }

    public void agregarListenerEvento(java.util.function.Consumer<MensajeEvento> listener) {
        if (listener != null) {
            this.listenersEvento.add(listener);
        }
    }

    public void notificarEvento(MensajeEvento evento) {
        for (var listener : listenersEvento) {
            try {
                listener.accept(evento);
            } catch (Exception ignored) {}
        }
        System.out.println("[Tick " + tickActual + "] [" + evento.tipo().name() + "] " + evento.mensaje());
    }

    public void notificarEvento(String mensaje, MensajeEvento.TipoMensaje tipo) {
        notificarEvento(new MensajeEvento(mensaje, tipo, tickActual));
    }

    // ===== API Principal de Ticks =====

    public void avanzarTick() {
        if (enPausa && !pasoUnico) return;

        tickActual++;

        // 1. Física permisiva con asistencia orbital
        motorFisica.avanzarPaso(sistemaSolar.getCuerpos());

        // 2. Actualizar civilizaciones
        actualizarCivilizaciones();

        // 2.5 Transformaciones de cuerpos por proximidad (Easter egg)
        transformadorCuerpos.procesarTransformaciones(sistemaSolar.getCuerpos());

        // 3. Economía pasiva y satélites
        actualizarEconomia();

        // 4. Misiones de colonización en curso
        gestorColonizacion.actualizar(this::notificarEvento, tickActual);

        // 5. Partículas y efectos visuales temporales
        gestorEfectos.actualizar();

        // 6. Eventos cósmicos (meteoritos periódicos dirigidos)
        generarEventosCosmicos();

        // 7. Actualizar vista legacy
        vista.actualizar();

        // 8. Condición de derrota / Game Over
        if (estaPerdida() && !gameOverDisparado) {
            gameOverDisparado = true;
            enPausa = true;
            if (onGameOver != null) {
                onGameOver.run();
            }
        }

        // 9. Callbacks de eventos o estado
        if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();

        pasoUnico = false;
    }

    public void avanzarTicks(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            avanzarTick();
        }
    }

    private void actualizarCivilizaciones() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>(sistemaSolar.getCuerpos());
        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.tieneCivilizacion()) {
                if (c.getCivilizacion() != null && c.getCivilizacion().getEmisorEventos() == null) {
                    c.getCivilizacion().setEmisorEventos(this::notificarEvento);
                }
                c.actualizarCivilizacion(cuerpos);
            }
        }
    }

    private void actualizarEconomia() {
        gestorEconomia.actualizarEconomia(sistemaSolar.getCuerpos(), inventario);
    }

    private void generarEventosCosmicos() {
        generadorEventosCosmicos.actualizarEventosCosmicos(
                sistemaSolar.getCuerpos(),
                anchoMundo,
                altoMundo,
                this::agregarCuerpo,
                this::notificarEvento,
                tickActual
        );
    }

    // ===== Gestión de Cuerpos (Spawn y Presets) =====

    public int getCantidadPlanetasActuales() {
        int count = 0;
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c != null && c.esSimulado() && c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta()) {
                count++;
            }
        }
        return count;
    }

    public boolean spawnCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return spawnCuerpo(tipo, null, x, y, factorMasa, velocidadInicial);
    }

    public boolean spawnCuerpo(TipoCuerpo tipo, String nombrePersonalizado, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        if (tipo == TipoCuerpo.SATELITE) {
            factorMasa = 1.0; // Satélite es una estructura fija no modificable en masa
        }
        if (!progresion.estaDesbloqueado(tipo)) {
            notificarEvento("Cuerpo bloqueado: " + (tipo != null ? tipo.nombre : "Desconocido") + ". Desbloquéalo con ciencia.", MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        if (tipo != null && tipo.esPlaneta()) {
            int actual = getCantidadPlanetasActuales();
            int maximo = progresion != null ? progresion.getLimitePlanetas() : ConfiguracionSimulacion.PLANETAS_INICIALES_MAX;
            if (actual >= maximo) {
                notificarEvento("Límite de planetas alcanzado (" + actual + "/" + maximo + "). Amplía la capacidad en el Árbol de Investigación [P].", MensajeEvento.TipoMensaje.ADVERTENCIA);
                return false;
            }
        }

        if (tipo != TipoCuerpo.SATELITE && factorMasa > progresion.getLimiteMasaActual() + 1e-6) {
            notificarEvento("Límite de masa excedido (" + String.format("%.1fx", factorMasa) + " > " + String.format("%.1fx", progresion.getLimiteMasaActual()) + ").", MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        if (!inventario.puedeCrear(tipo, factorMasa)) {
            notificarEvento("Recursos insuficientes para " + tipo.nombre, MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        CuerpoCeleste cuerpo = CuerpoCelesteFactory.crearCuerpo(tipo, x, y, factorMasa, velocidadInicial);
        if (cuerpo == null) {
            return false;
        }

        if (nombrePersonalizado != null && !nombrePersonalizado.isBlank()) {
            cuerpo.setNombre(nombrePersonalizado.trim());
        }
        if (inventario.gastarParaCrear(tipo, factorMasa)) {
            agregarCuerpo(cuerpo);
            salirModoColocacion();
            notificarEvento("Cuerpo colocado: " + cuerpo.getNombre() + " (" + tipo.nombre + ")", MensajeEvento.TipoMensaje.INFO);
            return true;
        }
        return false;
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        sistemaSolar.agregarCuerpo(cuerpo);
        vista.agregarCuerpo(cuerpo);
        if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();
    }

    public void eliminarCuerpo(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return;
        cuerpo.setSimulado(false);
        cuerpo.setMasa(0);
        if (cuerpo.getCivilizacion() != null) {
            cuerpo.getCivilizacion().destruir();
        }
        sistemaSolar.getCuerpos().remove(cuerpo);
        vista.removerCuerpo(cuerpo);

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

        if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();
    }

    public void limpiarCuerpos() {
        sistemaSolar.getCuerpos().clear();
        vista.getPane().getChildren().clear();
        if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();
    }

    // ===== Presets del Sistema =====

    public void cargarPresetSistemaBasico() {
        limpiarCuerpos();

        // 1. Estrella Sol central
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        agregarCuerpo(sol);

        // 2. Planeta Rocoso (Tierra) con civilización próspera
        Vector2D posTierra = new Vector2D(160, 0);
        Vector2D velTierra = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(sol, posTierra, false);
        Planeta tierra = CuerpoCelesteFactory.crearPlanetaRocoso(posTierra.x, posTierra.y, 1.0, velTierra);
        tierra.setCuerpoOrbitado(sol);
        tierra.desarrollarCivilizacion();
        agregarCuerpo(tierra);

        // 3. Luna orbitando a la Tierra (órbita lejana para dejar espacio interior a satélites)
        Vector2D posLuna = new Vector2D(160 + 65, 0);
        Vector2D velLuna = velTierra.sumar(new Vector2D(0, -1.07));
        Luna luna = CuerpoCelesteFactory.crearLuna(posLuna.x, posLuna.y, 0.8, velLuna);
        luna.setCuerpoOrbitado(tierra);
        agregarCuerpo(luna);
    }

    public void cargarPresetEstrellaBinaria() {
        limpiarCuerpos();

        // Dos estrellas orbitándose
        Estrella sol1 = CuerpoCelesteFactory.crearEstrella(-100, 0, 0.8);
        sol1.setVelocidad(new Vector2D(0, 55));
        agregarCuerpo(sol1);

        Estrella sol2 = CuerpoCelesteFactory.crearEstrella(100, 0, 0.8);
        sol2.setVelocidad(new Vector2D(0, -55));
        agregarCuerpo(sol2);

        // Planeta circumbinario exterior
        Planeta planetaExt = CuerpoCelesteFactory.crearPlanetaRocoso(0, 360, 1.0, new Vector2D(-88, 0));
        planetaExt.desarrollarCivilizacion();
        agregarCuerpo(planetaExt);
    }

    // ===== Derrota y Reinicio =====

    public boolean estaPerdida() {
        boolean hayAgujeroNegro = false;
        boolean hayPlanetaOLuna = false;
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c != null && c.esSimulado()) {
                TipoCuerpo t = c.getTipoCuerpo();
                if (t != null) {
                    if (t.esAgujeroNegro()) {
                        hayAgujeroNegro = true;
                    } else if (t.esPlaneta() || t.esLuna()) {
                        hayPlanetaOLuna = true;
                    }
                }
            }
        }
        return hayAgujeroNegro && !hayPlanetaOLuna;
    }

    public void reiniciar() {
        limpiarCuerpos();
        gestorColonizacion.limpiar();
        gestorEfectos.limpiar();
        inventario.reiniciar();
        progresion.reiniciar();
        tickActual = 0;
        gameOverDisparado = false;
        CuerpoCelesteFactory.resetearContador();
        salirModoColocacion();
        enPausa = false;
        notificarListenersUI();
        notificarEvento("Simulación reiniciada por completo.", MensajeEvento.TipoMensaje.INFO);
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }

    // ===== Modo Colocación y Órbita Asistida =====

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa, String nombre) {
        this.modoColocacion = ModoColocacion.COLOCANDO;
        this.tipoColocacion = tipo;
        setFactorMasaColocacion(factorMasa);
        this.nombreColocacion = nombre;
    }

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa) {
        entrarModoColocacion(tipo, factorMasa, null);
    }

    public void actualizarPosicionPreview(double xJavaFX, double yJavaFX) {
        double xFisica = ConstantesFisicas.javaFXAFisicaX(xJavaFX, anchoMundo);
        double yFisica = ConstantesFisicas.javaFXAFisica(yJavaFX, altoMundo);
        this.posicionPreview = new Vector2D(xFisica, yFisica);
    }

    public Vector2D calcularVelocidadOrbitalAsistida(Vector2D posFisica) {
        CuerpoCeleste cuerpoCercano = encontrarCuerpoGravitatorioCercano(posFisica);
        if (cuerpoCercano != null) {
            boolean esMenorOrbital = (tipoColocacion == TipoCuerpo.LUNA || tipoColocacion == TipoCuerpo.SATELITE);
            if (esMenorOrbital && cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().esPlaneta()) {
                Vector2D r = posFisica.restar(cuerpoCercano.getPosicion());
                double dist = Math.max(1.0, r.magnitud());
                double factorMasa = (cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().masaBase > 0)
                        ? Math.max(0.2, cuerpoCercano.getMasa() / cuerpoCercano.getTipoCuerpo().masaBase)
                        : 1.0;
                double vOrb = Math.sqrt(75.0 * factorMasa / dist);
                Vector2D uRadial = r.dividir(dist);
                Vector2D uTangencial = new Vector2D(-uRadial.y, uRadial.x);
                return cuerpoCercano.getVelocidad().sumar(uTangencial.multiplicar(vOrb));
            }
            return CuerpoCelesteFactory.calcularVelocidadOrbitalVector(cuerpoCercano, posFisica, false);
        }
        return Vector2D.cero();
    }

    public void confirmarColocacionAutoOrbita() {
        if (modoColocacion == ModoColocacion.COLOCANDO && posicionPreview != null && tipoColocacion != null) {
            Vector2D posPreviewLocal = posicionPreview;
            Vector2D velOrbital = calcularVelocidadOrbitalAsistida(posPreviewLocal);
            CuerpoCeleste cuerpoCercano = encontrarCuerpoGravitatorioCercano(posPreviewLocal);
            boolean spawned = spawnCuerpo(tipoColocacion, nombreColocacion, posPreviewLocal.x, posPreviewLocal.y, factorMasaColocacion, velOrbital);
            if (spawned && cuerpoCercano != null && !sistemaSolar.getCuerpos().isEmpty()) {
                CuerpoCeleste recienCreado = sistemaSolar.getCuerpos().get(sistemaSolar.getCuerpos().size() - 1);
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
        if (tipoColocacion != null) {
            spawnCuerpo(tipoColocacion, nombreColocacion, posFisica.x, posFisica.y, factorMasaColocacion, velFisica);
        }
    }

    public void salirModoColocacion() {
        this.modoColocacion = ModoColocacion.NINGUNO;
        this.tipoColocacion = null;
        this.nombreColocacion = null;
        this.factorMasaColocacion = 1.0;
        this.posicionPreview = null;
    }

    public String getNombreColocacion() {
        return nombreColocacion;
    }

    public void setNombreColocacion(String nombreColocacion) {
        this.nombreColocacion = nombreColocacion;
    }

    public CuerpoCeleste encontrarCuerpoGravitatorioCercano(Vector2D pos) {
        CuerpoCeleste mejor = null;
        double maxFuerza = 0;

        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c.getTipoCuerpo() == TipoCuerpo.LUNA || c.getTipoCuerpo() == TipoCuerpo.SATELITE ||
                c.getTipoCuerpo() == TipoCuerpo.METEORITO) {
                continue;
            }
            if (!c.getTipoCuerpo().esMasivo && c.getMasa() < 1e22) continue;

            double dist = pos.distanciaA(c.getPosicion());
            if (dist < 10.0) continue;

            double atraccion = c.getMasa() / (dist * dist + 100.0);
            if (atraccion > maxFuerza) {
                maxFuerza = atraccion;
                mejor = c;
            }
        }
        return mejor;
    }

    // ===== Control de Simulación =====

    public void play() { enPausa = false; }
    public void pause() { enPausa = true; }
    public void togglePause() { enPausa = !enPausa; }
    public void step() { pasoUnico = true; enPausa = false; }

    public void setVelocidadSimulacion(double v) { this.velocidadSimulacion = Math.max(0.1, Math.min(10, v)); }

    // ===== Callbacks y Observadores UI =====

    public void agregarListenerUI(Runnable listener) {
        if (listener != null && !listenersUI.contains(listener)) {
            listenersUI.add(listener);
        }
    }

    public void removerListenerUI(Runnable listener) {
        if (listener != null) {
            listenersUI.remove(listener);
        }
    }

    public void notificarListenersUI() {
        for (Runnable r : listenersUI) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    public SistemaSolar getSistemaSolar() { return sistemaSolar; }
    public MotorFisica getMotorFisica() { return motorFisica; }
    public InventarioJugador getInventario() { return inventario; }
    public SistemaProgresion getProgresion() { return progresion; }
    public GestorColonizacion getGestorColonizacion() { return gestorColonizacion; }
    public GestorEfectos getGestorEfectos() { return gestorEfectos; }
    public VistaSistemaSolar getVista() { return vista; }
    public TrayectoriaPredictor getPredictor() { return predictor; }
    public long getTickActual() { return tickActual; }
    public boolean isEnPausa() { return enPausa; }
    public double getVelocidadSimulacion() { return velocidadSimulacion; }
    public ModoColocacion getModoColocacion() { return modoColocacion; }
    public TipoCuerpo getTipoColocacion() { return tipoColocacion; }
    public Vector2D getPosicionPreview() { return posicionPreview; }
    public double getFactorMasaColocacion() { return factorMasaColocacion; }

    public void setFactorMasaColocacion(double f) {
        double maxPermitido = progresion != null ? progresion.getLimiteMasaActual() : ConfiguracionSimulacion.MASA_FACTOR_MAX;
        this.factorMasaColocacion = Math.max(
            ConfiguracionSimulacion.MASA_FACTOR_MIN,
            Math.min(maxPermitido, f)
        );
    }

    public void setDimensionesMundo(double ancho, double alto) {
        if (ancho > 0 && alto > 0) {
            this.anchoMundo = ancho;
            this.altoMundo = alto;
        }
    }

    public double getAnchoMundo() { return anchoMundo; }
    public double getAltoMundo() { return altoMundo; }
    public TransformadorCuerpos getTransformadorCuerpos() { return transformadorCuerpos; }

    public enum ModoColocacion {
        NINGUNO, COLOCANDO
    }
}