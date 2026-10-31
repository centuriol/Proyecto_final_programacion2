package org.example.game.colonizacion;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
/**
 * Gestor del subsistema de colonización espacial entre planetas.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de validar, lanzar y actualizar las expediciones de colonización.
 * - DIP: Emite notificaciones desacopladas y delega la física/economía a sus respectivos contratos.
 */
public class GestorColonizacion {

    private final List<NaveColonizadora> naves = new ArrayList<>();

    public boolean puedeColonizar(Planeta origen, InventarioJugador inventario) {
        if (origen == null || !origen.tieneCivilizacion() || origen.getCivilizacion() == null) {
            return false;
        }
        if (origen.getCivilizacion().getPorcentajeCapacidad() < ConfiguracionSimulacion.UMBRAL_COLONIZAR_PORCENTAJE) {
            return false;
        }
        if (inventario == null) return false;
        return inventario.getRecurso(TipoRecurso.ENERGIA) >= ConfiguracionSimulacion.COSTO_COLONIZAR_ENERGIA
                && inventario.getRecurso(TipoRecurso.MINERALES) >= ConfiguracionSimulacion.COSTO_COLONIZAR_MINERALES;
    }

    public boolean lanzar(Planeta origen, CuerpoCeleste destino, InventarioJugador inventario, Consumer<MensajeEvento> emisorEventos, long tickActual) {
        if (!puedeColonizar(origen, inventario)) {
            if (emisorEventos != null) {
                emisorEventos.accept(new MensajeEvento(
                        "No se cumplen los requisitos para colonizar (80% capacidad y recursos).",
                        MensajeEvento.TipoMensaje.ADVERTENCIA,
                        tickActual
                ));
            }
            return false;
        }

        org.example.game.cuerpo.Colonizable colDestino = destino != null ? destino.comoColonizable().orElse(null) : null;
        if (colDestino == null || destino == origen || !colDestino.puedeSerColonizado()) {
            if (emisorEventos != null) {
                emisorEventos.accept(new MensajeEvento(
                        "El destino seleccionado no puede ser colonizado o ya tiene civilización.",
                        MensajeEvento.TipoMensaje.ADVERTENCIA,
                        tickActual
                ));
            }
            return false;
        }

        // Cobrar recursos
        inventario.gastarRecurso(TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_COLONIZAR_ENERGIA);
        inventario.gastarRecurso(TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_COLONIZAR_MINERALES);

        // Descontar 10% de la población del planeta origen
        double poblacionActual = origen.getCivilizacion().getPoblacion();
        double colonos = poblacionActual * ConfiguracionSimulacion.PORCENTAJE_POBLACION_COLONIZADORA;
        origen.getCivilizacion().restarPoblacion(colonos);

        NaveColonizadora nave = new NaveColonizadora(origen.getPosicion(), destino, colonos);
        naves.add(nave);

        if (emisorEventos != null) {
            emisorEventos.accept(new MensajeEvento(
                    "Misión de colonización lanzada desde " + origen.getNombre() + " hacia " + destino.getNombre() + " con " + (int)colonos + " colonos.",
                    MensajeEvento.TipoMensaje.INFO,
                    tickActual
            ));
        }

        return true;
    }

    public void actualizar(Consumer<MensajeEvento> emisorEventos, long tickActual) {
        Iterator<NaveColonizadora> it = naves.iterator();
        while (it.hasNext()) {
            NaveColonizadora nave = it.next();
            nave.actualizar();

            if (nave.isCompletada()) {
                it.remove();
                if (emisorEventos != null) {
                    emisorEventos.accept(new MensajeEvento(
                            "Colonización exitosa: " + nave.getDestino().getNombre() + " ha fundado una nueva colonia con " + (int)nave.getPoblacionColonos() + " habitantes.",
                            MensajeEvento.TipoMensaje.EXITO,
                            tickActual
                    ));
                }
            } else if (nave.isPerdida()) {
                it.remove();
                if (emisorEventos != null) {
                    emisorEventos.accept(new MensajeEvento(
                            "Misión de colonización fallida: El planeta de destino fue destruido en el trayecto. La población se perdió.",
                            MensajeEvento.TipoMensaje.PELIGRO,
                            tickActual
                    ));
                }
            }
        }
    }

    public List<NaveColonizadora> getNaves() {
        return Collections.unmodifiableList(naves);
    }

    public void limpiar() {
        naves.clear();
    }

    public void restaurarNave(NaveColonizadora nave) {
        if (nave != null && !nave.isCompletada() && !nave.isPerdida()) {
            naves.add(nave);
        }
    }
}
