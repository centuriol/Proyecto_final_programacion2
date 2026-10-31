package org.example.game.controlador;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import java.util.List;
/**
 * Responsable de la detección y selección de cuerpos celestes bajo las coordenadas del ratón (SRP).
 */
public class SeleccionadorCuerpoMouse {

    public CuerpoCeleste encontrarCuerpo(double mouseX, double mouseY, double anchoMundo, double altoMundo, List<CuerpoCeleste> cuerpos) {
        double xFisica = ConstantesFisicas.javaFXAFisicaX(mouseX, anchoMundo);
        double yFisica = ConstantesFisicas.javaFXAFisica(mouseY, altoMundo);
        Vector2D clickPos = new Vector2D(xFisica, yFisica);

        CuerpoCeleste masCercano = null;
        double menorDistancia = Double.MAX_VALUE;

        for (CuerpoCeleste c : cuerpos) {
            if (c == null || !c.esSimulado() || !c.esInteractuablePorUsuario()) {
                continue;
            }
            double d = clickPos.distanciaA(c.getPosicion());
            double radioTolerancia = Math.max(28.0, c.getRadio() + 10.0);
            if (d <= radioTolerancia && d < menorDistancia) {
                menorDistancia = d;
                masCercano = c;
            }
        }
        return masCercano;
    }
}
