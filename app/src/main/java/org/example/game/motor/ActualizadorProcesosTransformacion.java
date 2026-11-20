package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.RegistroCapacidades;
import org.example.game.cuerpo.Transformable;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Responsable de la transición temporal, verificación de órbita libre y migración
 * física suave de un proceso de transformación (SRP).
 */
public class ActualizadorProcesosTransformacion {

    private final BuscadorOrbita buscadorOrbitaLibre;
    private final EvaluadorReglasTransformacion evaluadorReglas = new EvaluadorReglasTransformacion();

    public ActualizadorProcesosTransformacion() {
        this(new FabricaMotorFisica().crearBuscadorOrbitaPorDefecto());
    }

    public ActualizadorProcesosTransformacion(BuscadorOrbita buscadorOrbitaLibre) {
        this.buscadorOrbitaLibre = buscadorOrbitaLibre != null ? buscadorOrbitaLibre : new FabricaMotorFisica().crearBuscadorOrbitaPorDefecto();
    }

    public BuscadorOrbita getBuscadorOrbitaLibre() {
        return buscadorOrbitaLibre;
    }

    public EvaluadorReglasTransformacion getEvaluadorReglas() {
        return evaluadorReglas;
    }

    public void actualizarProceso(ProcesoTransformacion proceso,
                                  List<CuerpoCeleste> cuerpos,
                                  Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos,
                                  Map<CuerpoCeleste, Transformable> transformables,
                                  Consumer<String> listenerMensaje) {
        if (proceso.getEstado() == EstadoTransformacion.ESPERANDO) {
            actualizarEspera(proceso, cuerpos, procesosActivos, transformables, listenerMensaje);
        } else if (proceso.getEstado() == EstadoTransformacion.MIGRANDO) {
            actualizarMigracion(proceso, procesosActivos);
        }
    }

    private void actualizarEspera(ProcesoTransformacion proceso, List<CuerpoCeleste> cuerpos,
                                  Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos,
                                  Map<CuerpoCeleste, Transformable> transformables,
                                  Consumer<String> listenerMensaje) {
        CuerpoCeleste cuerpo = proceso.getCuerpo();
        CuerpoCeleste estrella = proceso.getEstrella();
        double dist = cuerpo.getPosicion().distanciaA(estrella.getPosicion());
        double radioMinimo = Math.max(estrella.getRadio() + cuerpo.getRadio() + 5.0, proceso.getRadioMinimoActivacion());

        if (dist > proceso.getRadioActivacion() || dist <= radioMinimo) {
            procesosActivos.remove(cuerpo);
            return;
        }
        proceso.incrementarTicksEspera();
        if (proceso.getTicksEspera() >= ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG) {
            completarEspera(proceso, cuerpos, procesosActivos, transformables, listenerMensaje);
        }
    }

    private void completarEspera(ProcesoTransformacion proceso, List<CuerpoCeleste> cuerpos,
                                 Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos,
                                 Map<CuerpoCeleste, Transformable> transformables,
                                 Consumer<String> listenerMensaje) {
        CuerpoCeleste cuerpo = proceso.getCuerpo();
        CuerpoCeleste estrella = proceso.getEstrella();
        if (!proceso.isMigrarSiguienteOrbita()) {
            aplicarTransformacionDirecta(proceso, cuerpo, estrella, procesosActivos, transformables, listenerMensaje);
            return;
        }
        Vector2D posLibre = buscadorOrbitaLibre.buscarPosicionLibre(cuerpo, estrella, proceso.getRadioActivacion(), cuerpos);
        if (posLibre == null) {
            procesosActivos.remove(cuerpo);
            notificar(listenerMensaje, "Migracion abortada para " + cuerpo.getNombre() + ": no hay orbita libre disponible.");
            return;
        }
        ejecutarCambioTipo(proceso, cuerpo, transformables);
        proceso.iniciarMigracion(cuerpo.getPosicion(), posLibre, posLibre.distanciaA(estrella.getPosicion()));
        notificar(listenerMensaje, proceso.getMensajeEvento());
    }

    private void actualizarMigracion(ProcesoTransformacion proceso, Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos) {
        CuerpoCeleste cuerpo = proceso.getCuerpo();
        CuerpoCeleste estrella = proceso.getEstrella();
        proceso.incrementarTicksMigracion();
        double factorProgreso = Math.min(1.0, (double) proceso.getTicksMigracion() / ConfiguracionSimulacion.TICKS_MIGRACION_ORBITA);

        double tSuave = factorProgreso * factorProgreso * (3.0 - 2.0 * factorProgreso);
        Vector2D posActual = proceso.getPosInicialMigracion().interpolar(proceso.getPosDestinoMigracion(), tSuave);
        cuerpo.setPosicion(posActual);
        cuerpo.setRadioOrbita(posActual.distanciaA(estrella.getPosicion()));

        if (factorProgreso >= 1.0) {
            cuerpo.setPosicion(proceso.getPosDestinoMigracion());
            cuerpo.setCuerpoOrbitado(estrella);
            cuerpo.setRadioOrbita(proceso.getRadioOrbitaDestino());
            cuerpo.setVelocidad(CalculadorOrbital.calcularVelocidadOrbitalVector(estrella, cuerpo.getPosicion(), false));
            procesosActivos.remove(cuerpo);
        }
    }

    private void aplicarTransformacionDirecta(ProcesoTransformacion proceso, CuerpoCeleste cuerpo,
                                              CuerpoCeleste estrella,
                                              Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos,
                                              Map<CuerpoCeleste, Transformable> transformables,
                                              Consumer<String> listenerMensaje) {
        ejecutarCambioTipo(proceso, cuerpo, transformables);
        cuerpo.setCuerpoOrbitado(estrella);
        cuerpo.setRadioOrbita(CalculadorOrbital.ajustarARanuraSolar(cuerpo.getPosicion().distanciaA(estrella.getPosicion())));
        cuerpo.setVelocidad(CalculadorOrbital.calcularVelocidadOrbitalVector(estrella, cuerpo.getPosicion(), false));
        notificar(listenerMensaje, proceso.getMensajeEvento());
        procesosActivos.remove(cuerpo);
    }

    private void ejecutarCambioTipo(ProcesoTransformacion proceso, CuerpoCeleste cuerpo,
                                    Map<CuerpoCeleste, Transformable> transformables) {
        Transformable t = transformables.get(cuerpo);
        if (t == null) {
            t = RegistroCapacidades.obtenerTransformable(cuerpo).orElse(null);
        }
        if (t != null) {
            t.transformarA(proceso.getTipoDestino());
        }
        cuerpo.setNombre(proceso.getNombreResultante());
    }

    private void notificar(Consumer<String> listener, String msg) {
        if (listener != null) {
            listener.accept(msg);
        }
    }
}
