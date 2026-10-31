package org.example.game.motor.colision;
import org.example.game.cuerpo.AgujeroNegro;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.TipoCuerpo;
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

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        if (a.esEstrella() && b.esEstrella()) return true;
        return a.esAgujeroNegro() || b.esAgujeroNegro();
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        if (a.esEstrella() && b.esEstrella()) {
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

        boolean aEsBH = a.esAgujeroNegro();
        boolean bEsBH = b.esAgujeroNegro();

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
