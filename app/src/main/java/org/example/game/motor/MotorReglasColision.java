package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Meteorito;
import org.example.game.motor.colision.ContextoColision;
import org.example.game.motor.colision.ReglaColision;
import org.example.game.motor.colision.ReglaColisionMeteorito;
import org.example.game.simulacion.MensajeEvento;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * SRP: Mantiene el registro ordenado de reglas de colisión y ejecuta la regla aplicable con notificaciones.
 */
public class MotorReglasColision {

    private final List<ReglaColision> reglas = new ArrayList<>();
    private final NotificadorColisiones notificador = new NotificadorColisiones();

    public MotorReglasColision(List<ReglaColision> reglasIniciales) {
        if (reglasIniciales != null) {
            this.reglas.addAll(reglasIniciales);
        }
    }

    public void agregarRegla(ReglaColision regla) {
        if (regla != null) this.reglas.add(0, regla);
    }

    public void setListenerMensaje(Consumer<String> listener) { notificador.setListenerMensaje(listener); }
    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) { notificador.setListenerNotificacion(listener); }
    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) { notificador.setListenerExplosion(listener); }

    public void setProveedorProgreso(DoubleSupplier proveedorProgreso) {
        for (ReglaColision regla : reglas) {
            regla.setProveedorProgreso(proveedorProgreso);
        }
    }

    public void resolver(CuerpoCeleste a, CuerpoCeleste b, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        boolean[] explosionEmitida = {false};
        boolean[] eventoEmitido = {false};
        int eliminadosAntes = aEliminar != null ? aEliminar.size() : 0;
        int agregadosAntes = aAgregar != null ? aAgregar.size() : 0;
        ContextoColision ctx = notificador.crearContextoRastreado(aEliminar, aAgregar, explosionEmitida, eventoEmitido);

        for (ReglaColision regla : reglas) {
            if (regla.aplica(a, b)) {
                ejecutarRegla(regla, a, b, ctx, explosionEmitida, eventoEmitido, eliminadosAntes, agregadosAntes);
                return;
            }
        }
    }

    private void ejecutarRegla(ReglaColision regla, CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx,
                               boolean[] explosionEmitida, boolean[] eventoEmitido,
                               int eliminadosAntes, int agregadosAntes) {
        Vector2D posColision = a.getPosicion().sumar(b.getPosicion()).multiplicar(0.5);
        double radioColision = Math.max(18.0, Math.max(a.getRadio(), b.getRadio()) * 1.8);
        regla.resolver(a, b, ctx);
        if (!explosionEmitida[0] && huboCambios(eventoEmitido[0], ctx, eliminadosAntes, agregadosAntes)) {
            ctx.emitirExplosion(posColision, radioColision);
        }
    }

    private boolean huboCambios(boolean eventoEmitido, ContextoColision ctx, int eliminadosAntes, int agregadosAntes) {
        if (eventoEmitido) return true;
        if (ctx.aEliminar() != null && ctx.aEliminar().size() > eliminadosAntes) return true;
        return ctx.aAgregar() != null && ctx.aAgregar().size() > agregadosAntes;
    }

    public void impactarMeteorito(Meteorito meteorito, CuerpoCeleste objetivo, Set<CuerpoCeleste> aEliminar) {
        ContextoColision ctx = notificador.crearContextoSimple(aEliminar, new ArrayList<>());
        ReglaColision reglaMeteorito = reglas.stream()
                .filter(ReglaColisionMeteorito.class::isInstance)
                .findFirst()
                .orElseGet(ReglaColisionMeteorito::new);
        reglaMeteorito.resolver(meteorito, objetivo, ctx);
    }
}
