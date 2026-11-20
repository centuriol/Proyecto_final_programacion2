package org.example.game.guardado;

import org.example.game.render.ControlZoom;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio coordinador de casos de uso para guardar, autoguardar y cargar partidas (SRP / DIP).
 */
public class GestorGuardadoPartida {

    public static final String SLOT_PRINCIPAL = "partida_principal";

    private static final FabricaGuardado FABRICA = new FabricaGuardado();

    private final ServicioPersistenciaSlot persistencia;
    private final SelectorRanuraActiva selectorRanura;

    public GestorGuardadoPartida() {
        this(FABRICA.crearRepositorioPorDefecto(), FABRICA.crearMapeadorPorDefecto(), FABRICA.crearPoliticaPorDefecto());
    }

    public GestorGuardadoPartida(RepositorioPartidas repositorio) {
        this(repositorio, FABRICA.crearMapeadorPorDefecto(), FABRICA.crearPoliticaPorDefecto());
    }

    public GestorGuardadoPartida(RepositorioPartidas repositorio, MapeadorPartida mapeador) {
        this(repositorio, mapeador, FABRICA.crearPoliticaPorDefecto());
    }

    public GestorGuardadoPartida(RepositorioPartidas repositorio,
                                 MapeadorPartida mapeador,
                                 PoliticaRanuras politicaRanuras) {
        this.persistencia = new ServicioPersistenciaSlot(repositorio, mapeador);
        this.selectorRanura = new SelectorRanuraActiva(politicaRanuras);
    }

    public Path guardarPartida(String nombreSlot, SimulacionSolar simulacion, ControlZoom zoom) {
        String slot = (nombreSlot != null && !nombreSlot.isBlank()) ? nombreSlot.trim() : SLOT_PRINCIPAL;
        long ts = selectorRanura.siguienteMarcaTiempo(persistencia.getRepositorio());
        return persistencia.escribir(slot, slot, ts, simulacion, zoom, "Partida guardada en '" + slot + "'.");
    }

    public Path guardarPartidaRapida(SimulacionSolar simulacion, ControlZoom zoom) {
        InfoRanuraGuardado destino = selectorRanura.seleccionarDestino(persistencia.getRepositorio());
        long ts = selectorRanura.siguienteMarcaTiempo(persistencia.getRepositorio());
        return persistencia.escribir(destino.identificadorSlot(), destino.tituloRanura(), ts, simulacion, zoom,
                "Partida guardada en " + destino.tituloRanura() + ".");
    }

    public Path guardarAutoguardado(SimulacionSolar simulacion, ControlZoom zoom) {
        String idSlot = selectorRanura.identificadorActiva();
        String titulo = "Ranura " + selectorRanura.getRanuraSeleccionada();
        long ts = selectorRanura.siguienteMarcaTiempo(persistencia.getRepositorio());
        return persistencia.escribir(idSlot, titulo, ts, simulacion, zoom, "Autoguardado completado en " + titulo + ".");
    }

    public int getRanuraSeleccionada() { return selectorRanura.getRanuraSeleccionada(); }
    public void setRanuraSeleccionada(int numeroRanura) { selectorRanura.setRanuraSeleccionada(numeroRanura); }

    public int seleccionarRanuraParaNuevaPartida() {
        return selectorRanura.seleccionarDestino(persistencia.getRepositorio()).numeroRanura();
    }

    public boolean cargarPartida(String nombreSlot, SimulacionSolar simulacion, ControlZoom zoom) {
        String slot = (nombreSlot != null && !nombreSlot.isBlank()) ? nombreSlot.trim() : SLOT_PRINCIPAL;
        return persistencia.cargar(slot, simulacion, zoom, () -> selectorRanura.sincronizarDesdeIdentificador(slot));
    }

    public boolean cargarRanura(int numeroRanura, SimulacionSolar simulacion, ControlZoom zoom) {
        return cargarPartida(selectorRanura.identificador(numeroRanura), simulacion, zoom);
    }

    public boolean cargarUltimoGuardado(SimulacionSolar simulacion, ControlZoom zoom) {
        Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        Optional<InfoRanuraGuardado> ultimo = selectorRanura.obtenerUltimoGuardado(persistencia.getRepositorio());
        if (ultimo.isPresent()) return cargarPartida(ultimo.get().identificadorSlot(), simulacion, zoom);
        if (persistencia.getRepositorio().existe(SLOT_PRINCIPAL)) return cargarPartida(SLOT_PRINCIPAL, simulacion, zoom);
        simulacion.notificarEvento("No existen partidas guardadas para restaurar.", MensajeEvento.TipoMensaje.ADVERTENCIA);
        return false;
    }

    public boolean cargarPartidaRapida(SimulacionSolar simulacion, ControlZoom zoom) { return cargarUltimoGuardado(simulacion, zoom); }
    public List<InfoRanuraGuardado> obtenerRanuras() { return selectorRanura.consultarRanuras(persistencia.getRepositorio()); }
    public void agregarListenerPostCarga(Runnable listener) { persistencia.agregarListenerPostCarga(listener); }
    public RepositorioPartidas getRepositorio() { return persistencia.getRepositorio(); }
}
