package org.example.game.motor;

import org.example.game.cuerpo.*;
import org.example.game.simulacion.ConfiguracionColisiones;
import java.util.List;
import java.util.Set;

/**
 * Responsable de la detección de contactos físicos y derivación al resolvedor de colisiones (SRP).
 */
public class DetectorColisiones implements DetectorContactoFisico {

    private static final FabricaMotorFisica FABRICA = new FabricaMotorFisica();

    private final ResolucionColision resolvedorColisiones;
    private final SeparadorContactoOrbital separadorContacto;

    public DetectorColisiones(ResolucionColision resolvedorColisiones) {
        this(resolvedorColisiones, FABRICA.crearSeparadorContactoPorDefecto());
    }

    public DetectorColisiones(ResolucionColision resolvedorColisiones, SeparadorContactoOrbital separadorContacto) {
        this.resolvedorColisiones = resolvedorColisiones;
        this.separadorContacto = separadorContacto != null ? separadorContacto : FABRICA.crearSeparadorContactoPorDefecto();
    }

    @Override
    public void detectarYResolver(List<CuerpoCeleste> simulados, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        FABRICA.crearInterceptorPorDefecto(resolvedorColisiones).interceptar(simulados, aEliminar, aAgregar);
        for (int i = 0; i < simulados.size(); i++) {
            CuerpoCeleste a = simulados.get(i);
            if (!estaActivo(a, aEliminar)) continue;
            evaluarParesDesde(i, a, simulados, aEliminar, aAgregar);
        }
    }

    private void evaluarParesDesde(int i, CuerpoCeleste a, List<CuerpoCeleste> simulados,
                                   Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        for (int j = i + 1; j < simulados.size(); j++) {
            if (!estaActivo(a, aEliminar)) break;
            CuerpoCeleste b = simulados.get(j);
            if (!estaActivo(b, aEliminar) || esParIgnorado(a, b)) continue;
            if (a.getPosicion().distanciaA(b.getPosicion()) < a.getRadio() + b.getRadio()) {
                procesarContactoPar(a, b, aEliminar, aAgregar);
            }
        }
    }

    private boolean estaActivo(CuerpoCeleste c, Set<CuerpoCeleste> aEliminar) {
        return c != null && c.esSimulado() && !aEliminar.contains(c);
    }

    private boolean esParIgnorado(CuerpoCeleste a, CuerpoCeleste b) {
        boolean aSat = a.getTipoCuerpo() == TipoCuerpo.SATELITE;
        boolean bSat = b.getTipoCuerpo() == TipoCuerpo.SATELITE;
        if (aSat && bSat) return true;
        return (aSat && b.getTipoCuerpo() == TipoCuerpo.LUNA)
                || (bSat && a.getTipoCuerpo() == TipoCuerpo.LUNA);
    }

    private void procesarContactoPar(CuerpoCeleste a, CuerpoCeleste b,
                                     Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        if (esMeteoritoDirigidoPasandoPorEstrella(a, b)) return;
        if (manejarContactoSatelite(a, b, aEliminar, aAgregar)) return;
        if (manejarContactoPlanetaLuna(a, b, aEliminar, aAgregar)) return;
        if (separarSiEstanEnOrbitaMutua(a, b)) return;
        resolvedorColisiones.resolverColision(a, b, aEliminar, aAgregar);
    }

    private boolean separarSiEstanEnOrbitaMutua(CuerpoCeleste a, CuerpoCeleste b) {
        if (a.estaOrbitando(b)) {
            separadorContacto.separar(a, b);
            return true;
        }
        if (b.estaOrbitando(a)) {
            separadorContacto.separar(b, a);
            return true;
        }
        return false;
    }

    private boolean manejarContactoSatelite(CuerpoCeleste a, CuerpoCeleste b,
                                            Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        boolean aSat = a.getTipoCuerpo() == TipoCuerpo.SATELITE;
        boolean bSat = b.getTipoCuerpo() == TipoCuerpo.SATELITE;
        if (!aSat && !bSat) return false;
        CuerpoCeleste satelite = aSat ? a : b;
        CuerpoCeleste otro = aSat ? b : a;
        if (otro.getTipoCuerpo() == TipoCuerpo.METEORITO) {
            resolvedorColisiones.resolverColision(satelite, otro, aEliminar, aAgregar);
            return true;
        }
        if (separadorContacto.estaOrbitandoPlaneta(satelite)) {
            if (otro == satelite.getCuerpoOrbitado()) separadorContacto.separar(satelite, otro);
            return true;
        }
        return manejarContactoSateliteLibre(satelite, otro);
    }

    private boolean manejarContactoSateliteLibre(CuerpoCeleste satelite, CuerpoCeleste otro) {
        TipoCuerpo tipoOtro = otro.getTipoCuerpo();
        if (tipoOtro != null && tipoOtro.esPlaneta()) {
            satelite.setCuerpoOrbitado(otro);
            satelite.setRadioOrbita(AsistenteOrbital.calcularRadioOrbitalMinimo(satelite, otro));
            separadorContacto.separar(satelite, otro);
            return true;
        }
        return tipoOtro != TipoCuerpo.AGUJERO_NEGRO && tipoOtro != TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO;
    }

    private boolean esMeteoritoDirigidoPasandoPorEstrella(CuerpoCeleste a, CuerpoCeleste b) {
        CuerpoCeleste met = a.getTipoCuerpo() == TipoCuerpo.METEORITO ? a : (b.getTipoCuerpo() == TipoCuerpo.METEORITO ? b : null);
        CuerpoCeleste otro = (met == a) ? b : a;
        if (met instanceof Meteorito m && otro != null && otro.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
            return m.getObjetivo() != null && m.getObjetivo().esSimulado() && m.getObjetivo() != otro;
        }
        return false;
    }

    private boolean manejarContactoPlanetaLuna(CuerpoCeleste a, CuerpoCeleste b,
                                               Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        CuerpoCeleste planeta = identificarPlanetaEnParLuna(a, b);
        if (planeta == null) return false;
        CuerpoCeleste luna = (planeta == a) ? b : a;
        if (separadorContacto.estaOrbitandoOtroPlaneta(luna, planeta)) {
            return true;
        }
        if (planeta.getMasa() >= ConfiguracionColisiones.RATIO_ABSORCION_PLANETA_LUNA * luna.getMasa()) {
            luna.setCuerpoOrbitado(null);
            resolvedorColisiones.resolverColision(a, b, aEliminar, aAgregar);
        } else {
            luna.setCuerpoOrbitado(planeta);
            luna.setRadioOrbita(AsistenteOrbital.calcularRadioOrbitalMinimo(luna, planeta));
            separadorContacto.separar(luna, planeta);
        }
        return true;
    }

    private CuerpoCeleste identificarPlanetaEnParLuna(CuerpoCeleste a, CuerpoCeleste b) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();
        if (ta == null || tb == null) return null;
        if (ta.esPlaneta() && tb == TipoCuerpo.LUNA) return a;
        if (tb.esPlaneta() && ta == TipoCuerpo.LUNA) return b;
        return null;
    }
}
