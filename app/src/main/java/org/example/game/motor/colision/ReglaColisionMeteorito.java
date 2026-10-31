package org.example.game.motor.colision;
import org.example.game.cuerpo.*;
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
        if (a.esSatelite() && b.esSatelite()) return true;
        return a.esMeteorito() || b.esMeteorito();
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        if (a.esSatelite() && b.esSatelite()) {
            return;
        }

        if (a.esMeteorito() && b.esMeteorito()) {
            utilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            utilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("Destrucción mutua de meteoritos por impacto directo.");
            return;
        }

        CuerpoCeleste meteorito = a.esMeteorito() ? a : b;
        CuerpoCeleste objetivo = (meteorito == a) ? b : a;

        utilidadColision.desactivarYEliminar(meteorito, ctx.aEliminar());

        if (objetivo.esSatelite()) {
            ctx.emitirExplosion(meteorito.getPosicion(), meteorito.getRadio() * 2.0);
            ctx.emitirEvento(
                    "El satélite " + objetivo.getNombre() + " interceptó y detuvo a " + meteorito.getNombre() + ".",
                    MensajeEvento.TipoMensaje.INFO
            );
            ctx.emitirMensaje("Satélite " + objetivo.getNombre() + " detuvo a " + meteorito.getNombre() + ".");
            return;
        }

        if (objetivo.esPlaneta()) {
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
        double progresoEfectivo = meteorito.aceptar(new VisitanteCuerpo<>() {
            @Override public Double visitarEstrella(Estrella estrella) { return proveedorProgreso.getAsDouble(); }
            @Override public Double visitarPlaneta(Planeta planeta) { return proveedorProgreso.getAsDouble(); }
            @Override public Double visitarLuna(Luna luna) { return proveedorProgreso.getAsDouble(); }
            @Override public Double visitarSatelite(Satelite satelite) { return proveedorProgreso.getAsDouble(); }
            @Override public Double visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return proveedorProgreso.getAsDouble(); }
            @Override public Double visitarMeteorito(Meteorito met) {
                return met.tieneProgresoExplicito() ? met.getFactorProgresoOrigen() : proveedorProgreso.getAsDouble();
            }
        });
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
