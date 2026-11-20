package org.example.game.guardado;

import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.cuerpo.*;
import org.example.game.simulacion.MensajeEvento;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reconstruye instancias polimórficas de CuerpoCeleste a partir de DatosCuerpo (OCP / SRP).
 */
public class ReconstructorCuerpos implements ReconstructorCuerpo {

    @FunctionalInterface
    public interface CreadorDesdeDatos {
        CuerpoCeleste reconstruir(DatosCuerpo datos, List<CuerpoCeleste> contexto, List<Runnable> vinculacionesDiferidas);
    }

    private static final FabricaGuardado FABRICA = new FabricaGuardado();

    private final Map<TipoCuerpo, CreadorDesdeDatos> registro = new EnumMap<>(TipoCuerpo.class);
    private final VinculadorOrbitasCargadas vinculador;

    public ReconstructorCuerpos() {
        this(FABRICA.crearGeneradorIdentidadPorDefecto());
    }

    public ReconstructorCuerpos(GeneradorIdentidadCuerpos generadorIdentidad) {
        GeneradorIdentidadCuerpos gen = generadorIdentidad != null ? generadorIdentidad : FABRICA.crearGeneradorIdentidadPorDefecto();
        this.vinculador = new VinculadorOrbitasCargadas(gen);
        inicializarRegistroPorDefecto();
    }

    private void inicializarRegistroPorDefecto() {
        registrar(TipoCuerpo.ESTRELLA, (d, ctx, vinc) ->
                new Estrella(d.nombre(), d.masa(), d.cinematica().posicion().x(), d.cinematica().posicion().y()));
        registrar(TipoCuerpo.LUNA, (d, ctx, vinc) ->
                new Luna(d.nombre(), d.masa(), d.cinematica().posicion().x(), d.cinematica().posicion().y()));

        CreadorDesdeDatos creadorAgujero = (d, ctx, vinc) ->
                new AgujeroNegro(d.nombre(), d.masa(), d.cinematica().posicion().x(), d.cinematica().posicion().y(), d.tipo());
        registrar(TipoCuerpo.AGUJERO_NEGRO, creadorAgujero);
        registrar(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, creadorAgujero);

        registrar(TipoCuerpo.SATELITE, (d, ctx, vinc) -> {
            Satelite sat = new Satelite(d.nombre(), d.masa(), d.cinematica().posicion().x(), d.cinematica().posicion().y());
            sat.restaurarProduccionAcumulada(d.detalle().cienciaGenerada(), d.detalle().energiaGenerada());
            return sat;
        });
        registrar(TipoCuerpo.METEORITO, this::reconstruirMeteorito);

        for (TipoCuerpo tipo : TipoCuerpo.values()) {
            if (tipo.esPlaneta()) registrar(tipo, this::reconstruirPlaneta);
        }
    }

    public void registrar(TipoCuerpo tipo, CreadorDesdeDatos creador) {
        if (tipo != null && creador != null) registro.put(tipo, creador);
    }

    public List<CuerpoCeleste> reconstruirTodos(List<DatosCuerpo> datosCuerpos, Consumer<MensajeEvento> emisorEventos) {
        if (datosCuerpos == null || datosCuerpos.isEmpty()) return List.of();
        List<CuerpoCeleste> reconstruidos = new ArrayList<>(datosCuerpos.size());
        List<Runnable> vinculacionesDiferidas = new ArrayList<>();

        for (DatosCuerpo datos : datosCuerpos) {
            CreadorDesdeDatos creador = registro.get(datos.tipo());
            if (creador == null) {
                throw new ExcepcionPersistenciaPartida("Tipo de cuerpo sin reconstructor registrado: " + datos.tipo());
            }
            CuerpoCeleste cuerpo = creador.reconstruir(datos, reconstruidos, vinculacionesDiferidas);
            cuerpo.setVelocidad(datos.cinematica().velocidad());
            cuerpo.setSimulado(datos.simulado());
            if (cuerpo instanceof Planeta p && p.getCivilizacion() != null) {
                p.getCivilizacion().setEmisorEventos(emisorEventos);
            }
            vinculador.sincronizarContadorNombre(datos.nombre());
            reconstruidos.add(cuerpo);
        }

        vinculador.vincularOrbitas(datosCuerpos, reconstruidos);
        vinculacionesDiferidas.forEach(Runnable::run);
        return reconstruidos;
    }

    private Planeta reconstruirPlaneta(DatosCuerpo d, List<CuerpoCeleste> ctx, List<Runnable> vinc) {
        Planeta planeta = new Planeta(d.nombre(), d.masa(), d.cinematica().posicion().x(), d.cinematica().posicion().y(), d.tipo());
        DetalleCuerpo det = d.detalle();
        boolean transformado = det.transformadoPorEvento();
        boolean poseeCiv = !transformado && det.tieneCivilizacionRegistrada();
        planeta.restaurarAtributosPlaneta(transformado, det.golpesEscudo(), poseeCiv);
        RegistroCapacidades.registrarPlaneta(planeta);
        RegistroCapacidades.obtenerTransformable(planeta).ifPresent(t -> t.setTransformadoPorEvento(transformado));
        if (poseeCiv && planeta.getCivilizacion() != null) {
            DatosCivilizacion civDatos = det.civilizacion();
            CivilizacionComponent civ = planeta.getCivilizacion();
            civ.restaurarMetricas(civDatos.poblacion(), civDatos.nivelTecnologico(), civDatos.estabilidad(), civDatos.felicidad());
            civ.restaurarEstadoGeneral(civDatos.estado(), civDatos.ticksEfectoEvento());
        }
        return planeta;
    }

    private Meteorito reconstruirMeteorito(DatosCuerpo d, List<CuerpoCeleste> ctx, List<Runnable> vinc) {
        DetalleCuerpo det = d.detalle();
        Meteorito meteorito = new Meteorito(
                d.nombre(), d.masa(),
                d.cinematica().posicion().x(), d.cinematica().posicion().y(),
                d.cinematica().velocidad(), det.tiempoVidaRestante(), det.lanzadoPorJugador()
        );
        int idxObjetivo = det.indiceObjetivo();
        if (idxObjetivo >= 0) {
            vinc.add(() -> {
                if (idxObjetivo < ctx.size()) meteorito.setObjetivo(ctx.get(idxObjetivo));
            });
        }
        return meteorito;
    }
}
