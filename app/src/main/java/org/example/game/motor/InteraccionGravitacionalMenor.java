package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * SRP: Identifica y filtra interacciones gravitacionales especiales de satélites y lunas en órbita.
 */
public class InteraccionGravitacionalMenor {

    public boolean debeIgnorarPar(CuerpoCeleste ci, CuerpoCeleste cj) {
        if (esMeteorito(ci) || esMeteorito(cj)) return true;
        if (esInteraccionEstrellaYMenor(ci, cj)) return true;
        return esParSateliteYLunaOSatelites(ci, cj);
    }

    public boolean esParConSatelite(CuerpoCeleste ci, CuerpoCeleste cj) {
        return ci.getTipoCuerpo() == TipoCuerpo.SATELITE || cj.getTipoCuerpo() == TipoCuerpo.SATELITE;
    }

    public boolean puedeInteractuarSateliteCon(CuerpoCeleste ci, CuerpoCeleste cj) {
        CuerpoCeleste sat = ci.getTipoCuerpo() == TipoCuerpo.SATELITE ? ci : cj;
        CuerpoCeleste otro = (sat == ci) ? cj : ci;
        if (esSateliteOrbitandoPlaneta(sat)) {
            return otro == sat.getCuerpoOrbitado();
        }
        return esPlaneta(otro);
    }

    public boolean esLunaOrbitandoPlaneta(CuerpoCeleste c) {
        if (c == null || c.getTipoCuerpo() != TipoCuerpo.LUNA || !c.estaOrbitando()) return false;
        return esAnfitrionPlanetaActivo(c.getCuerpoOrbitado());
    }

    public boolean esParPlanetaLuna(CuerpoCeleste ci, CuerpoCeleste cj) {
        return (esPlaneta(ci) && cj.getTipoCuerpo() == TipoCuerpo.LUNA)
                || (esPlaneta(cj) && ci.getTipoCuerpo() == TipoCuerpo.LUNA);
    }

    private boolean esMeteorito(CuerpoCeleste c) {
        return c.getTipoCuerpo() == TipoCuerpo.METEORITO;
    }

    private boolean esPlaneta(CuerpoCeleste c) {
        return c != null && c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta();
    }

    private boolean esAnfitrionPlanetaActivo(CuerpoCeleste host) {
        return host != null && host.esSimulado() && esPlaneta(host);
    }

    private boolean esSateliteOrbitandoPlaneta(CuerpoCeleste sat) {
        return sat != null && sat.getTipoCuerpo() == TipoCuerpo.SATELITE
                && sat.estaOrbitando() && esAnfitrionPlanetaActivo(sat.getCuerpoOrbitado());
    }

    private boolean esInteraccionEstrellaYMenor(CuerpoCeleste ci, CuerpoCeleste cj) {
        TipoCuerpo ti = ci.getTipoCuerpo();
        TipoCuerpo tj = cj.getTipoCuerpo();
        if (ti == null || tj == null) return false;
        return (ti == TipoCuerpo.ESTRELLA && tj.puedeOrbitarPlanetas())
                || (tj == TipoCuerpo.ESTRELLA && ti.puedeOrbitarPlanetas());
    }

    private boolean esParSateliteYLunaOSatelites(CuerpoCeleste ci, CuerpoCeleste cj) {
        TipoCuerpo ti = ci.getTipoCuerpo();
        TipoCuerpo tj = cj.getTipoCuerpo();
        if (ti == TipoCuerpo.SATELITE && tj == TipoCuerpo.SATELITE) return true;
        return (ti == TipoCuerpo.SATELITE && tj == TipoCuerpo.LUNA)
                || (tj == TipoCuerpo.SATELITE && ti == TipoCuerpo.LUNA);
    }
}
