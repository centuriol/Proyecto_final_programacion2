package org.example.game.motor.colision;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo;
/**
 * Regla para desintegración de escombros y satélites en la corona estelar.
 */
public class ReglaColisionEstrella implements ReglaColision {

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();
        boolean aEsEstrella = (ta == TipoCuerpo.ESTRELLA);
        boolean bEsEstrella = (tb == TipoCuerpo.ESTRELLA);
        if (aEsEstrella && (tb == TipoCuerpo.METEORITO || tb == TipoCuerpo.SATELITE)) return true;
        return bEsEstrella && (ta == TipoCuerpo.METEORITO || ta == TipoCuerpo.SATELITE);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        CuerpoCeleste estrella = (a.getTipoCuerpo() == TipoCuerpo.ESTRELLA) ? a : b;
        CuerpoCeleste menor = (estrella == a) ? b : a;

        UtilidadColision.desactivarYEliminar(menor, ctx.aEliminar());
        ctx.emitirMensaje(estrella.getNombre() + " desintegró a " + menor.getNombre() + " en su corona.");
    }
}
