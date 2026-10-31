package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Meteorito;
import org.example.game.motor.colision.ContextoColision;
import org.example.game.motor.colision.ReglaColision;
import org.example.game.motor.colision.ReglaColisionAbsorcionPorRatio;
import org.example.game.motor.colision.ReglaColisionAgujeroNegro;
import org.example.game.motor.colision.ReglaColisionAsimetrica;
import org.example.game.motor.colision.ReglaColisionColapsoMutuo;
import org.example.game.motor.colision.ReglaColisionEscudo;
import org.example.game.motor.colision.ReglaColisionEstrella;
import org.example.game.motor.colision.ReglaColisionFusionAguaLava;
import org.example.game.motor.colision.ReglaColisionMeteorito;
import org.example.game.motor.colision.UtilidadColision;
import org.example.game.simulacion.MensajeEvento;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
/**
 * Resolvedor desacoplado de colisiones e interacciones físicas y destructivas entre cuerpos celestes.
 *
 * Principios SOLID:
 * - SRP: Orquesta la resolución de colisiones despachando a la regla de colisión adecuada.
 * - OCP: Extensible agregando o sustituyendo estrategias ReglaColision sin modificar código existente.
 */
public class ResolvedorColisiones {

    private final UtilidadColision utilidadColision;
    private final List<ReglaColision> reglas = new ArrayList<>();
    private Consumer<String> listenerMensaje;
    private BiConsumer<String, MensajeEvento.TipoMensaje> listenerNotificacion;
    private BiConsumer<Vector2D, Double> listenerExplosion;

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
        if (reglas != null) {
            this.reglas.addAll(reglas);
        }
    }

    public static List<ReglaColision> crearReglasPorDefecto() {
        return crearReglasPorDefecto(new UtilidadColision());
    }

    public static List<ReglaColision> crearReglasPorDefecto(UtilidadColision utilidad) {
        UtilidadColision u = utilidad != null ? utilidad : new UtilidadColision();
        return List.of(
                new ReglaColisionAgujeroNegro(u),
                new ReglaColisionEstrella(u),
                new ReglaColisionEscudo(u),
                new ReglaColisionMeteorito(u),
                new ReglaColisionFusionAguaLava(u),
                new ReglaColisionAbsorcionPorRatio(u),
                new ReglaColisionColapsoMutuo(u),
                new ReglaColisionAsimetrica(u)
        );
    }

    public void agregarRegla(ReglaColision regla) {
        if (regla != null) {
            this.reglas.add(0, regla);
        }
    }

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
    }

    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) {
        this.listenerNotificacion = listener;
    }

    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) {
        this.listenerExplosion = listener;
    }

    public void setProveedorProgreso(DoubleSupplier proveedorProgreso) {
        for (ReglaColision regla : reglas) {
            regla.setProveedorProgreso(proveedorProgreso);
        }
    }

    public void resolverColision(CuerpoCeleste a, CuerpoCeleste b, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        if (a == null || b == null) return;
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, listenerMensaje, listenerNotificacion, listenerExplosion);

        for (ReglaColision regla : reglas) {
            if (regla.aplica(a, b)) {
                regla.resolver(a, b, ctx);
                return;
            }
        }
    }

    // Métodos utilitarios delegados para compatibilidad
    public void absorber(CuerpoCeleste absorbente, CuerpoCeleste victima, Set<CuerpoCeleste> aEliminar) {
        utilidadColision.absorber(absorbente, victima, aEliminar);
    }

    public void impactarMeteorito(Meteorito meteorito, CuerpoCeleste objetivo, Set<CuerpoCeleste> aEliminar) {
        ContextoColision ctx = new ContextoColision(aEliminar, new ArrayList<>(), listenerMensaje, listenerNotificacion, listenerExplosion);
        ReglaColision reglaMeteorito = reglas.stream()
                .filter(ReglaColisionMeteorito.class::isInstance)
                .findFirst()
                .orElseGet(ReglaColisionMeteorito::new);
        reglaMeteorito.resolver(meteorito, objetivo, ctx);
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
