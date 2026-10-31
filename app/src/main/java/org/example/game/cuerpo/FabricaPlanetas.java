package org.example.game.cuerpo;

import org.example.game.motor.CalculadorOrbital;
import org.example.game.motor.Vector2D;

import java.util.function.Function;

/**
 * Responsable de la construcción y parametrización orbital de planetas (SRP).
 */
public class FabricaPlanetas {

    private final Function<String, String> generadorNombre;

    public FabricaPlanetas(Function<String, String> generadorNombre) {
        this.generadorNombre = generadorNombre != null ? generadorNombre : prefijo -> prefijo + "-" + System.nanoTime();
    }

    public Planeta crearPlaneta(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        TipoCuerpo tipoEfectivo = (tipo != null && tipo.esPlaneta()) ? tipo : TipoCuerpo.PLANETA_ROCOSO;
        double masa = tipoEfectivo.masaBase * factorMasa;
        Planeta p = new Planeta(generadorNombre.apply("Planeta"), masa, x, y, tipoEfectivo);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public Planeta crearPlanetaPersonalizado(String nombre, double masa, double x, double y, TipoCuerpo tipo, Vector2D velocidadInicial) {
        Planeta p = new Planeta(nombre, masa, x, y, tipo != null ? tipo : TipoCuerpo.PLANETA_ROCOSO);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public Planeta crearPlanetaEnOrbita(CuerpoCeleste central, double distancia, double factorMasa, TipoCuerpo tipo) {
        if (central == null) {
            return crearPlaneta(tipo, distancia, 0, factorMasa, Vector2D.cero());
        }
        double x = central.getPosicion().x() + distancia;
        double y = central.getPosicion().y();
        double vOrbital = CalculadorOrbital.calcularRapidezOrbital(central.getMasa(), distancia);
        Vector2D vel = new Vector2D(0, -vOrbital);
        TipoCuerpo tipoPlaneta = (tipo != null && tipo.esPlaneta()) ? tipo : TipoCuerpo.PLANETA_ROCOSO;
        return crearPlaneta(tipoPlaneta, x, y, factorMasa, vel);
    }
}
