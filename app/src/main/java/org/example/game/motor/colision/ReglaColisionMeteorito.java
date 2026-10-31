package org.example.game.motor.colision;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
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

    public ReglaColisionMeteorito() {
        this(new UtilidadColision(), () -> ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
    }

    public ReglaColisionMeteorito(DoubleSupplier proveedorProgreso) {
        this(new UtilidadColision(), proveedorProgreso);
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision) {
        this(utilidadColision, () -> ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
    }

    public ReglaColisionMeteorito(UtilidadColision utilidadColision, DoubleSupplier proveedorProgreso) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
        this.proveedorProgreso = proveedorProgreso != null
                ? proveedorProgreso
                : () -> ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL;
    }

    public void setProveedorProgreso(DoubleSupplier proveedorProgreso) {
        if (proveedorProgreso != null) {
            this.proveedorProgreso = proveedorProgreso;
        }
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();
        if (ta == TipoCuerpo.SATELITE && tb == TipoCuerpo.SATELITE) return true;
        return (ta == TipoCuerpo.METEORITO || tb == TipoCuerpo.METEORITO);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();

        if (ta == TipoCuerpo.SATELITE && tb == TipoCuerpo.SATELITE) {
            return;
        }

        if (ta == TipoCuerpo.METEORITO && tb == TipoCuerpo.METEORITO) {
            utilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            utilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("Destrucción mutua de meteoritos por impacto directo.");
            return;
        }

        CuerpoCeleste meteorito = (ta == TipoCuerpo.METEORITO) ? a : b;
        CuerpoCeleste objetivo = (meteorito == a) ? b : a;

        utilidadColision.desactivarYEliminar(meteorito, ctx.aEliminar());

        if (objetivo.getTipoCuerpo() == TipoCuerpo.SATELITE) {
            ctx.emitirExplosion(meteorito.getPosicion(), meteorito.getRadio() * 2.0);
            ctx.emitirEvento(
                    "El satélite " + objetivo.getNombre() + " interceptó y detuvo a " + meteorito.getNombre() + ".",
                    MensajeEvento.TipoMensaje.INFO
            );
            ctx.emitirMensaje("Satélite " + objetivo.getNombre() + " detuvo a " + meteorito.getNombre() + ".");
            return;
        }

        if (objetivo.getTipoCuerpo() != null && objetivo.getTipoCuerpo().esPlaneta()) {
            aplicarImpactoAPlaneta(meteorito, objetivo, ctx);
            return;
        }

        if (objetivo.getMasa() > 0) {
            Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
            Vector2D nuevaVelocidad = objetivo.getVelocidad().sumar(momentoMeteorito.dividir(objetivo.getMasa()));
            objetivo.setVelocidad(nuevaVelocidad);
        }

        objetivo.comoCivilizable().ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                civ.getCivilizacion().recibirImpactoMeteorito();
            }
        });

        ctx.emitirMensaje(meteorito.getNombre() + " colisionó contra " + objetivo.getNombre() + ".");
    }

    private void aplicarImpactoAPlaneta(CuerpoCeleste meteorito, CuerpoCeleste planeta, ContextoColision ctx) {
        double masaInicial = planeta.getMasaInicial();
        double progresoEfectivo = (meteorito instanceof Meteorito met && met.tieneProgresoExplicito())
                ? ((Meteorito) meteorito).getFactorProgresoOrigen()
                : proveedorProgreso.getAsDouble();
        double factorDano = EscaladorAmenazaMeteorito.calcularFraccionDano(progresoEfectivo);
        double danoMasa = masaInicial * factorDano;
        double nuevaMasa = Math.max(0.0, planeta.getMasa() - danoMasa);
        planeta.setMasa(nuevaMasa);

        planeta.comoCivilizable().ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                civ.getCivilizacion().recalcularPoblacionMaxima();
                civ.getCivilizacion().recibirImpactoMeteorito();
            }
        });

        if (nuevaMasa <= 0.0 || nuevaMasa < masaInicial * ConfiguracionColisiones.UMBRAL_EXPLOSION_MASA_FRACCION) {
            utilidadColision.desactivarYEliminar(planeta, ctx.aEliminar());
            ctx.emitirExplosion(planeta.getPosicion(), planeta.getRadio() * 2.5);
            ctx.emitirEvento(
                    "¡CATASTROFE! El impacto de meteorito masivo destruyó completamente al planeta " + planeta.getNombre() + ".",
                    MensajeEvento.TipoMensaje.PELIGRO
            );
            return;
        }

        ctx.emitirMensaje(
                meteorito.getNombre() + " impactó en " + planeta.getNombre()
                        + " reduciendo su masa en " + String.format("%.1f%%", factorDano * 100.0) + "."
        );
    }
}
