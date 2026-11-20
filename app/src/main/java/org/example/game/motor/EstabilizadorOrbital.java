package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import java.util.List;

/**
 * SRP: Aplica inicialización y estabilización continua a cuerpos capturados en órbita.
 */
public class EstabilizadorOrbital {

    private static final LimitesOrbitales LIMITES = new LimitesOrbitales();

    private final CalculadorOrbital calculadorOrbital;
    private final AsistenciaOrbitalLibre asistenciaLibre;

    public EstabilizadorOrbital(double G, CalculadorOrbital calculadorOrbital, LimitesOrbitales limites) {
        this.calculadorOrbital = calculadorOrbital != null ? calculadorOrbital : new CalculadorOrbital();
        this.asistenciaLibre = new AsistenciaOrbitalLibre(G, limites != null ? limites : LIMITES);
    }

    public void inicializarVelocidadOrbital(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        Vector2D r = orbitando.getPosicion().restar(anfitrion.getPosicion());
        double d = ajustarPosicionInicialAlRango(orbitando, anfitrion, r);
        r = orbitando.getPosicion().restar(anfitrion.getPosicion());
        orbitando.setRadioOrbita(d);

        Vector2D uRadial = r.dividir(Math.max(0.1, d));
        Vector2D vRel = orbitando.getVelocidad().restar(anfitrion.getVelocidad());
        Vector2D uTangencial = LIMITES.calcularUnitarioTangencial(r, uRadial, vRel);

        double vOrb = calcularVelocidadOrbitalIdeal(orbitando, anfitrion, d);
        orbitando.setVelocidad(anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vOrb)));
    }

    private double ajustarPosicionInicialAlRango(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, Vector2D r) {
        double d = r.magnitud();
        double minOrb = LIMITES.calcularRadioMinimo(orbitando, anfitrion);
        double maxOrb = LIMITES.calcularRadioMaximo(orbitando, anfitrion);
        if (d < minOrb || d > maxOrb) {
            d = Math.max(minOrb, Math.min(maxOrb, d));
            Vector2D norm = (r.magnitud() > 0.001) ? r.normalizar() : new Vector2D(1, 0);
            orbitando.setPosicion(anfitrion.getPosicion().sumar(norm.multiplicar(d)));
        }
        return d;
    }

    public void aplicarDinamicaOrbitalEstable(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        boolean esMenor = LIMITES.esMenorAlrededorDePlaneta(orbitando, anfitrion);
        if (!esMenor && distReal > 1500.0) {
            orbitando.setCuerpoOrbitado(null);
            return;
        }
        Vector2D r = orbitando.getPosicion().restar(anfitrion.getPosicion());
        double rMag = r.magnitud();
        Vector2D uRadial = rMag > 0.001 ? r.dividir(rMag) : new Vector2D(1, 0);
        double d = Math.max(0.1, rMag);

        boolean esPlanetaEnSol = anfitrion.getTipoCuerpo() == TipoCuerpo.ESTRELLA && LIMITES.esPlaneta(orbitando);
        double rDeseado = resolverRadioDeseado(orbitando, anfitrion, d, esPlanetaEnSol);
        orbitando.setRadioOrbita(rDeseado);

        boolean enRanuraSolar = esPlanetaEnSol && rDeseado >= CalculadorOrbital.RADIOS_RANURAS_SOL[0] - 1.0;
        d = ajustarDistanciaOrbital(orbitando, anfitrion, uRadial, d, rDeseado, enRanuraSolar, esMenor);
        aplicarVelocidadYFuerzaCentripeta(orbitando, anfitrion, r, uRadial, d, rDeseado, enRanuraSolar || esMenor);
    }

    private double resolverRadioDeseado(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double d, boolean esPlanetaEnSol) {
        double rDeseado = orbitando.getRadioOrbita();
        if (rDeseado <= 0 || (esPlanetaEnSol && Math.abs(d - rDeseado) > 250.0)) {
            double radioZonaLava = TransformadorCuerpos.calcularRadioActivacionPorroLava(orbitando, anfitrion);
            rDeseado = (esPlanetaEnSol && d > radioZonaLava) ? CalculadorOrbital.ajustarARanuraSolar(d) : d;
        }
        return LIMITES.acotarRadio(orbitando, anfitrion, rDeseado);
    }

    private double ajustarDistanciaOrbital(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, Vector2D uRadial,
                                           double d, double rDeseado, boolean enRanuraSolar, boolean esMenor) {
        if (enRanuraSolar) {
            double nuevoD = avanzarHaciaRanura(d, rDeseado);
            orbitando.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(nuevoD)));
            return nuevoD;
        }
        double minOrb = LIMITES.calcularRadioMinimo(orbitando, anfitrion);
        double maxOrb = LIMITES.calcularRadioMaximo(orbitando, anfitrion);
        if (esMenor && (d < minOrb || d > maxOrb)) {
            double clamped = Math.max(minOrb, Math.min(maxOrb, d));
            orbitando.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(clamped)));
            return clamped;
        }
        if (Math.abs(d - rDeseado) > 0.01) {
            double ajustado = d + (rDeseado - d) * 0.15;
            orbitando.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(ajustado)));
            return ajustado;
        }
        return d;
    }

    private double avanzarHaciaRanura(double d, double rDeseado) {
        double diff = rDeseado - d;
        if (Math.abs(diff) <= 0.8) {
            return rDeseado;
        }
        double paso = diff * 0.15;
        if (Math.abs(paso) < 0.8) {
            paso = Math.copySign(0.8, diff);
        }
        return d + paso;
    }

    private void aplicarVelocidadYFuerzaCentripeta(CuerpoCeleste orbitando, CuerpoCeleste anfitrion,
                                                   Vector2D r, Vector2D uRadial, double d,
                                                   double rDeseado, boolean ajusteDirecto) {
        Vector2D vRel = orbitando.getVelocidad().restar(anfitrion.getVelocidad());
        Vector2D uTangencial = LIMITES.calcularUnitarioTangencial(r, uRadial, vRel);
        double vOrbIdeal = calcularVelocidadOrbitalIdeal(orbitando, anfitrion, rDeseado);

        Vector2D vDeseada = anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vOrbIdeal));
        if (ajusteDirecto) {
            orbitando.setVelocidad(vDeseada);
        } else {
            orbitando.setVelocidad(orbitando.getVelocidad().sumar(vDeseada.restar(orbitando.getVelocidad()).multiplicar(0.15)));
        }

        double aCentripeta = (vOrbIdeal * vOrbIdeal) / Math.max(1.0, d);
        orbitando.aplicarFuerza(uRadial.multiplicar(-1.0 * orbitando.getMasa() * aCentripeta));
        if (anfitrion.getMasa() > 0) {
            Vector2D aAnfitrion = anfitrion.getFuerza().dividir(anfitrion.getMasa());
            orbitando.aplicarFuerza(aAnfitrion.multiplicar(orbitando.getMasa()));
        }
    }

    public void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos, double asistenciaOrbital) {
        asistenciaLibre.aplicarAsistenciaOrbital(cuerpos, asistenciaOrbital);
    }

    public double calcularAsistenciaEfectiva(double masa, double asistenciaOrbital) {
        return asistenciaLibre.calcularAsistenciaEfectiva(masa, asistenciaOrbital);
    }

    public double calcularVelocidadOrbitalIdeal(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double rOrb) {
        double d = Math.max(10.0, rOrb);
        if (LIMITES.esMenorAlrededorDePlaneta(orbitando, anfitrion)) {
            double masaBase = anfitrion.getTipoCuerpo().masaBase;
            double factorMasa = masaBase > 0 ? Math.max(0.2, anfitrion.getMasa() / masaBase) : 1.0;
            return Math.sqrt(75.0 * factorMasa / d);
        }
        return calculadorOrbital.calcularRapidezOrbital(anfitrion.getMasa(), d, asistenciaLibre.getG());
    }
}
