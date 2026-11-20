package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.MensajeEvento;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Motor de física newtoniana asistida / permisiva (SRP / DIP / OCP).
 */
public class MotorFisicaPermisiva extends MotorFisicaBase {

    private static MotorFisicaPermisiva INSTANCIA_GLOBAL;

    public static MotorFisicaPermisiva getInstanciaGlobal() {
        if (INSTANCIA_GLOBAL == null) {
            INSTANCIA_GLOBAL = new MotorFisicaPermisiva(10000.0);
        }
        return INSTANCIA_GLOBAL;
    }

    private static final FabricaMotorFisica FABRICA = new FabricaMotorFisica();
    private final PipelineFisicaPermisiva pipeline;

    public MotorFisicaPermisiva(double radioMundo, CalculadorFuerzaGravitatoria calculadorGravedad,
                                AsistenciaOrbital asistenteOrbital, ResolucionColision resolvedorColisiones,
                                ProcesadorAgujeroNegro gestorAgujeroNegro, IntegradorFisico integradorMovimiento,
                                DetectorContactoFisico detectorColisiones) {
        super(radioMundo);
        this.pipeline = FABRICA.crearPipelinePorDefecto(
                calculadorGravedad, asistenteOrbital, resolvedorColisiones,
                gestorAgujeroNegro, integradorMovimiento, detectorColisiones
        );
        INSTANCIA_GLOBAL = this;
    }

    public MotorFisicaPermisiva(double radioMundo, CalculadorFuerzaGravitatoria calculadorGravedad,
                                AsistenciaOrbital asistenteOrbital, ResolucionColision resolvedorColisiones,
                                ProcesadorAgujeroNegro gestorAgujeroNegro) {
        this(radioMundo, calculadorGravedad, asistenteOrbital, resolvedorColisiones, gestorAgujeroNegro, null, null);
    }

    public MotorFisicaPermisiva(double radioMundo, CalculadorFuerzaGravitatoria calculadorGravedad,
                                AsistenciaOrbital asistenteOrbital, ResolucionColision resolvedorColisiones) {
        this(radioMundo, calculadorGravedad, asistenteOrbital, resolvedorColisiones, null, null, null);
    }

    public MotorFisicaPermisiva(double radioMundo) {
        this(radioMundo, null, null, null, null, null, null);
    }

    public MotorFisicaPermisiva() {
        this(ConstantesFisicas.RADIO_MUNDO_DEFAULT);
    }

    @Override
    public void avanzarPaso(List<CuerpoCeleste> cuerpos) {
        pipeline.ejecutarTick(cuerpos, registroFisica, notificadorEventos, 1.0);
        limpiarCuerposExpirados(cuerpos);
    }

    public double calcularAsistenciaEfectiva(double masa) { return pipeline.calcularAsistenciaEfectiva(masa); }
    @Override public void setListenerMensaje(Consumer<String> listener) { super.setListenerMensaje(listener); pipeline.setListenerMensaje(listener); }
    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) { pipeline.setListenerNotificacion(listener); }
    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) { pipeline.setListenerExplosion(listener); }
    public void setProveedorProgreso(DoubleSupplier proveedorProgreso) { pipeline.setProveedorProgreso(proveedorProgreso); }
    public ResolucionColision getResolvedorColisiones() { return pipeline.getResolvedorColisiones(); }
    public ProcesadorAgujeroNegro getGestorAgujeroNegro() { return pipeline.getGestorAgujeroNegro(); }
    public double getAsistenciaOrbital() { return pipeline.getAsistenciaOrbital(); }
    public void setAsistenciaOrbital(double asistencia) { pipeline.setAsistenciaOrbital(asistencia); }
}
