package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
/**
 * Responsable de la integración numérica del movimiento de los cuerpos celestes (SRP).
 */
public class IntegradorMovimiento {

    private final double dt;
    private final double velocidadMaxima;

    public IntegradorMovimiento(double dt, double velocidadMaxima) {
        this.dt = dt;
        this.velocidadMaxima = velocidadMaxima;
    }

    public IntegradorMovimiento() {
        this(1.0, 1200.0);
    }

    public void integrar(List<CuerpoCeleste> cuerpos) {
        for (CuerpoCeleste c : cuerpos) {
            double masaValida = Math.max(1e-3, c.getMasa());
            Vector2D aceleracion = c.getFuerza().dividir(masaValida);

            double aMag = aceleracion.magnitud();
            if (aMag > 200.0) {
                aceleracion = aceleracion.normalizar().multiplicar(200.0);
            }

            Vector2D nuevaVel = c.getVelocidad().sumar(aceleracion.multiplicar(dt));

            double vMag = nuevaVel.magnitud();
            if (vMag > velocidadMaxima) {
                nuevaVel = nuevaVel.normalizar().multiplicar(velocidadMaxima);
            }

            Vector2D nuevaPos = c.getPosicion().sumar(nuevaVel.multiplicar(dt));
            c.confirmarPaso(nuevaPos, nuevaVel);
        }
    }

    public double getDt() {
        return dt;
    }

    public double getVelocidadMaxima() {
        return velocidadMaxima;
    }
}
