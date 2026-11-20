package org.example.game.motor.colision;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.RegistroCapacidades;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionColisiones;
import org.example.game.simulacion.MensajeEvento;

/**
 * SRP: Aplica las consecuencias físicas y de civilización de un impacto de meteorito sobre su objetivo.
 */
public class AplicadorImpactoMeteorito {

    private final UtilidadColision utilidadColision;

    public AplicadorImpactoMeteorito(UtilidadColision utilidadColision) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
    }

    public void notificarIntercepcionSatelite(CuerpoCeleste meteorito, double radio, CuerpoCeleste satelite, ContextoColision ctx) {
        ctx.emitirExplosion(meteorito.getPosicion(), radio * 2.0);
        ctx.emitirEvento("El satélite " + satelite.getNombre() + " interceptó y detuvo a " + meteorito.getNombre() + ".", MensajeEvento.TipoMensaje.INFO);
        ctx.emitirMensaje("Satélite " + satelite.getNombre() + " detuvo a " + meteorito.getNombre() + ".");
    }

    public void aplicarImpactoGenerico(CuerpoCeleste meteorito, double masaMeteorito, CuerpoCeleste objetivo, ContextoColision ctx) {
        if (objetivo.getMasa() > 0) {
            Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(masaMeteorito);
            objetivo.setVelocidad(objetivo.getVelocidad().sumar(momentoMeteorito.dividir(objetivo.getMasa())));
        }
        extinguirCivilizacionSiExiste(objetivo, ctx, false);
        ctx.emitirMensaje(meteorito.getNombre() + " colisionó contra " + objetivo.getNombre() + ".");
    }

    public void aplicarImpactoAPlaneta(CuerpoCeleste meteorito, double masaMeteorito, CuerpoCeleste planeta, ContextoColision ctx) {
        double masaAntes = planeta.getMasa();
        double danoMasa = masaMeteorito > ConfiguracionColisiones.MASA_MINIMA_METEORITO
                ? masaMeteorito
                : masaAntes * ConfiguracionColisiones.FRACCION_MASA_METEORITO_PLANETA_MAX;
        double nuevaMasa = Math.max(0.0, masaAntes - danoMasa);
        planeta.setMasa(nuevaMasa);
        extinguirCivilizacionSiExiste(planeta, ctx, true);

        if (nuevaMasa <= 0.0) {
            utilidadColision.desactivarYEliminar(planeta, ctx.aEliminar());
            ctx.emitirExplosion(planeta.getPosicion(), planeta.getRadio() * 2.5);
            ctx.emitirEvento("¡CATASTROFE! El impacto de meteorito masivo destruyó completamente al planeta " + planeta.getNombre() + ".", MensajeEvento.TipoMensaje.PELIGRO);
            return;
        }
        ctx.emitirExplosion(planeta.getPosicion(), Math.max(18.0, planeta.getRadio() * 1.8));
        double porcentajePerdido = masaAntes > 0.0 ? (danoMasa / masaAntes) * 100.0 : 0.0;
        ctx.emitirMensaje(meteorito.getNombre() + " impactó en " + planeta.getNombre() + " reduciendo su masa en " + String.format("%.1f%%", porcentajePerdido) + ".");
    }

    private void extinguirCivilizacionSiExiste(CuerpoCeleste cuerpo, ContextoColision ctx, boolean emitirAlerta) {
        RegistroCapacidades.obtenerCivilizable(cuerpo).ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                civ.getCivilizacion().recibirImpactoMeteorito();
                if (emitirAlerta) {
                    ctx.emitirEvento("¡Extinción en " + cuerpo.getNombre() + "! El impacto del meteorito exterminó a su civilización.", MensajeEvento.TipoMensaje.PELIGRO);
                }
            }
        });
    }
}
