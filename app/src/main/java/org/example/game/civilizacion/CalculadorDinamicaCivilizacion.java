package org.example.game.civilizacion;

import org.example.game.simulacion.ConfiguracionSimulacion;

/**
 * Encapsula las fórmulas demográficas y socio-tecnológicas de una civilización (SRP).
 */
public class CalculadorDinamicaCivilizacion {

    public double calcularPoblacionMaxima(double masaPlaneta, double factorCapacidadPoblacion, int nivelTecnologico) {
        double factorMasa = masaPlaneta / 5.97e24;
        return ConfiguracionSimulacion.POBLACION_MAXIMA_FACTOR * factorMasa * factorCapacidadPoblacion * (1 + nivelTecnologico * 0.2);
    }

    public double calcularFactorCrecimiento(CondicionesHabitabilidad c, int nivelTecnologico) {
        double factor = 0;

        if (c.enZonaHabitable()) {
            double centroZona = (ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN
                    + ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX) / 2;
            double desviacion = Math.abs(c.ratioZonaHabitable() - centroZona);
            double maxDesviacion = (ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX
                    - ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN) / 2;
            double bonusZona = 1.0 - (desviacion / maxDesviacion);
            factor += ConfiguracionSimulacion.CRECIMIENTO_POBLACION_BASE * (0.5 + bonusZona * 0.5);
        } else {
            factor -= ConfiguracionSimulacion.DECADENCIA_FUERA_ZONA;
        }

        if (c.bajoAmenazaAgujero()) {
            factor -= c.intensidadAmenaza() * 0.01;
        }

        factor -= c.excentricidadOrbital() * 0.005;
        factor += nivelTecnologico * 0.0001;

        return factor;
    }

    public double calcularNuevaPoblacion(double poblacionActual, double poblacionMaxima, double factorCrecimiento) {
        double poblacion = poblacionActual;
        if (factorCrecimiento > 0) {
            double saturacion = poblacionMaxima > 0 ? (1.0 - (poblacion / poblacionMaxima)) : 0.0;
            poblacion *= (1.0 + factorCrecimiento * Math.max(0.0, saturacion));
        } else {
            poblacion *= (1.0 + factorCrecimiento);
        }
        return Math.max(0, Math.min(poblacion, poblacionMaxima));
    }

    public double calcularNuevaEstabilidad(double estabilidadActual, CondicionesHabitabilidad c) {
        double objetivoEstabilidad = 1.0;
        if (c.bajoAmenazaAgujero()) objetivoEstabilidad -= c.intensidadAmenaza() * 0.5;
        objetivoEstabilidad -= c.excentricidadOrbital() * 0.3;
        if (!c.enZonaHabitable()) objetivoEstabilidad -= 0.2;

        double nuevaEstabilidad = estabilidadActual * 0.99 + objetivoEstabilidad * 0.01;
        return Math.max(0, Math.min(1, nuevaEstabilidad));
    }

    public double calcularNuevaFelicidad(double felicidadActual, double estabilidad, double poblacion, double poblacionMaxima) {
        double ratioPob = poblacionMaxima > 0 ? (poblacion / poblacionMaxima) : 0.0;
        double objetivoFelicidad = estabilidad * 0.7 + ratioPob * 0.3;
        double nuevaFelicidad = felicidadActual * 0.98 + objetivoFelicidad * 0.02;
        return Math.max(0, Math.min(1, nuevaFelicidad));
    }

    public EstadoCivilizacion determinarEstado(double poblacion, double estabilidad, double felicidad) {
        if (poblacion < 10) return EstadoCivilizacion.EXTINGUIDA;
        if (estabilidad < 0.3) return EstadoCivilizacion.EN_PELIGRO;
        if (estabilidad < 0.6 || felicidad < 0.4) return EstadoCivilizacion.LUCHANDO;
        if (estabilidad > 0.8 && felicidad > 0.7) return EstadoCivilizacion.PRÓSPERA;
        return EstadoCivilizacion.ESTABLE;
    }

    public double calcularMultiplicadorProduccion(EstadoCivilizacion estado,
                                                  double estabilidad,
                                                  double felicidad,
                                                  int nivelTecnologico,
                                                  int ticksEfectoEventoReciente) {
        if (estado == EstadoCivilizacion.EXTINGUIDA) return 0.0;
        double base = (estabilidad * 0.6) + (felicidad * 0.4);
        double techBonus = 1.0 + (nivelTecnologico * ConfiguracionSimulacion.MULTIPLICADOR_TECNOLOGIA_CIV);
        double factor = base * techBonus;
        if (ticksEfectoEventoReciente > 0) {
            factor *= (1.0 - ConfiguracionSimulacion.PENALIZACION_EVENTO_CATASTROFE);
        }
        return Math.max(0.1, factor);
    }
}
