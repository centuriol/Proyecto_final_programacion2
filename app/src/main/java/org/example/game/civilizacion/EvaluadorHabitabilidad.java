package org.example.game.civilizacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.CalculadorOrbital;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.List;

/**
 * Evalua las condiciones astrofisicas de habitabilidad alrededor de un planeta.
 *
 * Principios SOLID:
 * - SRP: Descompone el analisis de habitabilidad en evaluadores especificos (estelar, agujero negro, orbita).
 */
public final class EvaluadorHabitabilidad {

    private record AstrosRelevantes(CuerpoCeleste estrella, double distEstrella, CuerpoCeleste agujero, double distAgujero) {}
    private record EvaluacionEstelar(double distIdeal, double ratioZona, boolean enZona, double flujo) {}
    private record EvaluacionAgujero(double distancia, boolean bajoAmenaza, double intensidad) {}

    private EvaluadorHabitabilidad() {}

    public static CondicionesHabitabilidad evaluar(Planeta planeta, List<CuerpoCeleste> cuerpos) {
        if (planeta == null || cuerpos == null) {
            return CondicionesHabitabilidad.porDefecto();
        }

        AstrosRelevantes astros = buscarAstrosRelevantes(planeta, cuerpos);
        EvaluacionEstelar estelar = calcularCondicionesEstelares(astros.estrella(), astros.distEstrella());
        EvaluacionAgujero agujero = calcularAmenazaAgujero(astros.agujero(), astros.distAgujero());
        double excentricidad = calcularExcentricidadAproximada(planeta, astros.estrella());

        return new CondicionesHabitabilidad(
                astros.distEstrella(),
                estelar.distIdeal(),
                estelar.ratioZona(),
                estelar.enZona(),
                estelar.flujo(),
                agujero.distancia(),
                agujero.bajoAmenaza(),
                agujero.intensidad(),
                excentricidad
        );
    }

    private static AstrosRelevantes buscarAstrosRelevantes(Planeta planeta, List<CuerpoCeleste> cuerpos) {
        CuerpoCeleste estrellaMasCercana = null;
        double distEstrellaMin = Double.MAX_VALUE;
        CuerpoCeleste agujeroMasCercano = null;
        double distAgujeroMin = Double.MAX_VALUE;

        for (CuerpoCeleste otro : cuerpos) {
            if (otro == planeta) {
                continue;
            }
            double dist = planeta.getPosicion().distanciaA(otro.getPosicion());
            if (otro.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
                if (dist < distEstrellaMin) {
                    distEstrellaMin = dist;
                    estrellaMasCercana = otro;
                }
            } else if (otro.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO || otro.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
                if (dist < distAgujeroMin) {
                    distAgujeroMin = dist;
                    agujeroMasCercano = otro;
                }
            }
        }
        return new AstrosRelevantes(estrellaMasCercana, distEstrellaMin, agujeroMasCercano, distAgujeroMin);
    }

    private static EvaluacionEstelar calcularCondicionesEstelares(CuerpoCeleste estrella, double distEstrella) {
        if (estrella == null) {
            return new EvaluacionEstelar(-1, 0, false, 0);
        }
        double masaSol = 1.989e30;
        double factorLuminosidad = Math.pow(Math.max(0.0, estrella.getMasa()) / masaSol, 3.5);
        double distIdeal = Math.max(0.001, Math.sqrt(factorLuminosidad) * 150);
        double ratioZona = distEstrella / distIdeal;
        boolean enZona = ratioZona >= ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN
                && ratioZona <= ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX;
        double dist2 = Math.max(0.001, distEstrella * distEstrella);
        double flujo = factorLuminosidad / dist2;
        return new EvaluacionEstelar(distIdeal, ratioZona, enZona, flujo);
    }

    private static EvaluacionAgujero calcularAmenazaAgujero(CuerpoCeleste agujero, double distAgujero) {
        if (agujero == null) {
            return new EvaluacionAgujero(-1, false, 0);
        }
        boolean bajoAmenaza = distAgujero < ConfiguracionSimulacion.AMENAZA_AGUJERO_NEGRO_RADIO;
        double intensidad = 1.0 - (distAgujero / ConfiguracionSimulacion.AMENAZA_AGUJERO_NEGRO_RADIO);
        return new EvaluacionAgujero(distAgujero, bajoAmenaza, intensidad);
    }

    private static double calcularExcentricidadAproximada(Planeta planeta, CuerpoCeleste estrella) {
        if (estrella == null) {
            return 0;
        }
        double r = planeta.getPosicion().distanciaA(estrella.getPosicion());
        if (r <= 0.001 || estrella.getMasa() <= 0) {
            return 1.0;
        }
        double vCircular = CalculadorOrbital.calcularRapidezOrbital(estrella.getMasa(), r);
        if (vCircular <= 0.001) {
            return 1.0;
        }
        double vActual = planeta.getVelocidad().magnitud();
        return Math.abs(vActual - vCircular) / vCircular;
    }
}
