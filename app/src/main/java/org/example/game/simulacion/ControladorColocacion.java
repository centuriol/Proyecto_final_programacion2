package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;

import java.util.List;

/**
 * Controla la lógica de selección, preview y parámetros para colocación de cuerpos (SRP).
 */
public class ControladorColocacion {

    public enum ModoColocacion {
        NINGUNO, COLOCANDO
    }

    private ModoColocacion modoColocacion = ModoColocacion.NINGUNO;
    private TipoCuerpo tipoColocacion = null;
    private double factorMasaColocacion = 1.0;
    private String nombreColocacion = null;
    private Vector2D posicionPreview = null;

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa, String nombre, double maxMasaPermitida) {
        this.modoColocacion = ModoColocacion.COLOCANDO;
        this.tipoColocacion = tipo;
        setFactorMasaColocacion(factorMasa, maxMasaPermitida);
        this.nombreColocacion = nombre;
    }

    public void salirModoColocacion() {
        this.modoColocacion = ModoColocacion.NINGUNO;
        this.tipoColocacion = null;
        this.nombreColocacion = null;
        this.factorMasaColocacion = 1.0;
        this.posicionPreview = null;
    }

    public void actualizarPosicionPreview(double xJavaFX, double yJavaFX, double anchoMundo, double altoMundo) {
        double xFisica = ConstantesFisicas.javaFXAFisicaX(xJavaFX, anchoMundo);
        double yFisica = ConstantesFisicas.javaFXAFisica(yJavaFX, altoMundo);
        this.posicionPreview = new Vector2D(xFisica, yFisica);
    }

    public Vector2D calcularVelocidadOrbitalAsistida(Vector2D posFisica, List<CuerpoCeleste> cuerpos) {
        CuerpoCeleste cuerpoCercano = encontrarCuerpoGravitatorioCercano(posFisica, cuerpos);
        if (cuerpoCercano != null) {
            boolean esMenorOrbital = (tipoColocacion == TipoCuerpo.LUNA || tipoColocacion == TipoCuerpo.SATELITE);
            if (esMenorOrbital && cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().esPlaneta()) {
                Vector2D r = posFisica.restar(cuerpoCercano.getPosicion());
                double dist = Math.max(1.0, r.magnitud());
                double factorMasa = (cuerpoCercano.getTipoCuerpo() != null && cuerpoCercano.getTipoCuerpo().masaBase > 0)
                        ? Math.max(0.2, cuerpoCercano.getMasa() / cuerpoCercano.getTipoCuerpo().masaBase)
                        : 1.0;
                double vOrb = Math.sqrt(75.0 * factorMasa / dist);
                Vector2D uRadial = r.dividir(dist);
                Vector2D uTangencial = new Vector2D(-uRadial.y(), uRadial.x());
                return cuerpoCercano.getVelocidad().sumar(uTangencial.multiplicar(vOrb));
            }
            return CuerpoCelesteFactory.calcularVelocidadOrbitalVector(cuerpoCercano, posFisica, false);
        }
        return Vector2D.cero();
    }

    public CuerpoCeleste encontrarCuerpoGravitatorioCercano(Vector2D pos, List<CuerpoCeleste> cuerpos) {
        CuerpoCeleste mejor = null;
        double maxFuerza = 0;

        for (CuerpoCeleste c : cuerpos) {
            if (c.getTipoCuerpo() == TipoCuerpo.LUNA || c.getTipoCuerpo() == TipoCuerpo.SATELITE ||
                c.getTipoCuerpo() == TipoCuerpo.METEORITO) {
                continue;
            }
            if (!c.getTipoCuerpo().esMasivo && c.getMasa() < 1e22) continue;

            double dist = pos.distanciaA(c.getPosicion());
            if (dist < 10.0) continue;

            double atraccion = c.getMasa() / (dist * dist + 100.0);
            if (atraccion > maxFuerza) {
                maxFuerza = atraccion;
                mejor = c;
            }
        }
        return mejor;
    }

    public void setFactorMasaColocacion(double f, double maxPermitido) {
        this.factorMasaColocacion = Math.max(
                ConfiguracionSimulacion.MASA_FACTOR_MIN,
                Math.min(maxPermitido, f)
        );
    }

    public ModoColocacion getModoColocacion() {
        return modoColocacion;
    }

    public TipoCuerpo getTipoColocacion() {
        return tipoColocacion;
    }

    public double getFactorMasaColocacion() {
        return factorMasaColocacion;
    }

    public String getNombreColocacion() {
        return nombreColocacion;
    }

    public void setNombreColocacion(String nombreColocacion) {
        this.nombreColocacion = nombreColocacion;
    }

    public Vector2D getPosicionPreview() {
        return posicionPreview;
    }
}
