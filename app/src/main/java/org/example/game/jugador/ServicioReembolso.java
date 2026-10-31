package org.example.game.jugador;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.simulacion.MensajeEvento;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
/**
 * Servicio de negocio para el cálculo de reembolsos y desmantelamiento de cuerpos celestes.
 *
 * Principios SOLID:
 * - SRP: Encapsula la lógica económica de reciclaje/reembolso sin acoplarse a la interfaz gráfica.
 * - LSP: Interactúa polimórficamente con CuerpoCeleste sin comprobaciones tipo instanceof.
 */
public class ServicioReembolso {

    public static final double FACTOR_REEMBOLSO = 0.5;

    public ServicioReembolso() {}

    /**
     * Calcula los recursos devueltos al reciclar un cuerpo celeste.
     * Si el cuerpo fue transformado por evento u otro mecanismo, no otorga reembolso.
     */
    public Map<TipoRecurso, Double> calcularReembolso(CuerpoCeleste cuerpo) {
        if (cuerpo == null || !cuerpo.permiteReembolso() || cuerpo.getTipoCuerpo() == null) {
            return Collections.emptyMap();
        }
        double masaBase = cuerpo.getTipoCuerpo().masaBase;
        double factorMasa = (masaBase > 0)
                ? Math.max(0.1, cuerpo.getMasa() / masaBase)
                : 1.0;
        Map<TipoRecurso, Double> costosOriginales = InventarioJugador.getCostosEscalados(cuerpo.getTipoCuerpo(), factorMasa);
        if (costosOriginales.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<TipoRecurso, Double> reembolso = new EnumMap<>(TipoRecurso.class);
        for (Map.Entry<TipoRecurso, Double> entry : costosOriginales.entrySet()) {
            double cantidad = entry.getValue() * FACTOR_REEMBOLSO;
            if (cantidad > 0) {
                reembolso.put(entry.getKey(), cantidad);
            }
        }
        return Collections.unmodifiableMap(reembolso);
    }

    public boolean reembolsarYEliminar(CuerpoCeleste cuerpo, SimulacionSolar simulacion) {
        return desmantelarCuerpo(simulacion, cuerpo);
    }

    /**
     * Desmantela el cuerpo en la simulación, acreditando el reembolso correspondiente
     * al inventario del jugador y emitiendo un evento informativo.
     */
    public boolean desmantelarCuerpo(SimulacionSolar simulacion, CuerpoCeleste cuerpo) {
        if (simulacion == null || cuerpo == null || !cuerpo.esInteractuablePorUsuario()) return false;

        boolean permiteReembolso = cuerpo.permiteReembolso();
        Map<TipoRecurso, Double> reembolso = permiteReembolso ? calcularReembolso(cuerpo) : Collections.emptyMap();

        if (permiteReembolso && simulacion.getInventario() != null) {
            for (Map.Entry<TipoRecurso, Double> entry : reembolso.entrySet()) {
                simulacion.getInventario().agregarRecurso(entry.getKey(), entry.getValue());
            }
        }

        simulacion.getGestorCicloVida().eliminarCuerpo(cuerpo);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TipoRecurso, Double> entry : reembolso.entrySet()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(String.format("+%.1f %s", entry.getValue(), entry.getKey().nombre));
        }

        String detalle = !permiteReembolso
                ? "Sin reembolso (cuerpo no reembolsable)."
                : (sb.isEmpty() ? "Sin materiales reembolsados." : "Reembolsado 50%: " + sb);

        simulacion.notificarEvento(
                "Cuerpo eliminado: " + cuerpo.getNombre() + ". " + detalle,
                MensajeEvento.TipoMensaje.INFO
        );
        return true;
    }
}
