package org.example.game.render;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Calcula la posicion en el borde de la pantalla y el angulo de direccion hacia cada sol
 * que se encuentre fuera del area visible actual.
 *
 * Principios SOLID:
 * - SRP: Encapsula exclusivamente la geometria de interseccion rayo-borde para soles fuera de pantalla.
 * - DIP: Opera sobre la abstraccion ControlZoom sin acoplarse a componentes graficos de JavaFX.
 */
public class CalculadorIndicadoresSol {

    public static final double MARGEN_BORDE_POR_DEFECTO = 48.0;

    private final double margenBorde;

    public CalculadorIndicadoresSol() {
        this(MARGEN_BORDE_POR_DEFECTO);
    }

    public CalculadorIndicadoresSol(double margenBorde) {
        this.margenBorde = (Double.isFinite(margenBorde) && margenBorde >= 8.0)
                ? margenBorde
                : MARGEN_BORDE_POR_DEFECTO;
    }

    public List<IndicadorSolBorde> calcularIndicadores(List<CuerpoCeleste> cuerpos,
                                                       ControlZoom camara,
                                                       double anchoVista,
                                                       double altoVista) {
        if (cuerpos == null || camara == null || anchoVista <= 0.0 || altoVista <= 0.0) {
            return Collections.emptyList();
        }

        Vector2D vista = new Vector2D(anchoVista, altoVista);
        List<IndicadorSolBorde> resultado = new ArrayList<>();
        for (CuerpoCeleste cuerpo : cuerpos) {
            if (esSolActivo(cuerpo)) {
                double sx = camara.mundoAPantallaX(cuerpo.getPosicion().x(), anchoVista);
                double sy = camara.mundoAPantallaY(cuerpo.getPosicion().y(), altoVista);
                if (estaFueraDePantalla(sx, sy, anchoVista, altoVista)) {
                    resultado.add(proyectarSobreBorde(cuerpo, new Vector2D(sx, sy), vista));
                }
            }
        }
        return resultado;
    }

    public boolean estaFueraDePantalla(double sx, double sy, double anchoVista, double altoVista) {
        return sx < 0.0 || sx > anchoVista || sy < 0.0 || sy > altoVista;
    }

    private IndicadorSolBorde proyectarSobreBorde(CuerpoCeleste sol, Vector2D posPantalla, Vector2D vista) {
        double centroX = vista.x() / 2.0;
        double centroY = vista.y() / 2.0;
        double vx = posPantalla.x() - centroX;
        double vy = posPantalla.y() - centroY;

        double margenX = Math.min(margenBorde, Math.max(4.0, vista.x() * 0.25));
        double margenY = Math.min(margenBorde, Math.max(4.0, vista.y() * 0.25));
        double semiAncho = Math.max(1.0, centroX - margenX);
        double semiAlto = Math.max(1.0, centroY - margenY);

        double escalaX = Math.abs(vx) > 1e-9 ? semiAncho / Math.abs(vx) : Double.POSITIVE_INFINITY;
        double escalaY = Math.abs(vy) > 1e-9 ? semiAlto / Math.abs(vy) : Double.POSITIVE_INFINITY;
        double factor = Math.min(escalaX, escalaY);

        double bordeX = acotar(centroX + vx * factor, margenX, vista.x() - margenX);
        double bordeY = acotar(centroY + vy * factor, margenY, vista.y() - margenY);
        double anguloGrados = Math.toDegrees(Math.atan2(vy, vx));

        return new IndicadorSolBorde(sol, bordeX, bordeY, anguloGrados);
    }

    private static boolean esSolActivo(CuerpoCeleste cuerpo) {
        return cuerpo != null
                && cuerpo.esSimulado()
                && cuerpo.getMasa() >= 1e29
                && cuerpo.getMasa() < 1e31;
    }

    private static double acotar(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }
}
