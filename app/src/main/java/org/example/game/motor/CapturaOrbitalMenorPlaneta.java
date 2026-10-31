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
        boolean ciEsMenor = ci.getMasa() < 1e23 && ci.permiteReembolso();
        boolean cjEsMenor = cj.getMasa() < 1e23 && cj.permiteReembolso();

        if (ciEsMenor ^ cjEsMenor) {
            CuerpoCeleste menor = ciEsMenor ? ci : cj;
            CuerpoCeleste planeta = ciEsMenor ? cj : ci;

            if (planeta.getMasa() >= 1e22 && planeta.getMasa() <= 1e28) {
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
        boolean ciEsMenor = ci.getMasa() < 1e23 && ci.permiteReembolso();
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
