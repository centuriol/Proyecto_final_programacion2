package org.example.game.progresion;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.EnumMap;
import java.util.Map;

/**
 * Gestor del árbol de progresión tecnológica dividido en tres ramas independientes:
 * 1. Planetas y Cuerpos celestes (Luna, Porro, Escudo, Lava).
 * 2. Límite de Masa de colocación (0.3x, 0.6x, 1.0x, 2.0x, 5.0x, 10.0x).
 * 3. Cantidad de Planetas simultáneos (3, 4, 5, 6, 8, 12).
 *
 * Principios SOLID:
 * - SRP: Controla exclusivamente el estado de investigación, costos y límites tecnológicos.
 * - OCP: Estructurado por ramas independientes ampliables sin afectar la simulación física ni la economía.
 */
public class SistemaProgresion {

    private final Map<RamaProgresion, Integer> nivelesPorRama = new EnumMap<>(RamaProgresion.class);
    private int nivelEscudo = 0; // Nivel del escudo (golpes)

    public SistemaProgresion() {
        reiniciar();
    }

    public void reiniciar() {
        nivelesPorRama.put(RamaProgresion.PLANETAS, 0);
        nivelesPorRama.put(RamaProgresion.MASA, 0);
        nivelesPorRama.put(RamaProgresion.CANTIDAD_PLANETAS, 0);
        nivelEscudo = 0;
    }

    public int getNivel(RamaProgresion rama) {
        if (rama == null) return 0;
        return nivelesPorRama.getOrDefault(rama, 0);
    }

    public int getNivelMaximo(RamaProgresion rama) {
        if (rama == null) return 0;
        return switch (rama) {
            case PLANETAS -> ElementoDesbloqueable.values().length; // 4 (Luna, Porro, Escudo, Lava)
            case MASA -> ConfiguracionSimulacion.ESCALONES_MASA_MAX.length - 1; // 5 (hasta 10.0x)
            case CANTIDAD_PLANETAS -> ConfiguracionSimulacion.ESCALONES_CANTIDAD_PLANETAS.length - 1; // 5 (hasta 12)
        };
    }

    private static final double[] COSTOS_POR_NIVEL = {0.0, 100.0, 500.0, 1500.0, 3500.0, 8000.0};

    public boolean esNivelMaximo(RamaProgresion rama) {
        return getNivel(rama) >= getNivelMaximo(rama);
    }

    /**
     * Calcula el costo en Ciencia del siguiente nivel de la rama especificada.
     * Nivel 1: 100 CIE, Nivel 2: 500 CIE, Nivel 3: 1500 CIE, Nivel 4: 3500 CIE, Nivel 5: 8000 CIE.
     */
    public double getCostoSiguiente(RamaProgresion rama) {
        if (rama == null || esNivelMaximo(rama)) {
            return -1.0;
        }
        int nivelSiguiente = getNivel(rama) + 1;
        if (nivelSiguiente < COSTOS_POR_NIVEL.length) {
            return COSTOS_POR_NIVEL[nivelSiguiente];
        }
        return COSTOS_POR_NIVEL[COSTOS_POR_NIVEL.length - 1];
    }

    public boolean puedeInvestigar(RamaProgresion rama, InventarioJugador inventario) {
        if (rama == null || inventario == null || esNivelMaximo(rama)) {
            return false;
        }
        double costo = getCostoSiguiente(rama);
        return inventario.getRecurso(TipoRecurso.CIENCIA) >= costo;
    }

    /**
     * Realiza la investigación de la tecnología seleccionada en la rama indicada.
     * Descuenta la Ciencia del inventario y avanza el nivel de la rama de forma inmediata.
     */
    public boolean investigar(RamaProgresion rama, InventarioJugador inventario) {
        if (!puedeInvestigar(rama, inventario)) {
            return false;
        }
        double costo = getCostoSiguiente(rama);
        if (inventario.gastarRecurso(TipoRecurso.CIENCIA, costo)) {
            int nuevoNivel = getNivel(rama) + 1;
            nivelesPorRama.put(rama, nuevoNivel);

            if (rama == RamaProgresion.PLANETAS && nuevoNivel >= 4) {
                nivelEscudo = Math.max(nivelEscudo, 1);
            }
            return true;
        }
        return false;
    }

    // ===== Consultas de Desbloqueo de Cuerpos =====

    public boolean estaDesbloqueado(TipoCuerpo tipo) {
        if (tipo == null) return false;
        if (tipo == TipoCuerpo.ESTRELLA || tipo == TipoCuerpo.PLANETA_AGUA) {
            return true; // Desbloqueados desde el inicio
        }
        int nivelPlanetas = getNivel(RamaProgresion.PLANETAS);
        if (tipo == TipoCuerpo.LUNA) {
            return nivelPlanetas >= 1;
        }
        if (tipo == TipoCuerpo.PLANETA_ROCOSO) {
            return nivelPlanetas >= 2;
        }
        if (tipo == TipoCuerpo.SATELITE) {
            return nivelPlanetas >= 3;
        }
        if (tipo == TipoCuerpo.PLANETA_LAVA) {
            return nivelPlanetas >= 5;
        }
        return false;
    }

