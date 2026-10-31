package org.example.game.render.capa;
import javafx.scene.canvas.GraphicsContext;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.render.UtilidadRender;
import java.util.List;
/**
 * Capa de renderizado para zonas y órbitas de referencia alrededor de estrellas (SRP).
 */
public class CapaZonasHabitables {

    private static final double[] RADIOS_REFERENCIA_ORBITAL = { 160.0, 280.0, 400.0 };

    public void dibujar(GraphicsContext gc, List<CuerpoCeleste> cuerpos, double anchoCanvas, double altoCanvas) {
        for (CuerpoCeleste c : cuerpos) {
            if (c.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
                double sx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas);
                double sy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas);

                double factorMasa = 1.0;
                if (c.getTipoCuerpo() != null && c.getTipoCuerpo().masaBase > 0) {
                    factorMasa = Math.max(0.1, c.getMasa() / c.getTipoCuerpo().masaBase);
                }
                double escala = Math.sqrt(factorMasa);

                gc.setLineWidth(1.0);
                gc.setLineDashes(3, 5);

                for (double rBase : RADIOS_REFERENCIA_ORBITAL) {
                    double rOrbita = rBase * escala;
                    gc.setStroke(UtilidadRender.colorRgb(91, 227, 255, 0.14));
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
