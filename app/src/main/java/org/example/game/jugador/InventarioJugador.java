package org.example.game.jugador;
import org.example.game.cuerpo.TipoCuerpo; import java.util.EnumMap; import java.util.Map;
/**
 * Inventario del jugador: almacena recursos y gestiona costos de creación.
 */
public class InventarioJugador {
    private final Map<TipoRecurso, Recurso> recursos = new EnumMap<>(TipoRecurso.class);

    public InventarioJugador() {
        // Inicializar recursos a 0
        for (TipoRecurso tr : TipoRecurso.values()) {
            recursos.put(tr, new Recurso(tr));
        }

        // Recursos iniciales para empezar
        darRecursosIniciales();
    }

    private void darRecursosIniciales() {
        // Recursos iniciales exactos para colocar unicamente 1 Sol y 1 Planeta de Agua al inicio (masa inicial)
        double factorInicial = org.example.game.simulacion.ConfiguracionEconomia.FACTOR_MASA_INICIAL;
        double minInicial = calcularCostoEscalado(org.example.game.simulacion.ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES, factorInicial)
                + calcularCostoEscalado(org.example.game.simulacion.ConfiguracionEconomia.COSTO_PLANETA_AGUA_MINERALES, factorInicial);
        double eneInicial = calcularCostoEscalado(org.example.game.simulacion.ConfiguracionEconomia.COSTO_ESTRELLA_ENERGIA, factorInicial)
                + calcularCostoEscalado(org.example.game.simulacion.ConfiguracionEconomia.COSTO_PLANETA_AGUA_ENERGIA, factorInicial);

        agregarRecurso(TipoRecurso.MINERALES, minInicial);
        agregarRecurso(TipoRecurso.ENERGIA, eneInicial);
        agregarRecurso(TipoRecurso.POBLACION, 0.0);
        agregarRecurso(TipoRecurso.CIENCIA, 0.0);
    }

    public void reiniciar() {
        for (Recurso r : recursos.values()) {
            r.setCantidad(0.0);
        }
        darRecursosIniciales();
    }

    /**
     * Devuelve el mapa de materiales requeridos base para poner un tipo de ítem.
     */
    public static Map<TipoRecurso, Double> getCostosBase(TipoCuerpo tipo) {
        if (tipo == null) return Map.of();
        return tipo.getCostosBase();
    }

    /**
     * Devuelve el desglose estático de costos requeridos para colocar un cuerpo celeste escalado por su masa.
     */
    public static Map<TipoRecurso, Double> getCostosEscalados(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> base = getCostosBase(tipo);
        if (base.isEmpty()) return Map.of();
        double factorEfectivo = (tipo == TipoCuerpo.SATELITE)
                ? org.example.game.simulacion.ConfiguracionEconomia.FACTOR_MASA_SATELITE_FIJO
                : factorMasa;
        Map<TipoRecurso, Double> escalado = new java.util.LinkedHashMap<>();
        for (Map.Entry<TipoRecurso, Double> e : base.entrySet()) {
            escalado.put(e.getKey(), calcularCostoEscalado(e.getValue(), factorEfectivo));
        }
        return Map.copyOf(escalado);
    }

    /**
     * Devuelve el mapa de materiales que genera por tick un tipo de ítem.
     * Reglas:
     * - Sol: Energia y Minerales
     * - Planetas: Minerales, Ciencia y Poblacion
     * - Luna: Minerales
     */
    public static Map<TipoRecurso, Double> getProduccionBasePorTick(TipoCuerpo tipo) {
        if (tipo == null) return Map.of();
        return tipo.getProduccionBase();
    }

    /**
     * Devuelve el mapa de materiales que genera por tick un tipo de ítem escalado por su masa.
     */
    public static Map<TipoRecurso, Double> getProduccionEscaladaPorTick(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> base = getProduccionBasePorTick(tipo);
        if (base.isEmpty()) return Map.of();
        double mult = (tipo == TipoCuerpo.SATELITE)
                ? org.example.game.simulacion.ConfiguracionEconomia.FACTOR_MASA_SATELITE_FIJO
                : Math.max(0.1, factorMasa);
        Map<TipoRecurso, Double> escalada = new java.util.LinkedHashMap<>();
        for (Map.Entry<TipoRecurso, Double> e : base.entrySet()) {
            escalada.put(e.getKey(), e.getValue() * mult);
        }
        return Map.copyOf(escalada);
    }

    // ===== API Pública =====

