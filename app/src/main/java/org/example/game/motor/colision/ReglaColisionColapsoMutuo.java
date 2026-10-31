package org.example.game.motor.colision;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.ConfiguracionColisiones;
/**
 * Regla de colapso mutuo en escombros para masas similares (ratio <= 1.3).
 */
public class ReglaColisionColapsoMutuo implements ReglaColision {

    private final UtilidadColision utilidadColision;

    public ReglaColisionColapsoMutuo() {
        this(new UtilidadColision());
    }

    public ReglaColisionColapsoMutuo(UtilidadColision utilidadColision) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        return utilidadColision.calcularRatioMasas(a, b) <= ConfiguracionColisiones.RATIO_COLISION_COLAPSO;
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        utilidadColision.generarEscombros(a, ctx.aEliminar(), ctx.aAgregar());
        utilidadColision.generarEscombros(b, ctx.aEliminar(), ctx.aAgregar());
        ctx.emitirMensaje("Colisión catastrófica mutua: " + a.getNombre() + " y " + b.getNombre() + " colapsaron en escombros.");
    }
}
