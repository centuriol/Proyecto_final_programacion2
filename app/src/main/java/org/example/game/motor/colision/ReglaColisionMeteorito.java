package org.example.game.motor.colision;
import org.example.game.cuerpo.*;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;
import org.example.game.simulacion.ConfiguracionColisiones;
import org.example.game.simulacion.EscaladorAmenazaMeteorito;
import org.example.game.simulacion.MensajeEvento;
import java.util.function.DoubleSupplier;
/**
 * Regla de colision para impactos balisticos de meteoritos con dano escalado segun el progreso
 * del jugador y coexistencia de satelites (SRP / OCP / DIP).
 */
public class ReglaColisionMeteorito implements ReglaColision {

    private final UtilidadColision utilidadColision;
    private DoubleSupplier proveedorProgreso;
    private final CalculadorAmenazaMeteorito calculadorAmenaza;

    public ReglaColisionMeteorito() {
        this(new UtilidadColision(), () -> ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL, new EscaladorAmenazaMeteorito());
    }

    public ReglaColisionMeteorito(DoubleSupplier proveedorProgreso) {
        this(new UtilidadColision(), proveedorProgreso, new EscaladorAmenazaMeteorito());
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision) {
        this(utilidadColision, () -> ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL, new EscaladorAmenazaMeteorito());
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision, DoubleSupplier proveedorProgreso) {
        this(utilidadColision, proveedorProgreso, new EscaladorAmenazaMeteorito());
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision, DoubleSupplier proveedorProgreso, CalculadorAmenazaMeteorito calculadorAmenaza) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
        this.proveedorProgreso = proveedorProgreso != null
                ? proveedorProgreso
                : () -> ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL;
        this.calculadorAmenaza = calculadorAmenaza != null ? calculadorAmenaza : new EscaladorAmenazaMeteorito();
    }

    public void setProveedorProgreso(DoubleSupplier proveedorProgreso) {
        if (proveedorProgreso != null) {
            this.proveedorProgreso = proveedorProgreso;
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

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        if (esSatelite(a) && esSatelite(b)) return true;
        return esMeteorito(a) || esMeteorito(b);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        if (esSatelite(a) && esSatelite(b)) return;
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
            notificarIntercepcionSatelite(meteorito, radioMeteorito, objetivo, ctx);
            return;
        }
        if (esPlaneta(objetivo)) {
            aplicarImpactoAPlaneta(meteorito, masaMeteorito, objetivo, ctx);
            return;
        }
        aplicarImpactoGenerico(meteorito, masaMeteorito, objetivo, ctx);
    }

    private void notificarIntercepcionSatelite(CuerpoCeleste meteorito, double radio, CuerpoCeleste satelite, ContextoColision ctx) {
        ctx.emitirExplosion(meteorito.getPosicion(), radio * 2.0);
        ctx.emitirEvento("El satélite " + satelite.getNombre() + " interceptó y detuvo a " + meteorito.getNombre() + ".", MensajeEvento.TipoMensaje.INFO);
        ctx.emitirMensaje("Satélite " + satelite.getNombre() + " detuvo a " + meteorito.getNombre() + ".");
    }

    private void aplicarImpactoGenerico(CuerpoCeleste meteorito, double masaMeteorito, CuerpoCeleste objetivo, ContextoColision ctx) {
        if (objetivo.getMasa() > 0) {
            Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(masaMeteorito);
            objetivo.setVelocidad(objetivo.getVelocidad().sumar(momentoMeteorito.dividir(objetivo.getMasa())));
        }
        extinguirCivilizacionSiExiste(objetivo, ctx, false);
        ctx.emitirMensaje(meteorito.getNombre() + " colisionó contra " + objetivo.getNombre() + ".");
    }

    private void aplicarImpactoAPlaneta(CuerpoCeleste meteorito, double masaMeteorito, CuerpoCeleste planeta, ContextoColision ctx) {
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
