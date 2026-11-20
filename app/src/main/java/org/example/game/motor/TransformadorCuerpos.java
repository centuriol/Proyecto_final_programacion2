package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Transformable;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.*;
import java.util.function.Consumer;

/**
 * Orquestador del sistema de transformación gradual de cuerpos celestes basado en proximidad a estrellas (SRP / OCP / DIP).
 */
public class TransformadorCuerpos implements ProcesadorTransformaciones {

    private static final TransformadorCuerpos INSTANCIA_GLOBAL = new TransformadorCuerpos();

    public static TransformadorCuerpos getInstanciaGlobal() {
        return INSTANCIA_GLOBAL;
    }

    private final RegistroEntidadesTransformables registro = new RegistroEntidadesTransformables();
    private final ActualizadorProcesosTransformacion actualizadorProcesos;

    public TransformadorCuerpos(BuscadorOrbita buscadorOrbitaLibre) {
        this(buscadorOrbitaLibre, new ActualizadorProcesosTransformacion(buscadorOrbitaLibre));
    }

    public TransformadorCuerpos(BuscadorOrbita buscadorOrbitaLibre, ActualizadorProcesosTransformacion actualizadorProcesos) {
        BuscadorOrbita buscador = buscadorOrbitaLibre != null ? buscadorOrbitaLibre : new FabricaMotorFisica().crearBuscadorOrbitaPorDefecto();
        this.actualizadorProcesos = actualizadorProcesos != null
                ? actualizadorProcesos
                : new ActualizadorProcesosTransformacion(buscador);
    }

    public TransformadorCuerpos() {
        this(new FabricaMotorFisica().crearBuscadorOrbitaPorDefecto());
    }

    public void registrarTransformable(CuerpoCeleste cuerpo, Transformable transformable) { registro.registrarTransformable(cuerpo, transformable); }
    public void registrarEstrella(CuerpoCeleste estrella) { registro.registrarEstrella(estrella); }
    public void desregistrar(CuerpoCeleste cuerpo) { registro.desregistrar(cuerpo); }
    public Optional<Transformable> obtenerTransformable(CuerpoCeleste cuerpo) { return registro.obtenerTransformable(cuerpo); }
    public void limpiar() { registro.limpiar(); }
    public void agregarRegla(ReglaTransformacion regla) { actualizadorProcesos.getEvaluadorReglas().agregarRegla(regla); }
    public List<ReglaTransformacion> getReglas() { return actualizadorProcesos.getEvaluadorReglas().getReglas(); }
    public List<Double> getRadiosActivacion() { return actualizadorProcesos.getEvaluadorReglas().getRadiosActivacion(); }

    public void procesarTransformaciones(List<CuerpoCeleste> cuerpos) {
        if (cuerpos == null || cuerpos.isEmpty()) {
            registro.limpiarProcesos();
            return;
        }
        registro.depurarProcesosInvalidos(cuerpos);
        for (ProcesoTransformacion proceso : new ArrayList<>(registro.getProcesosActivos().values())) {
            actualizadorProcesos.actualizarProceso(
                    proceso, cuerpos, registro.getProcesosActivos(), registro.getTransformables(), registro.getListenerMensaje()
            );
        }
        List<CuerpoCeleste> estrellas = registro.recolectarEstrellasActivas(cuerpos);
        actualizadorProcesos.getEvaluadorReglas().detectarNuevosCandidatos(cuerpos, estrellas, registro.getTransformables(), registro.getProcesosActivos());
    }

    public Vector2D encontrarPosicionOrbitalLibre(CuerpoCeleste cuerpo, CuerpoCeleste estrella, double radioActivacion, List<CuerpoCeleste> cuerpos) {
        return actualizadorProcesos.getBuscadorOrbitaLibre().buscarPosicionLibre(cuerpo, estrella, radioActivacion, cuerpos);
    }

    public boolean posicionEstaLibre(Vector2D pos, CuerpoCeleste cuerpo, List<CuerpoCeleste> cuerpos) {
        return actualizadorProcesos.getBuscadorOrbitaLibre().esPosicionLibre(pos, cuerpo, cuerpos);
    }

    public void cancelarProceso(CuerpoCeleste cuerpo) { registro.cancelarProceso(cuerpo); }
    public boolean tieneProcesoActivo(CuerpoCeleste cuerpo) { return registro.tieneProcesoActivo(cuerpo); }
    public ProcesoTransformacion getProceso(CuerpoCeleste cuerpo) { return registro.getProceso(cuerpo); }
    public int getCantidadProcesosActivos() { return registro.getCantidadProcesosActivos(); }
    public void setListenerMensaje(Consumer<String> listener) { registro.setListenerMensaje(listener); }

    public static double calcularRadioActivacionPorroLava(CuerpoCeleste planeta, CuerpoCeleste estrella) {
        double base = ConfiguracionSimulacion.RADIO_TRANSFORMACION_PORRO_LAVA;
        if (planeta == null || estrella == null) {
            return base;
        }
        double dinamico = estrella.getRadio() + planeta.getRadio() + 45.0;
        return Math.min(CalculadorOrbital.RADIOS_RANURAS_SOL[0] - 12.0, Math.max(base, dinamico));
    }
}
