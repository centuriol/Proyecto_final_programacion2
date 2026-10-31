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
/**
 * Motor de física newtoniana asistida / permisiva para un gameplay amigable y satisfactorio.
 *
 * Principios SOLID:
 * - SRP: Orquesta el cálculo gravitacional, integración y resolución de colisiones mediante componentes especializados.
 * - DIP: Implementa MotorFisica; colaboradores inyectados por constructor con valores por defecto.
 * - OCP: Nuevos tipos de interacción y comportamientos de cuerpos se manejan mediante polimorfismo.
 */
public class MotorFisicaPermisiva extends MotorFisicaBase {

    private static MotorFisicaPermisiva INSTANCIA_GLOBAL;

    public static MotorFisicaPermisiva getInstanciaGlobal() {
        if (INSTANCIA_GLOBAL == null) {
            INSTANCIA_GLOBAL = new MotorFisicaPermisiva(10000.0);
        }
        return INSTANCIA_GLOBAL;
    }

    private final double G;
    private final double dt;
    private double asistenciaOrbital = 0.015;
    private Consumer<String> listenerMensaje;

    // Listas internas reutilizables para optimización de memoria y GC por tick
    private final List<CuerpoCeleste> simuladosCache = new ArrayList<>();
    private final Set<CuerpoCeleste> aEliminarCache = new LinkedHashSet<>();
    private final List<CuerpoCeleste> aAgregarCache = new ArrayList<>();

    // Componentes delegados (SRP / DIP)
    private final CalculadorGravedad calculadorGravedad;
    private final AsistenteOrbital asistenteOrbital;
    private final ResolvedorColisiones resolvedorColisiones;
    private final GestorAgujeroNegro gestorAgujeroNegro;
    private final IntegradorMovimiento integradorMovimiento;
    private final DetectorColisiones detectorColisiones;

    public MotorFisicaPermisiva(double radioMundo, CalculadorGravedad calculadorGravedad,
                                AsistenteOrbital asistenteOrbital, ResolvedorColisiones resolvedorColisiones,
                                GestorAgujeroNegro gestorAgujeroNegro, IntegradorMovimiento integradorMovimiento,
                                DetectorColisiones detectorColisiones) {
        super(radioMundo);
        this.G = ConstantesFisicas.G_ESCALADO;
        this.dt = 1.0;
        this.calculadorGravedad = calculadorGravedad != null ? calculadorGravedad : new CalculadorGravedad(G, 35.0);
        this.asistenteOrbital = asistenteOrbital != null ? asistenteOrbital : new AsistenteOrbital(G);
        this.resolvedorColisiones = resolvedorColisiones != null ? resolvedorColisiones : new ResolvedorColisiones();
        this.gestorAgujeroNegro = gestorAgujeroNegro != null ? gestorAgujeroNegro : new GestorAgujeroNegro();
        this.integradorMovimiento = integradorMovimiento != null ? integradorMovimiento : new IntegradorMovimiento(dt, 1200.0);
        this.detectorColisiones = detectorColisiones != null ? detectorColisiones : new DetectorColisiones(this.resolvedorColisiones);
        INSTANCIA_GLOBAL = this;
    }

    public MotorFisicaPermisiva(double radioMundo, CalculadorGravedad calculadorGravedad,
                                AsistenteOrbital asistenteOrbital, ResolvedorColisiones resolvedorColisiones,
                                GestorAgujeroNegro gestorAgujeroNegro) {
        this(radioMundo, calculadorGravedad, asistenteOrbital, resolvedorColisiones, gestorAgujeroNegro, null, null);
    }

