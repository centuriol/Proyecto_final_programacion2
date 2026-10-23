package org.example.game.motor;

import org.example.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Encapsula la lógica de asistencia orbital, captura gravitacional y estabilización dinámica de órbitas.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de la dinámica orbital permisiva y cálculo de órbitas estables.
 * - DIP: Desacoplado del motor de física principal y notificaciones a UI.
 */
public class AsistenteOrbital {

    private final double G;

    public AsistenteOrbital(double G) {
        this.G = G;
    }

    public AsistenteOrbital() {
        this(ConstantesFisicas.G_ESCALADO);
    }

    /**
     * Intenta capturar dos cuerpos en órbita asistida (Luna-Planeta o Planeta-Estrella/BH).
     * Retorna el cuerpo orbitando si se estableció un enlace orbital, o null.
     */
    public CuerpoCeleste[] verificarYCapturarOrbita(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje) {
        if (ci.estaOrbitando(cj)) {
            return new CuerpoCeleste[]{ci, cj};
        } else if (cj.estaOrbitando(ci)) {
            return new CuerpoCeleste[]{cj, ci};
        }

        // Caso A: Satélite o Luna y Planeta
        boolean ciEsMenor = (ci.getTipoCuerpo() == TipoCuerpo.LUNA || ci.getTipoCuerpo() == TipoCuerpo.SATELITE);
        boolean cjEsMenor = (cj.getTipoCuerpo() == TipoCuerpo.LUNA || cj.getTipoCuerpo() == TipoCuerpo.SATELITE);

        if (ciEsMenor ^ cjEsMenor) {
            CuerpoCeleste menor = ciEsMenor ? ci : cj;
            CuerpoCeleste planeta = ciEsMenor ? cj : ci;

            if (planeta.getTipoCuerpo() != null && planeta.getTipoCuerpo().esPlaneta()) {
                double radioCaptura;
                if (menor.getTipoCuerpo() == TipoCuerpo.LUNA) {
                    radioCaptura = Math.max(planeta.getRadioAtraccion() * 2.2, 120.0);
                } else {
                    radioCaptura = Math.max(planeta.getRadioAtraccion(), 65.0);
                }

                if (distReal <= radioCaptura && !menor.estaOrbitando()) {
                    menor.setCuerpoOrbitado(planeta);
                    menor.setRadioOrbita(distReal);
                    if (listenerMensaje != null) {
                        listenerMensaje.accept(menor.getNombre() + " esta orbitando " + planeta.getNombre());
                    }
                    inicializarVelocidadOrbital(menor, planeta, distReal);
                    return new CuerpoCeleste[]{menor, planeta};
                }
            }
        }

        // Caso B: Planeta y Estrella / Agujero Negro
        boolean ciEsPlaneta = ci.getTipoCuerpo() != null && ci.getTipoCuerpo().name().startsWith("PLANETA");
        boolean cjEsPlaneta = cj.getTipoCuerpo() != null && cj.getTipoCuerpo().name().startsWith("PLANETA");
        CuerpoCeleste planeta = ciEsPlaneta ? ci : (cjEsPlaneta ? cj : null);
        CuerpoCeleste estrella = (planeta == ci) ? cj : ci;

        boolean esPlanetaYEstrella = (planeta != null && estrella != null && estrella.getTipoCuerpo() != null &&
                (estrella.getTipoCuerpo() == TipoCuerpo.ESTRELLA ||
                 estrella.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO ||
                 estrella.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO));

        if (esPlanetaYEstrella) {
            double radioCapturaPlaneta = estrella.getRadioAtraccion() * 7.0;
            double radioMinimoCaptura = estrella.getRadio() + planeta.getRadio() + 5.0;

            if (!planeta.estaOrbitando() && distReal > radioMinimoCaptura && distReal <= radioCapturaPlaneta) {
                planeta.setCuerpoOrbitado(estrella);
                planeta.setRadioOrbita(distReal);
                if (listenerMensaje != null) {
                    listenerMensaje.accept(planeta.getNombre() + " esta orbitando " + estrella.getNombre());
                }
                inicializarVelocidadOrbital(planeta, estrella, distReal);
                return new CuerpoCeleste[]{planeta, estrella};
            }
        }

        return null;
    }

