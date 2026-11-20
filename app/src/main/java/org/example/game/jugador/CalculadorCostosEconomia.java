package org.example.game.jugador;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.ConfiguracionEconomia;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SRP: Calcula costos escalados por masa, bonificaciones de progresión y tasas de producción por tipo de cuerpo.
 */
public class CalculadorCostosEconomia {

    public double calcularCostoEscalado(double costoBase, double factorMasa) {
        return costoBase * Math.pow(Math.max(0, factorMasa), ConfiguracionEconomia.EXPONENTE_COSTO_MASA);
    }

    public Map<TipoRecurso, Double> calcularCostosEscalados(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> base = tipo != null ? tipo.getCostosBase() : Map.of();
        if (base.isEmpty()) return Map.of();
        double factorEfectivo = tipo.ajustarFactorMasaEfectivo(factorMasa);
        Map<TipoRecurso, Double> escalado = new LinkedHashMap<>();
        for (Map.Entry<TipoRecurso, Double> e : base.entrySet()) {
            escalado.put(e.getKey(), calcularCostoEscalado(e.getValue(), factorEfectivo));
        }
        return Map.copyOf(escalado);
    }

    public Map<TipoRecurso, Double> calcularCostoTotal(TipoCuerpo tipo, double factorMasa, SistemaProgresion progresion) {
        Map<TipoRecurso, Double> baseEscalada = calcularCostosEscalados(tipo, factorMasa);
        if (baseEscalada.isEmpty() || progresion == null) return baseEscalada;
        double multCosto = progresion.multiplicadorCosto(tipo);
        if (Math.abs(multCosto - 1.0) < 1e-9) return baseEscalada;
        Map<TipoRecurso, Double> ajustado = new LinkedHashMap<>();
        for (Map.Entry<TipoRecurso, Double> e : baseEscalada.entrySet()) {
            ajustado.put(e.getKey(), e.getValue() * multCosto);
        }
        return Map.copyOf(ajustado);
    }

    public Map<TipoRecurso, Double> calcularProduccionEscalada(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> base = tipo != null ? tipo.getProduccionBase() : Map.of();
        if (base.isEmpty()) return Map.of();
        double mult = Math.max(0.1, tipo.ajustarFactorMasaEfectivo(factorMasa));
        Map<TipoRecurso, Double> escalada = new LinkedHashMap<>();
        for (Map.Entry<TipoRecurso, Double> e : base.entrySet()) {
            escalada.put(e.getKey(), e.getValue() * mult);
        }
        return Map.copyOf(escalada);
    }
}
