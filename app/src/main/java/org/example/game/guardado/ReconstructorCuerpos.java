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
 * Reconstruye instancias polimorficas de CuerpoCeleste y sus relaciones orbitales
 * a partir de DatosCuerpo mediante un registro extensible por TipoCuerpo (OCP / SRP).
 */
public class ReconstructorCuerpos {

    @FunctionalInterface
    public interface CreadorDesdeDatos {
        CuerpoCeleste reconstruir(DatosCuerpo datos, List<CuerpoCeleste> contexto, List<Runnable> vinculacionesDiferidas);
    }

    private final Map<TipoCuerpo, CreadorDesdeDatos> registro = new EnumMap<>(TipoCuerpo.class);
    private Consumer<MensajeEvento> emisorActivo;
    private final org.example.game.cuerpo.FabricaCuerpos fabricaCuerpos;

    public ReconstructorCuerpos() {
        this(new org.example.game.cuerpo.CuerpoCelesteFactory());
    }

    public ReconstructorCuerpos(org.example.game.cuerpo.FabricaCuerpos fabricaCuerpos) {
        this.fabricaCuerpos = fabricaCuerpos != null ? fabricaCuerpos : new org.example.game.cuerpo.CuerpoCelesteFactory();
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

        CreadorDesdeDatos creadorPlaneta = this::reconstruirPlaneta;
        for (TipoCuerpo tipo : TipoCuerpo.values()) {
            if (tipo.esPlaneta()) {
                registrar(tipo, creadorPlaneta);
            }
        }
    }

    public void registrar(TipoCuerpo tipo, CreadorDesdeDatos creador) {
        if (tipo != null && creador != null) {
            registro.put(tipo, creador);
        }
    }

    public List<CuerpoCeleste> reconstruirTodos(List<DatosCuerpo> datosCuerpos,
                                                Consumer<MensajeEvento> emisorEventos) {
        if (datosCuerpos == null || datosCuerpos.isEmpty()) {
            return List.of();
        }
        this.emisorActivo = emisorEventos;
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
            sincronizarContadorNombre(datos.nombre());
            reconstruidos.add(cuerpo);
        }

        vincularOrbitas(datosCuerpos, reconstruidos);
        vinculacionesDiferidas.forEach(Runnable::run);
        this.emisorActivo = null;
        return reconstruidos;
    }

    private Planeta reconstruirPlaneta(DatosCuerpo d, List<CuerpoCeleste> ctx, List<Runnable> vinc) {
        Planeta planeta = new Planeta(d.nombre(), d.masa(), d.cinematica().posicion().x(), d.cinematica().posicion().y(), d.tipo());
        DetalleCuerpo det = d.detalle();
        boolean poseeCiv = det.tieneCivilizacionRegistrada();
        planeta.restaurarAtributosPlaneta(det.transformadoPorEvento(), det.golpesEscudo(), poseeCiv);
        if (poseeCiv && planeta.getCivilizacion() != null) {
            DatosCivilizacion civDatos = det.civilizacion();
            CivilizacionComponent civ = planeta.getCivilizacion();
            civ.restaurarMetricas(civDatos.poblacion(), civDatos.nivelTecnologico(), civDatos.estabilidad(), civDatos.felicidad());
            civ.restaurarEstadoGeneral(civDatos.estado(), civDatos.ticksEfectoEvento());
            civ.setEmisorEventos(emisorActivo);
            planeta.sincronizarFlagCivilizacion();
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
                if (idxObjetivo < ctx.size()) {
                    meteorito.setObjetivo(ctx.get(idxObjetivo));
                }
            });
        }
        return meteorito;
    }

    private void vincularOrbitas(List<DatosCuerpo> datosCuerpos, List<CuerpoCeleste> reconstruidos) {
        for (int i = 0; i < datosCuerpos.size(); i++) {
            EstadoCinematica cin = datosCuerpos.get(i).cinematica();
            CuerpoCeleste cuerpo = reconstruidos.get(i);
            cuerpo.setRadioOrbita(cin.radioOrbita());
            int idxOrbitado = cin.indiceOrbitado();
            if (idxOrbitado >= 0 && idxOrbitado < reconstruidos.size() && idxOrbitado != i) {
                cuerpo.setCuerpoOrbitado(reconstruidos.get(idxOrbitado));
            }
        }
    }

    private void sincronizarContadorNombre(String nombre) {
        int guion = nombre.lastIndexOf('-');
        if (guion > 0 && guion < nombre.length() - 1) {
            try {
                fabricaCuerpos.asegurarContadorMinimo(Long.parseLong(nombre.substring(guion + 1)));
            } catch (NumberFormatException e) {
                System.err.println("[DEBUG] Nombre de cuerpo sin id numerico estandar: " + nombre);
            }
        }
    }
}
