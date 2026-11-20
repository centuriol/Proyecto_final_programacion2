package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.AsistenciaOrbital;
import org.example.game.motor.AsistenteOrbital;
import org.example.game.motor.CalculadorOrbital;
import org.example.game.motor.FabricaMotorFisica;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.motor.Vector2D;

/**
 * SRP / DIP: Vincula automáticamente un cuerpo recién colocado a la órbita del cuerpo anfitrión más cercano cuando corresponde.
 */
public class VinculadorAutoOrbita {

    private static final FabricaMotorFisica FABRICA_MOTOR = new FabricaMotorFisica();
    private final AsistenciaOrbital asistenciaOrbital;

    public VinculadorAutoOrbita() {
        this(FABRICA_MOTOR.crearAsistenciaOrbitalPorDefecto());
    }

    public VinculadorAutoOrbita(AsistenciaOrbital asistenciaOrbital) {
        this.asistenciaOrbital = asistenciaOrbital != null ? asistenciaOrbital : FABRICA_MOTOR.crearAsistenciaOrbitalPorDefecto();
    }

    public void vincularSiCorresponde(CuerpoCeleste recienCreado, CuerpoCeleste cuerpoCercano, Vector2D posPreview) {
        if (recienCreado == null || cuerpoCercano == null) return;
        boolean recienEsMenor = recienCreado.getTipoCuerpo() != null && recienCreado.getTipoCuerpo().puedeOrbitarPlanetas();
        boolean cercanoEsPlaneta = cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().esPlaneta();
        boolean recienEsPlaneta = recienCreado.getTipoCuerpo() != null && recienCreado.getTipoCuerpo().esPlaneta();
        boolean cercanoEsMasivo = cuerpoCercano.getMasa() >= 1e29;

        if (!debeVincular(recienCreado, cuerpoCercano, recienEsMenor, cercanoEsPlaneta, recienEsPlaneta, cercanoEsMasivo)) {
            return;
        }
        double dist = posPreview.distanciaA(cuerpoCercano.getPosicion());
        double radioOrbita = calcularRadioOrbitaInicial(recienCreado, cuerpoCercano, dist, recienEsPlaneta);
        recienCreado.setCuerpoOrbitado(cuerpoCercano);
        recienCreado.setRadioOrbita(radioOrbita);
        if (recienEsMenor && cercanoEsPlaneta) {
            asistenciaOrbital.inicializarVelocidadOrbital(recienCreado, cuerpoCercano, radioOrbita);
        }
    }

    private boolean debeVincular(CuerpoCeleste recienCreado, CuerpoCeleste cuerpoCercano,
                                 boolean recienEsMenor, boolean cercanoEsPlaneta,
                                 boolean recienEsPlaneta, boolean cercanoEsMasivo) {
        if (recienEsPlaneta && cercanoEsMasivo) return true;
        if (!recienEsMenor || !cercanoEsPlaneta) return false;
        boolean lunaComida = recienCreado.getTipoCuerpo() == TipoCuerpo.LUNA
                && cuerpoCercano.getMasa() >= ConfiguracionColisiones.RATIO_ABSORCION_PLANETA_LUNA * recienCreado.getMasa();
        return !lunaComida;
    }

    private double calcularRadioOrbitaInicial(CuerpoCeleste recienCreado, CuerpoCeleste cuerpoCercano,
                                              double dist, boolean recienEsPlaneta) {
        if (recienEsPlaneta && cuerpoCercano.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
            double radioZonaLava = TransformadorCuerpos.calcularRadioActivacionPorroLava(recienCreado, cuerpoCercano);
            return dist <= radioZonaLava ? dist : CalculadorOrbital.ajustarARanuraSolar(dist);
        }
        return AsistenteOrbital.acotarRadioOrbital(recienCreado, cuerpoCercano, dist);
    }
}
