package org.example.game.guardado;
import org.example.game.civilizacion.CivilizacionComponent; import org.example.game.cuerpo.*; import java.util.IdentityHashMap; import java.util.List; import java.util.Map;
/**
 * Visitante polimorfico que extrae DatosCuerpo de cualquier subclase de CuerpoCeleste
 * sin emplear condicionales instanceof (OCP / SRP).
 */
public class ExtractorDatosCuerpo implements VisitanteCuerpo<DatosCuerpo> {

    private final Map<CuerpoCeleste, Integer> indicesPorIdentidad = new IdentityHashMap<>();

    public ExtractorDatosCuerpo(List<CuerpoCeleste> cuerposEnSistema) {
        if (cuerposEnSistema != null) {
            for (int i = 0; i < cuerposEnSistema.size(); i++) {
                CuerpoCeleste c = cuerposEnSistema.get(i);
                if (c != null) {
                    indicesPorIdentidad.put(c, i);
                }
            }
        }
    }

    public int obtenerIndice(CuerpoCeleste cuerpo) {
        if (cuerpo == null) {
            return -1;
        }
        return indicesPorIdentidad.getOrDefault(cuerpo, -1);
    }

    @Override
    public DatosCuerpo visitarEstrella(Estrella estrella) {
        return construirDatos(estrella, DetalleCuerpo.basico());
    }

    @Override
    public DatosCuerpo visitarLuna(Luna luna) {
        return construirDatos(luna, DetalleCuerpo.basico());
    }

    @Override
    public DatosCuerpo visitarAgujeroNegro(AgujeroNegro agujeroNegro) {
        return construirDatos(agujeroNegro, DetalleCuerpo.basico());
    }

    @Override
    public DatosCuerpo visitarPlaneta(Planeta planeta) {
        DatosCivilizacion datosCiv = null;
        CivilizacionComponent civ = planeta.getCivilizacion();
        if (civ != null) {
            datosCiv = new DatosCivilizacion(
                    civ.getPoblacion(),
                    civ.getNivelTecnologico(),
                    civ.getEstabilidad(),
                    civ.getFelicidad(),
                    civ.getEstado(),
                    civ.getTicksEfectoEventoReciente()
            );
        }
        DetalleCuerpo detalle = DetalleCuerpo.dePlaneta(
                planeta.isTransformadoPorEvento(),
                planeta.getGolpesEscudoRestantes(),
                datosCiv
        );
        return construirDatos(planeta, detalle);
    }

    @Override
    public DatosCuerpo visitarSatelite(Satelite satelite) {
        DetalleCuerpo detalle = DetalleCuerpo.deSatelite(
                satelite.getCienciaGenerada(),
                satelite.getEnergiaGenerada()
        );
        return construirDatos(satelite, detalle);
    }

    @Override
    public DatosCuerpo visitarMeteorito(Meteorito meteorito) {
        DetalleCuerpo detalle = DetalleCuerpo.deMeteorito(
                meteorito.getTiempoVidaRestante(),
                meteorito.isLanzadoPorJugador(),
                obtenerIndice(meteorito.getObjetivo())
        );
        return construirDatos(meteorito, detalle);
    }

    private DatosCuerpo construirDatos(CuerpoCeleste cuerpo, DetalleCuerpo detalle) {
        EstadoCinematica cinematica = new EstadoCinematica(
                cuerpo.getPosicion(),
                cuerpo.getVelocidad(),
                obtenerIndice(cuerpo.getCuerpoOrbitado()),
                cuerpo.getRadioOrbita()
        );
        return new DatosCuerpo(
                cuerpo.getTipoCuerpo(),
                cuerpo.getNombre(),
                cuerpo.getMasa(),
                cuerpo.esSimulado(),
                cinematica,
                detalle
        );
    }
}
