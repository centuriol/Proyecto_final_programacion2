package org.example.game.guardado;
import org.example.game.render.ControlZoom; import org.example.game.simulacion.MensajeEvento; import org.example.game.simulacion.SimulacionSolar; import java.nio.file.Path; import java.util.ArrayList; import java.util.List; import java.util.Objects; import java.util.Optional;
/**
 * Servicio coordinador de casos de uso para guardar, autoguardar y cargar partidas
 * sobre las tres ranuras rotativas o identificadores explicitos.
 *
 * Principios SOLID:
 * - SRP: Coordina repositorio, mapeador y politica de tres ranuras emitiendo notificaciones.
 * - DIP: Depende de las abstracciones RepositorioPartidas, MapeadorPartida y ControlZoom.
 */
public class GestorGuardadoPartida {

    public static final String SLOT_PRINCIPAL = "partida_principal";

    private final RepositorioPartidas repositorio;
    private final MapeadorPartida mapeador;
    private final PoliticaRanurasGuardado politicaRanuras;
    private final List<Runnable> listenersPostCarga = new ArrayList<>();
    private int ranuraSeleccionada = 1;

    public GestorGuardadoPartida() {
        this(new RepositorioPartidasArchivo(), new MapeadorEstadoSimulacion(), new PoliticaRanurasGuardado());
    }

    public GestorGuardadoPartida(RepositorioPartidas repositorio) {
        this(repositorio, new MapeadorEstadoSimulacion(), new PoliticaRanurasGuardado());
    }

    public GestorGuardadoPartida(RepositorioPartidas repositorio, MapeadorPartida mapeador) {
        this(repositorio, mapeador, new PoliticaRanurasGuardado());
    }

