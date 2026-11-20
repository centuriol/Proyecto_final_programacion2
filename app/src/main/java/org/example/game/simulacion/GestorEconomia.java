package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.RegistroCapacidades;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.Transformable;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.SistemaProgresion;

import java.util.List;
import java.util.Map;

/**
 * Gestor del ciclo económico y producción pasiva de recursos por tick (SRP / LSP).
 */
public class GestorEconomia implements ControladorEconomia {

    private SistemaProgresion progresion;

    public GestorEconomia() {}

    public GestorEconomia(SistemaProgresion progresion) {
        this.progresion = progresion;
    }

    public void setProgresion(SistemaProgresion progresion) { this.progresion = progresion; }

    public void actualizarEconomia(List<CuerpoCeleste> cuerpos, InventarioJugador inventario) {
        SistemaProgresion prog = progresion != null ? progresion : (inventario != null ? inventario.getProgresion() : null);
        actualizarEconomia(cuerpos, inventario, prog);
    }

    public void actualizarEconomia(List<CuerpoCeleste> cuerpos, InventarioJugador inventario, SistemaProgresion prog) {
        if (cuerpos == null || inventario == null) return;
        for (CuerpoCeleste c : cuerpos) {
            procesarProduccionCuerpo(c, inventario, prog);
        }
    }

    private void procesarProduccionCuerpo(CuerpoCeleste c, InventarioJugador inventario, SistemaProgresion prog) {
        if (c == null || !c.esSimulado() || c.getTipoCuerpo() == null || esTransformadoPorEvento(c)) return;
        TipoCuerpo tipo = c.getTipoCuerpo();
        double factorMasa = tipo.masaBase > 0 ? Math.max(0.1, c.getMasa() / tipo.masaBase) : 1.0;
        Map<TipoRecurso, Double> produccionEscalada = InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa);
        if (produccionEscalada.isEmpty()) return;

        double factorCiv = calcularFactorCivilizacion(c, tipo);
        for (Map.Entry<TipoRecurso, Double> entry : produccionEscalada.entrySet()) {
            double multProg = prog != null ? prog.multiplicadorProduccion(tipo, entry.getKey()) : 1.0;
            inventario.agregarRecurso(entry.getKey(), entry.getValue() * factorCiv * multProg);
        }
    }

    private boolean esTransformadoPorEvento(CuerpoCeleste c) {
        return RegistroCapacidades.obtenerTransformable(c)
                .map(Transformable::isTransformadoPorEvento)
                .orElse(false);
    }

    private double calcularFactorCivilizacion(CuerpoCeleste c, TipoCuerpo tipo) {
        return RegistroCapacidades.obtenerCivilizable(c)
                .filter(civ -> civ.tieneCivilizacion() && civ.getCivilizacion() != null)
                .map(civ -> {
                    double multBase = civ.getCivilizacion().calcularMultiplicadorProduccion();
                    boolean esPoblado = tipo != TipoCuerpo.PLANETA_ROCOSO || civ.getCivilizacion().getPoblacion() >= 100.0;
                    return esPoblado ? multBase * ConfiguracionEconomia.MULTIPLICADOR_PRODUCCION_PLANETA_POBLADO : multBase;
                })
                .orElse(1.0);
    }
}
