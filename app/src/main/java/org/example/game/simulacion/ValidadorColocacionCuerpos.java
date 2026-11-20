package org.example.game.simulacion;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.progresion.SistemaProgresion;

import java.util.function.BiConsumer;

/**
 * Validador de requisitos de recursos, tecnología y capacidad previa a la colocación de astros (SRP).
 */
public class ValidadorColocacionCuerpos implements ValidadorColocacion {

    public boolean validar(TipoCuerpo tipo, double factorMasa, int cantidadPlanetasActuales,
                           SistemaProgresion progresion, InventarioJugador inventario,
                           BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        if (!validarDesbloqueo(tipo, progresion, notificador)) return false;
        if (!validarLimitePlanetas(tipo, cantidadPlanetasActuales, progresion, notificador)) return false;
        if (!validarLimiteMasa(tipo, factorMasa, progresion, notificador)) return false;
        return validarRecursos(tipo, factorMasa, inventario, notificador);
    }

    private boolean validarDesbloqueo(TipoCuerpo tipo, SistemaProgresion progresion,
                                      BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        if (progresion != null && !progresion.estaDesbloqueado(tipo)) {
            notificar(notificador, "Cuerpo bloqueado: " + (tipo != null ? tipo.nombre : "Desconocido") + ". Desbloquéalo con ciencia.");
            return false;
        }
        return true;
    }

    private boolean validarLimitePlanetas(TipoCuerpo tipo, int cantidadPlanetasActuales, SistemaProgresion progresion,
                                          BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        if (tipo == null || !tipo.esPlaneta()) return true;
        int maximo = progresion != null ? progresion.getLimitePlanetas() : ConfiguracionSimulacion.PLANETAS_INICIALES_MAX;
        if (cantidadPlanetasActuales >= maximo) {
            notificar(notificador, "Límite de planetas alcanzado (" + cantidadPlanetasActuales + "/" + maximo + "). Amplía la capacidad en el Árbol de Investigación [P].");
            return false;
        }
        return true;
    }

    private boolean validarLimiteMasa(TipoCuerpo tipo, double factorMasa, SistemaProgresion progresion,
                                      BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        if (tipo != null && tipo.permiteAjusteMasa() && progresion != null && factorMasa > progresion.getLimiteMasaActual() + 1e-6) {
            notificar(notificador, "Límite de masa excedido (" + String.format("%.1fx", factorMasa) + " > " + String.format("%.1fx", progresion.getLimiteMasaActual()) + ").");
            return false;
        }
        return true;
    }

    private boolean validarRecursos(TipoCuerpo tipo, double factorMasa, InventarioJugador inventario,
                                    BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        if (inventario != null && !inventario.puedeCrear(tipo, factorMasa)) {
            notificar(notificador, "Recursos insuficientes para " + (tipo != null ? tipo.nombre : "cuerpo"));
            return false;
        }
        return true;
    }

    private void notificar(BiConsumer<String, MensajeEvento.TipoMensaje> notificador, String mensaje) {
        if (notificador != null) notificador.accept(mensaje, MensajeEvento.TipoMensaje.ADVERTENCIA);
    }
}
