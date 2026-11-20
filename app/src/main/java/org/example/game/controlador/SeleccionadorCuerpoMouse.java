package org.example.game.controlador;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.Vector2D;

import java.util.List;

/**
 * Responsable de la detección y selección de cuerpos celestes bajo las coordenadas del ratón (SRP / DIP).
 */
public class SeleccionadorCuerpoMouse implements SeleccionadorCuerpo {

    private static final FabricaControlador FABRICA = new FabricaControlador();
    private final ConversorCoordenadas conversor;

    public SeleccionadorCuerpoMouse() {
        this(FABRICA.crearConversorCoordenadas());
    }

    public SeleccionadorCuerpoMouse(ConversorCoordenadas conversor) {
        this.conversor = conversor != null ? conversor : FABRICA.crearConversorCoordenadas();
    }

    public CuerpoCeleste encontrarCuerpo(double mouseX, double mouseY, double anchoMundo, double altoMundo, List<CuerpoCeleste> cuerpos) {
        Vector2D clickPos = new Vector2D(
                conversor.javaFXAFisicaX(mouseX, anchoMundo),
                conversor.javaFXAFisica(mouseY, altoMundo)
        );
        CuerpoCeleste principal = buscarMasCercano(clickPos, cuerpos, false);
        return principal != null ? principal : buscarMasCercano(clickPos, cuerpos, true);
    }

    private CuerpoCeleste buscarMasCercano(Vector2D clickPos, List<CuerpoCeleste> cuerpos, boolean soloSateliteOrbitando) {
        CuerpoCeleste mejor = null;
        double menorDist = Double.MAX_VALUE;
        for (CuerpoCeleste c : cuerpos) {
            if (!esCandidatoValido(c, soloSateliteOrbitando)) continue;
            double d = clickPos.distanciaA(c.getPosicion());
            if (d <= calcularRadioTolerancia(c, soloSateliteOrbitando) && d < menorDist) {
                menorDist = d;
                mejor = c;
            }
        }
        return mejor;
    }

    private boolean esCandidatoValido(CuerpoCeleste c, boolean soloSateliteOrbitando) {
        if (c == null || !c.esSimulado() || !c.esInteractuablePorUsuario()) return false;
        boolean esSatOrbitando = c.getTipoCuerpo() == TipoCuerpo.SATELITE && c.estaOrbitando();
        return esSatOrbitando == soloSateliteOrbitando;
    }

    private double calcularRadioTolerancia(CuerpoCeleste c, boolean esSateliteOrbitando) {
        return esSateliteOrbitando ? c.getRadio() + 4.0 : Math.max(28.0, c.getRadio() + 10.0);
    }
}
