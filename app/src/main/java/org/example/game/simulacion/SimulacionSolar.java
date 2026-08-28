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
import org.example.game.motor.Vector2D;
import org.example.game.motor.ConstantesFisicas;
import org.example.MotorFisica;
import org.example.SistemaSolar;
import org.example.VistaSistemaSolar;
import org.example.RenderizadorCircular;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Orquestador principal de la simulación "Órbita".
 * SRP: Coordina el bucle de simulación, gestión de cuerpos, economía y presets.
 * DIP: Depende de interfaces MotorFisica e Inventario.
 */
public class SimulacionSolar {
    private final SistemaSolar sistemaSolar;
    private final MotorFisicaPermisiva motorFisica;
    private final InventarioJugador inventario;
    private final VistaSistemaSolar vista;
    private final TrayectoriaPredictor predictor;
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
    private Vector2D posicionPreview = null;

    // Eventos cósmicos
    private int ticksUltimoMeteorito = 0;

    // Callbacks para UI
    private Runnable onTickCallback;
    private Runnable onCambioEstadoCallback;

    public SimulacionSolar(double anchoMundo, double altoMundo) {
        this.motorFisica = new MotorFisicaPermisiva(Math.max(anchoMundo, altoMundo));
        this.sistemaSolar = new SistemaSolar(motorFisica);
        this.inventario = new InventarioJugador();
        this.vista = new VistaSistemaSolar(new RenderizadorCircular(anchoMundo, altoMundo));
        this.predictor = new TrayectoriaPredictor();

        // Cargar preset básico inicial por defecto para que el juego arranque vivo
        cargarPresetSistemaBasico();

        // Listener de colisiones
        motorFisica.agregarListenerColision(() -> {
            if (onCambioEstadoCallback != null) onCambioEstadoCallback.run();
        });
    }

    // ===== API Principal de Ticks =====

