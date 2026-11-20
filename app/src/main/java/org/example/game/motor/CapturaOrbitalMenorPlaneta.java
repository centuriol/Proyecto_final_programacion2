package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import java.util.function.Consumer;
/**
 * Estrategia de captura para satélites y lunas alrededor de planetas.
 */
public class CapturaOrbitalMenorPlaneta implements EstrategiaCapturaOrbital {

    @Override
    public boolean puedeCapturar(CuerpoCeleste ci, CuerpoCeleste cj, double distReal) {
        boolean ciEsMenor = esMenorOrbital(ci);
        boolean cjEsMenor = esMenorOrbital(cj);
        if (!(ciEsMenor ^ cjEsMenor)) return false;

        CuerpoCeleste menor = ciEsMenor ? ci : cj;
        CuerpoCeleste planeta = ciEsMenor ? cj : ci;
        if (!esPlaneta(planeta) || esLunaAbsorbiblePorMasa(menor, planeta)) return false;

        double radioCaptura = calcularRadioCaptura(menor, planeta);
        return distReal <= radioCaptura && estaDisponibleParaCaptura(menor);
    }

    private boolean esMenorOrbital(CuerpoCeleste c) {
        return c != null && c.getTipoCuerpo() != null && c.getTipoCuerpo().puedeOrbitarPlanetas();
    }

    private boolean esPlaneta(CuerpoCeleste c) {
        return c != null && c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta();
    }

    private boolean esLunaAbsorbiblePorMasa(CuerpoCeleste menor, CuerpoCeleste planeta) {
        return menor.getTipoCuerpo() == TipoCuerpo.LUNA
                && planeta.getMasa() >= org.example.game.simulacion.ConfiguracionColisiones.RATIO_ABSORCION_PLANETA_LUNA * menor.getMasa();
    }

    private double calcularRadioCaptura(CuerpoCeleste menor, CuerpoCeleste planeta) {
        return menor.getTipoCuerpo() == TipoCuerpo.LUNA
                ? Math.max(planeta.getRadioAtraccion() * 3.0, 160.0)
                : Math.max(planeta.getRadioAtraccion() * 2.5, 140.0);
    }

    private boolean estaDisponibleParaCaptura(CuerpoCeleste menor) {
        CuerpoCeleste host = menor.getCuerpoOrbitado();
        return !menor.estaOrbitando() || host == null || !host.esSimulado() || host.getTipoCuerpo() == TipoCuerpo.ESTRELLA;
    }

    @Override
    public CuerpoCeleste[] ejecutarCaptura(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje, AsistenteOrbital asistente) {
        boolean ciEsMenor = ci.getTipoCuerpo() != null && ci.getTipoCuerpo().puedeOrbitarPlanetas();
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
