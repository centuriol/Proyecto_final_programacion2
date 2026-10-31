package org.example.game.motor.colision;
import org.example.game.cuerpo.AgujeroNegro;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Defensor;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Luna;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.Satelite;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.VisitanteCuerpo;
/**
 * Regla de intercepción y amortiguación mediante escudos planetarios (OCP / Polimorfismo).
 */
public class ReglaColisionEscudo implements ReglaColision {

    private final UtilidadColision utilidadColision;

    public ReglaColisionEscudo() {
        this(new UtilidadColision());
    }

    public ReglaColisionEscudo(UtilidadColision utilidadColision) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
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

    private boolean esPlanetaOLuna(CuerpoCeleste c) {
        if (c == null) return false;
        return c.aceptar(new VisitanteCuerpo<Boolean>() {
            @Override public Boolean visitarEstrella(Estrella estrella) { return false; }
            @Override public Boolean visitarPlaneta(Planeta planeta) { return true; }
            @Override public Boolean visitarLuna(Luna luna) { return true; }
            @Override public Boolean visitarSatelite(Satelite satelite) { return false; }
            @Override public Boolean visitarMeteorito(Meteorito meteorito) { return false; }
            @Override public Boolean visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return false; }
        });
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        boolean aTiene = ServicioEscudo.getInstanciaPorDefecto().tieneEscudo(a);
        boolean bTiene = ServicioEscudo.getInstanciaPorDefecto().tieneEscudo(b);
        if (!aTiene && !bTiene) return false;

        if (esMeteorito(a) || esMeteorito(b)) return true;

        return esPlanetaOLuna(a) && esPlanetaOLuna(b);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        Defensor defA = ServicioEscudo.getInstanciaPorDefecto().obtenerDefensor(a).orElse(null);
        Defensor defB = ServicioEscudo.getInstanciaPorDefecto().obtenerDefensor(b).orElse(null);
        boolean aTiene = defA != null && defA.tieneEscudo();
        boolean bTiene = defB != null && defB.tieneEscudo();

        if (esMeteorito(a) && bTiene) {
            defB.absorberGolpeEscudo();
            utilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            ctx.emitirMensaje("El escudo planetario de " + b.getNombre() + " absorbió un impacto de meteorito (" + defB.getGolpesEscudoRestantes() + " golpes restantes).");
            return;
        }
        if (esMeteorito(b) && aTiene) {
            defA.absorberGolpeEscudo();
            utilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("El escudo planetario de " + a.getNombre() + " absorbió un impacto de meteorito (" + defA.getGolpesEscudoRestantes() + " golpes restantes).");
            return;
        }

        if (aTiene) defA.absorberGolpeEscudo();
        if (bTiene) defB.absorberGolpeEscudo();
        utilidadColision.reboteElastico(a, b);
        ctx.emitirMensaje("Interacción orbital amortiguada por escudo planetario: rebote elástico entre " + a.getNombre() + " y " + b.getNombre() + ".");
    }
}
