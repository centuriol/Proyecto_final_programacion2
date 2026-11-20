package org.example.game.motor;

import org.example.game.cuerpo.Actualizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.MensajeEvento;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * SRP: Encapsula el pipeline de etapas de un tick del motor de física permisiva y sus buffers reutilizables.
 */
public class PipelineFisicaPermisiva {

    private static class BuffersTick {
        final List<CuerpoCeleste> simulados = new ArrayList<>();
        final Set<CuerpoCeleste> aEliminar = new LinkedHashSet<>();
        final List<CuerpoCeleste> aAgregar = new ArrayList<>();

        void recolectarSimulados(List<CuerpoCeleste> cuerpos) {
            simulados.clear();
            for (CuerpoCeleste c : cuerpos) {
                if (c != null && c.esSimulado()) {
                    simulados.add(c);
                }
            }
        }

        void reiniciarMutaciones() {
            aEliminar.clear();
            aAgregar.clear();
        }
    }

    private static class ComponentesFisica {
        final CalculadorFuerzaGravitatoria calculadorGravedad;
        final AsistenciaOrbital asistenteOrbital;
        final ResolucionColision resolvedorColisiones;
        final ConfiguracionEventosColision configuracionColision;
        final ProcesadorAgujeroNegro gestorAgujeroNegro;
        final IntegradorFisico integradorMovimiento;
        final DetectorContactoFisico detectorColisiones;
        Consumer<String> listenerMensaje;
        BiConsumer<Vector2D, Double> listenerExplosion;
        double asistenciaOrbital = 0.015;

        ComponentesFisica(CalculadorFuerzaGravitatoria cg, AsistenciaOrbital ao, ResolucionColision rc,
                          ConfiguracionEventosColision configColision, ProcesadorAgujeroNegro gan,
                          IntegradorFisico im, DetectorContactoFisico dc) {
            this.calculadorGravedad = cg;
            this.asistenteOrbital = ao;
            this.resolvedorColisiones = rc;
            this.configuracionColision = configColision != null
                    ? configColision
                    : (rc instanceof ConfiguracionEventosColision cfg ? cfg : null);
            this.gestorAgujeroNegro = gan;
            this.integradorMovimiento = im;
            this.detectorColisiones = dc;
        }

        void ejecutarDinamica(List<CuerpoCeleste> simulados) {
            calculadorGravedad.calcularGravedad(simulados, asistenteOrbital, listenerMensaje);
            asistenteOrbital.aplicarAsistenciaOrbital(simulados, asistenciaOrbital);
            integradorMovimiento.integrar(simulados);
        }

        void emitirExplosion(CuerpoCeleste c) {
            if (listenerExplosion != null) {
                listenerExplosion.accept(c.getPosicion(), Math.max(20.0, c.getRadio() * 2.0));
            }
        }
    }

    private final BuffersTick buffers = new BuffersTick();
    private final ComponentesFisica comp;

    public PipelineFisicaPermisiva(CalculadorFuerzaGravitatoria cg, AsistenciaOrbital ao, ResolucionColision rc,
                                   ProcesadorAgujeroNegro gan, IntegradorFisico im, DetectorContactoFisico dc) {
        this(cg, ao, rc, null, gan, im, dc);
    }

    public PipelineFisicaPermisiva(CalculadorFuerzaGravitatoria cg, AsistenciaOrbital ao, ResolucionColision rc,
                                   ConfiguracionEventosColision configColision, ProcesadorAgujeroNegro gan,
                                   IntegradorFisico im, DetectorContactoFisico dc) {
        this.comp = new ComponentesFisica(cg, ao, rc, configColision, gan, im, dc);
    }

    public void ejecutarTick(List<CuerpoCeleste> cuerpos, RegistroCapacidadesFisica registro,
                             NotificadorEventosFisica notificador, double dt) {
        buffers.recolectarSimulados(cuerpos);
        if (buffers.simulados.isEmpty()) return;

        resetearFuerzas();
        comp.ejecutarDinamica(buffers.simulados);
        actualizarEntidadesEspeciales(registro, dt);
        procesarAgujerosNegros(cuerpos, registro, notificador);
        procesarColisiones(cuerpos, registro, notificador);
    }

    private void resetearFuerzas() {
        for (CuerpoCeleste c : buffers.simulados) {
            c.aplicarFuerza(null);
        }
    }

    private void actualizarEntidadesEspeciales(RegistroCapacidadesFisica registro, double dt) {
        for (CuerpoCeleste c : buffers.simulados) {
            Actualizable act = registro.obtenerActualizable(c);
            if (act == null && c instanceof Actualizable a) {
                act = a;
            }
            if (act != null) {
                act.actualizarEntidad(dt);
            }
        }
    }

