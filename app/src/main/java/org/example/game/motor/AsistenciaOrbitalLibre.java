package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import java.util.List;

/**
 * SRP: Aplica asistencia orbital suave a cuerpos libres bajo la influencia del astro dominante.
 */
public class AsistenciaOrbitalLibre {

    private final double G;
    private final LimitesOrbitales limites;

    public AsistenciaOrbitalLibre(double G, LimitesOrbitales limites) {
        this.G = G;
        this.limites = limites != null ? limites : new LimitesOrbitales();
    }

    public double getG() {
        return G;
    }

    public void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos, double asistenciaOrbital) {
        if (asistenciaOrbital <= 0) return;
        for (CuerpoCeleste c : cuerpos) {
            if (esCandidatoAsistencia(c)) {
                asistirCuerpoLibre(c, cuerpos, asistenciaOrbital);
            }
        }
    }

    private boolean esCandidatoAsistencia(CuerpoCeleste c) {
        if (c == null || c.estaOrbitando() || c.getTipoCuerpo() == TipoCuerpo.METEORITO) return false;
        if (c.getTipoCuerpo() != null && c.getTipoCuerpo().puedeOrbitarPlanetas()) return false;
        return c.getMasa() < TipoCuerpo.ESTRELLA.masaBase * 0.5;
    }

    private void asistirCuerpoLibre(CuerpoCeleste c, List<CuerpoCeleste> cuerpos, double asistenciaOrbital) {
        CuerpoCeleste primario = encontrarCuerpoDominante(c, cuerpos);
        if (primario == null) return;
        Vector2D r = c.getPosicion().restar(primario.getPosicion());
        double dist = r.magnitud();
        if (dist <= 30.0 || dist >= 1200.0) return;

        Vector2D vRel = c.getVelocidad().restar(primario.getVelocidad());
        if (vRel.magnitud() <= 0.1) return;

        Vector2D unitRadial = r.dividir(dist);
        Vector2D unitTangencial = limites.calcularUnitarioTangencial(r, unitRadial, vRel);
        double vIdeal = Math.sqrt(G * primario.getMasa() / dist);
        Vector2D vObjetivo = unitTangencial.multiplicar(vIdeal);
        double asistenciaEfectiva = calcularAsistenciaEfectiva(c.getMasa(), asistenciaOrbital);
        c.aplicarFuerza(vObjetivo.restar(vRel).multiplicar(c.getMasa() * asistenciaEfectiva));
    }

    public double calcularAsistenciaEfectiva(double masa, double asistenciaOrbital) {
        double factorMasaCuerpo = masa / TipoCuerpo.PLANETA_ROCOSO.masaBase;
        return asistenciaOrbital / (1.0 + Math.log10(Math.max(1.0, factorMasaCuerpo)));
    }

    private CuerpoCeleste encontrarCuerpoDominante(CuerpoCeleste cuerpo, List<CuerpoCeleste> todos) {
        CuerpoCeleste dominante = null;
        double maxAtraccion = 0;
        for (CuerpoCeleste otro : todos) {
            double atraccion = calcularAtraccionDominante(cuerpo, otro);
            if (atraccion > maxAtraccion) {
                maxAtraccion = atraccion;
                dominante = otro;
            }
        }
        return dominante;
    }

    private double calcularAtraccionDominante(CuerpoCeleste cuerpo, CuerpoCeleste otro) {
        if (otro == cuerpo || !otro.esSimulado() || otro.getMasa() < TipoCuerpo.ESTRELLA.masaBase * 0.1) {
            return 0.0;
        }
        double dist = cuerpo.getPosicion().distanciaA(otro.getPosicion());
        return dist < 1.0 ? 0.0 : otro.getMasa() / (dist * dist);
    }
}