    public void inicializarVelocidadOrbital(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        Vector2D r = orbitando.getPosicion().restar(anfitrion.getPosicion());
        double d = r.magnitud();
        double minOrb;
        if (orbitando.getTipoCuerpo() == TipoCuerpo.LUNA && anfitrion.getTipoCuerpo() != null && anfitrion.getTipoCuerpo().esPlaneta()) {
            minOrb = Math.max(60.0, anfitrion.getRadio() + orbitando.getRadio() + 35.0);
        } else {
            minOrb = anfitrion.getRadio() + orbitando.getRadio() + 5.0;
        }
        if (d < minOrb) {
            d = minOrb;
            Vector2D norm = (r.magnitud() > 0.001) ? r.normalizar() : new Vector2D(1, 0);
            orbitando.setPosicion(anfitrion.getPosicion().sumar(norm.multiplicar(d)));
            r = norm.multiplicar(d);
        }

        orbitando.setRadioOrbita(d);

        Vector2D uRadial = r.dividir(Math.max(0.1, d));
        Vector2D vRel = orbitando.getVelocidad().restar(anfitrion.getVelocidad());
        Vector2D uTangencial = calcularUnitarioTangencial(r, uRadial, vRel);

        double vOrb = calcularVelocidadOrbitalIdeal(orbitando, anfitrion, d);
        Vector2D vFinal = anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vOrb));
        orbitando.setVelocidad(vFinal);
    }

    public void aplicarDinamicaOrbitalEstable(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        double limiteEscape;
        boolean esMenorAlrededorDePlaneta = (orbitando.getTipoCuerpo() == TipoCuerpo.LUNA || orbitando.getTipoCuerpo() == TipoCuerpo.SATELITE) &&
                anfitrion.getTipoCuerpo() != null && anfitrion.getTipoCuerpo().esPlaneta();
        if (esMenorAlrededorDePlaneta) {
            limiteEscape = Math.max(orbitando.getRadioAtraccion() * 3.5, 300.0);
        } else {
            limiteEscape = 1500.0;
        }

        if (distReal > limiteEscape) {
            orbitando.setCuerpoOrbitado(null);
            return;
        }

        Vector2D r = orbitando.getPosicion().restar(anfitrion.getPosicion());
        double d = Math.max(0.1, r.magnitud());
        Vector2D uRadial = r.dividir(d);

        double rDeseado = orbitando.getRadioOrbita();
        if (rDeseado <= 0) {
            rDeseado = d;
            orbitando.setRadioOrbita(rDeseado);
        }

        double minOrb;
        if (orbitando.getTipoCuerpo() == TipoCuerpo.LUNA && anfitrion.getTipoCuerpo() != null && anfitrion.getTipoCuerpo().esPlaneta()) {
            minOrb = Math.max(60.0, anfitrion.getRadio() + orbitando.getRadio() + 35.0);
        } else {
            minOrb = anfitrion.getRadio() + orbitando.getRadio() + 5.0;
        }
        if (rDeseado < minOrb) {
            rDeseado = minOrb;
            orbitando.setRadioOrbita(rDeseado);
        }

        if (Math.abs(d - rDeseado) > 0.01) {
            double dAjustado = d + (rDeseado - d) * 0.15;
            orbitando.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(dAjustado)));
            d = dAjustado;
        }

        Vector2D vRel = orbitando.getVelocidad().restar(anfitrion.getVelocidad());
        Vector2D uTangencial = calcularUnitarioTangencial(r, uRadial, vRel);

        double vOrbIdeal = calcularVelocidadOrbitalIdeal(orbitando, anfitrion, rDeseado);

        Vector2D vDeseada = anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vOrbIdeal));
        orbitando.setVelocidad(orbitando.getVelocidad().sumar(vDeseada.restar(orbitando.getVelocidad()).multiplicar(0.15)));

        double aCentripeta = (vOrbIdeal * vOrbIdeal) / Math.max(1.0, d);
        Vector2D fCentripeta = uRadial.multiplicar(-1.0 * orbitando.getMasa() * aCentripeta);
        orbitando.aplicarFuerza(fCentripeta);

        if (anfitrion.getMasa() > 0) {
            Vector2D aAnfitrion = anfitrion.getFuerza().dividir(anfitrion.getMasa());
            orbitando.aplicarFuerza(aAnfitrion.multiplicar(orbitando.getMasa()));
        }
    }

    public void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos, double asistenciaOrbital) {
        if (asistenciaOrbital <= 0) return;

        for (CuerpoCeleste c : cuerpos) {
            if (c.getTipoCuerpo().esMasivo && c.getMasa() >= TipoCuerpo.ESTRELLA.masaBase * 0.5) {
                continue;
            }
            if (c.estaOrbitando()) {
                continue;
            }

            CuerpoCeleste primario = encontrarCuerpoDominante(c, cuerpos);
            if (primario != null) {
                Vector2D r = c.getPosicion().restar(primario.getPosicion());
                double dist = r.magnitud();
                if (dist > 30.0 && dist < 1200.0) {
                    double vIdeal = Math.sqrt(G * primario.getMasa() / dist);
                    Vector2D vRel = c.getVelocidad().restar(primario.getVelocidad());
                    double speedRel = vRel.magnitud();

                    if (speedRel > 0.1) {
                        double cross = r.x * vRel.y - r.y * vRel.x;
                        boolean horario = cross < 0;

                        Vector2D unitRadial = r.dividir(dist);
                        Vector2D unitTangencial = horario
                                ? new Vector2D(unitRadial.y, -unitRadial.x)
                                : new Vector2D(-unitRadial.y, unitRadial.x);

                        Vector2D vObjetivo = unitTangencial.multiplicar(vIdeal);
                        double asistenciaEfectiva = calcularAsistenciaEfectiva(c.getMasa(), asistenciaOrbital);

                        Vector2D correccion = vObjetivo.restar(vRel).multiplicar(c.getMasa() * asistenciaEfectiva);
                        c.aplicarFuerza(correccion);
                    }
                }
            }
        }
    }

    public double calcularAsistenciaEfectiva(double masa, double asistenciaOrbital) {
        double factorMasaCuerpo = masa / TipoCuerpo.PLANETA_ROCOSO.masaBase;
        return asistenciaOrbital / (1.0 + Math.log10(Math.max(1.0, factorMasaCuerpo)));
    }

    public double calcularVelocidadOrbitalIdeal(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double rOrb) {
        double d = Math.max(10.0, rOrb);
        boolean esCuerpoMenorOrbital = (orbitando.getTipoCuerpo() == TipoCuerpo.LUNA || orbitando.getTipoCuerpo() == TipoCuerpo.SATELITE) &&
                anfitrion.getTipoCuerpo() != null && anfitrion.getTipoCuerpo().esPlaneta();
        if (esCuerpoMenorOrbital) {
            double factorMasa = (anfitrion.getTipoCuerpo() != null && anfitrion.getTipoCuerpo().masaBase > 0)
                    ? Math.max(0.2, anfitrion.getMasa() / anfitrion.getTipoCuerpo().masaBase)
                    : 1.0;
            return Math.sqrt(75.0 * factorMasa / d);
        }
        return Math.sqrt(G * anfitrion.getMasa() / d);
    }

    private CuerpoCeleste encontrarCuerpoDominante(CuerpoCeleste cuerpo, List<CuerpoCeleste> todos) {
        CuerpoCeleste dominante = null;
        double maxAtraccion = 0;

        for (CuerpoCeleste otro : todos) {
            if (otro == cuerpo || !otro.esSimulado()) continue;
            if (!otro.getTipoCuerpo().esMasivo && otro.getMasa() < TipoCuerpo.ESTRELLA.masaBase * 0.1) continue;

            double dist = cuerpo.getPosicion().distanciaA(otro.getPosicion());
            if (dist < 1.0) continue;

            double atraccion = otro.getMasa() / (dist * dist);
            if (atraccion > maxAtraccion) {
                maxAtraccion = atraccion;
                dominante = otro;
            }
        }

        return dominante;
    }

    private Vector2D calcularUnitarioTangencial(Vector2D r, Vector2D uRadial, Vector2D vRel) {
        double cross = r.x * vRel.y - r.y * vRel.x;
        boolean horario = cross < -1e-4;
        return horario
                ? new Vector2D(uRadial.y, -uRadial.x)
                : new Vector2D(-uRadial.y, uRadial.x);
    }
}