    private void procesarAgujerosNegros(List<CuerpoCeleste> cuerpos, RegistroCapacidadesFisica registro,
                                        NotificadorEventosFisica notificador) {
        buffers.reiniciarMutaciones();
        comp.gestorAgujeroNegro.procesar(buffers.simulados, buffers.aEliminar, comp.listenerMensaje);
        if (buffers.aEliminar.isEmpty()) return;

        cuerpos.removeAll(buffers.aEliminar);
        buffers.simulados.removeAll(buffers.aEliminar);
        for (CuerpoCeleste c : buffers.aEliminar) {
            registro.desregistrar(c);
            comp.emitirExplosion(c);
            notificador.notificarColision();
        }
        limpiarSatelitesHuerfanos(cuerpos, registro, notificador);
        buffers.simulados.retainAll(cuerpos);
    }

    private void procesarColisiones(List<CuerpoCeleste> todos, RegistroCapacidadesFisica registro,
                                    NotificadorEventosFisica notificador) {
        buffers.reiniciarMutaciones();
        comp.detectorColisiones.detectarYResolver(buffers.simulados, buffers.aEliminar, buffers.aAgregar);

        todos.removeAll(buffers.aEliminar);
        todos.addAll(buffers.aAgregar);
        for (CuerpoCeleste c : buffers.aEliminar) {
            registro.desregistrar(c);
            notificador.notificarColision();
        }
        limpiarSatelitesHuerfanos(todos, registro, notificador);
    }

    private void limpiarSatelitesHuerfanos(List<CuerpoCeleste> todos, RegistroCapacidadesFisica registro,
                                           NotificadorEventosFisica notificador) {
        List<CuerpoCeleste> huerfanos = new ArrayList<>();
        for (CuerpoCeleste c : todos) {
            if (c == null || buffers.aEliminar.contains(c)) continue;
            if (tieneAnfitrionPerdido(c, todos)) {
                procesarAnfitrionPerdido(c, registro, huerfanos);
            }
        }
        if (!huerfanos.isEmpty()) {
            todos.removeAll(huerfanos);
            for (int i = 0; i < huerfanos.size(); i++) {
                notificador.notificarColision();
            }
        }
    }

    private boolean tieneAnfitrionPerdido(CuerpoCeleste c, List<CuerpoCeleste> todos) {
        CuerpoCeleste orbitado = c.getCuerpoOrbitado();
        if (orbitado == null) return false;
        return buffers.aEliminar.contains(orbitado) || !orbitado.esSimulado() || !todos.contains(orbitado);
    }

    private void procesarAnfitrionPerdido(CuerpoCeleste c, RegistroCapacidadesFisica registro, List<CuerpoCeleste> huerfanos) {
        if (c.getTipoCuerpo() == TipoCuerpo.SATELITE || registro.esActualizableRegistrado(c)) {
            huerfanos.add(c);
            c.setSimulado(false);
            c.setMasa(0);
            registro.desregistrar(c);
        }
        c.setCuerpoOrbitado(null);
    }

    public double calcularAsistenciaEfectiva(double masa) {
        return comp.asistenteOrbital.calcularAsistenciaEfectiva(masa, comp.asistenciaOrbital);
    }

    public void setListenerMensaje(Consumer<String> listener) {
        comp.listenerMensaje = listener;
        if (comp.configuracionColision != null) {
            comp.configuracionColision.setListenerMensaje(listener);
        }
    }

    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) {
        if (comp.configuracionColision != null) {
            comp.configuracionColision.setListenerNotificacion(listener);
        }
    }

    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) {
        comp.listenerExplosion = listener;
        if (comp.configuracionColision != null) {
            comp.configuracionColision.setListenerExplosion(listener);
        }
    }

    public void setProveedorProgreso(DoubleSupplier proveedorProgreso) {
        if (comp.configuracionColision != null) {
            comp.configuracionColision.setProveedorProgreso(proveedorProgreso);
        }
    }

    public ResolucionColision getResolvedorColisiones() { return comp.resolvedorColisiones; }
    public ProcesadorAgujeroNegro getGestorAgujeroNegro() { return comp.gestorAgujeroNegro; }
    public double getAsistenciaOrbital() { return comp.asistenciaOrbital; }
    public void setAsistenciaOrbital(double asistencia) { comp.asistenciaOrbital = Math.max(0, Math.min(0.1, asistencia)); }
}
