package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste;
import java.util.function.Consumer;
/**
 * Estrategia de captura para planetas alrededor de estrellas o agujeros negros.
 */
public class CapturaOrbitalPlanetaAtractor implements EstrategiaCapturaOrbital {

    @Override
    public boolean puedeCapturar(CuerpoCeleste ci, CuerpoCeleste cj, double distReal) {
        CuerpoCeleste planeta = esPlaneta(ci) ? ci : (esPlaneta(cj) ? cj : null);
        CuerpoCeleste estrella = (planeta == ci) ? cj : ci;
        if (planeta == null || estrella == null || estrella.getMasa() < 1e29) {
            return false;
        }
        double radioCapturaPlaneta = Math.max(estrella.getRadioAtraccion() * 7.0, 800.0);
        double radioMinimoCaptura = estrella.getRadio() + planeta.getRadio() + 5.0;
        return !planeta.estaOrbitando() && distReal > radioMinimoCaptura && distReal <= radioCapturaPlaneta;
    }

    private boolean esPlaneta(CuerpoCeleste c) {
        return c != null && c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta();
    }

    @Override
    public CuerpoCeleste[] ejecutarCaptura(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje, AsistenteOrbital asistente) {
        boolean ciEsPlaneta = ci.getTipoCuerpo() != null && ci.getTipoCuerpo().esPlaneta();
        CuerpoCeleste planeta = ciEsPlaneta ? ci : cj;
        CuerpoCeleste estrella = (planeta == ci) ? cj : ci;

        double radioOrbita = distReal;
        double radioZonaLava = TransformadorCuerpos.calcularRadioActivacionPorroLava(planeta, estrella);
        if (estrella.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.ESTRELLA
                && distReal > radioZonaLava) {
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
