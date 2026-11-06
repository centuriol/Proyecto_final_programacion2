package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import java.util.function.Consumer;
/**
 * Estrategia de captura para planetas alrededor de estrellas o agujeros negros.
 */
public class CapturaOrbitalPlanetaAtractor implements EstrategiaCapturaOrbital {

    @Override
    public boolean puedeCapturar(CuerpoCeleste ci, CuerpoCeleste cj, double distReal) {
        boolean ciEsPlaneta = ci.getTipoCuerpo() != null && ci.getTipoCuerpo().esPlaneta();
        boolean cjEsPlaneta = cj.getTipoCuerpo() != null && cj.getTipoCuerpo().esPlaneta();
        CuerpoCeleste planeta = ciEsPlaneta ? ci : (cjEsPlaneta ? cj : null);
        CuerpoCeleste estrella = (planeta == ci) ? cj : ci;

        boolean esPlanetaYEstrella = (planeta != null && estrella != null && estrella.getMasa() >= 1e29);

        if (esPlanetaYEstrella) {
            double radioCapturaPlaneta = Math.max(estrella.getRadioAtraccion() * 7.0, 800.0);
            double radioMinimoCaptura = estrella.getRadio() + planeta.getRadio() + 5.0;
            return !planeta.estaOrbitando() && distReal > radioMinimoCaptura && distReal <= radioCapturaPlaneta;
        }
        return false;
    }

    @Override
    public CuerpoCeleste[] ejecutarCaptura(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje, AsistenteOrbital asistente) {
        boolean ciEsPlaneta = ci.getTipoCuerpo() != null && ci.getTipoCuerpo().esPlaneta();
        CuerpoCeleste planeta = ciEsPlaneta ? ci : cj;
        CuerpoCeleste estrella = (planeta == ci) ? cj : ci;

        double radioOrbita = distReal;
        if (estrella.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.ESTRELLA
                && distReal > org.example.game.simulacion.ConfiguracionSimulacion.RADIO_TRANSFORMACION_PORRO_LAVA) {
            radioOrbita = CalculadorOrbital.ajustarARanuraSolar(distReal);
        }

        planeta.setCuerpoOrbitado(estrella);
        asistente.inicializarVelocidadOrbital(planeta, estrella, distReal);
        planeta.setRadioOrbita(radioOrbita);
        if (listenerMensaje != null) {
            listenerMensaje.accept(planeta.getNombre() + " esta orbitando " + estrella.getNombre());
        }
        return new CuerpoCeleste[]{planeta, estrella};
    }
}
