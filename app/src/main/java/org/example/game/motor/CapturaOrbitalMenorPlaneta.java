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
        boolean ciEsMenor = ci.getTipoCuerpo() != null && ci.getTipoCuerpo().puedeOrbitarPlanetas();
        boolean cjEsMenor = cj.getTipoCuerpo() != null && cj.getTipoCuerpo().puedeOrbitarPlanetas();

        if (ciEsMenor ^ cjEsMenor) {
            CuerpoCeleste menor = ciEsMenor ? ci : cj;
            CuerpoCeleste planeta = ciEsMenor ? cj : ci;

            if (planeta.getTipoCuerpo() != null && planeta.getTipoCuerpo().esPlaneta()) {
                boolean esLuna = menor.getTipoCuerpo() == TipoCuerpo.LUNA;
                if (esLuna && planeta.getMasa() >= org.example.game.simulacion.ConfiguracionColisiones.RATIO_ABSORCION_PLANETA_LUNA * menor.getMasa()) {
                    return false;
                }
                double radioCaptura = esLuna
                        ? Math.max(planeta.getRadioAtraccion() * 3.0, 160.0)
                        : Math.max(planeta.getRadioAtraccion(), 65.0);

                boolean disponible = !menor.estaOrbitando()
                        || (menor.getCuerpoOrbitado() != null && menor.getCuerpoOrbitado().getTipoCuerpo() == TipoCuerpo.ESTRELLA);
                return distReal <= radioCaptura && disponible;
            }
        }
        return false;
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
