package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import java.util.List; import java.util.function.Consumer;
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
                procesarInteraccionPar(ci, cj, softeningCuadrado, asistenteOrbital, listenerMensaje);
            }
        }
    }

    private void procesarInteraccionPar(CuerpoCeleste ci, CuerpoCeleste cj, double softeningCuadrado,
                                       AsistenteOrbital asistenteOrbital, Consumer<String> listenerMensaje) {
        Vector2D r = cj.getPosicion().restar(ci.getPosicion());
        double distReal = r.magnitud();

        if (procesarCapturaOrbital(ci, cj, distReal, asistenteOrbital, listenerMensaje)) {
            return;
        }

        if (ci.getTipoCuerpo() == TipoCuerpo.SATELITE && cj.getTipoCuerpo() == TipoCuerpo.SATELITE) {
            return;
        }

        aplicarGravedadNewtoniana(ci, cj, r, distReal, softeningCuadrado);
        aplicarAtraccionArcade(ci, cj, distReal);
    }

    private boolean procesarCapturaOrbital(CuerpoCeleste ci, CuerpoCeleste cj, double distReal,
                                          AsistenteOrbital asistenteOrbital, Consumer<String> listenerMensaje) {
        if (asistenteOrbital == null) return false;
        CuerpoCeleste[] parOrbital = asistenteOrbital.verificarYCapturarOrbita(ci, cj, distReal, listenerMensaje);
        if (parOrbital != null) {
            asistenteOrbital.aplicarDinamicaOrbitalEstable(parOrbital[0], parOrbital[1], distReal);
            return true;
        }
        return false;
    }

    private void aplicarGravedadNewtoniana(CuerpoCeleste ci, CuerpoCeleste cj, Vector2D r, double distReal, double softeningCuadrado) {
        double distSegura = Math.max(0.1, distReal);
        double r2 = distSegura * distSegura + softeningCuadrado;
        double rMag = Math.sqrt(r2);

        double fuerzaMag = G * ci.getMasa() * cj.getMasa() / r2;
        double factorDir = fuerzaMag / rMag;
        Vector2D f = new Vector2D(r.x() * factorDir, r.y() * factorDir);

        ci.aplicarFuerza(f);
        cj.aplicarFuerza(f.multiplicar(-1.0));
    }

    private void aplicarAtraccionArcade(CuerpoCeleste ci, CuerpoCeleste cj, double distReal) {
        CuerpoCeleste mayor = ci.getMasa() >= cj.getMasa() ? ci : cj;
        CuerpoCeleste menor = (mayor == ci) ? cj : ci;

        double radioColapso = mayor.getRadioAtraccion();
        if (distReal >= radioColapso) return;

        double penetracion = Math.max(0.0, Math.min(1.0, 1.0 - (distReal / radioColapso)));
        Vector2D dirHaciaMayor = mayor.getPosicion().restar(menor.getPosicion()).normalizar();

        double factorMasaMayor = 1.0;
        if (mayor.getTipoCuerpo() != null && mayor.getTipoCuerpo().masaBase > 0) {
            factorMasaMayor = Math.max(0.1, mayor.getMasa() / mayor.getTipoCuerpo().masaBase);
        }

        double aMenor = (mayor.getTipoCuerpo() != null && mayor.getTipoCuerpo().esAtractorOrbital())
                ? (0.5 + 3.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor)
                : (0.35 + 2.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor);

        double fuerzaColapsoMag = menor.getMasa() * aMenor;
        Vector2D fSobreMenor = dirHaciaMayor.multiplicar(fuerzaColapsoMag);

        menor.aplicarFuerza(fSobreMenor);
        mayor.aplicarFuerza(fSobreMenor.multiplicar(-1.0));
    }
}
