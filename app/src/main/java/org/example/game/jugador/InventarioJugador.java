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
            new CostoCreacion(TipoRecurso.MASA_ESTELAR, 1000)
                .agregar(TipoRecurso.ENERGIA, 500));

        costos.put(TipoCuerpo.PLANETA_ROCOSO,
            new CostoCreacion(TipoRecurso.MATERIA_PLANETARIA, 100)
                .agregar(TipoRecurso.MINERALES, 50));

        costos.put(TipoCuerpo.PLANETA_GASEOSO,
            new CostoCreacion(TipoRecurso.MATERIA_PLANETARIA, 500)
                .agregar(TipoRecurso.MASA_ESTELAR, 100)
                .agregar(TipoRecurso.ENERGIA, 200));

        costos.put(TipoCuerpo.PLANETA_HELADO,
            new CostoCreacion(TipoRecurso.MATERIA_PLANETARIA, 50)
                .agregar(TipoRecurso.MINERALES, 20));

        costos.put(TipoCuerpo.LUNA,
            new CostoCreacion(TipoRecurso.MATERIA_PLANETARIA, 30)
                .agregar(TipoRecurso.MINERALES, 20));

        costos.put(TipoCuerpo.SATELITE,
            new CostoCreacion(TipoRecurso.MINERALES, 40)
                .agregar(TipoRecurso.ENERGIA, 30)
                .agregar(TipoRecurso.CIENCIA, 10));

        costos.put(TipoCuerpo.ESCUDO_DOME,
            new CostoCreacion(TipoRecurso.ENERGIA, 150)
                .agregar(TipoRecurso.CIENCIA, 50));

        costos.put(TipoCuerpo.AGUJERO_NEGRO,
            new CostoCreacion(TipoRecurso.MATERIA_OSCURA, 500)
                .agregar(TipoRecurso.MASA_ESTELAR, 200)
                .agregar(TipoRecurso.ENERGIA, 1000));

        costos.put(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO,
            new CostoCreacion(TipoRecurso.MATERIA_OSCURA, 5000)
                .agregar(TipoRecurso.MASA_ESTELAR, 2000)
                .agregar(TipoRecurso.ENERGIA, 5000)
                .agregar(TipoRecurso.CIENCIA, 1000));

        costos.put(TipoCuerpo.METEORITO,
            new CostoCreacion(TipoRecurso.MINERALES, 10)
                .agregar(TipoRecurso.ENERGIA, 5));

        costos.put(TipoCuerpo.NAVE_COLONIZADORA,
            new CostoCreacion(TipoRecurso.MINERALES, 200)
                .agregar(TipoRecurso.CIENCIA, 100)
                .agregar(TipoRecurso.ENERGIA, 100));
    }

    private void darRecursosIniciales() {
        // Para empezar: suficientes para 1 estrella y unos planetas
        agregarRecurso(TipoRecurso.MASA_ESTELAR, 2000);
        agregarRecurso(TipoRecurso.MATERIA_PLANETARIA, 1000);
        agregarRecurso(TipoRecurso.MINERALES, 500);
        agregarRecurso(TipoRecurso.ENERGIA, 1000);
        agregarRecurso(TipoRecurso.MATERIA_OSCURA, 100); // Escaso al inicio
        agregarRecurso(TipoRecurso.CIENCIA, 0);
    }

    // ===== API Pública =====

    public boolean puedeCrear(TipoCuerpo tipo, double factorMasa) {
        CostoCreacion costo = costos.get(tipo);
        if (costo == null) return false;

        for (Map.Entry<TipoRecurso, Double> entry : costo.getCostos().entrySet()) {
            TipoRecurso tr = entry.getKey();
            double cantRequerida = entry.getValue() * factorMasa;
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
            recursos.get(entry.getKey()).gastar(entry.getValue() * factorMasa);
        }
        return true;
    }

    public void agregarRecurso(TipoRecurso tipo, double cantidad) {
        recursos.get(tipo).agregar(cantidad);
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
                sb.append(tr.icono).append(" ").append(tr.nombre).append(": ").append(String.format("%.1f", cant)).append("  ");
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