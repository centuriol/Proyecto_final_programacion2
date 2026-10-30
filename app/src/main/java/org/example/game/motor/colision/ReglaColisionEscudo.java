package org.example.game.motor.colision;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Defensor;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * Regla de intercepción y amortiguación mediante escudos planetarios.
 */
public class ReglaColisionEscudo implements ReglaColision {

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        boolean aTiene = (a instanceof Defensor def && def.tieneEscudo());
        boolean bTiene = (b instanceof Defensor def && def.tieneEscudo());
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
        if (a.getTipoCuerpo() == TipoCuerpo.METEORITO && b instanceof Defensor def && def.tieneEscudo()) {
            def.absorberGolpeEscudo();
            UtilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            ctx.emitirMensaje("El escudo planetario de " + b.getNombre() + " absorbió un impacto de meteorito (" + def.getGolpesEscudoRestantes() + " golpes restantes).");
            return;
        }
        if (b.getTipoCuerpo() == TipoCuerpo.METEORITO && a instanceof Defensor def && def.tieneEscudo()) {
            def.absorberGolpeEscudo();
            UtilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("El escudo planetario de " + a.getNombre() + " absorbió un impacto de meteorito (" + def.getGolpesEscudoRestantes() + " golpes restantes).");
            return;
        }

        if (a instanceof Defensor def && def.tieneEscudo()) def.absorberGolpeEscudo();
        if (b instanceof Defensor def && def.tieneEscudo()) def.absorberGolpeEscudo();
        UtilidadColision.reboteElastico(a, b);
        ctx.emitirMensaje("Interacción orbital amortiguada por escudo planetario: rebote elástico entre " + a.getNombre() + " y " + b.getNombre() + ".");
    }
}
