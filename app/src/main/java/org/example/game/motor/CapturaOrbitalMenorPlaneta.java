package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import java.util.function.Consumer;
/**
 * Estrategia de captura para satélites y lunas alrededor de planetas.
 */
public class CapturaOrbitalMenorPlaneta implements EstrategiaCapturaOrbital {

    private boolean esMenorOrbital(CuerpoCeleste c) {
        if (c == null || !c.permiteReembolso()) return false;
        if (c.getTipoCuerpo() != null) {
            return c.getTipoCuerpo().puedeOrbitarPlanetas();
        }
        return c.getMasa() < 1e23;
    }

    private boolean esPlanetaAnfitrion(CuerpoCeleste c) {
        if (c == null) return false;
        if (c.getTipoCuerpo() != null && c.getTipoCuerpo().puedeOrbitarPlanetas()) {
            return false;
        }
        return c.getMasa() >= 1e22 && c.getMasa() <= 1e28;
    }

    @Override
    public boolean puedeCapturar(CuerpoCeleste ci, CuerpoCeleste cj, double distReal) {
        boolean ciEsMenor = esMenorOrbital(ci);
        boolean cjEsMenor = esMenorOrbital(cj);

        if (ciEsMenor ^ cjEsMenor) {
            CuerpoCeleste menor = ciEsMenor ? ci : cj;
            CuerpoCeleste planeta = ciEsMenor ? cj : ci;

            if (esPlanetaAnfitrion(planeta)) {
                boolean esLuna = menor.getMasa() >= 1e22;
                double radioCaptura = esLuna
                        ? Math.max(planeta.getRadioAtraccion() * 2.2, 120.0)
                        : Math.max(planeta.getRadioAtraccion(), 65.0);

                return distReal <= radioCaptura && !menor.estaOrbitando();
            }
        }
        return false;
    }

    @Override
    public CuerpoCeleste[] ejecutarCaptura(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje, AsistenteOrbital asistente) {
        boolean ciEsMenor = esMenorOrbital(ci);
        CuerpoCeleste menor = ciEsMenor ? ci : cj;
        CuerpoCeleste planeta = ciEsMenor ? cj : ci;

        menor.setCuerpoOrbitado(planeta);
        menor.setRadioOrbita(distReal);
        if (listenerMensaje != null) {
            listenerMensaje.accept(menor.getNombre() + " esta orbitando " + planeta.getNombre());
        }
        asistente.inicializarVelocidadOrbital(menor, planeta, distReal);
        return new CuerpoCeleste[]{menor, planeta};
    }
}
