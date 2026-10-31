package org.example.game.motor.colision;

import org.example.game.cuerpo.AgujeroNegro;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.VisitanteCuerpo;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.MensajeEvento;

/**
 * Regla de colisión para interacción gravitatoria extrema de Agujeros Negros y fusión de estrellas.
 * Despacho doble polimórfico sin consultar subtipos desde el consumidor (Tarea 3).
 */
public class ReglaColisionAgujeroNegro implements ReglaColision {

    private final UtilidadColision utilidadColision;

    public ReglaColisionAgujeroNegro() {
        this(new UtilidadColision());
    }

    public ReglaColisionAgujeroNegro(UtilidadColision utilidadColision) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        Boolean res = a.aceptar(new VisitanteCuerpo<>() {
            @Override
            public Boolean visitarEstrella(Estrella eA) {
                return b.aceptar(new VisitanteCuerpo<>() {
                    @Override public Boolean visitarEstrella(Estrella eB) { return true; }
                    @Override public Boolean visitarAgujeroNegro(AgujeroNegro anB) { return true; }
                });
            }

            @Override
            public Boolean visitarAgujeroNegro(AgujeroNegro anA) {
                return true;
            }

            @Override
            public Boolean visitarPlaneta(org.example.game.cuerpo.Planeta p) {
                return esAgujero(b);
            }

            @Override
            public Boolean visitarLuna(org.example.game.cuerpo.Luna l) {
                return esAgujero(b);
            }

            @Override
            public Boolean visitarSatelite(org.example.game.cuerpo.Satelite s) {
                return esAgujero(b);
            }

            @Override
            public Boolean visitarMeteorito(org.example.game.cuerpo.Meteorito m) {
                return esAgujero(b);
            }

            private Boolean esAgujero(CuerpoCeleste otro) {
                return Boolean.TRUE.equals(otro.aceptar(new VisitanteCuerpo<>() {
                    @Override public Boolean visitarAgujeroNegro(AgujeroNegro an) { return true; }
                }));
            }
        });
        return Boolean.TRUE.equals(res);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        boolean ambasEstrellas = Boolean.TRUE.equals(a.aceptar(new VisitanteCuerpo<>() {
            @Override public Boolean visitarEstrella(Estrella eA) {
                return Boolean.TRUE.equals(b.aceptar(new VisitanteCuerpo<>() {
                    @Override public Boolean visitarEstrella(Estrella eB) { return true; }
                }));
            }
        }));

        if (ambasEstrellas) {
            double masaTotal = a.getMasa() + b.getMasa();
            Vector2D posCM = (a.getPosicion().multiplicar(a.getMasa()).sumar(b.getPosicion().multiplicar(b.getMasa()))).dividir(Math.max(1e-9, masaTotal));
            Vector2D velCM = (a.getVelocidad().multiplicar(a.getMasa()).sumar(b.getVelocidad().multiplicar(b.getMasa()))).dividir(Math.max(1e-9, masaTotal));

            AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroPersonalizado(posCM.x(), posCM.y(), masaTotal, velCM);
            utilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            utilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.aAgregar().add(bh);

            ctx.emitirExplosion(posCM, bh.getRadio() * 3.0);
            ctx.emitirEvento("Colapso estelar: Choque entre dos Soles dio origen a un Agujero Negro.", MensajeEvento.TipoMensaje.PELIGRO);
            return;
        }

        boolean aEsBH = Boolean.TRUE.equals(a.aceptar(new VisitanteCuerpo<>() {
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro an) { return true; }
        }));
        boolean bEsBH = Boolean.TRUE.equals(b.aceptar(new VisitanteCuerpo<>() {
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro an) { return true; }
        }));

        if (aEsBH && bEsBH) {
            CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
            CuerpoCeleste menor = (mayor == a) ? b : a;
            utilidadColision.absorber(mayor, menor, ctx.aEliminar());
            return;
        }
        if (aEsBH) {
            utilidadColision.absorber(a, b, ctx.aEliminar());
            return;
        }
        utilidadColision.absorber(b, a, ctx.aEliminar());
    }
}
