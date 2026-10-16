package org.example.game.simulacion;

import org.example.CuerpoCeleste;
import org.example.Estrella;
import org.example.Planeta;
import org.example.AgujeroNegro;
import org.example.Civilizable;
import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.motor.Vector2D;
import org.example.game.motor.ConstantesFisicas;
import org.example.MotorFisica;
import org.example.SistemaSolar;
import org.example.VistaSistemaSolar;
import org.example.RenderizadorCircular;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Orquestador principal de la simulación "Órbita".
 * SRP: Coordina el bucle de simulación, gestión de cuerpos, economía y presets.
 * DIP: Depende de interfaces MotorFisica e Inventario.
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
    private boolean enPausa = false; // Arrancar listo para jugar
    private boolean pasoUnico = false;
    private double velocidadSimulacion = 1.0;

    // Modo de colocación
    private ModoColocacion modoColocacion = ModoColocacion.NINGUNO;
    private TipoCuerpo tipoColocacion = null;
    private double factorMasaColocacion = 1.0;
    private String nombreColocacion = null;
    private Vector2D posicionPreview = null;

    // Componentes delegados (SRP / DIP)
    private final GestorEconomia gestorEconomia;
    private final GeneradorEventosCosmicos generadorEventosCosmicos;

    // Callbacks y listeners para UI y eventos (Patron Observer / SRP / ISP)
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
                           GeneradorEventosCosmicos generadorEventosCosmicos) {
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

        this.transformadorCuerpos.setListenerMensaje(msg -> notificarEvento(msg, MensajeEvento.TipoMensaje.EXITO));
        this.motorFisica.setListenerMensaje(msg -> notificarEvento(msg, MensajeEvento.TipoMensaje.EXITO));
        this.motorFisica.setListenerDefensaEscudo(this::procesarDefensaEscudo);

        this.motorFisica.agregarListenerColision(() -> {
            if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();
        });
    }

    public SimulacionSolar(double anchoMundo, double altoMundo) {
        this(anchoMundo, altoMundo, null, null, null, null, null, null, null, null);
    }

    private void procesarDefensaEscudo() {
        inventario.gastarRecurso(TipoRecurso.ENERGIA, ConfiguracionSimulacion.CONSUMO_ENERGIA_ESCUDO_IMPACTO);
        inventario.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.RECOMPENSA_CIENCIA_ESCUDO_IMPACTO);
        notificarEvento("Escudo intercepto impacto. Consumo: " + (int)ConfiguracionSimulacion.CONSUMO_ENERGIA_ESCUDO_IMPACTO + " Energia | +" + (int)ConfiguracionSimulacion.RECOMPENSA_CIENCIA_ESCUDO_IMPACTO + " Ciencia", MensajeEvento.TipoMensaje.EXITO);
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
        actualizarGeneracionPorMasa();

        // 4. Eventos cósmicos aleatorios (desactivados para mantener solo estrella, planeta rocoso y luna)
        // generarEventosCosmicos();

        // 5. Actualizar vista legacy
        vista.actualizar();

        // 6. Callbacks de eventos o estado
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

    private void actualizarGeneracionPorMasa() {
        // En este modelo economico, los recursos provienen exclusivamente de los tipos de cuerpos
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

    public boolean spawnCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return spawnCuerpo(tipo, null, x, y, factorMasa, velocidadInicial);
    }

    public boolean spawnCuerpo(TipoCuerpo tipo, String nombrePersonalizado, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        if (!inventario.puedeCrear(tipo, factorMasa)) {
            notificarEvento("Recursos insuficientes para " + tipo.nombre, MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        CuerpoCeleste cuerpo = CuerpoCelesteFactory.crearCuerpo(tipo, x, y, factorMasa, velocidadInicial);
        if (cuerpo == null) {
            return false;
        }

        if (cuerpo != null) {
            if (nombrePersonalizado != null && !nombrePersonalizado.isBlank()) {
                cuerpo.setNombre(nombrePersonalizado.trim());
            }
            if (inventario.gastarParaCrear(tipo, factorMasa)) {
                agregarCuerpo(cuerpo);
                salirModoColocacion();
                notificarEvento("Cuerpo colocado: " + cuerpo.getNombre() + " (" + tipo.nombre + ")", MensajeEvento.TipoMensaje.INFO);
                return true;
            }
        }
        return false;
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        sistemaSolar.agregarCuerpo(cuerpo);
        vista.agregarCuerpo(cuerpo);
        if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();
    }

    public void eliminarCuerpo(CuerpoCeleste cuerpo) {
        // Tarea 3: Destruir civilizacion explicitamente antes de eliminar
        if (cuerpo != null && cuerpo.getCivilizacion() != null) {
            cuerpo.getCivilizacion().destruir();
        }
        sistemaSolar.getCuerpos().remove(cuerpo);
        vista.removerCuerpo(cuerpo);
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c.getCuerpoOrbitado() == cuerpo) {
                c.setCuerpoOrbitado(null);
            }
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

        // 3. Luna orbitando a la Tierra (velocidad más lenta)
        Vector2D posLuna = new Vector2D(160 + 26, 0);
        Vector2D velLuna = velTierra.sumar(new Vector2D(0, -1.7));
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

    public void cargarPresetAgujeroNegro() {
        limpiarCuerpos();

        // Agujero negro central
        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.5);
        agregarCuerpo(bh);

        // Planetas y satélites en órbita de peligro
        Vector2D p1 = new Vector2D(220, 0);
        Planeta p = CuerpoCelesteFactory.crearPlanetaGaseoso(p1.x, p1.y, 1.0, CuerpoCelesteFactory.calcularVelocidadOrbitalVector(bh, p1, false));
        agregarCuerpo(p);

        Vector2D p2 = new Vector2D(-140, 0);
        Satelite s = CuerpoCelesteFactory.crearSatelite(p2.x, p2.y, 1.0, CuerpoCelesteFactory.calcularVelocidadOrbitalVector(bh, p2, false));
        agregarCuerpo(s);
    }

    // ===== Modo Colocación y Órbita Asistida =====

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa, String nombre) {
        this.modoColocacion = ModoColocacion.COLOCANDO;
        this.tipoColocacion = tipo;
        this.factorMasaColocacion = factorMasa;
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
            if (tipoColocacion == TipoCuerpo.LUNA && cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().esPlaneta()) {
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
                boolean esLuna = recienCreado.getTipoCuerpo() == TipoCuerpo.LUNA;
                boolean cercanoEsPlaneta = cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().name().startsWith("PLANETA");
                boolean recienEsPlaneta = recienCreado.getTipoCuerpo() != null && recienCreado.getTipoCuerpo().name().startsWith("PLANETA");
                boolean cercanoEsMasivo = cuerpoCercano.getTipoCuerpo() != null &&
                        (cuerpoCercano.getTipoCuerpo() == TipoCuerpo.ESTRELLA ||
                         cuerpoCercano.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO ||
                         cuerpoCercano.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);

                double dist = posPreviewLocal.distanciaA(cuerpoCercano.getPosicion());
                if ((esLuna && cercanoEsPlaneta) || (recienEsPlaneta && cercanoEsMasivo)) {
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
                c.getTipoCuerpo() == TipoCuerpo.METEORITO || c.getTipoCuerpo() == TipoCuerpo.ESCUDO_DOME) {
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
        for (Runnable listener : listenersUI) {
            listener.run();
        }
        if (onTickCallback != null) {
            onTickCallback.run();
        }
    }

    public void setOnTickCallback(Runnable cb) { this.onTickCallback = cb; }
    public void setOnCambioEstadoCallback(Runnable cb) { this.onCambioEstadoCallback = cb; }

    // ===== Getters =====

    public SistemaSolar getSistemaSolar() { return sistemaSolar; }
    public MotorFisica getMotorFisica() { return motorFisica; }
    public InventarioJugador getInventario() { return inventario; }
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
        this.factorMasaColocacion = Math.max(
            ConfiguracionSimulacion.MASA_FACTOR_MIN,
            Math.min(ConfiguracionSimulacion.MASA_FACTOR_MAX, f)
        );
    }

    public void setDimensionesMundo(double ancho, double alto) {
        if (ancho > 0 && alto > 0) {
            this.anchoMundo = ancho;
            this.altoMundo = alto;
        }
    }

    public double getAnchoMundo() {
        return anchoMundo;
    }

    public double getAltoMundo() {
        return altoMundo;
    }

    public TransformadorCuerpos getTransformadorCuerpos() {
        return transformadorCuerpos;
    }

    public enum ModoColocacion {
        NINGUNO, COLOCANDO
    }
}