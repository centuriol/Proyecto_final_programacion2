package org.example.game.motor.colision;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.ConfiguracionColisiones;

/**
 * Regla de absorción gravitatoria por ratio de masa extrema (>= 8.0).
 */
public class ReglaColisionAbsorcionPorRatio implements ReglaColision {

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        return UtilidadColision.calcularRatioMasas(a, b) >= ConfiguracionColisiones.RATIO_ABSORCION_MASA;
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
        CuerpoCeleste menor = (mayor == a) ? b : a;

        UtilidadColision.absorber(mayor, menor, ctx.aEliminar());
        ctx.emitirMensaje(mayor.getNombre() + " absorbió por gravedad a " + menor.getNombre() + ".");
    }
}
