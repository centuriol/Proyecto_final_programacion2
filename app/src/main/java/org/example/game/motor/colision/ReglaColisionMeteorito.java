package org.example.game.motor.colision;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;
import org.example.game.simulacion.ConfiguracionColisiones;

import java.util.function.DoubleSupplier;

/**
 * Regla de colisión para impactos balísticos de meteoritos y coexistencia pacífica de satélites (SRP / OCP / DIP).
 */
public class ReglaColisionMeteorito implements ReglaColision {

    private final UtilidadColision utilidadColision;
    private final AplicadorImpactoMeteorito aplicadorImpacto;

    public ReglaColisionMeteorito() {
        this(new UtilidadColision(), null, null);
    }

    public ReglaColisionMeteorito(DoubleSupplier proveedorProgreso) {
        this(new UtilidadColision(), proveedorProgreso, null);
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision) {
        this(utilidadColision, null, null);
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision, DoubleSupplier proveedorProgreso) {
        this(utilidadColision, proveedorProgreso, null);
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision, DoubleSupplier proveedorProgreso, CalculadorAmenazaMeteorito calculadorAmenaza) {
        this(utilidadColision, new AplicadorImpactoMeteorito(utilidadColision != null ? utilidadColision : new UtilidadColision()));
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision, AplicadorImpactoMeteorito aplicadorImpacto) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
        this.aplicadorImpacto = aplicadorImpacto != null ? aplicadorImpacto : new AplicadorImpactoMeteorito(this.utilidadColision);
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        return esSatelite(a) || esSatelite(b) || esMeteorito(a) || esMeteorito(b);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        if (!esMeteorito(a) && !esMeteorito(b)) return;
        if (esMeteorito(a) && esMeteorito(b)) {
            utilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            utilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("Destrucción mutua de meteoritos por impacto directo.");
            return;
        }
        CuerpoCeleste meteorito = esMeteorito(a) ? a : b;
        CuerpoCeleste objetivo = (meteorito == a) ? b : a;
        double masaMeteorito = meteorito.getMasa();
        double radioMeteorito = meteorito.getRadio();
        utilidadColision.desactivarYEliminar(meteorito, ctx.aEliminar());

        if (esSatelite(objetivo)) {
            aplicadorImpacto.notificarIntercepcionSatelite(meteorito, radioMeteorito, objetivo, ctx);
        } else if (esPlaneta(objetivo)) {
            aplicadorImpacto.aplicarImpactoAPlaneta(meteorito, masaMeteorito, objetivo, ctx);
        } else {
            aplicadorImpacto.aplicarImpactoGenerico(meteorito, masaMeteorito, objetivo, ctx);
        }
    }

    private boolean esSatelite(CuerpoCeleste c) {
        return c != null && c.getTipoCuerpo() == TipoCuerpo.SATELITE;
    }

    private boolean esMeteorito(CuerpoCeleste c) {
        return c != null && c.getTipoCuerpo() == TipoCuerpo.METEORITO;
    }

    private boolean esPlaneta(CuerpoCeleste c) {
        return c != null && c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta();
    }
}
