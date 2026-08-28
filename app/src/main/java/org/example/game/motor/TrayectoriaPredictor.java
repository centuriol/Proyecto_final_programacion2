package org.example.game.motor;

import org.example.CuerpoCeleste;

import java.util.ArrayList;
import java.util.List;

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
        List<Vector2D> trayectoria = new ArrayList<>(pasos);
        if (posInicial == null || velInicial == null) return trayectoria;

        Vector2D pos = new Vector2D(posInicial.x, posInicial.y);
        Vector2D vel = new Vector2D(velInicial.x, velInicial.y);
        trayectoria.add(new Vector2D(pos.x, pos.y));

        double softeningCuadrado = softening * softening;

        for (int step = 0; step < pasos; step++) {
            Vector2D fuerzaTotal = Vector2D.cero();

            // Calcular atracción gravitacional de los cuerpos del sistema
            for (CuerpoCeleste c : cuerposSistema) {
                if (c == null) continue;
                Vector2D r = c.getPosicion().restar(pos);
                double r2 = r.magnitudCuadrado() + softeningCuadrado;
                double rMag = Math.sqrt(r2);

                if (rMag < 5.0) {
                    // Colisión con cuerpo central durante predicción
                    break;
                }

                double fMag = G * masaCuerpo * c.getMasa() / r2;
                Vector2D f = r.dividir(rMag).multiplicar(fMag);
                fuerzaTotal = fuerzaTotal.sumar(f);
            }

            Vector2D aceleracion = masaCuerpo > 0 ? fuerzaTotal.dividir(masaCuerpo) : Vector2D.cero();
            vel = vel.sumar(aceleracion.multiplicar(dt));
            pos = pos.sumar(vel.multiplicar(dt));

            trayectoria.add(new Vector2D(pos.x, pos.y));
        }

        return trayectoria;
    }
}
