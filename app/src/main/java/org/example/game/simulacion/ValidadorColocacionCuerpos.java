package org.example.game.simulacion;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.progresion.SistemaProgresion;

import java.util.function.BiConsumer;

/**
 * Validador de requisitos de recursos, tecnología y capacidad previa a la colocación de astros (SRP).
 */
public class ValidadorColocacionCuerpos {

    public boolean validar(TipoCuerpo tipo, double factorMasa, int cantidadPlanetasActuales,
                           SistemaProgresion progresion, InventarioJugador inventario,
                           BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        if (progresion != null && !progresion.estaDesbloqueado(tipo)) {
            if (notificador != null) {
                notificador.accept("Cuerpo bloqueado: " + (tipo != null ? tipo.nombre : "Desconocido") + ". Desbloquéalo con ciencia.", MensajeEvento.TipoMensaje.ADVERTENCIA);
            }
            return false;
        }
        if (tipo != null && tipo.esPlaneta()) {
            int maximo = progresion != null ? progresion.getLimitePlanetas() : ConfiguracionSimulacion.PLANETAS_INICIALES_MAX;
            if (cantidadPlanetasActuales >= maximo) {
                if (notificador != null) {
                    notificador.accept("Límite de planetas alcanzado (" + cantidadPlanetasActuales + "/" + maximo + "). Amplía la capacidad en el Árbol de Investigación [P].", MensajeEvento.TipoMensaje.ADVERTENCIA);
                }
                return false;
            }
        }
        if (tipo != null && tipo.permiteAjusteMasa() && progresion != null && factorMasa > progresion.getLimiteMasaActual() + 1e-6) {
            if (notificador != null) {
                notificador.accept("Límite de masa excedido (" + String.format("%.1fx", factorMasa) + " > " + String.format("%.1fx", progresion.getLimiteMasaActual()) + ").", MensajeEvento.TipoMensaje.ADVERTENCIA);
            }
            return false;
        }
        if (inventario != null && !inventario.puedeCrear(tipo, factorMasa)) {
            if (notificador != null) {
                notificador.accept("Recursos insuficientes para " + (tipo != null ? tipo.nombre : "cuerpo"), MensajeEvento.TipoMensaje.ADVERTENCIA);
            }
            return false;
        }
        return true;
    }
}
