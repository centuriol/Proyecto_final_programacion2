package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
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

    private final BuscadorOrbitaLibre buscadorOrbitaLibre;

    public ActualizadorProcesosTransformacion() {
        this(new BuscadorOrbitaLibre());
    }

    public ActualizadorProcesosTransformacion(BuscadorOrbitaLibre buscadorOrbitaLibre) {
        this.buscadorOrbitaLibre = buscadorOrbitaLibre != null ? buscadorOrbitaLibre : new BuscadorOrbitaLibre();
    }

    public void actualizarProceso(TransformadorCuerpos.ProcesoTransformacion proceso,
                                  List<CuerpoCeleste> cuerpos,
                                  Map<CuerpoCeleste, TransformadorCuerpos.ProcesoTransformacion> procesosActivos,
                                  Map<CuerpoCeleste, Transformable> transformables,
                                  Consumer<String> listenerMensaje) {
        CuerpoCeleste cuerpo = proceso.getCuerpo();
        CuerpoCeleste estrella = proceso.getEstrella();

        double dist = cuerpo.getPosicion().distanciaA(estrella.getPosicion());
        double radioMinimo = Math.max(estrella.getRadio() + cuerpo.getRadio() + 5.0, proceso.getRadioMinimoActivacion());

        if (proceso.getEstado() == TransformadorCuerpos.EstadoTransformacion.ESPERANDO) {
            if (dist > proceso.getRadioActivacion() || dist <= radioMinimo) {
                procesosActivos.remove(cuerpo);
                return;
            }

            proceso.incrementarTicksEspera();
            if (proceso.getTicksEspera() >= ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG) {
                if (!proceso.isMigrarSiguienteOrbita()) {
                    aplicarTransformacionDirecta(proceso, cuerpo, estrella, procesosActivos, transformables, listenerMensaje);
                    return;
                }
                Vector2D posLibre = buscadorOrbitaLibre.buscarPosicionLibre(cuerpo, estrella, proceso.getRadioActivacion(), cuerpos);

                if (posLibre == null) {
                    procesosActivos.remove(cuerpo);
                    if (listenerMensaje != null) {
                        listenerMensaje.accept("Migracion abortada para " + cuerpo.getNombre() + ": no hay orbita libre disponible.");
                    }
                    return;
                }

                ejecutarCambioTipo(proceso, cuerpo, transformables);
                proceso.iniciarMigracion(cuerpo.getPosicion(), posLibre, posLibre.distanciaA(estrella.getPosicion()));

                if (listenerMensaje != null) {
                    listenerMensaje.accept(proceso.getMensajeEvento());
                }
            }
        } else if (proceso.getEstado() == TransformadorCuerpos.EstadoTransformacion.MIGRANDO) {
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

                Vector2D velOrb = CalculadorOrbital.calcularVelocidadOrbitalVector(estrella, cuerpo.getPosicion(), false);
                cuerpo.setVelocidad(velOrb);

                procesosActivos.remove(cuerpo);
            }
        }
    }

    private void aplicarTransformacionDirecta(TransformadorCuerpos.ProcesoTransformacion proceso, CuerpoCeleste cuerpo,
                                              CuerpoCeleste estrella,
                                              Map<CuerpoCeleste, TransformadorCuerpos.ProcesoTransformacion> procesosActivos,
                                              Map<CuerpoCeleste, Transformable> transformables,
                                              Consumer<String> listenerMensaje) {
        ejecutarCambioTipo(proceso, cuerpo, transformables);
        cuerpo.setCuerpoOrbitado(estrella);
        cuerpo.setRadioOrbita(CalculadorOrbital.ajustarARanuraSolar(cuerpo.getPosicion().distanciaA(estrella.getPosicion())));
        cuerpo.setVelocidad(CalculadorOrbital.calcularVelocidadOrbitalVector(estrella, cuerpo.getPosicion(), false));
        if (listenerMensaje != null) {
            listenerMensaje.accept(proceso.getMensajeEvento());
        }
        procesosActivos.remove(cuerpo);
    }

    private void ejecutarCambioTipo(TransformadorCuerpos.ProcesoTransformacion proceso, CuerpoCeleste cuerpo,
                                    Map<CuerpoCeleste, Transformable> transformables) {
        Transformable t = transformables.get(cuerpo);
        if (t == null) {
            t = org.example.game.cuerpo.RegistroCapacidades.obtenerTransformable(cuerpo).orElse(null);
        }
        if (t != null) {
            t.transformarA(proceso.getTipoDestino());
        }
        cuerpo.setNombre(proceso.getNombreResultante());
    }
}