    public GestorGuardadoPartida(RepositorioPartidas repositorio,
                                 MapeadorPartida mapeador,
                                 PoliticaRanurasGuardado politicaRanuras) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio no puede ser null");
        this.mapeador = Objects.requireNonNull(mapeador, "El mapeador no puede ser null");
        this.politicaRanuras = Objects.requireNonNull(politicaRanuras, "La politica de ranuras no puede ser null");
    }

    public Path guardarPartida(String nombreSlot, SimulacionSolar simulacion, ControlZoom zoom) {
        String slot = (nombreSlot != null && !nombreSlot.isBlank()) ? nombreSlot.trim() : SLOT_PRINCIPAL;
        return escribirEnSlot(slot, slot, simulacion, zoom, "Partida guardada en '" + slot + "'.");
    }

    public Path guardarPartidaRapida(SimulacionSolar simulacion, ControlZoom zoom) {
        InfoRanuraGuardado destino = politicaRanuras.seleccionarRanuraDestino(repositorio);
        this.ranuraSeleccionada = destino.numeroRanura();
        return escribirEnSlot(
                destino.identificadorSlot(),
                destino.tituloRanura(),
                simulacion,
                zoom,
                "Partida guardada en " + destino.tituloRanura() + "."
        );
    }

    public Path guardarAutoguardado(SimulacionSolar simulacion, ControlZoom zoom) {
        String idSlot = politicaRanuras.identificadorRanura(ranuraSeleccionada);
        String titulo = "Ranura " + ranuraSeleccionada;
        return escribirEnSlot(
                idSlot,
                titulo,
                simulacion,
                zoom,
                "Autoguardado completado en " + titulo + "."
        );
    }

    public int getRanuraSeleccionada() {
        return ranuraSeleccionada;
    }

    public void setRanuraSeleccionada(int numeroRanura) {
        politicaRanuras.identificadorRanura(numeroRanura);
        this.ranuraSeleccionada = numeroRanura;
    }

    public int seleccionarRanuraParaNuevaPartida() {
        InfoRanuraGuardado inicial = politicaRanuras.seleccionarRanuraDestino(repositorio);
        this.ranuraSeleccionada = inicial.numeroRanura();
        return this.ranuraSeleccionada;
    }

    private Path escribirEnSlot(String idSlot, String titulo, SimulacionSolar simulacion, ControlZoom zoom, String exitoMsg) {
        Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        try {
            long marcaTiempo = politicaRanuras.siguienteMarcaTiempo(repositorio);
            DatosPartida datos = mapeador.capturar(titulo, simulacion, zoom).conMarcaTiempo(marcaTiempo);
            Path archivo = repositorio.guardar(idSlot, datos);
            simulacion.notificarEvento(exitoMsg, MensajeEvento.TipoMensaje.EXITO);
            return archivo;
        } catch (ExcepcionPersistenciaPartida e) {
            simulacion.notificarEvento("Error al guardar partida: " + e.getMessage(), MensajeEvento.TipoMensaje.PELIGRO);
            throw e;
        }
    }

    public boolean cargarPartida(String nombreSlot, SimulacionSolar simulacion, ControlZoom zoom) {
        Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        String slot = (nombreSlot != null && !nombreSlot.isBlank()) ? nombreSlot.trim() : SLOT_PRINCIPAL;
        try {
            Optional<DatosPartida> cargada = repositorio.cargar(slot);
            if (cargada.isEmpty()) {
                simulacion.notificarEvento("No existe partida guardada con nombre '" + slot + "'.", MensajeEvento.TipoMensaje.ADVERTENCIA);
                return false;
            }
            mapeador.restaurar(cargada.get(), simulacion, zoom);
            sincronizarRanuraDesdeIdentificador(slot);
            listenersPostCarga.forEach(Runnable::run);
            simulacion.notificarEvento("Partida '" + cargada.get().nombrePartida() + "' cargada correctamente.", MensajeEvento.TipoMensaje.EXITO);
            return true;
        } catch (ExcepcionPersistenciaPartida e) {
            simulacion.notificarEvento("Error al cargar partida: " + e.getMessage(), MensajeEvento.TipoMensaje.PELIGRO);
            throw e;
        }
    }

    private void sincronizarRanuraDesdeIdentificador(String slot) {
        if (slot == null || !slot.startsWith(PoliticaRanurasGuardado.PREFIJO_RANURA)) {
            return;
        }
        String sufijo = slot.substring(PoliticaRanurasGuardado.PREFIJO_RANURA.length());
        try {
            int numero = Integer.parseInt(sufijo);
            if (numero >= 1 && numero <= PoliticaRanurasGuardado.TOTAL_RANURAS) {
                this.ranuraSeleccionada = numero;
            }
        } catch (NumberFormatException ignored) {
            // Identificador personalizado distinto de ranura numerada
        }
    }

    public boolean cargarRanura(int numeroRanura, SimulacionSolar simulacion, ControlZoom zoom) {
        return cargarPartida(politicaRanuras.identificadorRanura(numeroRanura), simulacion, zoom);
    }

    public boolean cargarUltimoGuardado(SimulacionSolar simulacion, ControlZoom zoom) {
        Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        Optional<InfoRanuraGuardado> ultimo = politicaRanuras.obtenerUltimoGuardado(repositorio);
        if (ultimo.isPresent()) {
            return cargarPartida(ultimo.get().identificadorSlot(), simulacion, zoom);
        }
        if (repositorio.existe(SLOT_PRINCIPAL)) {
            return cargarPartida(SLOT_PRINCIPAL, simulacion, zoom);
        }
        simulacion.notificarEvento("No existen partidas guardadas para restaurar.", MensajeEvento.TipoMensaje.ADVERTENCIA);
        return false;
    }

    public boolean cargarPartidaRapida(SimulacionSolar simulacion, ControlZoom zoom) {
        return cargarUltimoGuardado(simulacion, zoom);
    }

    public List<InfoRanuraGuardado> obtenerRanuras() {
        return politicaRanuras.consultarRanuras(repositorio);
    }

    public void agregarListenerPostCarga(Runnable listener) {
        if (listener != null) {
            listenersPostCarga.add(listener);
        }
    }

    public RepositorioPartidas getRepositorio() {
        return repositorio;
    }
}
