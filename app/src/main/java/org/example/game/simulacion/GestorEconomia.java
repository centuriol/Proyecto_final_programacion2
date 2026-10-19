package org.example.game.simulacion;

import org.example.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;

import java.util.List;
import java.util.Map;

/**
 * Gestor del ciclo económico y producción pasiva de recursos por tick.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo del cálculo y recaudación periódica de recursos generados por cuerpos celestes.
 * - LSP: Utiliza las propiedades y componentes de civilización polimórficamente sin downcasts.
 */
public class GestorEconomia {

    public void actualizarEconomia(List<CuerpoCeleste> cuerpos, InventarioJugador inventario) {
        if (cuerpos == null || inventario == null) return;

        for (CuerpoCeleste c : cuerpos) {
            if (c == null || !c.esSimulado()) continue;
            TipoCuerpo tipo = c.getTipoCuerpo();
            if (tipo == null) continue;

            // Planetas o cuerpos transformados por evento no generan recursos
            if (c.isTransformadoPorEvento()) continue;

            double factorMasa = 1.0;
            if (tipo.masaBase > 0) {
                factorMasa = Math.max(0.1, c.getMasa() / tipo.masaBase);
            }

            Map<TipoRecurso, Double> produccionEscalada = InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa);
            if (produccionEscalada.isEmpty()) continue;

            double factorCiv = 1.0;
            if (c.tieneCivilizacion() && c.getCivilizacion() != null) {
                factorCiv = c.getCivilizacion().calcularMultiplicadorProduccion();
            }

            for (Map.Entry<TipoRecurso, Double> entry : produccionEscalada.entrySet()) {
                inventario.agregarRecurso(entry.getKey(), entry.getValue() * factorCiv);
            }
        }
    }
}
