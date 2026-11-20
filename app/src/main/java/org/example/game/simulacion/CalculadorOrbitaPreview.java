package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.CalculadorOrbital;
import org.example.game.motor.Vector2D;

import java.util.List;

/**
 * SRP: Calcula el cuerpo gravitatorio anfitrión más cercano y la velocidad orbital asistida para previsualización.
 */
public class CalculadorOrbitaPreview {

    public Vector2D calcularVelocidadOrbitalAsistida(TipoCuerpo tipoColocacion, Vector2D posFisica, List<CuerpoCeleste> cuerpos) {
        CuerpoCeleste cuerpoCercano = encontrarCuerpoGravitatorioCercano(tipoColocacion, posFisica, cuerpos);
        if (cuerpoCercano == null) return Vector2D.cero();

        boolean esMenorOrbital = tipoColocacion != null && tipoColocacion.puedeOrbitarPlanetas();
        boolean cercanoEsPlaneta = cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().esPlaneta();
        if (esMenorOrbital && cercanoEsPlaneta) {
            return calcularVelocidadOrbitalMenor(posFisica, cuerpoCercano);
        }
        return CalculadorOrbital.calcularVelocidadOrbitalVector(cuerpoCercano, posFisica, false);
    }

    private Vector2D calcularVelocidadOrbitalMenor(Vector2D posFisica, CuerpoCeleste planeta) {
        Vector2D r = posFisica.restar(planeta.getPosicion());
        double dist = Math.max(1.0, r.magnitud());
        double masaBase = planeta.getTipoCuerpo().masaBase;
        double factorMasa = masaBase > 0 ? Math.max(0.2, planeta.getMasa() / masaBase) : 1.0;
        double vOrb = Math.sqrt(75.0 * factorMasa / dist);
        Vector2D uRadial = r.dividir(dist);
        Vector2D uTangencial = new Vector2D(-uRadial.y(), uRadial.x());
        return planeta.getVelocidad().sumar(uTangencial.multiplicar(vOrb));
    }

    public CuerpoCeleste encontrarCuerpoGravitatorioCercano(TipoCuerpo tipoColocacion, Vector2D pos, List<CuerpoCeleste> cuerpos) {
        if (tipoColocacion != null && tipoColocacion.puedeOrbitarPlanetas()) {
            CuerpoCeleste planetaCercano = buscarPlanetaMasCercano(pos, cuerpos);
            if (planetaCercano != null) return planetaCercano;
        }
        return buscarAtractorMasFuerte(pos, cuerpos);
    }

    private CuerpoCeleste buscarAtractorMasFuerte(Vector2D pos, List<CuerpoCeleste> cuerpos) {
        CuerpoCeleste mejor = null;
        double maxFuerza = 0.0;
        for (CuerpoCeleste c : cuerpos) {
            if (!esCandidatoGravitatorio(c)) continue;
            double dist = pos.distanciaA(c.getPosicion());
            if (dist < 10.0) continue;
            double atraccion = c.getMasa() / (dist * dist + 100.0);
            if (atraccion > maxFuerza) {
                maxFuerza = atraccion;
                mejor = c;
            }
        }
        return mejor;
    }

    private boolean esCandidatoGravitatorio(CuerpoCeleste c) {
        TipoCuerpo tipo = c.getTipoCuerpo();
        return tipo != TipoCuerpo.METEORITO
                && tipo != TipoCuerpo.SATELITE
                && tipo != TipoCuerpo.LUNA
                && c.getMasa() >= 1e22;
    }

    private CuerpoCeleste buscarPlanetaMasCercano(Vector2D pos, List<CuerpoCeleste> cuerpos) {
        CuerpoCeleste mejorPlaneta = null;
        double menorDist = Double.MAX_VALUE;
        for (CuerpoCeleste c : cuerpos) {
            if (c.getTipoCuerpo() == null || !c.getTipoCuerpo().esPlaneta()) continue;
            double dist = pos.distanciaA(c.getPosicion());
            if (dist < menorDist) {
                menorDist = dist;
                mejorPlaneta = c;
            }
        }
        return mejorPlaneta;
    }
}
