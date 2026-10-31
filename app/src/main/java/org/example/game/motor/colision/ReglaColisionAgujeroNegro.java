package org.example.game.motor.colision;
import org.example.game.cuerpo.AgujeroNegro; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.CuerpoCelesteFactory; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.motor.Vector2D; import org.example.game.simulacion.MensajeEvento;
/**
 * Regla de colisión para interacción gravitatoria extrema de Agujeros Negros y fusión de estrellas.
 */
public class ReglaColisionAgujeroNegro implements ReglaColision {

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();
        if (ta == TipoCuerpo.ESTRELLA && tb == TipoCuerpo.ESTRELLA) return true;
        return (ta != null && ta.esAgujeroNegro()) || (tb != null && tb.esAgujeroNegro());
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();

        if (ta == TipoCuerpo.ESTRELLA && tb == TipoCuerpo.ESTRELLA) {
            double masaTotal = a.getMasa() + b.getMasa();
            Vector2D posCM = (a.getPosicion().multiplicar(a.getMasa()).sumar(b.getPosicion().multiplicar(b.getMasa()))).dividir(Math.max(1e-9, masaTotal));
            Vector2D velCM = (a.getVelocidad().multiplicar(a.getMasa()).sumar(b.getVelocidad().multiplicar(b.getMasa()))).dividir(Math.max(1e-9, masaTotal));

            AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroPersonalizado(posCM.x(), posCM.y(), masaTotal, velCM);
            UtilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            UtilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.aAgregar().add(bh);

            ctx.emitirExplosion(posCM, bh.getRadio() * 3.0);
            ctx.emitirEvento("¡COLAPSO ESTELAR! Choque entre dos Soles dio origen a un temible Agujero Negro.", MensajeEvento.TipoMensaje.PELIGRO);
            return;
        }

        boolean aEsBH = (ta != null && ta.esAgujeroNegro());
        boolean bEsBH = (tb != null && tb.esAgujeroNegro());

        if (aEsBH && bEsBH) {
            CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
            CuerpoCeleste menor = (mayor == a) ? b : a;
            UtilidadColision.absorber(mayor, menor, ctx.aEliminar());
            return;
        }
        if (aEsBH) {
            UtilidadColision.absorber(a, b, ctx.aEliminar());
            return;
        }
        UtilidadColision.absorber(b, a, ctx.aEliminar());
    }
}