    public MotorFisicaPermisiva(double radioMundo, CalculadorGravedad calculadorGravedad,
                                AsistenteOrbital asistenteOrbital, ResolvedorColisiones resolvedorColisiones) {
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
        simuladosCache.clear();
        for (int i = 0; i < cuerpos.size(); i++) {
            CuerpoCeleste c = cuerpos.get(i);
            if (c != null && c.esSimulado()) {
                simuladosCache.add(c);
            }
        }

        if (simuladosCache.isEmpty()) return;

        // 1. Resetear fuerzas
        for (int i = 0; i < simuladosCache.size(); i++) {
            simuladosCache.get(i).aplicarFuerza(null);
        }

        // 2. Calcular Gravedad N-Body con Softening Permisivo
        calculadorGravedad.calcularGravedad(simuladosCache, asistenteOrbital, listenerMensaje);

        // 3. Aplicar Asistencia de Estabilidad Orbital Permisiva
        asistenteOrbital.aplicarAsistenciaOrbital(simuladosCache, asistenciaOrbital);

        // 4. Integrar movimiento
        integradorMovimiento.integrar(simuladosCache);

        // 5. Procesar entidades especiales
        actualizarEntidadesEspeciales(simuladosCache);

        // 6. Atracción extrema y absorción en el horizonte de sucesos de agujeros negros
        aEliminarCache.clear();
        gestorAgujeroNegro.procesar(simuladosCache, aEliminarCache, listenerMensaje);
        if (!aEliminarCache.isEmpty()) {
            cuerpos.removeAll(aEliminarCache);
            for (CuerpoCeleste c : aEliminarCache) {
                notificarColision(c);
            }
        }

        // 7. Manejo de colisiones, absorciones y rebotes
        manejarColisiones(cuerpos, simuladosCache);

        // 8. Limpiar expirados o fuera del mundo
        limpiarCuerposExpirados(cuerpos);
    }

    public double calcularAsistenciaEfectiva(double masa) {
        return asistenteOrbital.calcularAsistenciaEfectiva(masa, asistenciaOrbital);
    }

    private void actualizarEntidadesEspeciales(List<CuerpoCeleste> cuerpos) {
        for (Actualizable act : actualizables.values()) {
            act.actualizarEntidad(dt);
        }
    }

    private void manejarColisiones(List<CuerpoCeleste> todos, List<CuerpoCeleste> simulados) {
        aEliminarCache.clear();
        aAgregarCache.clear();

        detectorColisiones.detectarYResolver(simulados, aEliminarCache, aAgregarCache);

        todos.removeAll(aEliminarCache);
        todos.addAll(aAgregarCache);

        limpiarSatelitesHuerfanos(todos, aEliminarCache);

        for (CuerpoCeleste c : aEliminarCache) {
            notificarColision(c);
        }
    }

    private void limpiarSatelitesHuerfanos(List<CuerpoCeleste> todos, Set<CuerpoCeleste> aEliminar) {
        List<CuerpoCeleste> satelitesAEliminar = new ArrayList<>();
        for (CuerpoCeleste c : todos) {
            if (c.getCuerpoOrbitado() != null && (aEliminar.contains(c.getCuerpoOrbitado()) || !todos.contains(c.getCuerpoOrbitado()))) {
                if (actualizables.containsKey(c)) {
                    satelitesAEliminar.add(c);
                    c.setSimulado(false);
                    c.setMasa(0);
                }
                c.setCuerpoOrbitado(null);
            }
        }
        if (!satelitesAEliminar.isEmpty()) {
            todos.removeAll(satelitesAEliminar);
            for (CuerpoCeleste s : satelitesAEliminar) {
                notificarColision(s);
            }
        }
    }

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
        this.resolvedorColisiones.setListenerMensaje(listener);
    }

    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) {
        this.resolvedorColisiones.setListenerNotificacion(listener);
    }

    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) {
        this.resolvedorColisiones.setListenerExplosion(listener);
    }

    public void setProveedorProgreso(java.util.function.DoubleSupplier proveedorProgreso) {
        this.resolvedorColisiones.setProveedorProgreso(proveedorProgreso);
    }

    public ResolvedorColisiones getResolvedorColisiones() {
        return resolvedorColisiones;
    }

    public GestorAgujeroNegro getGestorAgujeroNegro() {
        return gestorAgujeroNegro;
    }

    public double getAsistenciaOrbital() {
        return asistenciaOrbital;
    }

    public void setAsistenciaOrbital(double asistencia) {
        this.asistenciaOrbital = Math.max(0, Math.min(0.1, asistencia));
    }
}
