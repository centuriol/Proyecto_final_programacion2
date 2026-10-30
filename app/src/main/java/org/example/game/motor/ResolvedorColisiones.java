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

/**
 * Resolvedor desacoplado de colisiones e interacciones físicas y destructivas entre cuerpos celestes.
 *
 * Principios SOLID:
 * - SRP: Orquesta la resolución de colisiones despachando a la regla de colisión adecuada.
 * - OCP: Extensible agregando o sustituyendo estrategias ReglaColision sin modificar código existente.
 */
public class ResolvedorColisiones {

    private final List<ReglaColision> reglas = new ArrayList<>();
    private Consumer<String> listenerMensaje;
    private BiConsumer<String, MensajeEvento.TipoMensaje> listenerNotificacion;
    private BiConsumer<Vector2D, Double> listenerExplosion;

    public ResolvedorColisiones() {
        this(crearReglasPorDefecto());
    }

    public ResolvedorColisiones(List<ReglaColision> reglas) {
        if (reglas != null) {
            this.reglas.addAll(reglas);
        }
    }

    public static List<ReglaColision> crearReglasPorDefecto() {
        return List.of(
                new ReglaColisionAgujeroNegro(),
                new ReglaColisionEstrella(),
                new ReglaColisionEscudo(),
                new ReglaColisionMeteorito(),
                new ReglaColisionFusionAguaLava(),
                new ReglaColisionAbsorcionPorRatio(),
                new ReglaColisionColapsoMutuo(),
                new ReglaColisionAsimetrica()
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
        UtilidadColision.absorber(absorbente, victima, aEliminar);
    }

    public void impactarMeteorito(Meteorito meteorito, CuerpoCeleste objetivo, Set<CuerpoCeleste> aEliminar) {
        ContextoColision ctx = new ContextoColision(aEliminar, new ArrayList<>(), listenerMensaje, listenerNotificacion, listenerExplosion);
        new ReglaColisionMeteorito().resolver(meteorito, objetivo, ctx);
    }

    public void generarEscombros(CuerpoCeleste destruido, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        UtilidadColision.generarEscombros(destruido, aEliminar, aAgregar);
    }

    public static void reboteElastico(CuerpoFisico a, CuerpoFisico b) {
        UtilidadColision.reboteElastico(a, b);
    }

    public void reboteElastico(CuerpoCeleste a, CuerpoCeleste b) {
        UtilidadColision.reboteElastico(a, b);
    }
}
