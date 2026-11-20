package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
/**
 * Responsable de la integración numérica del movimiento de los cuerpos celestes (SRP).
 */
public class IntegradorMovimiento implements IntegradorFisico {

    private final double dt;
    private final double velocidadMaxima;

    public IntegradorMovimiento(double dt, double velocidadMaxima) {
        this.dt = dt;
        this.velocidadMaxima = velocidadMaxima;
    }

    public IntegradorMovimiento() {
        this(1.0, 1200.0);
    }

    public void integrar(List<CuerpoCeleste> cuerpos) {
        int n = cuerpos.size();
        double[] radiosPrevios = new double[n];
        Vector2D[] rRelPrevios = new Vector2D[n];
        Vector2D[] vRelPrevios = new Vector2D[n];

        for (int i = 0; i < n; i++) {
            CuerpoCeleste c = cuerpos.get(i);
            CuerpoCeleste anfitrion = c.getCuerpoOrbitado();
            if (anfitrion != null && anfitrion.esSimulado()) {
                Vector2D rRel = c.getPosicion().restar(anfitrion.getPosicion());
                radiosPrevios[i] = rRel.magnitud();
                rRelPrevios[i] = rRel;
                vRelPrevios[i] = c.getVelocidad().restar(anfitrion.getVelocidad());
            }
        }
        for (int i = 0; i < n; i++) {
            integrarCuerpoIndividual(cuerpos.get(i));
        }
        // Primero fijar planetas en órbita solar para que su posición y velocidad sean definitivas
        for (int i = 0; i < n; i++) {
            CuerpoCeleste c = cuerpos.get(i);
            if (!esOrbitaPlanetaria(c)) {
                fijarEnCarrilOrbital(c, radiosPrevios[i], rRelPrevios[i], vRelPrevios[i]);
            }
        }
        // Luego fijar lunas y satélites respecto a la posición y velocidad final de su planeta anfitrión
        for (int i = 0; i < n; i++) {
            CuerpoCeleste c = cuerpos.get(i);
            if (esOrbitaPlanetaria(c)) {
                fijarEnCarrilOrbital(c, radiosPrevios[i], rRelPrevios[i], vRelPrevios[i]);
            }
        }
    }

    private boolean esOrbitaPlanetaria(CuerpoCeleste c) {
        if (c == null || c.getTipoCuerpo() == null || !c.getTipoCuerpo().puedeOrbitarPlanetas()) {
            return false;
        }
        CuerpoCeleste anfitrion = c.getCuerpoOrbitado();
        return anfitrion != null && anfitrion.esSimulado()
                && anfitrion.getTipoCuerpo() != null && anfitrion.getTipoCuerpo().esPlaneta();
    }

    private void integrarCuerpoIndividual(CuerpoCeleste c) {
        double masaValida = Math.max(1e-3, c.getMasa());
        Vector2D aceleracion = c.getFuerza().dividir(masaValida);

        double aMag = aceleracion.magnitud();
        if (aMag > 200.0) {
            aceleracion = aceleracion.normalizar().multiplicar(200.0);
        }

        Vector2D nuevaVel = c.getVelocidad().sumar(aceleracion.multiplicar(dt));

        double vMag = nuevaVel.magnitud();
        if (vMag > velocidadMaxima) {
            nuevaVel = nuevaVel.normalizar().multiplicar(velocidadMaxima);
        }

        Vector2D nuevaPos = c.getPosicion().sumar(nuevaVel.multiplicar(dt));
        c.setPosicion(nuevaPos);
        c.setVelocidad(nuevaVel);
    }

    private void fijarEnCarrilOrbital(CuerpoCeleste c, double radioActual, Vector2D rRelPrevio, Vector2D vRelPrevio) {
        CuerpoCeleste anfitrion = c.getCuerpoOrbitado();
        if (!esAnfitrionValido(c, anfitrion)) return;
        if (esOrbitaPlanetaria(c)) {
            avanzarEnCarrilPlanetario(c, anfitrion, radioActual, rRelPrevio, vRelPrevio);
            return;
        }
        if (esOrbitaSolar(c, anfitrion)) {
            fijarEnCarrilSolar(c, anfitrion, radioActual);
        }
    }

