package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;

/**
 * SRP: Centraliza el cálculo de velocidades y órbitas keplerianas circulares e ideales.
 */
public final class CalculadorOrbital {

    private CalculadorOrbital() {}

    /**
     * Calcula la rapidez orbital circular v = sqrt(G * M / r).
     */
    public static double calcularRapidezOrbital(double masaCentral, double distancia, double G) {
        if (distancia <= 0.001 || masaCentral <= 0.0) return 0.0;
        return Math.sqrt(G * masaCentral / distancia);
    }

    public static double calcularRapidezOrbital(double masaCentral, double distancia) {
        return calcularRapidezOrbital(masaCentral, distancia, ConstantesFisicas.G_ESCALADO);
    }

    /**
     * Calcula la velocidad vectorial orbital circular para orbitar un cuerpo central.
     * @param central Cuerpo que genera el pozo gravitatorio principal.
     * @param posOrbitando Posición física del cuerpo orbitante.
     * @param sentidoHorario Si es true, rota en sentido horario; si es false, antihorario.
     * @return Vector2D con la velocidad orbital tangencial resultante.
     */
    public static Vector2D calcularVelocidadOrbitalVector(CuerpoCeleste central, Vector2D posOrbitando, boolean sentidoHorario) {
        if (central == null || posOrbitando == null) return Vector2D.cero();

        Vector2D r = posOrbitando.restar(central.getPosicion());
        double dist = r.magnitud();
        if (dist <= 0.001) return Vector2D.cero();

        Vector2D uRadial = r.dividir(dist);
        Vector2D uTangencial = sentidoHorario
                ? new Vector2D(uRadial.y(), -uRadial.x())
                : new Vector2D(-uRadial.y(), uRadial.x());

        double vOrbital = calcularRapidezOrbital(central.getMasa(), dist);
        return central.getVelocidad().sumar(uTangencial.multiplicar(vOrbital));
    }
}
