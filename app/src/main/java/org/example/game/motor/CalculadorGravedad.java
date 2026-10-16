package org.example.game.motor;

import org.example.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Calculador de fuerzas gravitacionales N-Body y atracción hacia colapso estelar/arcade.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de computar las fuerzas gravitacionales entre cuerpos.
 * - DIP: Delega captura y dinámica orbital en AsistenteOrbital.
 */
public class CalculadorGravedad {

    private final double G;
    private final double softening;

    public CalculadorGravedad(double G, double softening) {
        this.G = G;
        this.softening = softening;
    }

    public CalculadorGravedad() {
        this(ConstantesFisicas.G_ESCALADO, 15.0);
    }

    public void calcularGravedad(List<CuerpoCeleste> cuerpos, AsistenteOrbital asistenteOrbital, Consumer<String> listenerMensaje) {
        double softeningCuadrado = softening * softening;
        int n = cuerpos.size();

        for (int i = 0; i < n; i++) {
            CuerpoCeleste ci = cuerpos.get(i);

            for (int j = i + 1; j < n; j++) {
                CuerpoCeleste cj = cuerpos.get(j);

                Vector2D r = cj.getPosicion().restar(ci.getPosicion());
                double distReal = r.magnitud();
                double distSegura = Math.max(0.1, distReal);
                double r2 = distSegura * distSegura + softeningCuadrado;
                double rMag = Math.sqrt(r2);

                // 1. Interacción orbital especial: verificar enlaces orbitales activos o nuevas capturas
                CuerpoCeleste[] parOrbital = asistenteOrbital != null
                        ? asistenteOrbital.verificarYCapturarOrbita(ci, cj, distReal, listenerMensaje)
                        : null;

                if (parOrbital != null) {
                    asistenteOrbital.aplicarDinamicaOrbitalEstable(parOrbital[0], parOrbital[1], distReal);
                    continue;
                }

                // 2. Gravedad newtoniana base para cuerpos no enlazados en órbita
                double fuerzaMag = G * ci.getMasa() * cj.getMasa() / r2;
                double factorDir = fuerzaMag / rMag;
                double fx = r.x * factorDir;
                double fy = r.y * factorDir;
                ci.aplicarFuerza(new Vector2D(fx, fy));
                cj.aplicarFuerza(new Vector2D(-fx, -fy));

                // 3. Atracción arcade hacia el colapso para cuerpos no orbitantes dentro del área de atracción
                CuerpoCeleste mayor = ci.getMasa() >= cj.getMasa() ? ci : cj;
                CuerpoCeleste menor = (mayor == ci) ? cj : ci;

                double radioColapso = mayor.getRadioAtraccion();
                if (distReal < radioColapso) {
                    double penetracion = Math.max(0.0, Math.min(1.0, 1.0 - (distReal / radioColapso)));
                    Vector2D dirHaciaMayor = mayor.getPosicion().restar(menor.getPosicion()).normalizar();

                    double factorMasaMayor = 1.0;
                    if (mayor.getTipoCuerpo() != null && mayor.getTipoCuerpo().masaBase > 0) {
                        factorMasaMayor = Math.max(0.1, mayor.getMasa() / mayor.getTipoCuerpo().masaBase);
                    }

                    double aMenor;
                    if (mayor.getTipoCuerpo() == TipoCuerpo.ESTRELLA || mayor.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO || mayor.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
                        aMenor = (0.5 + 3.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor);
                    } else {
                        aMenor = (0.35 + 2.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor);
                    }

                    double fuerzaColapsoMag = menor.getMasa() * aMenor;
                    Vector2D fSobreMenor = dirHaciaMayor.multiplicar(fuerzaColapsoMag);
                    Vector2D fSobreMayor = fSobreMenor.multiplicar(-1.0);

                    menor.aplicarFuerza(fSobreMenor);
                    mayor.aplicarFuerza(fSobreMayor);
                }
            }
        }
    }
}
