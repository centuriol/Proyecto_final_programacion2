package org.example.game.render.capa;

import javafx.scene.canvas.GraphicsContext;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.render.FabricaRender;
import org.example.game.render.UtilidadRender;

import java.util.List;

/**
 * Capa de renderizado para zonas y órbitas de referencia alrededor de estrellas (SRP / DIP).
 */
public class CapaZonasHabitables {

    private static final FabricaRender FABRICA = new FabricaRender();
    private static final double[] RADIOS_REFERENCIA_ORBITAL = org.example.game.motor.CalculadorOrbital.RADIOS_RANURAS_SOL;
    private final ConversorCoordenadas conversor;

    public CapaZonasHabitables() {
        this(FABRICA.crearConversorCoordenadas());
    }

    public CapaZonasHabitables(ConversorCoordenadas conversor) {
        this.conversor = conversor != null ? conversor : FABRICA.crearConversorCoordenadas();
    }

    public void dibujar(GraphicsContext gc, List<CuerpoCeleste> cuerpos, double anchoCanvas, double altoCanvas) {
        for (CuerpoCeleste c : cuerpos) {
            boolean esEstrella = c != null && c.esSimulado()
                    && (c.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.ESTRELLA
                    || (c.getMasa() >= 1e29 && c.getMasa() < 1e31 && !org.example.game.motor.GestorAgujeroNegro.esAgujeroNegro(c)));
            if (esEstrella) {
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
