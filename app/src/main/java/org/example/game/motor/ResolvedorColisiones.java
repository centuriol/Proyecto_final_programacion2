package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Meteorito;
import org.example.game.motor.colision.ReglaColision;
import org.example.game.motor.colision.UtilidadColision;
import org.example.game.simulacion.MensajeEvento;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Resolvedor desacoplado de colisiones e interacciones físicas entre cuerpos celestes (SRP / OCP / DIP).
 */
public class ResolvedorColisiones implements ResolucionColision, ConfiguracionEventosColision {

    private static final FabricaMotorFisica FABRICA = new FabricaMotorFisica();

    private final UtilidadColision utilidadColision;
    private final MotorReglasColision motorReglas;

    public ResolvedorColisiones() {
        this(new UtilidadColision());
    }

    public ResolvedorColisiones(UtilidadColision utilidadColision) {
        this(utilidadColision, crearReglasPorDefecto(utilidadColision));
    }

    public ResolvedorColisiones(List<ReglaColision> reglas) {
        this(new UtilidadColision(), reglas);
    }

    public ResolvedorColisiones(UtilidadColision utilidadColision, List<ReglaColision> reglas) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
        this.motorReglas = new MotorReglasColision(reglas);
    }

    public static List<ReglaColision> crearReglasPorDefecto() {
        return crearReglasPorDefecto(new UtilidadColision());
    }

    public static List<ReglaColision> crearReglasPorDefecto(UtilidadColision utilidad) {
        return FABRICA.crearReglasColisionPorDefecto(utilidad);
    }

    public void agregarRegla(ReglaColision regla) { motorReglas.agregarRegla(regla); }
    public void setListenerMensaje(Consumer<String> listener) { motorReglas.setListenerMensaje(listener); }
    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) { motorReglas.setListenerNotificacion(listener); }
    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) { motorReglas.setListenerExplosion(listener); }
    public void setProveedorProgreso(DoubleSupplier proveedorProgreso) { motorReglas.setProveedorProgreso(proveedorProgreso); }

    public void resolverColision(CuerpoCeleste a, CuerpoCeleste b, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        if (a == null || b == null) return;
        motorReglas.resolver(a, b, aEliminar, aAgregar);
    }

    public void absorber(CuerpoCeleste absorbente, CuerpoCeleste victima, Set<CuerpoCeleste> aEliminar) {
        utilidadColision.absorber(absorbente, victima, aEliminar);
    }

    public void impactarMeteorito(Meteorito meteorito, CuerpoCeleste objetivo, Set<CuerpoCeleste> aEliminar) {
        motorReglas.impactarMeteorito(meteorito, objetivo, aEliminar);
    }

    public void generarEscombros(CuerpoCeleste destruido, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        utilidadColision.generarEscombros(destruido, aEliminar, aAgregar);
    }

    public static void reboteElastico(CuerpoFisico a, CuerpoFisico b) {
        new UtilidadColision().reboteElastico(a, b);
    }

    public void reboteElastico(CuerpoCeleste a, CuerpoCeleste b) {
        utilidadColision.reboteElastico(a, b);
    }

    public UtilidadColision getUtilidadColision() {
        return utilidadColision;
    }
}
