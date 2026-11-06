package org.example.game.render.capa;

import javafx.scene.canvas.GraphicsContext;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.render.UtilidadRender;

import java.util.List;

/**
 * Capa de renderizado para zonas y órbitas de referencia alrededor de estrellas (SRP).
 */
public class CapaZonasHabitables {

    private static final double[] RADIOS_REFERENCIA_ORBITAL = org.example.game.motor.CalculadorOrbital.RADIOS_RANURAS_SOL;
    private final ConversorCoordenadas conversor;

    public CapaZonasHabitables() {
        this(new TransformadorCoordenadas());
    }

    public CapaZonasHabitables(ConversorCoordenadas conversor) {
        this.conversor = conversor != null ? conversor : new TransformadorCoordenadas();
    }

    public void dibujar(GraphicsContext gc, List<CuerpoCeleste> cuerpos, double anchoCanvas, double altoCanvas) {
        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.esSimulado() && c.getMasa() >= 1e29 && c.getMasa() < 1e31) {
                double sx = conversor.fisicaAXJavaFX(c.getPosicion().x(), anchoCanvas);
                double sy = conversor.fisicaAYJavaFX(c.getPosicion().y(), altoCanvas);

                gc.setLineWidth(1.0);
                gc.setLineDashes(3, 5);

                for (double rOrbita : RADIOS_REFERENCIA_ORBITAL) {
                    gc.setStroke(UtilidadRender.colorRgb(91, 227, 255, 0.18));
                    gc.strokeOval(sx - rOrbita, sy - rOrbita, rOrbita * 2.0, rOrbita * 2.0);
                }

                gc.setLineDashes(null);
            }
        }
    }

    public static double[] getRadiosReferenciaOrbital() {
        return RADIOS_REFERENCIA_ORBITAL;
    }
}