    public void avanzarTick() {
        if (enPausa && !pasoUnico) return;

        tickActual++;

        // 1. Física permisiva con asistencia orbital
        motorFisica.avanzarPaso(sistemaSolar.getCuerpos());

        // 2. Actualizar civilizaciones
        actualizarCivilizaciones();

        // 3. Economía pasiva y satélites
        actualizarEconomia();

        // 4. Eventos cósmicos aleatorios (meteoritos)
        generarEventosCosmicos();

        // 5. Actualizar vista legacy
        vista.actualizar();

        // 6. Callbacks de UI
        if (onTickCallback != null) onTickCallback.run();
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
            if (c instanceof Planeta) {
                ((Planeta) c).actualizarCivilizacion(cuerpos);
            }
        }
    }

    private void actualizarEconomia() {
        // Ganancia pasiva
        inventario.agregarRecurso(TipoRecurso.MATERIA_PLANETARIA, ConfiguracionSimulacion.GANANCIA_PASIVA_POR_TICK);
        inventario.agregarRecurso(TipoRecurso.ENERGIA, 0.2);

        // Ganancias por satélites
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c instanceof Satelite) {
                inventario.agregarRecurso(TipoRecurso.CIENCIA, 0.08);
                inventario.agregarRecurso(TipoRecurso.ENERGIA, 0.15);
            }
        }

        // Bonus por civilizaciones prósperas
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c instanceof Planeta) {
                Planeta p = (Planeta) c;
                if (p.tieneCivilizacion() && p.getCivilizacion() != null) {
                    var estado = p.getCivilizacion().getEstado();
                    double bonus = switch (estado) {
                        case PRÓSPERA -> ConfiguracionSimulacion.BONUS_CIV_PROSPERA;
                        case ESTABLE -> ConfiguracionSimulacion.BONUS_CIV_PROSPERA * 0.5;
                        case LUCHANDO -> ConfiguracionSimulacion.BONUS_CIV_LUCHANDO;
                        case EN_PELIGRO -> 0.1;
                        case EXTINGUIDA -> 0;
                    };
                    inventario.agregarRecurso(TipoRecurso.MATERIA_PLANETARIA, bonus);
                    inventario.agregarRecurso(TipoRecurso.CIENCIA, bonus * 0.15);
                    inventario.agregarRecurso(TipoRecurso.ENERGIA, bonus * 0.25);
                }
            }
        }
    }

    private void generarEventosCosmicos() {
        ticksUltimoMeteorito++;
        if (ticksUltimoMeteorito >= ConfiguracionSimulacion.INTERVALO_METEORITOS_ALEATORIOS) {
            ticksUltimoMeteorito = 0;

            long meteoritosActivos = sistemaSolar.getCuerpos().stream()
                    .filter(c -> c instanceof Meteorito).count();

            if (meteoritosActivos < ConfiguracionSimulacion.MAX_METEORITOS_SIMULTANEOS) {
                double ancho = 1920;
                double alto = 1080;
                Meteorito m = CuerpoCelesteFactory.crearMeteoritoAleatorio(ancho, alto);
                agregarCuerpo(m);
            }
        }
    }

    // ===== Gestión de Cuerpos (Spawn y Presets) =====

    public boolean spawnCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        if (!inventario.puedeCrear(tipo, factorMasa)) {
            System.out.println("❌ Recursos insuficientes para " + tipo.nombre);
            return false;
        }

        CuerpoCeleste cuerpo = null;

        switch (tipo) {
            case ESTRELLA:
                cuerpo = CuerpoCelesteFactory.crearEstrella(x, y, factorMasa);
                cuerpo.setVelocidad(velocidadInicial);
                break;
            case PLANETA_ROCOSO:
                cuerpo = CuerpoCelesteFactory.crearPlanetaRocoso(x, y, factorMasa, velocidadInicial);
                break;
            case PLANETA_GASEOSO:
                cuerpo = CuerpoCelesteFactory.crearPlanetaGaseoso(x, y, factorMasa, velocidadInicial);
                break;
            case LUNA:
                cuerpo = CuerpoCelesteFactory.crearLuna(x, y, factorMasa, velocidadInicial);
                break;
            case SATELITE:
                cuerpo = CuerpoCelesteFactory.crearSatelite(x, y, factorMasa, velocidadInicial);
                break;
            case ESCUDO_DOME:
                cuerpo = CuerpoCelesteFactory.crearEscudo(x, y, factorMasa, velocidadInicial);
                break;
            case METEORITO:
                cuerpo = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(x, y, velocidadInicial);
                break;
            case AGUJERO_NEGRO:
                cuerpo = CuerpoCelesteFactory.crearAgujeroNegroEstelar(x, y, factorMasa);
                cuerpo.setVelocidad(velocidadInicial);
                break;
            case PLANETA_HELADO:
                cuerpo = CuerpoCelesteFactory.crearPlanetaHelado(x, y, factorMasa, velocidadInicial);
                break;
            case AGUJERO_NEGRO_SUPERMASIVO:
                cuerpo = CuerpoCelesteFactory.crearAgujeroNegroSupermasivo(x, y, factorMasa);
                break;
            default:
                return false;
        }

        if (cuerpo != null) {
            if (inventario.gastarParaCrear(tipo, factorMasa)) {
                agregarCuerpo(cuerpo);
                salirModoColocacion();
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
        sistemaSolar.getCuerpos().remove(cuerpo);
        vista.removerCuerpo(cuerpo);
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
        tierra.desarrollarCivilizacion();
        agregarCuerpo(tierra);

        // 3. Luna orbitando a la Tierra
        Vector2D posLuna = new Vector2D(160 + 26, 0);
        Vector2D velLuna = velTierra.sumar(new Vector2D(0, -65.0));
        Luna luna = CuerpoCelesteFactory.crearLuna(posLuna.x, posLuna.y, 0.8, velLuna);
        agregarCuerpo(luna);

        // 4. Gigante Gaseoso exterior (tipo Júpiter)
        Vector2D posJupiter = new Vector2D(-340, 0);
        Vector2D velJupiter = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(sol, posJupiter, false);
        Planeta jupiter = CuerpoCelesteFactory.crearPlanetaGaseoso(posJupiter.x, posJupiter.y, 1.3, velJupiter);
        agregarCuerpo(jupiter);

        // 5. Satélite orbitando la Tierra
        Vector2D posSat = new Vector2D(160 - 20, 0);
        Vector2D velSat = velTierra.sumar(new Vector2D(0, 75.0));
        Satelite sat = CuerpoCelesteFactory.crearSatelite(posSat.x, posSat.y, 1.0, velSat);
        agregarCuerpo(sat);
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

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa) {
        this.modoColocacion = ModoColocacion.COLOCANDO;
        this.tipoColocacion = tipo;
        this.factorMasaColocacion = factorMasa;
    }

    public void actualizarPosicionPreview(double xJavaFX, double yJavaFX) {
        double xFisica = ConstantesFisicas.javaFXAFisicaX(xJavaFX, 1920);
        double yFisica = ConstantesFisicas.javaFXAFisica(yJavaFX, 1080);
        this.posicionPreview = new Vector2D(xFisica, yFisica);
    }

    public Vector2D calcularVelocidadOrbitalAsistida(Vector2D posFisica) {
        CuerpoCeleste cuerpoCercano = encontrarCuerpoGravitatorioCercano(posFisica);
        if (cuerpoCercano != null) {
            return CuerpoCelesteFactory.calcularVelocidadOrbitalVector(cuerpoCercano, posFisica, false);
        }
        return Vector2D.cero();
    }

    public void confirmarColocacionAutoOrbita() {
        if (modoColocacion == ModoColocacion.COLOCANDO && posicionPreview != null && tipoColocacion != null) {
            Vector2D velOrbital = calcularVelocidadOrbitalAsistida(posicionPreview);
            spawnCuerpo(tipoColocacion, posicionPreview.x, posicionPreview.y, factorMasaColocacion, velOrbital);
        }
    }

    public void confirmarColocacionConImpulso(Vector2D posFisica, Vector2D velFisica) {
        if (tipoColocacion != null) {
            spawnCuerpo(tipoColocacion, posFisica.x, posFisica.y, factorMasaColocacion, velFisica);
        }
    }

    public void salirModoColocacion() {
        this.modoColocacion = ModoColocacion.NINGUNO;
        this.tipoColocacion = null;
        this.posicionPreview = null;
    }

    public CuerpoCeleste encontrarCuerpoGravitatorioCercano(Vector2D pos) {
        CuerpoCeleste mejor = null;
        double maxFuerza = 0;

        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (!c.getTipoCuerpo().esMasivo && c.getMasa() < 1e25) continue;

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

    // ===== Callbacks =====

    public void setOnTickCallback(Runnable cb) { this.onTickCallback = cb; }
    public void setOnCambioEstadoCallback(Runnable cb) { this.onCambioEstadoCallback = cb; }

    // ===== Getters =====

    public SistemaSolar getSistemaSolar() { return sistemaSolar; }
    public MotorFisicaPermisiva getMotorFisica() { return motorFisica; }
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
    public void setFactorMasaColocacion(double f) { this.factorMasaColocacion = Math.max(0.1, Math.min(10, f)); }

    public enum ModoColocacion {
        NINGUNO, COLOCANDO
    }
}