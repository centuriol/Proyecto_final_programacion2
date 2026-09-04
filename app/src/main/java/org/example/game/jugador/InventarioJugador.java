package org.example.game.jugador;

import org.example.game.cuerpo.TipoCuerpo;
import java.util.EnumMap;
import java.util.Map;

/**
 * Inventario del jugador: almacena recursos y gestiona costos de creación.
 */
public class InventarioJugador {
    private final Map<TipoRecurso, Recurso> recursos = new EnumMap<>(TipoRecurso.class);
    private final Map<TipoCuerpo, CostoCreacion> costos = new EnumMap<>(TipoCuerpo.class);

    public InventarioJugador() {
        // Inicializar recursos a 0
        for (TipoRecurso tr : TipoRecurso.values()) {
            recursos.put(tr, new Recurso(tr));
        }

        // Definir costos base de creación (se ajustan con balanceo)
        inicializarCostos();

        // Recursos iniciales para empezar
        darRecursosIniciales();
    }

    private void inicializarCostos() {
        // Costos en recursos base (se multiplican por factor de masa)
        costos.put(TipoCuerpo.ESTRELLA,
            new CostoCreacion(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_ESTRELLA_MINERALES)
                .agregar(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_ESTRELLA_ENERGIA)
                .agregar(TipoRecurso.CIENCIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_ESTRELLA_CIENCIA));

        costos.put(TipoCuerpo.PLANETA_ROCOSO,
            new CostoCreacion(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_MINERALES)
                .agregar(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA));

        costos.put(TipoCuerpo.PLANETA_GASEOSO,
            new CostoCreacion(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_MINERALES * 2)
                .agregar(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA * 2));

        costos.put(TipoCuerpo.PLANETA_HELADO,
            new CostoCreacion(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_MINERALES)
                .agregar(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA));

        costos.put(TipoCuerpo.LUNA,
            new CostoCreacion(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_LUNA_MINERALES)
                .agregar(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_LUNA_ENERGIA));

        costos.put(TipoCuerpo.SATELITE,
            new CostoCreacion(TipoRecurso.MINERALES, 40)
                .agregar(TipoRecurso.ENERGIA, 30)
                .agregar(TipoRecurso.CIENCIA, 10));

        costos.put(TipoCuerpo.ESCUDO_DOME,
            new CostoCreacion(TipoRecurso.ENERGIA, 150)
                .agregar(TipoRecurso.CIENCIA, 50));

        costos.put(TipoCuerpo.METEORITO,
            new CostoCreacion(TipoRecurso.MINERALES, 10)
                .agregar(TipoRecurso.ENERGIA, 5));
    }

    private void darRecursosIniciales() {
        // Recursos iniciales equilibrados para empezar el sistema
        agregarRecurso(TipoRecurso.MINERALES, 500);
        agregarRecurso(TipoRecurso.ENERGIA, 300);
        agregarRecurso(TipoRecurso.POBLACION, 100);
        agregarRecurso(TipoRecurso.CIENCIA, 50);
    }

    /**
     * Devuelve el mapa de materiales requeridos base para poner un tipo de ítem.
     */
    public static Map<TipoRecurso, Double> getCostosBase(TipoCuerpo tipo) {
        if (tipo == null) return Map.of();
        Map<TipoRecurso, Double> map = new java.util.LinkedHashMap<>();
        switch (tipo) {
            case ESTRELLA -> {
                map.put(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_ESTRELLA_MINERALES);
                map.put(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_ESTRELLA_ENERGIA);
                map.put(TipoRecurso.CIENCIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_ESTRELLA_CIENCIA);
            }
            case PLANETA_ROCOSO, PLANETA_GASEOSO, PLANETA_HELADO -> {
                map.put(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_MINERALES);
                map.put(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA);
            }
            case LUNA -> {
                map.put(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_LUNA_MINERALES);
                map.put(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.COSTO_LUNA_ENERGIA);
            }
            default -> {}
        }
        return Map.copyOf(map);
    }

    /**
     * Devuelve el desglose estático de costos requeridos para colocar un cuerpo celeste escalado por su masa.
     */
    public static Map<TipoRecurso, Double> getCostosEscalados(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> base = getCostosBase(tipo);
        if (base.isEmpty()) return Map.of();
        Map<TipoRecurso, Double> escalado = new java.util.LinkedHashMap<>();
        for (Map.Entry<TipoRecurso, Double> e : base.entrySet()) {
            escalado.put(e.getKey(), calcularCostoEscalado(e.getValue(), factorMasa));
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
        Map<TipoRecurso, Double> map = new java.util.LinkedHashMap<>();
        switch (tipo) {
            case ESTRELLA -> {
                map.put(TipoRecurso.ENERGIA, org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA);
                map.put(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_ESTRELLA_MINERALES);
            }
            case PLANETA_ROCOSO, PLANETA_GASEOSO, PLANETA_HELADO -> {
                map.put(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_PLANETA_MINERALES);
                map.put(TipoRecurso.CIENCIA, org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_PLANETA_CIENCIA);
                map.put(TipoRecurso.POBLACION, org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_PLANETA_POBLACION);
            }
            case LUNA -> {
                map.put(TipoRecurso.MINERALES, org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES);
            }
            default -> {}
        }
        return Map.copyOf(map);
    }

    /**
     * Devuelve el mapa de materiales que genera por tick un tipo de ítem escalado por su masa.
     */
    public static Map<TipoRecurso, Double> getProduccionEscaladaPorTick(TipoCuerpo tipo, double factorMasa) {
        Map<TipoRecurso, Double> base = getProduccionBasePorTick(tipo);
        if (base.isEmpty()) return Map.of();
        double mult = Math.max(0.1, factorMasa);
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
        return costoBase * Math.pow(Math.max(0, factorMasa), 1.5);
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
        CostoCreacion costo = costos.get(tipo);
        if (costo == null) return Map.of();

        Map<TipoRecurso, Double> total = new EnumMap<>(TipoRecurso.class);
        for (Map.Entry<TipoRecurso, Double> entry : costo.getCostos().entrySet()) {
            total.put(entry.getKey(), calcularCostoEscalado(entry.getValue(), factorMasa));
        }
        return Map.copyOf(total);
    }

    public boolean puedeCrear(TipoCuerpo tipo, double factorMasa) {
        CostoCreacion costo = costos.get(tipo);
        if (costo == null) return false;

        for (Map.Entry<TipoRecurso, Double> entry : costo.getCostos().entrySet()) {
            TipoRecurso tr = entry.getKey();
            double cantRequerida = calcularCostoEscalado(entry.getValue(), factorMasa);
            if (!recursos.get(tr).puedeGastar(cantRequerida)) {
                return false;
            }
        }
        return true;
    }

    public boolean gastarParaCrear(TipoCuerpo tipo, double factorMasa) {
        CostoCreacion costo = costos.get(tipo);
        if (costo == null || !puedeCrear(tipo, factorMasa)) return false;

        for (Map.Entry<TipoRecurso, Double> entry : costo.getCostos().entrySet()) {
            recursos.get(entry.getKey()).gastar(calcularCostoEscalado(entry.getValue(), factorMasa));
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
        return costos.get(tipo);
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