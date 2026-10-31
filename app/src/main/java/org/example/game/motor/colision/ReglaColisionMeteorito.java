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
        if (c == null) return false;
        return c.aceptar(new VisitanteCuerpo<Boolean>() {
            @Override public Boolean visitarEstrella(Estrella estrella) { return false; }
            @Override public Boolean visitarPlaneta(Planeta planeta) { return false; }
            @Override public Boolean visitarLuna(Luna luna) { return false; }
            @Override public Boolean visitarSatelite(Satelite satelite) { return true; }
            @Override public Boolean visitarMeteorito(Meteorito meteorito) { return false; }
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return false; }
        });
    }

    private boolean esMeteorito(CuerpoCeleste c) {
        if (c == null) return false;
        return c.aceptar(new VisitanteCuerpo<Boolean>() {
            @Override public Boolean visitarEstrella(Estrella estrella) { return false; }
            @Override public Boolean visitarPlaneta(Planeta planeta) { return false; }
            @Override public Boolean visitarLuna(Luna luna) { return false; }
            @Override public Boolean visitarSatelite(Satelite satelite) { return false; }
            @Override public Boolean visitarMeteorito(Meteorito meteorito) { return true; }
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return false; }
        });
    }

    private boolean esPlaneta(CuerpoCeleste c) {
        if (c == null) return false;
        return c.aceptar(new VisitanteCuerpo<Boolean>() {
            @Override public Boolean visitarEstrella(Estrella estrella) { return false; }
            @Override public Boolean visitarPlaneta(Planeta planeta) { return true; }
            @Override public Boolean visitarLuna(Luna luna) { return false; }
            @Override public Boolean visitarSatelite(Satelite satelite) { return false; }
            @Override public Boolean visitarMeteorito(Meteorito meteorito) { return false; }
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return false; }
        });
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        if (esSatelite(a) && esSatelite(b)) return true;
        return esMeteorito(a) || esMeteorito(b);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        if (esSatelite(a) && esSatelite(b)) {
            return;
        }

        if (esMeteorito(a) && esMeteorito(b)) {
            utilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            utilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("Destrucción mutua de meteoritos por impacto directo.");
            return;
        }

        CuerpoCeleste meteorito = esMeteorito(a) ? a : b;
        CuerpoCeleste objetivo = (meteorito == a) ? b : a;

        utilidadColision.desactivarYEliminar(meteorito, ctx.aEliminar());

        if (esSatelite(objetivo)) {
            ctx.emitirExplosion(meteorito.getPosicion(), meteorito.getRadio() * 2.0);
            ctx.emitirEvento(
                    "El satélite " + objetivo.getNombre() + " interceptó y detuvo a " + meteorito.getNombre() + ".",
                    MensajeEvento.TipoMensaje.INFO
            );
            ctx.emitirMensaje("Satélite " + objetivo.getNombre() + " detuvo a " + meteorito.getNombre() + ".");
            return;
        }

        if (esPlaneta(objetivo)) {
            aplicarImpactoAPlaneta(meteorito, objetivo, ctx);
            return;
        }

        if (objetivo.getMasa() > 0) {
            Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
            Vector2D nuevaVelocidad = objetivo.getVelocidad().sumar(momentoMeteorito.dividir(objetivo.getMasa()));
            objetivo.setVelocidad(nuevaVelocidad);
        }

        RegistroCapacidades.obtenerCivilizable(objetivo).ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                civ.getCivilizacion().recibirImpactoMeteorito();
            }
        });

        ctx.emitirMensaje(meteorito.getNombre() + " colisionó contra " + objetivo.getNombre() + ".");
    }

    private void aplicarImpactoAPlaneta(CuerpoCeleste meteorito, CuerpoCeleste planeta, ContextoColision ctx) {
        double masaInicial = planeta.aceptar(new VisitanteCuerpo<Double>() {
            @Override public Double visitarEstrella(Estrella estrella) { return estrella.getMasa(); }
            @Override public Double visitarPlaneta(Planeta p) { return p.getMasaInicial(); }
            @Override public Double visitarLuna(Luna luna) { return luna.getMasa(); }
            @Override public Double visitarSatelite(Satelite satelite) { return satelite.getMasa(); }
            @Override public Double visitarMeteorito(Meteorito met) { return met.getMasa(); }
            @Override public Double visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return agujeroNegro.getMasa(); }
        });
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
        double factorDano = calculadorAmenaza.fraccionDano(progresoEfectivo);
        double danoMasa = masaInicial * factorDano;
        double nuevaMasa = Math.max(0.0, planeta.getMasa() - danoMasa);
        planeta.setMasa(nuevaMasa);

        RegistroCapacidades.obtenerCivilizable(planeta).ifPresent(civ -> {
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
