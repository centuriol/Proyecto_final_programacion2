package org.example.game.motor.colision;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Defensor;
import org.example.game.cuerpo.TipoCuerpo;
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

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        boolean aTiene = a.comoDefensor().map(Defensor::tieneEscudo).orElse(false);
        boolean bTiene = b.comoDefensor().map(Defensor::tieneEscudo).orElse(false);
        if (!aTiene && !bTiene) return false;

        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();

        if (ta == TipoCuerpo.METEORITO || tb == TipoCuerpo.METEORITO) return true;

        boolean aEsPlanetaOLuna = ta != null && (ta.esPlaneta() || ta.esLuna());
        boolean bEsPlanetaOLuna = tb != null && (tb.esPlaneta() || tb.esLuna());
        return aEsPlanetaOLuna && bEsPlanetaOLuna;
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        Defensor defA = a.comoDefensor().orElse(null);
        Defensor defB = b.comoDefensor().orElse(null);
        boolean aTiene = defA != null && defA.tieneEscudo();
        boolean bTiene = defB != null && defB.tieneEscudo();

        if (a.getTipoCuerpo() == TipoCuerpo.METEORITO && bTiene) {
            defB.absorberGolpeEscudo();
            utilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            ctx.emitirMensaje("El escudo planetario de " + b.getNombre() + " absorbió un impacto de meteorito (" + defB.getGolpesEscudoRestantes() + " golpes restantes).");
            return;
        }
        if (b.getTipoCuerpo() == TipoCuerpo.METEORITO && aTiene) {
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
