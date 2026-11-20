package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionColisiones;

/**
 * SRP: Calcula límites de radio orbital, verificación de ruptura por masa y vectores tangenciales.
 */
public class LimitesOrbitales {

    public boolean debeRomperOrbitaPorMasa(CuerpoCeleste a, CuerpoCeleste b) {
        return esLunaSuperadaPorPlaneta(a, b) || esLunaSuperadaPorPlaneta(b, a);
    }

    private boolean esLunaSuperadaPorPlaneta(CuerpoCeleste posibleLuna, CuerpoCeleste posiblePlaneta) {
        if (posibleLuna == null || posiblePlaneta == null) return false;
        if (posibleLuna.getTipoCuerpo() != TipoCuerpo.LUNA) return false;
        TipoCuerpo tipoPlaneta = posiblePlaneta.getTipoCuerpo();
        return tipoPlaneta != null && tipoPlaneta.esPlaneta()
                && posiblePlaneta.getMasa() >= ConfiguracionColisiones.RATIO_ABSORCION_PLANETA_LUNA * posibleLuna.getMasa();
    }

    public double calcularRadioMinimo(CuerpoCeleste orbitando, CuerpoCeleste anfitrion) {
        if (orbitando == null || anfitrion == null) return 10.0;
        boolean enPlaneta = esPlaneta(anfitrion);
        if (enPlaneta && orbitando.getTipoCuerpo() == TipoCuerpo.SATELITE) {
            return anfitrion.getRadio() + orbitando.getRadio() + 6.0;
        }
        if (enPlaneta && orbitando.getTipoCuerpo() == TipoCuerpo.LUNA) {
            return Math.max(60.0, anfitrion.getRadio() + orbitando.getRadio() + 35.0);
        }
        return anfitrion.getRadio() + orbitando.getRadio() + 5.0;
    }

    public double calcularRadioMaximo(CuerpoCeleste orbitando, CuerpoCeleste anfitrion) {
        if (orbitando == null || !esPlaneta(anfitrion)) {
            return Double.POSITIVE_INFINITY;
        }
        if (orbitando.getTipoCuerpo() == TipoCuerpo.SATELITE) {
            double radioMinLuna = Math.max(60.0, anfitrion.getRadio() + 9.0 + 35.0);
            double maxPorLuna = radioMinLuna - 9.0 - orbitando.getRadio() - 6.0;
            return Math.max(calcularRadioMinimo(orbitando, anfitrion) + 4.0,
                    Math.min(anfitrion.getRadio() + orbitando.getRadio() + 14.0, maxPorLuna));
        }
        if (orbitando.getTipoCuerpo() == TipoCuerpo.LUNA) {
            return Math.max(calcularRadioMinimo(orbitando, anfitrion) + 25.0,
                    anfitrion.getRadio() + orbitando.getRadio() + 65.0);
        }
        return Double.POSITIVE_INFINITY;
    }

    public double acotarRadio(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double radioDeseado) {
        double minOrb = calcularRadioMinimo(orbitando, anfitrion);
        double maxOrb = calcularRadioMaximo(orbitando, anfitrion);
        double base = radioDeseado > 0 ? radioDeseado : minOrb;
        return Math.max(minOrb, Math.min(maxOrb, base));
    }

    public Vector2D calcularUnitarioTangencial(Vector2D r, Vector2D uRadial, Vector2D vRel) {
        double cross = r.x() * vRel.y() - r.y() * vRel.x();
        boolean horario = cross < -1e-4;
        return horario
                ? new Vector2D(uRadial.y(), -uRadial.x())
                : new Vector2D(-uRadial.y(), uRadial.x());
    }

    public boolean esPlaneta(CuerpoCeleste cuerpo) {
        return cuerpo != null && cuerpo.getTipoCuerpo() != null && cuerpo.getTipoCuerpo().esPlaneta();
    }

    public boolean esMenorAlrededorDePlaneta(CuerpoCeleste orbitando, CuerpoCeleste anfitrion) {
        return orbitando != null && orbitando.getTipoCuerpo() != null
                && orbitando.getTipoCuerpo().puedeOrbitarPlanetas()
                && esPlaneta(anfitrion);
    }
}
