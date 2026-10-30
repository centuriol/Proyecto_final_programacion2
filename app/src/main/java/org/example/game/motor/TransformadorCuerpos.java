package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.*;
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
    }

    /**
     * Regla individual de transformacion: si un planeta del tipo origen
     * permanece dentro del radio de activacion el tiempo suficiente,
     * se transformara al tipo destino y migrara a orbita libre.
     */
    public record ReglaTransformacion(
            TipoCuerpo tipoOrigen,
            TipoCuerpo tipoDestino,
            double radioActivacion,
            String nombreResultante,
            String mensajeEvento
    ) {}

    private final List<ReglaTransformacion> reglas = new ArrayList<>();
    private final Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos = new HashMap<>();
    private Consumer<String> listenerMensaje;

    public TransformadorCuerpos() {
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

    public void agregarRegla(ReglaTransformacion regla) {
        if (regla != null) {
            reglas.add(regla);
        }
    }

    public List<ReglaTransformacion> getReglas() {
        return Collections.unmodifiableList(reglas);
    }

    /**
     * Procesa un paso de simulacion para todos los cuerpos:
     * 1. Limpia y cancela procesos donde los cuerpos fueron eliminados, destruidos o absorbidos.
     * 2. Actualiza tiempos de espera y migraciones graduales.
     * 3. Registra nuevos planetas que entran al radio de activacion.
     *
     * @param cuerpos Lista activa de cuerpos celestes en la simulacion.
     */
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
            actualizarProceso(proceso, cuerpos);
        }

        // 3. Buscar nuevos planetas candidatos que entren al radio de activacion
        List<CuerpoCeleste> estrellas = new ArrayList<>();
        for (CuerpoCeleste c : cuerpos) {
            if (c.getTipoCuerpo() == TipoCuerpo.ESTRELLA && c.esSimulado()) {
                estrellas.add(c);
            }
        }
        if (estrellas.isEmpty()) return;

        for (CuerpoCeleste c : cuerpos) {
            if (!c.esSimulado() || !(c instanceof org.example.game.cuerpo.Transformable t && t.esTransformable())) continue;

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

    private void actualizarProceso(ProcesoTransformacion proceso, List<CuerpoCeleste> cuerpos) {
        CuerpoCeleste cuerpo = proceso.cuerpo;
        CuerpoCeleste estrella = proceso.estrella;

        double dist = cuerpo.getPosicion().distanciaA(estrella.getPosicion());
        double radioMinimo = estrella.getRadio() + cuerpo.getRadio() + 5.0;

        if (proceso.estado == EstadoTransformacion.ESPERANDO) {
            // Si sale del radio o esta en contacto destructivo, cancelar de inmediato sin estado colgado
            if (dist > proceso.radioActivacion || dist <= radioMinimo) {
                procesosActivos.remove(cuerpo);
                return;
            }

            proceso.ticksEspera++;
            if (proceso.ticksEspera >= ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG) {
                // Se cumplio el tiempo de espera.
                // Buscar destino libre en la orbita estable (o la mas cercana disponible)
                Vector2D posLibre = encontrarPosicionOrbitalLibre(cuerpo, estrella, proceso.radioActivacion, cuerpos);

                if (posLibre == null) {
                    // Destino ocupado y no hay orbita libre disponible: abortar migracion sin solapar
                    procesosActivos.remove(cuerpo);
                    if (listenerMensaje != null) {
                        listenerMensaje.accept("Migracion abortada para " + cuerpo.getNombre() + ": no hay orbita libre disponible.");
                    }
                    return;
                }

                // Aplicar transformacion estetica y fisica al cumplirse el tiempo (Tarea 2)
                if (cuerpo instanceof org.example.game.cuerpo.Transformable trans) {
                    trans.transformarA(proceso.tipoDestino);
                }
                cuerpo.setNombre(proceso.nombreResultante);

                // Iniciar migracion gradual
                proceso.estado = EstadoTransformacion.MIGRANDO;
                proceso.posInicialMigracion = cuerpo.getPosicion();
                proceso.posDestinoMigracion = posLibre;
                proceso.radioOrbitaDestino = posLibre.distanciaA(estrella.getPosicion());
                proceso.ticksMigracion = 0;

                if (listenerMensaje != null) {
                    listenerMensaje.accept(proceso.mensajeEvento);
                }
            }
        } else if (proceso.estado == EstadoTransformacion.MIGRANDO) {
            proceso.ticksMigracion++;
            double factorProgreso = Math.min(1.0, (double) proceso.ticksMigracion / ConfiguracionSimulacion.TICKS_MIGRACION_ORBITA);

            // Interpolacion suave (smoothstep para entrada y salida sin tirones)
            double tSuave = factorProgreso * factorProgreso * (3.0 - 2.0 * factorProgreso);
            Vector2D posActual = proceso.posInicialMigracion.interpolar(proceso.posDestinoMigracion, tSuave);
            cuerpo.setPosicion(posActual);

            if (factorProgreso >= 1.0) {
                // Migracion finalizada: posicionar en destino y fijar orbita estable
                cuerpo.setPosicion(proceso.posDestinoMigracion);
                cuerpo.setCuerpoOrbitado(estrella);
                cuerpo.setRadioOrbita(proceso.radioOrbitaDestino);

                // Calibrar velocidad tangencial circular exacta
                Vector2D velOrb = CalculadorOrbital.calcularVelocidadOrbitalVector(estrella, cuerpo.getPosicion(), false);
                cuerpo.setVelocidad(velOrb);

                procesosActivos.remove(cuerpo);
            }
        }
    }

    /**
     * Encuentra una posicion libre en la orbita para reubicar el planeta sin solapamientos.
     * Si el angulo natural esta ocupado, prueba otros angulos en la misma orbita.
     * Si toda la orbita esta ocupada, busca en radios adyacentes incrementales.
     * Si no existe ninguna posicion libre, retorna null.
     */
    public Vector2D encontrarPosicionOrbitalLibre(CuerpoCeleste cuerpo, CuerpoCeleste estrella, double radioActivacion, List<CuerpoCeleste> cuerpos) {
        return BuscadorOrbitaLibre.encontrarPosicionOrbitalLibre(cuerpo, estrella, radioActivacion, cuerpos);
    }

    public boolean posicionEstaLibre(Vector2D pos, CuerpoCeleste cuerpo, List<CuerpoCeleste> cuerpos) {
        return BuscadorOrbitaLibre.posicionEstaLibre(pos, cuerpo, cuerpos);
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
