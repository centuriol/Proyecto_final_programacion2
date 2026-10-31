package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;
import org.example.game.simulacion.ConfiguracionColisiones;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.EscaladorAmenazaMeteorito;

import java.util.function.Function;

/**
 * Responsable de la generación balística y dispersión cinemática de meteoritos y fragmentos (SRP).
 */
public class FabricaMeteoritos {

    private final CalculadorAmenazaMeteorito calculadorAmenaza;
    private final Function<String, String> generadorNombre;

    public FabricaMeteoritos(Function<String, String> generadorNombre) {
        this(generadorNombre, new EscaladorAmenazaMeteorito());
    }

    public FabricaMeteoritos(Function<String, String> generadorNombre, CalculadorAmenazaMeteorito calculadorAmenaza) {
        this.generadorNombre = generadorNombre != null ? generadorNombre : prefijo -> prefijo + "-" + System.nanoTime();
        this.calculadorAmenaza = calculadorAmenaza != null ? calculadorAmenaza : new EscaladorAmenazaMeteorito();
    }

    public Meteorito crearHaciaObjetivo(double anchoMundo, double altoMundo, CuerpoCeleste objetivo, double factorProgreso) {
        double semiAncho = Math.max(400.0, anchoMundo / 2.0);
        double semiAlto = Math.max(300.0, altoMundo / 2.0);

        double lado = Math.random() * 4.0;
        double xFisica, yFisica;

        if (lado < 1.0) {
            xFisica = -semiAncho - 50.0;
            yFisica = (Math.random() * 2.0 - 1.0) * semiAlto;
        } else if (lado < 2.0) {
            xFisica = semiAncho + 50.0;
            yFisica = (Math.random() * 2.0 - 1.0) * semiAlto;
        } else if (lado < 3.0) {
            xFisica = (Math.random() * 2.0 - 1.0) * semiAncho;
            yFisica = semiAlto + 50.0;
        } else {
            xFisica = (Math.random() * 2.0 - 1.0) * semiAncho;
            yFisica = -semiAlto - 50.0;
        }

        Vector2D posInicio = new Vector2D(xFisica, yFisica);
        Vector2D dir = (objetivo != null)
                ? objetivo.getPosicion().restar(posInicio)
                : Vector2D.cero().restar(posInicio);

        double velMag = ConfiguracionSimulacion.VELOCIDAD_METEORITO_PX_TICK;
        Vector2D vel = (dir.magnitud() > 1e-3 ? dir.normalizar() : new Vector2D(1, 0)).multiplicar(velMag);
        double ticksVida = Math.max(1200.0, (semiAncho + semiAlto) * 3.0 / Math.max(1.0, velMag));
        double masaEscalada = calculadorAmenaza.masaPorProgreso(factorProgreso);

        Meteorito meteorito = new Meteorito(
                generadorNombre.apply("Meteorito"),
                masaEscalada,
                posInicio.x(), posInicio.y(),
                vel, ticksVida, false, objetivo
        );
        meteorito.setFactorProgresoOrigen(factorProgreso);
        return meteorito;
    }

    public Meteorito crearLanzadoPorJugador(double x, double y, Vector2D vel) {
        Vector2D velInvertida = new Vector2D(vel.x(), -vel.y());
        return new Meteorito(
                generadorNombre.apply("Meteorito"),
                ConfiguracionColisiones.MASA_MINIMA_METEORITO,
                x, y, velInvertida, 2000, true
        );
    }

    public Meteorito crearFragmento(double xFisica, double yFisica, Vector2D velocidadBase, double masaFragmento) {
        double anguloExtra = (Math.random() - 0.5) * Math.PI;
        double velBase = velocidadBase != null ? velocidadBase.magnitud() : 0.0;
        double velEyeccion = ConfiguracionSimulacion.VELOCIDAD_EYECCION_FRAGMENTOS * (0.5 + Math.random());

        double anguloFinal;
        if (velBase > 1e-3 && velocidadBase != null) {
            double anguloBase = Math.atan2(velocidadBase.y(), velocidadBase.x());
            anguloFinal = anguloBase + anguloExtra;
        } else {
            anguloFinal = Math.random() * 2.0 * Math.PI;
        }

        Vector2D velFragmento = new Vector2D(
                Math.cos(anguloFinal) * (velBase * 0.3 + velEyeccion),
                Math.sin(anguloFinal) * (velBase * 0.3 + velEyeccion)
        );

        return new Meteorito(
                generadorNombre.apply("Escombro"),
                masaFragmento,
                xFisica, yFisica,
                velFragmento,
                250,
                false
        );
    }
}