    private boolean esAnfitrionValido(CuerpoCeleste c, CuerpoCeleste anfitrion) {
        return anfitrion != null && anfitrion.esSimulado()
                && c.getTipoCuerpo() != null && anfitrion.getTipoCuerpo() != null;
    }

    private boolean esOrbitaSolar(CuerpoCeleste c, CuerpoCeleste anfitrion) {
        return anfitrion.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.ESTRELLA
                && c.getTipoCuerpo().esPlaneta()
                && c.getRadioOrbita() >= CalculadorOrbital.RADIOS_RANURAS_SOL[0] - 1.0;
    }

    private void fijarEnCarrilSolar(CuerpoCeleste c, CuerpoCeleste anfitrion, double radioActual) {
        double rOrb = c.getRadioOrbita() > 0 ? c.getRadioOrbita() : radioActual;
        Vector2D r = c.getPosicion().restar(anfitrion.getPosicion());
        double dist = r.magnitud();
        Vector2D uRadial = dist > 0.001 ? r.dividir(dist) : new Vector2D(1, 0);

        double radioBase = radioActual > 0.001 ? radioActual : rOrb;
        double radioEfectivo = Math.abs(radioBase - rOrb) <= 0.8 ? rOrb : radioBase;
        c.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(radioEfectivo)));

        Vector2D vRel = c.getVelocidad().restar(anfitrion.getVelocidad());
        double cross = r.x() * vRel.y() - r.y() * vRel.x();
        Vector2D uTangencial = (cross < -1e-4)
                ? new Vector2D(uRadial.y(), -uRadial.x())
                : new Vector2D(-uRadial.y(), uRadial.x());
        double vIdeal = CalculadorOrbital.calcularRapidezOrbital(anfitrion.getMasa(), rOrb);
        c.setVelocidad(anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vIdeal)));
    }

    private void avanzarEnCarrilPlanetario(CuerpoCeleste c, CuerpoCeleste anfitrion, double radioActual,
                                           Vector2D rRelPrevio, Vector2D vRelPrevio) {
        double rOrb = AsistenteOrbital.acotarRadioOrbital(c, anfitrion, c.getRadioOrbita() > 0 ? c.getRadioOrbita() : radioActual);
        c.setRadioOrbita(rOrb);

        Vector2D rBase = (rRelPrevio != null && rRelPrevio.magnitud() > 0.001)
                ? rRelPrevio
                : c.getPosicion().restar(anfitrion.getPosicion());
        Vector2D vBase = (vRelPrevio != null)
                ? vRelPrevio
                : c.getVelocidad().restar(anfitrion.getVelocidad());

        double distBase = rBase.magnitud();
        double theta = distBase > 0.001 ? Math.atan2(rBase.y(), rBase.x()) : 0.0;
        double cross = rBase.x() * vBase.y() - rBase.y() * vBase.x();
        boolean horario = cross < -1e-4;

        double vIdeal = Math.sqrt(75.0 * Math.max(0.2, anfitrion.getMasa() / anfitrion.getTipoCuerpo().masaBase) / Math.max(10.0, rOrb));
        double omega = (horario ? -1.0 : 1.0) * (vIdeal / Math.max(10.0, rOrb));
        double thetaNuevo = theta + omega * dt;

        Vector2D uRadial = new Vector2D(Math.cos(thetaNuevo), Math.sin(thetaNuevo));
        Vector2D uTangencial = horario
                ? new Vector2D(uRadial.y(), -uRadial.x())
                : new Vector2D(-uRadial.y(), uRadial.x());

        c.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(rOrb)));
        c.setVelocidad(anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vIdeal)));
    }

    public double getDt() {
        return dt;
    }

    public double getVelocidadMaxima() {
        return velocidadMaxima;
    }
}
