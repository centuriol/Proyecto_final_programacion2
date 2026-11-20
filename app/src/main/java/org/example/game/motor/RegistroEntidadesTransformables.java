package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Transformable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * SRP: Gestiona el registro de entidades transformables, estrellas y procesos de transformación activos.
 */
public class RegistroEntidadesTransformables {

    private final Map<CuerpoCeleste, ProcesoTransformacion> procesosActivos = new HashMap<>();
    private final Map<CuerpoCeleste, Transformable> transformables = new ConcurrentHashMap<>();
    private final List<CuerpoCeleste> estrellasRegistradas = new CopyOnWriteArrayList<>();
    private Consumer<String> listenerMensaje;

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
        return cuerpo == null ? Optional.empty() : Optional.ofNullable(transformables.get(cuerpo));
    }

    public void limpiar() {
        procesosActivos.clear();
        transformables.clear();
        estrellasRegistradas.clear();
    }

    public void limpiarProcesos() {
        procesosActivos.clear();
    }

    public void depurarProcesosInvalidos(List<CuerpoCeleste> cuerpos) {
        procesosActivos.entrySet().removeIf(entry -> {
            CuerpoCeleste c = entry.getKey();
            CuerpoCeleste estrella = entry.getValue().getEstrella();
            return !cuerpos.contains(c) || !c.esSimulado()
                    || !cuerpos.contains(estrella) || !estrella.esSimulado();
        });
    }

    public List<CuerpoCeleste> recolectarEstrellasActivas(List<CuerpoCeleste> cuerpos) {
        List<CuerpoCeleste> estrellas = new ArrayList<>();
        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.esSimulado() && (estrellasRegistradas.contains(c) || c.getMasa() >= 1e29)) {
                estrellas.add(c);
            }
        }
        return estrellas;
    }

    public Map<CuerpoCeleste, ProcesoTransformacion> getProcesosActivos() { return procesosActivos; }
    public Map<CuerpoCeleste, Transformable> getTransformables() { return transformables; }
    public Consumer<String> getListenerMensaje() { return listenerMensaje; }
    public void setListenerMensaje(Consumer<String> listener) { this.listenerMensaje = listener; }
    public void cancelarProceso(CuerpoCeleste cuerpo) { procesosActivos.remove(cuerpo); }
    public boolean tieneProcesoActivo(CuerpoCeleste cuerpo) { return procesosActivos.containsKey(cuerpo); }
    public ProcesoTransformacion getProceso(CuerpoCeleste cuerpo) { return procesosActivos.get(cuerpo); }
    public int getCantidadProcesosActivos() { return procesosActivos.size(); }
}
