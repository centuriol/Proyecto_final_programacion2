package org.example.game.guardado;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.render.CamaraZoom;
import org.example.game.render.ControlZoom;
import org.example.game.simulacion.SimulacionSolar;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/**
 * Convierte el estado vivo de SimulacionSolar y ControlZoom hacia un objeto inmutable
 * DatosPartida y restaura dicho estado sin exponer detalles de archivo ni texto (SRP / DIP).
 */
public class MapeadorEstadoSimulacion implements MapeadorPartida {

    private final ReconstructorCuerpos reconstructorCuerpos;

    public MapeadorEstadoSimulacion() {
        this(new ReconstructorCuerpos());
    }

    public MapeadorEstadoSimulacion(ReconstructorCuerpos reconstructorCuerpos) {
        this.reconstructorCuerpos = reconstructorCuerpos != null
                ? reconstructorCuerpos
                : new ReconstructorCuerpos();
    }

    @Override
    public DatosPartida capturar(String nombrePartida, SimulacionSolar simulacion, ControlZoom zoom) {
        Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        String nombreEfectivo = (nombrePartida != null && !nombrePartida.isBlank())
                ? nombrePartida.trim()
                : GestorGuardadoPartida.SLOT_PRINCIPAL;

        List<CuerpoCeleste> cuerposVivos = new ArrayList<>();
        for (CuerpoCeleste c : simulacion.getSistemaSolar().getCuerpos()) {
            if (c != null) {
                cuerposVivos.add(c);
            }
        }
        ExtractorDatosCuerpo extractor = new ExtractorDatosCuerpo(cuerposVivos);
        List<DatosCuerpo> datosCuerpos = new ArrayList<>(cuerposVivos.size());
        for (CuerpoCeleste c : cuerposVivos) {
            datosCuerpos.add(c.aceptar(extractor));
        }

        return new DatosPartida(
                nombreEfectivo,
                capturarReloj(simulacion, zoom),
                capturarJugador(simulacion),
                datosCuerpos,
                capturarNaves(simulacion, extractor)
        );
    }

    @Override
    public void restaurar(DatosPartida datos, SimulacionSolar simulacion, ControlZoom zoom) {
        Objects.requireNonNull(datos, "Los datos de partida no pueden ser null");
        Objects.requireNonNull(simulacion, "La simulacion destino no puede ser null");

        List<CuerpoCeleste> cuerpos = reconstructorCuerpos.reconstruirTodos(datos.cuerpos(), simulacion::notificarEvento);
        simulacion.limpiarCuerpos();
        simulacion.getGestorColonizacion().limpiar();

        aplicarJugadorYReloj(datos, simulacion, zoom);
        aplicarEntidades(cuerpos, datos.naves(), simulacion);
        simulacion.sincronizarTrasCarga();
    }

    private List<DatosNaveColonizadora> capturarNaves(SimulacionSolar simulacion, ExtractorDatosCuerpo extractor) {
        List<DatosNaveColonizadora> resultado = new ArrayList<>();
        for (NaveColonizadora nave : simulacion.getGestorColonizacion().getNaves()) {
            if (nave == null || nave.isCompletada() || nave.isPerdida()) {
                continue;
            }
            int idxDestino = extractor.obtenerIndice(nave.getDestino());
            if (idxDestino >= 0) {
                resultado.add(new DatosNaveColonizadora(nave.getPosicion(), idxDestino, nave.getPoblacionColonos()));
            }
        }
        return resultado;
    }

    private EstadoRelojGuardado capturarReloj(SimulacionSolar simulacion, ControlZoom zoom) {
        double nivelZoom = zoom != null ? zoom.getNivelZoom() : CamaraZoom.ZOOM_INICIAL;
        Vector2D desplazamiento = (zoom != null && zoom.getDesplazamiento() != null)
                ? zoom.getDesplazamiento().comoVector()
                : Vector2D.cero();
        int ticksEvento = simulacion.getGeneradorEventosCosmicos() != null
                ? simulacion.getGeneradorEventosCosmicos().getTicksHastaSiguiente()
                : 3600;
        return new EstadoRelojGuardado(
                simulacion.getTickActual(),
                simulacion.isEnPausa(),
                simulacion.getVelocidadSimulacion(),
                nivelZoom,
                ticksEvento,
                desplazamiento
        );
    }

    private EstadoJugadorGuardado capturarJugador(SimulacionSolar simulacion) {
        InventarioJugador inv = simulacion.getInventario();
        SistemaProgresion prog = simulacion.getProgresion();
        Map<TipoRecurso, Double> recursos = new EnumMap<>(TipoRecurso.class);
        for (TipoRecurso tr : TipoRecurso.values()) {
            recursos.put(tr, inv.getRecurso(tr));
        }
        Map<RamaProgresion, Integer> ramas = new EnumMap<>(RamaProgresion.class);
        for (RamaProgresion rama : RamaProgresion.values()) {
            ramas.put(rama, prog.getNivel(rama));
        }
        return new EstadoJugadorGuardado(recursos, ramas, prog.getNivelEscudo());
    }

    private void aplicarJugadorYReloj(DatosPartida datos, SimulacionSolar simulacion, ControlZoom zoom) {
        EstadoJugadorGuardado jugador = datos.jugador();
        for (TipoRecurso tr : TipoRecurso.values()) {
            simulacion.getInventario().getRecursoObj(tr).setCantidad(jugador.obtenerRecurso(tr));
        }
        simulacion.getProgresion().restaurarEstado(jugador.nivelesProgresion(), jugador.nivelEscudo());

        EstadoRelojGuardado reloj = datos.reloj();
        simulacion.getReloj().restaurar(reloj.tickActual(), reloj.enPausa(), reloj.velocidadSimulacion());
        if (simulacion.getGeneradorEventosCosmicos() != null) {
            simulacion.getGeneradorEventosCosmicos().setTicksHastaSiguiente(reloj.ticksSiguienteEvento());
        }
        if (zoom != null) {
            zoom.setNivelZoom(reloj.nivelZoom());
            if (zoom.getDesplazamiento() != null) {
                Vector2D desp = reloj.desplazamientoCamara();
                zoom.getDesplazamiento().setPosicion(desp.x(), desp.y());
            }
        }
    }

    private void aplicarEntidades(List<CuerpoCeleste> cuerpos, List<DatosNaveColonizadora> naves, SimulacionSolar simulacion) {
        for (CuerpoCeleste c : cuerpos) {
            simulacion.getSistemaSolar().agregarCuerpo(c);
        }
        for (DatosNaveColonizadora dn : naves) {
            if (dn.esDestinoValido(cuerpos.size())) {
                simulacion.getGestorColonizacion().restaurarNave(
                        new NaveColonizadora(dn.posicion(), cuerpos.get(dn.indiceDestino()), dn.poblacionColonos())
                );
            }
        }
    }
}
