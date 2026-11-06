package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
/**
 * Responsable de la integración numérica del movimiento de los cuerpos celestes (SRP).
 */
public class IntegradorMovimiento {

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
        double[] radiosPrevios = new double[cuerpos.size()];
        for (int i = 0; i < cuerpos.size(); i++) {
            CuerpoCeleste c = cuerpos.get(i);
            radiosPrevios[i] = c.getCuerpoOrbitado() != null
                    ? c.getPosicion().distanciaA(c.getCuerpoOrbitado().getPosicion())
                    : 0.0;
            integrarCuerpoIndividual(c);
        }
        for (int i = 0; i < cuerpos.size(); i++) {
            fijarEnCarrilOrbital(cuerpos.get(i), radiosPrevios[i]);
        }
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

    private void fijarEnCarrilOrbital(CuerpoCeleste c, double radioActual) {
        CuerpoCeleste anfitrion = c.getCuerpoOrbitado();
        if (anfitrion == null || c.getTipoCuerpo() == null || anfitrion.getTipoCuerpo() == null) return;

        boolean esSolar = anfitrion.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.ESTRELLA
                && c.getTipoCuerpo().esPlaneta()
                && c.getRadioOrbita() >= CalculadorOrbital.RADIOS_RANURAS_SOL[0] - 1.0;
        boolean esPlanetario = anfitrion.getTipoCuerpo().esPlaneta() && c.getTipoCuerpo().puedeOrbitarPlanetas();
        if (!esSolar && !esPlanetario) return;

        double rOrb = c.getRadioOrbita() > 0 ? c.getRadioOrbita() : radioActual;
        Vector2D r = c.getPosicion().restar(anfitrion.getPosicion());
        double dist = r.magnitud();
        if (dist <= 0.001 || radioActual <= 0.001) return;

        double radioEfectivo = Math.abs(radioActual - rOrb) <= 0.8 ? rOrb : radioActual;
        Vector2D uRadial = r.dividir(dist);
        c.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(radioEfectivo)));

        Vector2D vRel = c.getVelocidad().restar(anfitrion.getVelocidad());
        double cross = r.x() * vRel.y() - r.y() * vRel.x();
        Vector2D uTangencial = (cross < -1e-4)
                ? new Vector2D(uRadial.y(), -uRadial.x())
                : new Vector2D(-uRadial.y(), uRadial.x());
        double vIdeal = esSolar
                ? CalculadorOrbital.calcularRapidezOrbital(anfitrion.getMasa(), rOrb)
                : Math.sqrt(75.0 * Math.max(0.2, anfitrion.getMasa() / anfitrion.getTipoCuerpo().masaBase) / Math.max(10.0, radioEfectivo));
        c.setVelocidad(anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vIdeal)));
    }

    public double getDt() {
        return dt;
    }

    public double getVelocidadMaxima() {
        return velocidadMaxima;
    }
}
