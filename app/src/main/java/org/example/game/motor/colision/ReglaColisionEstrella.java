package org.example.game.motor.colision;
import org.example.game.cuerpo.AgujeroNegro;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Luna;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.Satelite;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.VisitanteCuerpo;
/**
 * Regla para desintegración de escombros y satélites en la corona estelar.
 */
public class ReglaColisionEstrella implements ReglaColision {

    private final UtilidadColision utilidadColision;

    public ReglaColisionEstrella() {
        this(new UtilidadColision());
    }

    public ReglaColisionEstrella(UtilidadColision utilidadColision) {
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

    private boolean esMenor(CuerpoCeleste c) {
        if (c == null) return false;
        return c.aceptar(new VisitanteCuerpo<Boolean>() {
            @Override public Boolean visitarEstrella(Estrella estrella) { return false; }
            @Override public Boolean visitarPlaneta(Planeta planeta) { return false; }
            @Override public Boolean visitarLuna(Luna luna) { return false; }
            @Override public Boolean visitarSatelite(Satelite satelite) { return true; }
            @Override public Boolean visitarMeteorito(Meteorito meteorito) { return true; }
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return false; }
        });
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        boolean aEsEstrella = esEstrella(a);
        boolean bEsEstrella = esEstrella(b);
        if (aEsEstrella && esMenor(b)) return true;
        return bEsEstrella && esMenor(a);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        CuerpoCeleste estrella = esEstrella(a) ? a : b;
        CuerpoCeleste menor = (estrella == a) ? b : a;

        utilidadColision.desactivarYEliminar(menor, ctx.aEliminar());
        ctx.emitirMensaje(estrella.getNombre() + " desintegró a " + menor.getNombre() + " en su corona.");
    }
}
