package org.example.game.civilizacion;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.Planeta; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.motor.CalculadorOrbital; import org.example.game.simulacion.ConfiguracionSimulacion; import java.util.List;
/**
 * SRP: Evalúa las condiciones astrofísicas de habitabilidad alrededor de un planeta.
 */
public final class EvaluadorHabitabilidad {

    private EvaluadorHabitabilidad() {}

    public static CondicionesHabitabilidad evaluar(Planeta planeta, List<CuerpoCeleste> cuerpos) {
        if (planeta == null || cuerpos == null) return CondicionesHabitabilidad.porDefecto();

        CuerpoCeleste estrellaMasCercana = null;
        double distEstrellaMin = Double.MAX_VALUE;

        CuerpoCeleste agujeroMasCercano = null;
        double distAgujeroMin = Double.MAX_VALUE;

        for (CuerpoCeleste otro : cuerpos) {
            if (otro == planeta) continue;

            double dist = planeta.getPosicion().distanciaA(otro.getPosicion());
            if (otro.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
                if (dist < distEstrellaMin) {
                    distEstrellaMin = dist;
                    estrellaMasCercana = otro;
                }
            } else if (otro.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO ||
                       otro.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
                if (dist < distAgujeroMin) {
                    distAgujeroMin = dist;
                    agujeroMasCercano = otro;
                }
            }
        }

        double distanciaIdeal = -1;
        double ratioZonaHabitable = 0;
        boolean enZonaHabitable = false;
        double flujoEnergetico = 0;

        if (estrellaMasCercana != null) {
            double masaSol = 1.989e30;
            double factorLuminosidad = Math.pow(Math.max(0.0, estrellaMasCercana.getMasa()) / masaSol, 3.5);
            distanciaIdeal = Math.max(0.001, Math.sqrt(factorLuminosidad) * 150);
            ratioZonaHabitable = distEstrellaMin / distanciaIdeal;
            enZonaHabitable = ratioZonaHabitable >= ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN
                    && ratioZonaHabitable <= ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX;
            double dist2 = Math.max(0.001, distEstrellaMin * distEstrellaMin);
            flujoEnergetico = factorLuminosidad / dist2;
        }

        double distanciaAgujero = -1;
        boolean bajoAmenaza = false;
        double intensidadAmenaza = 0;

        if (agujeroMasCercano != null) {
            distanciaAgujero = distAgujeroMin;
            bajoAmenaza = distAgujeroMin < ConfiguracionSimulacion.AMENAZA_AGUJERO_NEGRO_RADIO;
            intensidadAmenaza = 1.0 - (distAgujeroMin / ConfiguracionSimulacion.AMENAZA_AGUJERO_NEGRO_RADIO);
        }

        double excentricidad = calcularExcentricidadAproximada(planeta, estrellaMasCercana);

        return new CondicionesHabitabilidad(
                distEstrellaMin,
                distanciaIdeal,
                ratioZonaHabitable,
                enZonaHabitable,
                flujoEnergetico,
                distanciaAgujero,
                bajoAmenaza,
                intensidadAmenaza,
                excentricidad
        );
    }

    private static double calcularExcentricidadAproximada(Planeta planeta, CuerpoCeleste estrella) {
        if (estrella == null) return 0;
        double r = planeta.getPosicion().distanciaA(estrella.getPosicion());
        if (r <= 0.001 || estrella.getMasa() <= 0) return 1.0;
        double vCircular = CalculadorOrbital.calcularRapidezOrbital(estrella.getMasa(), r);
        if (vCircular <= 0.001) return 1.0;
        double vActual = planeta.getVelocidad().magnitud();
        return Math.abs(vActual - vCircular) / vCircular;
    }
}
