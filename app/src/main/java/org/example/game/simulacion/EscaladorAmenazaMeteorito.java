package org.example.game.simulacion;

import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;

/**
 * SRP: Calcula de forma configurable el factor de progreso efectivo del jugador y escala
 * proporcionalmente la masa, el radio visual y el porcentaje de daño de los meteoritos.
 */
public class EscaladorAmenazaMeteorito implements CalculadorAmenazaMeteorito {

    public EscaladorAmenazaMeteorito() {}

    @Override
    public double factorProgreso(SistemaProgresion progresion) {
        if (progresion == null) {
            return ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL;
        }
        int nivelMaxArbol = 0;
        for (RamaProgresion rama : RamaProgresion.values()) {
            nivelMaxArbol = Math.max(nivelMaxArbol, progresion.getNivel(rama));
        }
        int idx = Math.min(nivelMaxArbol, ConfiguracionSimulacion.ESCALONES_MASA_MAX.length - 1);
        double factorPorArbol = ConfiguracionSimulacion.ESCALONES_MASA_MAX[idx];
        return Math.max(progresion.getLimiteMasaActual(), factorPorArbol);
    }

    @Override
    public double fraccionDano(double factorProgreso) {
        return interpolarPorProgreso(
                factorProgreso,
                ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION,
                ConfiguracionColisiones.DANO_METEORITO_PROGRESO_BAJO_MAX_FRACCION,
                ConfiguracionColisiones.DANO_METEORITO_PROGRESO_ALTO_FRACCION
        );
    }

    @Override
    public double radioPorProgreso(double factorProgreso) {
        return interpolarPorProgreso(
                factorProgreso,
                ConfiguracionColisiones.RADIO_MINIMO_METEORITO,
                ConfiguracionColisiones.RADIO_METEORITO_PROGRESO_BAJO,
                ConfiguracionColisiones.RADIO_MAXIMO_METEORITO
        );
    }

    @Override
    public double masaPorProgreso(double factorProgreso) {
        double logMasa = interpolarPorProgreso(
                factorProgreso,
                Math.log10(ConfiguracionColisiones.MASA_MINIMA_METEORITO),
                Math.log10(ConfiguracionColisiones.MASA_METEORITO_PROGRESO_BAJO),
                Math.log10(ConfiguracionColisiones.MASA_MAXIMA_METEORITO)
        );
        return Math.min(ConfiguracionColisiones.MASA_MAXIMA_METEORITO, Math.pow(10.0, logMasa));
    }

    @Override
    public double radioDesdeMasa(double masa) {
        if (!Double.isFinite(masa) || masa <= ConfiguracionColisiones.MASA_MINIMA_METEORITO) {
            return ConfiguracionColisiones.RADIO_MINIMO_METEORITO;
        }
        double masaAcotada = Math.min(masa, ConfiguracionColisiones.MASA_MAXIMA_METEORITO);
        double logMasa = Math.log10(masaAcotada);
        double logMin = Math.log10(ConfiguracionColisiones.MASA_MINIMA_METEORITO);
        double logBajo = Math.log10(ConfiguracionColisiones.MASA_METEORITO_PROGRESO_BAJO);
        double logMax = Math.log10(ConfiguracionColisiones.MASA_MAXIMA_METEORITO);
        if (logMasa <= logBajo) {
            double t = (logMasa - logMin) / Math.max(1e-9, logBajo - logMin);
            return ConfiguracionColisiones.RADIO_MINIMO_METEORITO
                    + t * (ConfiguracionColisiones.RADIO_METEORITO_PROGRESO_BAJO - ConfiguracionColisiones.RADIO_MINIMO_METEORITO);
        }
        double t = (logMasa - logBajo) / Math.max(1e-9, logMax - logBajo);
        return ConfiguracionColisiones.RADIO_METEORITO_PROGRESO_BAJO
                + t * (ConfiguracionColisiones.RADIO_MAXIMO_METEORITO - ConfiguracionColisiones.RADIO_METEORITO_PROGRESO_BAJO);
    }

    private double interpolarPorProgreso(double progreso, double valInicial, double valBajo, double valAlto) {
        double pIni = ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL;
        double pBajo = ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_BAJO;
        double pAlto = ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO;
        if (!Double.isFinite(progreso) || progreso <= pIni) {
            return valInicial;
        }
        if (progreso <= pBajo) {
            double t = (progreso - pIni) / Math.max(1e-9, pBajo - pIni);
            return valInicial + t * (valBajo - valInicial);
        }
        if (progreso >= pAlto) {
            return valAlto;
        }
        double t = (progreso - pBajo) / Math.max(1e-9, pAlto - pBajo);
        return valBajo + t * (valAlto - valBajo);
    }
}
