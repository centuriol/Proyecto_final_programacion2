package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;

import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;

/**
 * SRP: Coordina los flujos de colocación interactiva, preview y confirmación de cuerpos celestes.
 */
public class GestorColocacionCuerpos {

    private final ControladorColocacion controlador;
    private final GestorCicloVidaCuerpos cicloVida;
    private final DoubleSupplier anchoSupplier;
    private final DoubleSupplier altoSupplier;

    public GestorColocacionCuerpos(ControladorColocacion controlador, GestorCicloVidaCuerpos cicloVida) {
        this(controlador, cicloVida, () -> 2000.0, () -> 2000.0);
    }

    public GestorColocacionCuerpos(ControladorColocacion controlador, GestorCicloVidaCuerpos cicloVida,
                                   DoubleSupplier anchoSupplier, DoubleSupplier altoSupplier) {
        this.controlador = controlador != null ? controlador : new ControladorColocacion();
        this.cicloVida = cicloVida;
        this.anchoSupplier = anchoSupplier != null ? anchoSupplier : () -> 2000.0;
        this.altoSupplier = altoSupplier != null ? altoSupplier : () -> 2000.0;
    }

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa, String nombre, double maxMasaPermitida) {
        controlador.entrarModoColocacion(tipo, factorMasa, nombre, maxMasaPermitida);
    }

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa, String nombre) {
        controlador.entrarModoColocacion(tipo, factorMasa, nombre, ConfiguracionSimulacion.MASA_FACTOR_MAX);
    }

    public void actualizarPosicionPreview(double xJavaFX, double yJavaFX, double anchoMundo, double altoMundo) {
        controlador.actualizarPosicionPreview(xJavaFX, yJavaFX, anchoMundo, altoMundo);
    }

    public void actualizarPosicionPreview(double xJavaFX, double yJavaFX) {
        controlador.actualizarPosicionPreview(xJavaFX, yJavaFX, anchoSupplier.getAsDouble(), altoSupplier.getAsDouble());
    }

    public void salirModoColocacion() {
        controlador.salirModoColocacion();
    }

    public ControladorColocacion.ModoColocacion getModoColocacion() {
        return controlador.getModoColocacion();
    }

    public TipoCuerpo getTipoColocacion() {
        return controlador.getTipoColocacion();
    }

    public Vector2D getPosicionPreview() {
        return controlador.getPosicionPreview();
    }

    public double getFactorMasaColocacion() {
        return controlador.getFactorMasaColocacion();
    }

    public void setFactorMasaColocacion(double factor, double maxMasaPermitida) {
        controlador.setFactorMasaColocacion(factor, maxMasaPermitida);
    }

    public void setFactorMasaColocacion(double factor) {
        controlador.setFactorMasaColocacion(factor, ConfiguracionSimulacion.MASA_FACTOR_MAX);
    }

    public String getNombreColocacion() {
        return controlador.getNombreColocacion();
    }

    public Vector2D calcularVelocidadOrbitalAsistida(Vector2D posFisica, List<CuerpoCeleste> cuerpos) {
        return controlador.calcularVelocidadOrbitalAsistida(posFisica, cuerpos);
    }

    public CuerpoCeleste encontrarCuerpoGravitatorioCercano(Vector2D pos, List<CuerpoCeleste> cuerpos) {
        return controlador.encontrarCuerpoGravitatorioCercano(pos, cuerpos);
    }

    public Optional<CuerpoCeleste> confirmarColocacionAutoOrbita(SimulacionSolar simulacion) {
        if (controlador.getModoColocacion() == ControladorColocacion.ModoColocacion.COLOCANDO
                && controlador.getPosicionPreview() != null && controlador.getTipoColocacion() != null) {
            Vector2D posPreview = controlador.getPosicionPreview();
            Vector2D velOrbital = calcularVelocidadOrbitalAsistida(posPreview, simulacion.getSistemaSolar().getCuerpos());
            CuerpoCeleste cuerpoCercano = encontrarCuerpoGravitatorioCercano(posPreview, simulacion.getSistemaSolar().getCuerpos());

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
