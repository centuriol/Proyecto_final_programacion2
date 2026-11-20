package org.example.game.guardado;

import org.example.game.render.ControlZoom;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * SRP: Ejecuta la escritura y lectura transaccional de partidas en el repositorio y notifica eventos/listeners.
 */
public class ServicioPersistenciaSlot {

    private final RepositorioPartidas repositorio;
    private final MapeadorPartida mapeador;
    private final List<Runnable> listenersPostCarga = new ArrayList<>();

    public ServicioPersistenciaSlot(RepositorioPartidas repositorio, MapeadorPartida mapeador) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio no puede ser null");
        this.mapeador = Objects.requireNonNull(mapeador, "El mapeador no puede ser null");
    }

    public Path escribir(String idSlot, String titulo, long marcaTiempo, SimulacionSolar simulacion, ControlZoom zoom, String exitoMsg) {
        Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        try {
            DatosPartida datos = mapeador.capturar(titulo, simulacion, zoom).conMarcaTiempo(marcaTiempo);
            Path archivo = repositorio.guardar(idSlot, datos);
            simulacion.notificarEvento(exitoMsg, MensajeEvento.TipoMensaje.EXITO);
            return archivo;
        } catch (ExcepcionPersistenciaPartida e) {
            simulacion.notificarEvento("Error al guardar partida: " + e.getMessage(), MensajeEvento.TipoMensaje.PELIGRO);
            throw e;
        }
    }

    public boolean cargar(String slot, SimulacionSolar simulacion, ControlZoom zoom, Runnable onSlotCargado) {
        Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        try {
            Optional<DatosPartida> cargada = repositorio.cargar(slot);
            if (cargada.isEmpty()) {
                simulacion.notificarEvento("No existe partida guardada con nombre '" + slot + "'.", MensajeEvento.TipoMensaje.ADVERTENCIA);
                return false;
            }
            mapeador.restaurar(cargada.get(), simulacion, zoom);
            if (onSlotCargado != null) onSlotCargado.run();
            listenersPostCarga.forEach(Runnable::run);
            simulacion.notificarEvento("Partida '" + cargada.get().nombrePartida() + "' cargada correctamente.", MensajeEvento.TipoMensaje.EXITO);
            return true;
        } catch (ExcepcionPersistenciaPartida e) {
            simulacion.notificarEvento("Error al cargar partida: " + e.getMessage(), MensajeEvento.TipoMensaje.PELIGRO);
            throw e;
        }
    }

    public void agregarListenerPostCarga(Runnable listener) {
        if (listener != null) listenersPostCarga.add(listener);
    }

    public RepositorioPartidas getRepositorio() { return repositorio; }
}
