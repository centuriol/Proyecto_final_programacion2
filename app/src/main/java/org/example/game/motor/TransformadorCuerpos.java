package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.Transformable;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Sistema de transformacion gradual de cuerpos celestes basado en proximidad a estrellas.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivamente de gestionar el ciclo de espera, verificacion
 *   de destino libre y migracion suave hacia la orbita estable.
 * - OCP: Nuevas reglas de transformacion se agregan a la lista de reglas
 *   sin modificar la logica de transicion ni de deteccion de colisiones.
 * - DIP: Emite notificaciones via Consumer sin acoplarse a la interfaz grafica.
 */
public class TransformadorCuerpos {

    public enum EstadoTransformacion {
        ESPERANDO,
        MIGRANDO
    }

    private static final TransformadorCuerpos INSTANCIA_GLOBAL = new TransformadorCuerpos();

    public static TransformadorCuerpos getInstanciaGlobal() {
        return INSTANCIA_GLOBAL;
    }

    /**
     * Estado interno de un proceso de transformacion en curso para un planeta.
     */
    public static class ProcesoTransformacion {
        private final CuerpoCeleste cuerpo;
        private final CuerpoCeleste estrella;
        private final TipoCuerpo tipoDestino;
        private final String nombreResultante;
        private final String mensajeEvento;
        private final double radioActivacion;

        private EstadoTransformacion estado = EstadoTransformacion.ESPERANDO;
        private int ticksEspera = 1;
        private int ticksMigracion = 0;
        private Vector2D posInicialMigracion;
        private Vector2D posDestinoMigracion;
        private double radioOrbitaDestino;

        public ProcesoTransformacion(CuerpoCeleste cuerpo, CuerpoCeleste estrella, TipoCuerpo tipoDestino,
                                     String nombreResultante, String mensajeEvento, double radioActivacion) {
            this.cuerpo = cuerpo;
            this.estrella = estrella;
            this.tipoDestino = tipoDestino;
            this.nombreResultante = nombreResultante;
            this.mensajeEvento = mensajeEvento;
            this.radioActivacion = radioActivacion;
        }

        public CuerpoCeleste getCuerpo() { return cuerpo; }
        public CuerpoCeleste getEstrella() { return estrella; }
        public TipoCuerpo getTipoDestino() { return tipoDestino; }
        public EstadoTransformacion getEstado() { return estado; }
        public int getTicksEspera() { return ticksEspera; }
        public int getTicksMigracion() { return ticksMigracion; }
        public Vector2D getPosDestinoMigracion() { return posDestinoMigracion; }
        public String getNombreResultante() { return nombreResultante; }
        public String getMensajeEvento() { return mensajeEvento; }
        public double getRadioActivacion() { return radioActivacion; }
        public Vector2D getPosInicialMigracion() { return posInicialMigracion; }
        public double getRadioOrbitaDestino() { return radioOrbitaDestino; }

        public void incrementarTicksEspera() { this.ticksEspera++; }
        public void incrementarTicksMigracion() { this.ticksMigracion++; }
        public void iniciarMigracion(Vector2D posInicial, Vector2D posDestino, double radioOrbitaDestino) {
            this.estado = EstadoTransformacion.MIGRANDO;
            this.posInicialMigracion = posInicial;
            this.posDestinoMigracion = posDestino;
            this.radioOrbitaDestino = radioOrbitaDestino;
            this.ticksMigracion = 0;
        }
    }

    public record ReglaTransformacion(
            TipoCuerpo tipoOrigen,
            TipoCuerpo tipoDestino,
            double radioActivacion,
            String nombreResultante,
            String mensajeEvento
    ) {}

    private final List<ReglaTransformacion> reglas = new ArrayList<>();
    private final Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos = new HashMap<>();
    private final Map<CuerpoCeleste, Transformable> transformables = new ConcurrentHashMap<>();
    private final List<CuerpoCeleste> estrellasRegistradas = new CopyOnWriteArrayList<>();
    private final BuscadorOrbitaLibre buscadorOrbitaLibre;
    private final ActualizadorProcesosTransformacion actualizadorProcesos;
    private Consumer<String> listenerMensaje;

    public TransformadorCuerpos(BuscadorOrbitaLibre buscadorOrbitaLibre) {
        this(buscadorOrbitaLibre, new ActualizadorProcesosTransformacion(buscadorOrbitaLibre));
    }

    public TransformadorCuerpos(BuscadorOrbitaLibre buscadorOrbitaLibre, ActualizadorProcesosTransformacion actualizadorProcesos) {
        this.buscadorOrbitaLibre = buscadorOrbitaLibre != null ? buscadorOrbitaLibre : new BuscadorOrbitaLibre();
        this.actualizadorProcesos = actualizadorProcesos != null ? actualizadorProcesos : new ActualizadorProcesosTransformacion(this.buscadorOrbitaLibre);
        // Regla 1: Planeta de Agua cerca del sol -> Planeta Porro
        reglas.add(new ReglaTransformacion(
                TipoCuerpo.PLANETA_AGUA,
                TipoCuerpo.PLANETA_ROCOSO,
                ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO,
                "Planeta Porro",
                "El calor estelar evaporo los oceanos. Transformacion: Agua -> Porro"
        ));

        // Regla 2: Planeta Porro cerca del sol -> Planeta Lava
        reglas.add(new ReglaTransformacion(
                TipoCuerpo.PLANETA_ROCOSO,
                TipoCuerpo.PLANETA_LAVA,
                ConfiguracionSimulacion.RADIO_TRANSFORMACION_PORRO_LAVA,
                "Planeta Lava",
                "Proximidad extrema a la estrella. Transformacion: Porro -> Lava"
        ));
    }