    public boolean estaDesbloqueado(ElementoDesbloqueable elemento) {
        if (elemento == null) return false;
        int nivelPlanetas = getNivel(RamaProgresion.PLANETAS);
        return switch (elemento) {
            case LUNA -> nivelPlanetas >= 1;
            case PLANETA_PORRO -> nivelPlanetas >= 2;
            case SATELITE -> nivelPlanetas >= 3;
            case ESCUDO -> nivelPlanetas >= 4;
            case PLANETA_LAVA -> nivelPlanetas >= 5;
        };
    }

    public boolean tieneEscudoDesbloqueado() {
        return estaDesbloqueado(ElementoDesbloqueable.ESCUDO);
    }

    // ===== Límite de Masa =====

    public double getLimiteMasaActual() {
        int idx = Math.min(getNivel(RamaProgresion.MASA), ConfiguracionSimulacion.ESCALONES_MASA_MAX.length - 1);
        return ConfiguracionSimulacion.ESCALONES_MASA_MAX[idx];
    }

    // ===== Cantidad Máxima de Planetas =====

    public int getLimitePlanetas() {
        int idx = Math.min(getNivel(RamaProgresion.CANTIDAD_PLANETAS), ConfiguracionSimulacion.ESCALONES_CANTIDAD_PLANETAS.length - 1);
        return ConfiguracionSimulacion.ESCALONES_CANTIDAD_PLANETAS[idx];
    }

    // ===== Escudo Planetario =====

    public int getNivelEscudo() {
        return nivelEscudo;
    }

    public int getGolpesEscudo() {
        return Math.max(1, 1 + nivelEscudo);
    }

    public double getCostoSiguienteNivelEscudo() {
        if (nivelEscudo >= ConfiguracionSimulacion.NIVEL_ESCUDO_MAX) {
            return -1.0;
        }
        int idx = nivelEscudo + 1;
        return idx < COSTOS_POR_NIVEL.length ? COSTOS_POR_NIVEL[idx] : 500.0;
    }

    public boolean puedeMejorarNivelEscudo(InventarioJugador inventario) {
        if (!tieneEscudoDesbloqueado() || nivelEscudo >= ConfiguracionSimulacion.NIVEL_ESCUDO_MAX || inventario == null) {
            return false;
        }
        return inventario.getRecurso(TipoRecurso.CIENCIA) >= getCostoSiguienteNivelEscudo();
    }

    public boolean mejorarNivelEscudo(InventarioJugador inventario) {
        if (!puedeMejorarNivelEscudo(inventario)) return false;
        double costo = getCostoSiguienteNivelEscudo();
        if (inventario.gastarRecurso(TipoRecurso.CIENCIA, costo)) {
            nivelEscudo++;
            return true;
        }
        return false;
    }

    // ===== Métodos de compatibilidad retroactiva =====

    public ElementoDesbloqueable getSiguienteDesbloqueable() {
        int nivel = getNivel(RamaProgresion.PLANETAS);
        ElementoDesbloqueable[] elems = ElementoDesbloqueable.values();
        if (nivel < elems.length) {
            return elems[nivel];
        }
        return null;
    }

    public boolean puedeComprarSiguiente(InventarioJugador inventario) {
        return puedeInvestigar(RamaProgresion.PLANETAS, inventario);
    }

    public boolean comprarSiguienteDesbloqueable(InventarioJugador inventario) {
        return investigar(RamaProgresion.PLANETAS, inventario);
    }

    public double getCostoSiguienteMasa() {
        return getCostoSiguiente(RamaProgresion.MASA);
    }

    public boolean puedeSubirLimiteMasa(InventarioJugador inventario) {
        return puedeInvestigar(RamaProgresion.MASA, inventario);
    }

    public boolean subirLimiteMasa(InventarioJugador inventario) {
        return investigar(RamaProgresion.MASA, inventario);
    }

    public void desbloquearTodo() {
        nivelesPorRama.put(RamaProgresion.PLANETAS, getNivelMaximo(RamaProgresion.PLANETAS));
        nivelesPorRama.put(RamaProgresion.MASA, getNivelMaximo(RamaProgresion.MASA));
        nivelesPorRama.put(RamaProgresion.CANTIDAD_PLANETAS, getNivelMaximo(RamaProgresion.CANTIDAD_PLANETAS));
        nivelEscudo = ConfiguracionSimulacion.NIVEL_ESCUDO_MAX;
    }
}
