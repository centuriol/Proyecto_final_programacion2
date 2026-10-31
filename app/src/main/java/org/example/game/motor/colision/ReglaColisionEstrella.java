package org.example.game.motor.colision;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
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

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        boolean aEsEstrella = a.esEstrella();
        boolean bEsEstrella = b.esEstrella();
        if (aEsEstrella && (b.esMeteorito() || b.esSatelite())) return true;
        return bEsEstrella && (a.esMeteorito() || a.esSatelite());
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        CuerpoCeleste estrella = a.esEstrella() ? a : b;
        CuerpoCeleste menor = (estrella == a) ? b : a;

        utilidadColision.desactivarYEliminar(menor, ctx.aEliminar());
        ctx.emitirMensaje(estrella.getNombre() + " desintegró a " + menor.getNombre() + " en su corona.");
    }
}