    public TransformadorCuerpos() {
        this(new BuscadorOrbitaLibre());
    }

    public void registrarTransformable(CuerpoCeleste cuerpo, Transformable transformable) {
        if (cuerpo != null && transformable != null) {
            transformables.put(cuerpo, transformable);
        }
    }

    public void registrarEstrella(CuerpoCeleste estrella) {
        if (estrella != null && !estrellasRegistradas.contains(estrella)) {
            estrellasRegistradas.add(estrella);
        }
    }

    public void desregistrar(CuerpoCeleste cuerpo) {
        if (cuerpo != null) {
            transformables.remove(cuerpo);
            estrellasRegistradas.remove(cuerpo);
            procesosActivos.remove(cuerpo);
        }
    }

    public Optional<Transformable> obtenerTransformable(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return Optional.empty();
        return Optional.ofNullable(transformables.get(cuerpo));
    }

    public void limpiar() {
        procesosActivos.clear();
        transformables.clear();
        estrellasRegistradas.clear();
    }

    public void agregarRegla(ReglaTransformacion regla) {
        if (regla != null) {
            reglas.add(regla);
        }
    }

    public List<ReglaTransformacion> getReglas() {
        return Collections.unmodifiableList(reglas);
    }

    public void procesarTransformaciones(List<CuerpoCeleste> cuerpos) {
        if (cuerpos == null || cuerpos.isEmpty()) {
            procesosActivos.clear();
            return;
        }

        // 1. Limpieza de procesos cuyo cuerpo o estrella ya no estan en la simulacion
        procesosActivos.entrySet().removeIf(entry -> {
            CuerpoCeleste c = entry.getKey();
            ProcesoTransformacion proceso = entry.getValue();
            return !cuerpos.contains(c) || !c.esSimulado() ||
                    !cuerpos.contains(proceso.estrella) || !proceso.estrella.esSimulado();
        });

        // 2. Actualizar procesos activos en curso
        List<ProcesoTransformacion> snapshotProcesos = new ArrayList<>(procesosActivos.values());
        for (ProcesoTransformacion proceso : snapshotProcesos) {
            actualizadorProcesos.actualizarProceso(proceso, cuerpos, procesosActivos, transformables, listenerMensaje);
        }

        // 3. Buscar nuevos planetas candidatos que entren al radio de activacion
        List<CuerpoCeleste> estrellas = new ArrayList<>();
        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.esSimulado() && (estrellasRegistradas.contains(c) || c.getMasa() >= 1e29)) {
                estrellas.add(c);
            }
        }
        if (estrellas.isEmpty()) return;

        for (CuerpoCeleste c : cuerpos) {
            Transformable t = transformables.get(c);
            if (t == null) {
                t = org.example.game.cuerpo.RegistroCapacidades.obtenerTransformable(c).orElse(null);
            }
            if (t == null || !c.esSimulado() || !t.esTransformable()) continue;

            // Un cuerpo no puede tener dos procesos a la vez
            if (procesosActivos.containsKey(c)) continue;

            TipoCuerpo tipoActual = c.getTipoCuerpo();
            for (ReglaTransformacion regla : reglas) {
                if (tipoActual != regla.tipoOrigen()) continue;

                for (CuerpoCeleste estrella : estrellas) {
                    double dist = c.getPosicion().distanciaA(estrella.getPosicion());
                    double radioMinimo = estrella.getRadio() + c.getRadio() + 5.0;

                    if (dist > radioMinimo && dist <= regla.radioActivacion()) {
                        ProcesoTransformacion nuevo = new ProcesoTransformacion(
                                c, estrella, regla.tipoDestino(),
                                regla.nombreResultante(), regla.mensajeEvento(), regla.radioActivacion()
                        );
                        procesosActivos.put(c, nuevo);
                        break;
                    }
                }

                if (procesosActivos.containsKey(c)) break;
            }
        }
    }

    public Vector2D encontrarPosicionOrbitalLibre(CuerpoCeleste cuerpo, CuerpoCeleste estrella, double radioActivacion, List<CuerpoCeleste> cuerpos) {
        return buscadorOrbitaLibre.buscarPosicionLibre(cuerpo, estrella, radioActivacion, cuerpos);
    }

    public boolean posicionEstaLibre(Vector2D pos, CuerpoCeleste cuerpo, List<CuerpoCeleste> cuerpos) {
        return buscadorOrbitaLibre.esPosicionLibre(pos, cuerpo, cuerpos);
    }

    public void cancelarProceso(CuerpoCeleste cuerpo) {
        procesosActivos.remove(cuerpo);
    }

    public boolean tieneProcesoActivo(CuerpoCeleste cuerpo) {
        return procesosActivos.containsKey(cuerpo);
    }

    public ProcesoTransformacion getProceso(CuerpoCeleste cuerpo) {
        return procesosActivos.get(cuerpo);
    }

    public int getCantidadProcesosActivos() {
        return procesosActivos.size();
    }

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
    }

    public List<Double> getRadiosActivacion() {
        List<Double> radios = new ArrayList<>();
        for (ReglaTransformacion regla : reglas) {
            radios.add(regla.radioActivacion());
        }
        return radios;
    }
}
