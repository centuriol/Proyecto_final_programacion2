package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;

import java.util.Optional;

/**
 * SRP: Coordina los flujos de confirmacion de colocacion con impulso y auto-orbita.
 */
public class GestorColocacionCuerpos {

    private final ControladorColocacion controlador;
    private final GestorCicloVidaCuerpos cicloVida;

    public GestorColocacionCuerpos(ControladorColocacion controlador, GestorCicloVidaCuerpos cicloVida) {
        this.controlador = controlador;
        this.cicloVida = cicloVida;
    }

    public Optional<CuerpoCeleste> confirmarColocacionAutoOrbita(SimulacionSolar simulacion) {
        if (controlador.getModoColocacion() == ControladorColocacion.ModoColocacion.COLOCANDO
                && controlador.getPosicionPreview() != null && controlador.getTipoColocacion() != null) {
            Vector2D posPreview = controlador.getPosicionPreview();
            Vector2D velOrbital = simulacion.calcularVelocidadOrbitalAsistida(posPreview);
            CuerpoCeleste cuerpoCercano = simulacion.encontrarCuerpoGravitatorioCercano(posPreview);

            Optional<CuerpoCeleste> spawnedOpt = cicloVida.spawnCuerpo(
                    controlador.getTipoColocacion(),
                    controlador.getNombreColocacion(),
                    posPreview.x(), posPreview.y(),
                    controlador.getFactorMasaColocacion(),
                    velOrbital,
                    simulacion::notificarEvento,
                    controlador::salirModoColocacion
            );

            if (spawnedOpt.isPresent() && cuerpoCercano != null) {
                CuerpoCeleste recienCreado = spawnedOpt.get();
                boolean recienEsMenor = recienCreado.getMasa() < 1e23;
                boolean cercanoEsPlaneta = cuerpoCercano.getMasa() <= 1e28 && cuerpoCercano.getMasa() >= 1e22;
                boolean recienEsPlaneta = recienCreado.getMasa() <= 1e28 && recienCreado.getMasa() >= 1e22;
                boolean cercanoEsMasivo = cuerpoCercano.getMasa() >= 1e29;

                double dist = posPreview.distanciaA(cuerpoCercano.getPosicion());
                if ((recienEsMenor && cercanoEsPlaneta) || (recienEsPlaneta && cercanoEsMasivo)) {
                    recienCreado.setCuerpoOrbitado(cuerpoCercano);
                    recienCreado.setRadioOrbita(dist);
                }
            }
            return spawnedOpt;
        }
        return Optional.empty();
    }

    public Optional<CuerpoCeleste> confirmarColocacionConImpulso(SimulacionSolar simulacion, Vector2D posFisica, Vector2D velFisica) {
        if (controlador.getTipoColocacion() != null) {
            return cicloVida.spawnCuerpo(
                    controlador.getTipoColocacion(),
                    controlador.getNombreColocacion(),
                    posFisica.x(), posFisica.y(),
                    controlador.getFactorMasaColocacion(),
                    velFisica,
                    simulacion::notificarEvento,
                    controlador::salirModoColocacion
            );
        }
        return Optional.empty();
    }
}
