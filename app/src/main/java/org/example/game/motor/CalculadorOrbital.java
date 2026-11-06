package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
/**
 * SRP: Centraliza el cálculo de velocidades y órbitas keplerianas circulares e ideales.
 */
public class CalculadorOrbital {

    public static final double[] RADIOS_RANURAS_SOL = { 160.0, 320.0, 480.0, 640.0 };
    private static final CalculadorOrbital INSTANCIA_POR_DEFECTO = new CalculadorOrbital();

    public CalculadorOrbital() {}

    public static CalculadorOrbital getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public static double ajustarARanuraSolar(double distancia) {
        double mejorRadio = RADIOS_RANURAS_SOL[0];
        double menorDif = Math.abs(distancia - mejorRadio);
        for (int i = 1; i < RADIOS_RANURAS_SOL.length; i++) {
            double dif = Math.abs(distancia - RADIOS_RANURAS_SOL[i]);
            if (dif < menorDif) {
                menorDif = dif;
                mejorRadio = RADIOS_RANURAS_SOL[i];
            }
        }
        return mejorRadio;
    }

    public static Vector2D ajustarPosicionARanuraSolar(Vector2D posPlaneta, Vector2D posEstrella) {
        if (posPlaneta == null || posEstrella == null) return posPlaneta;
        Vector2D r = posPlaneta.restar(posEstrella);
        double dist = r.magnitud();
        double rRanura = ajustarARanuraSolar(dist);
        Vector2D uRadial = dist > 0.001 ? r.dividir(dist) : new Vector2D(1.0, 0.0);
        return posEstrella.sumar(uRadial.multiplicar(rRanura));
    }

    public double rapidezOrbital(double masaCentral, double distancia, double G) {
        if (distancia <= 0.001 || masaCentral <= 0.0) return 0.0;
        if (masaCentral >= 1e29 && masaCentral <= TipoCuerpo.ESTRELLA.getMasaMax()) {
            double rRanura = ajustarARanuraSolar(distancia);
            return Math.sqrt(G * TipoCuerpo.ESTRELLA.masaBase / rRanura);
        }
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
