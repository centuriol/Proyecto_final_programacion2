package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.Vector2D;

import java.util.List;

/**
 * Controla el estado de selección, preview y parámetros para colocación de cuerpos (SRP / DIP).
 */
public class ControladorColocacion {

    public enum ModoColocacion {
        NINGUNO, COLOCANDO
    }

    private static final FabricaSimulacion FABRICA_SIM = new FabricaSimulacion();
    private static final CalculadorOrbitaPreview CALCULADOR_ORBITA = new CalculadorOrbitaPreview();

    private static class EstadoPreviewColocacion {
        ModoColocacion modo = ModoColocacion.NINGUNO;
        TipoCuerpo tipo = null;
        double factorMasa = 1.0;
        String nombre = null;
        Vector2D posicionPreview = null;

        void entrar(TipoCuerpo t, double fm, String nom, double maxMasa) {
            this.modo = ModoColocacion.COLOCANDO;
            this.tipo = t;
            this.factorMasa = Math.max(ConfiguracionSimulacion.MASA_FACTOR_MIN, Math.min(maxMasa, fm));
            this.nombre = nom;
        }

        void salir() {
            this.modo = ModoColocacion.NINGUNO;
            this.tipo = null;
            this.nombre = null;
            this.factorMasa = 1.0;
            this.posicionPreview = null;
        }
    }

    private final EstadoPreviewColocacion estado = new EstadoPreviewColocacion();
    private final ConversorCoordenadas conversor;

    public ControladorColocacion() {
        this(FABRICA_SIM.crearConversorCoordenadas());
    }

    public ControladorColocacion(ConversorCoordenadas conversor) {
        this.conversor = conversor != null ? conversor : FABRICA_SIM.crearConversorCoordenadas();
    }

    public void entrarModoColocacion(TipoCuerpo tipo, double factorMasa, String nombre, double maxMasaPermitida) {
        estado.entrar(tipo, factorMasa, nombre, maxMasaPermitida);
    }

    public void salirModoColocacion() { estado.salir(); }

    public void actualizarPosicionPreview(double xJavaFX, double yJavaFX, double anchoMundo, double altoMundo) {
        estado.posicionPreview = new Vector2D(
                conversor.javaFXAFisicaX(xJavaFX, anchoMundo),
                conversor.javaFXAFisica(yJavaFX, altoMundo)
        );
    }

    public Vector2D calcularVelocidadOrbitalAsistida(Vector2D posFisica, List<CuerpoCeleste> cuerpos) {
        return CALCULADOR_ORBITA.calcularVelocidadOrbitalAsistida(estado.tipo, posFisica, cuerpos);
    }

    public CuerpoCeleste encontrarCuerpoGravitatorioCercano(Vector2D pos, List<CuerpoCeleste> cuerpos) {
        return CALCULADOR_ORBITA.encontrarCuerpoGravitatorioCercano(estado.tipo, pos, cuerpos);
    }

    public void setFactorMasaColocacion(double f, double maxPermitido) {
        estado.factorMasa = Math.max(ConfiguracionSimulacion.MASA_FACTOR_MIN, Math.min(maxPermitido, f));
    }

    public ModoColocacion getModoColocacion() { return estado.modo; }
    public TipoCuerpo getTipoColocacion() { return estado.tipo; }
    public double getFactorMasaColocacion() { return estado.factorMasa; }
    public String getNombreColocacion() { return estado.nombre; }
    public void setNombreColocacion(String nombre) { estado.nombre = nombre; }
    public Vector2D getPosicionPreview() { return estado.posicionPreview; }
    public void setPosicionPreview(Vector2D pos) { estado.posicionPreview = pos; }
}
