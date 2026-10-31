package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
/**
 * SRP: Centraliza el cálculo de velocidades y órbitas keplerianas circulares e ideales.
 */
public class CalculadorOrbital {

    private static final CalculadorOrbital INSTANCIA_POR_DEFECTO = new CalculadorOrbital();

    public CalculadorOrbital() {}

    public static CalculadorOrbital getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public double rapidezOrbital(double masaCentral, double distancia, double G) {
        if (distancia <= 0.001 || masaCentral <= 0.0) return 0.0;
        return Math.sqrt(G * masaCentral / distancia);
    }

    public double rapidezOrbital(double masaCentral, double distancia) {
        return rapidezOrbital(masaCentral, distancia, ConstantesFisicas.G_ESCALADO);
    }

    public Vector2D velocidadOrbitalVector(CuerpoCeleste central, Vector2D posOrbitando, boolean sentidoHorario) {
        if (central == null || posOrbitando == null) return Vector2D.cero();

        Vector2D r = posOrbitando.restar(central.getPosicion());
        double dist = r.magnitud();
        if (dist <= 0.001) return Vector2D.cero();

        Vector2D uRadial = r.dividir(dist);
        Vector2D uTangencial = sentidoHorario
                ? new Vector2D(uRadial.y(), -uRadial.x())
                : new Vector2D(-uRadial.y(), uRadial.x());

        double vOrbital = rapidezOrbital(central.getMasa(), dist);
        return central.getVelocidad().sumar(uTangencial.multiplicar(vOrbital));
    }

    public static double calcularRapidezOrbital(double masaCentral, double distancia, double G) {
        return INSTANCIA_POR_DEFECTO.rapidezOrbital(masaCentral, distancia, G);
    }

    public static double calcularRapidezOrbital(double masaCentral, double distancia) {
        return INSTANCIA_POR_DEFECTO.rapidezOrbital(masaCentral, distancia);
    }

    public static Vector2D calcularVelocidadOrbitalVector(CuerpoCeleste central, Vector2D posOrbitando, boolean sentidoHorario) {
        return INSTANCIA_POR_DEFECTO.velocidadOrbitalVector(central, posOrbitando, sentidoHorario);
    }
}
