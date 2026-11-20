package org.example.game.jugador;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.ConfiguracionEconomia;

import java.util.EnumMap;
import java.util.Map;

/**
 * Inventario del jugador: almacena recursos y valida gastos de creación delegando cálculos a CalculadorCostosEconomia (SRP).
 */
public class InventarioJugador {

    private static final CalculadorCostosEconomia CALCULADOR = new CalculadorCostosEconomia();

    private final Map<TipoRecurso, Recurso> recursos = new EnumMap<>(TipoRecurso.class);
    private SistemaProgresion progresion;

    public InventarioJugador() {
        for (TipoRecurso tr : TipoRecurso.values()) {
            recursos.put(tr, new Recurso(tr));
        }
        darRecursosIniciales();
    }

    public void setProgresion(SistemaProgresion progresion) { this.progresion = progresion; }
    public SistemaProgresion getProgresion() { return progresion; }

    private void darRecursosIniciales() {
        double factor = ConfiguracionEconomia.FACTOR_MASA_INICIAL;
        double min = calcularCostoEscalado(ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES, factor)
                + calcularCostoEscalado(ConfiguracionEconomia.COSTO_PLANETA_AGUA_MINERALES, factor);
        double ene = calcularCostoEscalado(ConfiguracionEconomia.COSTO_ESTRELLA_ENERGIA, factor)
                + calcularCostoEscalado(ConfiguracionEconomia.COSTO_PLANETA_AGUA_ENERGIA, factor);
        agregarRecurso(TipoRecurso.MINERALES, min);
        agregarRecurso(TipoRecurso.ENERGIA, ene);
        agregarRecurso(TipoRecurso.POBLACION, 0.0);
        agregarRecurso(TipoRecurso.CIENCIA, 0.0);
    }

    public void reiniciar() {
        for (Recurso r : recursos.values()) r.setCantidad(0.0);
        darRecursosIniciales();
    }

    public static Map<TipoRecurso, Double> getCostosBase(TipoCuerpo tipo) { return tipo == null ? Map.of() : tipo.getCostosBase(); }
    public static Map<TipoRecurso, Double> getCostosEscalados(TipoCuerpo tipo, double factorMasa) { return CALCULADOR.calcularCostosEscalados(tipo, factorMasa); }
    public static Map<TipoRecurso, Double> getProduccionBasePorTick(TipoCuerpo tipo) { return tipo == null ? Map.of() : tipo.getProduccionBase(); }
    public static Map<TipoRecurso, Double> getProduccionEscaladaPorTick(TipoCuerpo tipo, double factorMasa) { return CALCULADOR.calcularProduccionEscalada(tipo, factorMasa); }
    public static double calcularCostoEscalado(double costoBase, double factorMasa) { return CALCULADOR.calcularCostoEscalado(costoBase, factorMasa); }
    public Map<TipoRecurso, Double> calcularCostoTotal(TipoCuerpo tipo, double factorMasa) { return CALCULADOR.calcularCostoTotal(tipo, factorMasa, progresion); }

    public boolean puedeCrear(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> costos = calcularCostoTotal(tipo, factorMasa);
        if (costos.isEmpty()) return false;
        for (var entry : costos.entrySet()) {
            if (!recursos.get(entry.getKey()).puedeGastar(entry.getValue())) return false;
        }
        return true;
    }

    public boolean gastarParaCrear(TipoCuerpo tipo, double factorMasa) {
        if (!puedeCrear(tipo, factorMasa)) return false;
        for (var entry : calcularCostoTotal(tipo, factorMasa).entrySet()) {
            recursos.get(entry.getKey()).gastar(entry.getValue());
        }
        return true;
    }

    public void agregarRecurso(TipoRecurso tipo, double cantidad) { recursos.get(tipo).agregar(cantidad); }
    public boolean gastarRecurso(TipoRecurso tipo, double cantidad) { return recursos.get(tipo).gastar(cantidad); }
    public double getRecurso(TipoRecurso tipo) { return recursos.get(tipo).getCantidad(); }
    public Recurso getRecursoObj(TipoRecurso tipo) { return recursos.get(tipo); }
    public Map<TipoRecurso, Recurso> getTodosRecursos() { return Map.copyOf(recursos); }

    public CostoCreacion getCosto(TipoCuerpo tipo) {
        Map<TipoRecurso, Double> base = getCostosBase(tipo);
        if (base.isEmpty()) return null;
        CostoCreacion cc = null;
        for (var e : base.entrySet()) {
            cc = (cc == null) ? new CostoCreacion(e.getKey(), e.getValue()) : cc.agregar(e.getKey(), e.getValue());
        }
        return cc;
    }

    public String toStringResumen() {
        StringBuilder sb = new StringBuilder();
        for (TipoRecurso tr : TipoRecurso.values()) {
            double cant = recursos.get(tr).getCantidad();
            if (cant > 0) sb.append(tr.nombre).append(": ").append(String.format("%.1f", cant)).append("  ");
        }
        return sb.toString();
    }

    public static class CostoCreacion {
        private final Map<TipoRecurso, Double> costos = new EnumMap<>(TipoRecurso.class);
        public CostoCreacion(TipoRecurso principal, double cantidad) { costos.put(principal, cantidad); }
        public CostoCreacion agregar(TipoRecurso tipo, double cantidad) { costos.put(tipo, cantidad); return this; }
        public Map<TipoRecurso, Double> getCostos() { return Map.copyOf(costos); }
    }
}
