package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.RegistroCapacidades;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.Transformable;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.ConfiguracionTransformacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SRP: Administra el catálogo de reglas de transformación y detecta candidatos aptos para iniciar transformación.
 */
public class EvaluadorReglasTransformacion {

    private final List<ReglaTransformacion> reglas = new ArrayList<>();

    public EvaluadorReglasTransformacion() {
        reglas.add(new ReglaTransformacion(
                TipoCuerpo.PLANETA_AGUA,
                TipoCuerpo.PLANETA_ROCOSO,
                ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO,
                "Planeta Porro",
                "El calor estelar evaporo los oceanos. Transformacion: Agua -> Porro"
        ));
        reglas.add(new ReglaTransformacion(
                TipoCuerpo.PLANETA_ROCOSO,
                TipoCuerpo.PLANETA_LAVA,
                ConfiguracionSimulacion.RADIO_TRANSFORMACION_PORRO_LAVA,
                "Planeta Lava",
                "Proximidad extrema a la estrella. Transformacion: Porro -> Lava"
        ));
        reglas.add(new ReglaTransformacion(
                null,
                TipoCuerpo.PLANETA_AGUA,
                ConfiguracionTransformacion.RADIO_MIN_ULTIMA_ORBITA_AGUA,
                ConfiguracionTransformacion.RADIO_MAX_ULTIMA_ORBITA_AGUA,
                false,
                "Planeta de Agua",
                "La lejania estelar condenso oceanos. Transformacion a Planeta de Agua"
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

    public List<Double> getRadiosActivacion() {
        List<Double> radios = new ArrayList<>();
        for (ReglaTransformacion regla : reglas) {
            if (regla.migrarSiguienteOrbita()) {
                radios.add(regla.radioActivacion());
            }
        }
        return radios;
    }

    public void detectarNuevosCandidatos(List<CuerpoCeleste> cuerpos, List<CuerpoCeleste> estrellas,
                                         Map<CuerpoCeleste, Transformable> transformables,
                                         Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos) {
        if (estrellas.isEmpty()) return;
        for (CuerpoCeleste c : cuerpos) {
            if (esCandidatoTransformable(c, transformables, procesosActivos)) {
                buscarYRegistrarProceso(c, estrellas, procesosActivos);
            }
        }
    }

    private boolean esCandidatoTransformable(CuerpoCeleste c, Map<CuerpoCeleste, Transformable> transformables,
                                             Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos) {
        if (c == null || !c.esSimulado() || procesosActivos.containsKey(c)) return false;
        Transformable t = transformables.get(c);
        if (t == null) {
            t = RegistroCapacidades.obtenerTransformable(c).orElse(null);
        }
        return t != null && t.esTransformable();
    }

    private void buscarYRegistrarProceso(CuerpoCeleste cuerpo, List<CuerpoCeleste> estrellas,
                                         Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos) {
        TipoCuerpo tipoActual = cuerpo.getTipoCuerpo();
        for (ReglaTransformacion regla : reglas) {
            if (!coincideTipoOrigen(regla, tipoActual)) continue;
            ProcesoTransformacion proceso = intentarCrearProcesoConEstrellas(cuerpo, regla, estrellas);
            if (proceso != null) {
                procesosActivos.put(cuerpo, proceso);
                return;
            }
        }
    }

    private boolean coincideTipoOrigen(ReglaTransformacion regla, TipoCuerpo tipoActual) {
        if (regla.tipoOrigen() != null) {
            return tipoActual == regla.tipoOrigen();
        }
        return tipoActual != null && tipoActual.esPlaneta() && tipoActual != regla.tipoDestino();
    }

    private ProcesoTransformacion intentarCrearProcesoConEstrellas(CuerpoCeleste c, ReglaTransformacion regla,
                                                                   List<CuerpoCeleste> estrellas) {
        for (CuerpoCeleste estrella : estrellas) {
            double dist = c.getPosicion().distanciaA(estrella.getPosicion());
            double radioMinimo = Math.max(estrella.getRadio() + c.getRadio() + 5.0, regla.radioMinimoActivacion());
            double radioEfectivo = calcularRadioEfectivoRegla(regla, c, estrella);
            if (dist > radioMinimo && dist <= radioEfectivo) {
                return new ProcesoTransformacion(
                        c, estrella, regla.tipoDestino(),
                        regla.nombreResultante(), regla.mensajeEvento(),
                        regla.radioMinimoActivacion(), radioEfectivo, regla.migrarSiguienteOrbita()
                );
            }
        }
        return null;
    }

    private double calcularRadioEfectivoRegla(ReglaTransformacion regla, CuerpoCeleste c, CuerpoCeleste estrella) {
        if (regla.tipoOrigen() == TipoCuerpo.PLANETA_ROCOSO && regla.tipoDestino() == TipoCuerpo.PLANETA_LAVA) {
            return TransformadorCuerpos.calcularRadioActivacionPorroLava(c, estrella);
        }
        return regla.radioActivacion();
    }
}
