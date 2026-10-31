package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste; import java.util.ArrayList; import java.util.List;
/**
 * Predictor de trayectorias orbitales en tiempo real.
 * SRP: Realiza integración rápida hacia el futuro para mostrar la línea de trayectoria prevista
 * durante la colocación y lanzamiento con vector de impulso (slingshot).
 */
public class TrayectoriaPredictor {

    private final double G;
    private final double softening;

    public TrayectoriaPredictor(double G, double softening) {
        this.G = G;
        this.softening = softening;
    }

    public TrayectoriaPredictor() {
        this(ConstantesFisicas.G_ESCALADO, 35.0);
    }

    /**
     * Calcula los puntos futuros de la trayectoria para un cuerpo de prueba.
     *
     * @param posInicial Posición inicial en coordenadas físicas
     * @param velInicial Velocidad inicial en coordenadas físicas
     * @param masaCuerpo Masa del cuerpo proyectado
     * @param cuerposSistema Lista de cuerpos masivos actuales en la simulación
     * @param pasos Cantidad de pasos futuros a proyectar
     * @param dt Paso de tiempo por iteración
     * @return Lista de puntos Vector2D proyectados en coordenadas físicas
     */
    public List<Vector2D> predecirTrayectoria(Vector2D posInicial, Vector2D velInicial,
                                              double masaCuerpo, List<CuerpoCeleste> cuerposSistema,
                                              int pasos, double dt) {
        List<Vector2D> trayectoria = new ArrayList<>(pasos + 1);
        if (posInicial == null || velInicial == null) return trayectoria;

        double px = posInicial.x();
        double py = posInicial.y();
        double vx = velInicial.x();
        double vy = velInicial.y();

        trayectoria.add(new Vector2D(px, py));

        double softeningCuadrado = softening * softening;

        for (int step = 0; step < pasos; step++) {
            double axTotal = 0.0;
            double ayTotal = 0.0;
            boolean colision = false;

            // Calcular aceleración gravitacional de los cuerpos del sistema sin crear instancias intermedias
            for (int i = 0; i < cuerposSistema.size(); i++) {
                CuerpoCeleste c = cuerposSistema.get(i);
                if (c == null) continue;

                Vector2D posC = c.getPosicion();
                double rx = posC.x() - px;
                double ry = posC.y() - py;
                double r2 = rx * rx + ry * ry + softeningCuadrado;
                double rMag = Math.sqrt(r2);

                if (rMag < 5.0) {
                    colision = true;
                    break;
                }

                // a = G * M / r^2 en dirección normalizada (rx/rMag, ry/rMag)
                double aMag = G * c.getMasa() / r2;
                double invRMag = 1.0 / rMag;
                axTotal += rx * invRMag * aMag;
                ayTotal += ry * invRMag * aMag;
            }

            if (colision) {
                break;
            }

            vx += axTotal * dt;
            vy += ayTotal * dt;
            px += vx * dt;
            py += vy * dt;

            trayectoria.add(new Vector2D(px, py));
        }

        return trayectoria;
    }
}
