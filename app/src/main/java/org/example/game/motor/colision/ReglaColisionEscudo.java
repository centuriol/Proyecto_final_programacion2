package org.example.game.motor.colision;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo;
/**
 * Regla de intercepción y amortiguación mediante escudos planetarios (OCP / Polimorfismo).
 */
public class ReglaColisionEscudo implements ReglaColision {

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        boolean aTiene = a.tieneEscudo();
        boolean bTiene = b.tieneEscudo();
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
        if (a.getTipoCuerpo() == TipoCuerpo.METEORITO && b.tieneEscudo()) {
            b.absorberGolpeEscudo();
            UtilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            ctx.emitirMensaje("El escudo planetario de " + b.getNombre() + " absorbió un impacto de meteorito (" + b.getGolpesEscudoRestantes() + " golpes restantes).");
            return;
        }
        if (b.getTipoCuerpo() == TipoCuerpo.METEORITO && a.tieneEscudo()) {
            a.absorberGolpeEscudo();
            UtilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("El escudo planetario de " + a.getNombre() + " absorbió un impacto de meteorito (" + a.getGolpesEscudoRestantes() + " golpes restantes).");
            return;
        }

        if (a.tieneEscudo()) a.absorberGolpeEscudo();
        if (b.tieneEscudo()) b.absorberGolpeEscudo();
        UtilidadColision.reboteElastico(a, b);
        ctx.emitirMensaje("Interacción orbital amortiguada por escudo planetario: rebote elástico entre " + a.getNombre() + " y " + b.getNombre() + ".");
    }
}
