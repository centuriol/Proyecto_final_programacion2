package org.example.game.motor.colision;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.cuerpo.Transformable; import org.example.game.motor.Vector2D;
/**
 * Regla de reacción hidro-magmática (Easter egg) que fusiona Agua y Lava en Planeta Porro inerte.
 */
public class ReglaColisionFusionAguaLava implements ReglaColision {

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();
        return (ta == TipoCuerpo.PLANETA_AGUA && tb == TipoCuerpo.PLANETA_LAVA)
                || (ta == TipoCuerpo.PLANETA_LAVA && tb == TipoCuerpo.PLANETA_AGUA);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        CuerpoCeleste sobreviviente = (a.getMasa() >= b.getMasa()) ? a : b;
        CuerpoCeleste absorbido = (sobreviviente == a) ? b : a;

        double masaA = Math.max(1e-9, a.getMasa());
        double masaB = Math.max(1e-9, b.getMasa());
        double masaTotal = masaA + masaB;

        Vector2D posA = a.getPosicion();
        Vector2D posB = b.getPosicion();
        Vector2D nuevaPos = (posA.multiplicar(masaA).sumar(posB.multiplicar(masaB))).dividir(masaTotal);

        Vector2D velA = a.getVelocidad();
        Vector2D velB = b.getVelocidad();
        Vector2D nuevoMomento = velA.multiplicar(masaA).sumar(velB.multiplicar(masaB));
        Vector2D nuevaVel = nuevoMomento.dividir(masaTotal);

        sobreviviente.setPosicion(nuevaPos);
        sobreviviente.setVelocidad(nuevaVel);
        sobreviviente.setMasa(masaTotal);
        sobreviviente.setNombre("Planeta Porro");
        sobreviviente.transformarA(TipoCuerpo.PLANETA_ROCOSO);
        sobreviviente.setTransformadoPorEvento(true);

        if (sobreviviente.getCuerpoOrbitado() == null && absorbido.getCuerpoOrbitado() != null) {
            sobreviviente.setCuerpoOrbitado(absorbido.getCuerpoOrbitado());
        }

        UtilidadColision.desactivarYEliminar(absorbido, ctx.aEliminar());
        ctx.emitirMensaje("Reacción hidro-magmática: Colisión entre Agua y Lava formó un nuevo Planeta Porro inerte.");
    }
}