    /**
     * Calcula el costo escalado en función del factor de masa del cuerpo a crear.
     * Utiliza una curva de progresión económica superlineal (costoBase * factorMasa^1.5):
     * - Para factorMasa = 1.0 (mínimo estándar), el costo base se mantiene 1:1.
     * - Para factorMasa = 10.0 (máximo), el costo se multiplica por aprox. 31.62x (en vez de 10x lineal),
     *   premiando la planificación y haciendo los cuerpos de masa extrema un desafío económico de late-game.
     *
     * @param costoBase Costo base nominal del recurso.
     * @param factorMasa Factor multiplicador de masa elegido por el jugador.
     * @return Costo total requerido para ese recurso.
     */
    public static double calcularCostoEscalado(double costoBase, double factorMasa) {
        return costoBase * Math.pow(
                Math.max(0, factorMasa),
                org.example.game.simulacion.ConfiguracionEconomia.EXPONENTE_COSTO_MASA
        );
    }

    /**
     * Calcula el desglose completo de costos escalados para un tipo de cuerpo y un factor de masa.
     * Evita duplicar fórmulas en la interfaz de usuario y garantiza sincronización total entre lo cobrado y lo mostrado.
     *
     * @param tipo Tipo de cuerpo celeste.
     * @param factorMasa Factor multiplicador de masa.
     * @return Mapa inmutable de TipoRecurso a la cantidad requerida, o mapa vacío si no existe costo.
     */
    public Map<TipoRecurso, Double> calcularCostoTotal(TipoCuerpo tipo, double factorMasa) {
        return getCostosEscalados(tipo, factorMasa);
    }

    public boolean puedeCrear(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> costosEscalados = getCostosEscalados(tipo, factorMasa);
        if (costosEscalados.isEmpty()) return false;

        for (Map.Entry<TipoRecurso, Double> entry : costosEscalados.entrySet()) {
            TipoRecurso tr = entry.getKey();
            double cantRequerida = entry.getValue();
            if (!recursos.get(tr).puedeGastar(cantRequerida)) {
                return false;
            }
        }
        return true;
    }

    public boolean gastarParaCrear(TipoCuerpo tipo, double factorMasa) {
        if (!puedeCrear(tipo, factorMasa)) return false;

        Map<TipoRecurso, Double> costosEscalados = getCostosEscalados(tipo, factorMasa);
        for (Map.Entry<TipoRecurso, Double> entry : costosEscalados.entrySet()) {
            recursos.get(entry.getKey()).gastar(entry.getValue());
        }
        return true;
    }

    public void agregarRecurso(TipoRecurso tipo, double cantidad) {
        recursos.get(tipo).agregar(cantidad);
    }

    public boolean gastarRecurso(TipoRecurso tipo, double cantidad) {
        return recursos.get(tipo).gastar(cantidad);
    }

    public double getRecurso(TipoRecurso tipo) {
        return recursos.get(tipo).getCantidad();
    }

    public Recurso getRecursoObj(TipoRecurso tipo) {
        return recursos.get(tipo);
    }

    public CostoCreacion getCosto(TipoCuerpo tipo) {
        Map<TipoRecurso, Double> base = getCostosBase(tipo);
        if (base.isEmpty()) return null;
        CostoCreacion cc = null;
        for (Map.Entry<TipoRecurso, Double> e : base.entrySet()) {
            if (cc == null) cc = new CostoCreacion(e.getKey(), e.getValue());
            else cc.agregar(e.getKey(), e.getValue());
        }
        return cc;
    }

    public Map<TipoRecurso, Recurso> getTodosRecursos() {
        return Map.copyOf(recursos);
    }

    /** Para debug/UI: string formateado */
    public String toStringResumen() {
        StringBuilder sb = new StringBuilder();
        for (TipoRecurso tr : TipoRecurso.values()) {
            double cant = recursos.get(tr).getCantidad();
            if (cant > 0) {
                sb.append(tr.nombre).append(": ").append(String.format("%.1f", cant)).append("  ");
            }
        }
        return sb.toString();
    }

    // ===== Clase interna para costos =====

    public static class CostoCreacion {
        private final Map<TipoRecurso, Double> costos = new EnumMap<>(TipoRecurso.class);

        public CostoCreacion(TipoRecurso principal, double cantidad) {
            costos.put(principal, cantidad);
        }

        public CostoCreacion agregar(TipoRecurso tipo, double cantidad) {
            costos.put(tipo, cantidad);
            return this;
        }

        public Map<TipoRecurso, Double> getCostos() {
            return Map.copyOf(costos);
        }
    }
}
