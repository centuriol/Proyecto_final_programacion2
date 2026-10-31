package org.example.game.motor.colision;
import org.example.game.cuerpo.*;
import org.example.game.motor.GestorAgujeroNegro;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.MensajeEvento;
/**
 * Regla de colisión para interacción gravitatoria extrema de Agujeros Negros y fusión de estrellas.
 */
public class ReglaColisionAgujeroNegro implements ReglaColision {

    private final UtilidadColision utilidadColision;

    public ReglaColisionAgujeroNegro() {
        this(new UtilidadColision());
    }

    public ReglaColisionAgujeroNegro(UtilidadColision utilidadColision) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
    }

    private boolean esEstrella(CuerpoCeleste c) {
        if (c == null) return false;
        return c.aceptar(new VisitanteCuerpo<Boolean>() {
            @Override public Boolean visitarEstrella(Estrella estrella) { return true; }
            @Override public Boolean visitarPlaneta(Planeta planeta) { return false; }
            @Override public Boolean visitarLuna(Luna luna) { return false; }
            @Override public Boolean visitarSatelite(Satelite satelite) { return false; }
            @Override public Boolean visitarMeteorito(Meteorito meteorito) { return false; }
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return false; }
        });
    }

    private boolean esAgujeroNegro(CuerpoCeleste c) {
        if (c == null) return false;
        return c.aceptar(new VisitanteCuerpo<Boolean>() {
            @Override public Boolean visitarEstrella(Estrella estrella) { return false; }
            @Override public Boolean visitarPlaneta(Planeta planeta) { return false; }
            @Override public Boolean visitarLuna(Luna luna) { return false; }
            @Override public Boolean visitarSatelite(Satelite satelite) { return false; }
            @Override public Boolean visitarMeteorito(Meteorito meteorito) { return false; }
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return true; }
        });
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        if (esEstrella(a) && esEstrella(b)) return true;
        return esAgujeroNegro(a) || esAgujeroNegro(b);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        if (esEstrella(a) && esEstrella(b)) {
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

        boolean aEsBH = esAgujeroNegro(a);
        boolean bEsBH = esAgujeroNegro(b);

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
