package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionColisiones;
import java.util.List;
import java.util.function.Consumer;

/**
 * Calculador de fuerzas gravitacionales N-Body y atracción hacia colapso estelar/arcade (SRP / DIP).
 */
public class CalculadorGravedad implements CalculadorFuerzaGravitatoria {

    private static final InteraccionGravitacionalMenor FILTRO_MENORES = new InteraccionGravitacionalMenor();

    private final double G;
    private final double softening;

    public CalculadorGravedad(double G, double softening) {
        this.G = G;
        this.softening = softening;
    }

    public CalculadorGravedad() {
        this(ConstantesFisicas.G_ESCALADO, 15.0);
    }

    @Override
    public void calcularGravedad(List<CuerpoCeleste> cuerpos, AsistenciaOrbital asistenteOrbital, Consumer<String> listenerMensaje) {
        double softeningCuadrado = softening * softening;
        int n = cuerpos.size();
        for (int i = 0; i < n; i++) {
            CuerpoCeleste ci = cuerpos.get(i);
            for (int j = i + 1; j < n; j++) {
                procesarInteraccionPar(ci, cuerpos.get(j), softeningCuadrado, asistenteOrbital, listenerMensaje);
            }
        }
    }

    private void procesarInteraccionPar(CuerpoCeleste ci, CuerpoCeleste cj, double softeningCuadrado,
                                        AsistenciaOrbital asistenteOrbital, Consumer<String> listenerMensaje) {
        if (FILTRO_MENORES.debeIgnorarPar(ci, cj)) return;

        Vector2D r = cj.getPosicion().restar(ci.getPosicion());
        double distReal = r.magnitud();

        if (FILTRO_MENORES.esParConSatelite(ci, cj)) {
            procesarInteraccionSatelite(ci, cj, distReal, asistenteOrbital, listenerMensaje);
            return;
        }
        if (FILTRO_MENORES.esLunaOrbitandoPlaneta(ci) || FILTRO_MENORES.esLunaOrbitandoPlaneta(cj)) {
            procesarConLunaOrbitando(ci, cj, r, distReal, softeningCuadrado, asistenteOrbital, listenerMensaje);
            return;
        }
        procesarInteraccionEstandar(ci, cj, r, distReal, softeningCuadrado, asistenteOrbital, listenerMensaje);
    }

    private void procesarInteraccionSatelite(CuerpoCeleste ci, CuerpoCeleste cj, double distReal,
                                             AsistenciaOrbital asistenteOrbital, Consumer<String> listenerMensaje) {
        if (FILTRO_MENORES.puedeInteractuarSateliteCon(ci, cj)) {
            procesarCapturaOrbital(ci, cj, distReal, asistenteOrbital, listenerMensaje);
        }
    }

    private void procesarInteraccionEstandar(CuerpoCeleste ci, CuerpoCeleste cj, Vector2D r, double distReal,
                                             double softeningCuadrado, AsistenciaOrbital asistenteOrbital,
                                             Consumer<String> listenerMensaje) {
        if (procesarCapturaOrbital(ci, cj, distReal, asistenteOrbital, listenerMensaje)) return;
        if (ci.getMasa() <= 10.0 && cj.getMasa() <= 10.0) return;

        if (FILTRO_MENORES.esParPlanetaLuna(ci, cj)) {
            procesarGravedadPlanetaLuna(ci, cj, r, distReal, softeningCuadrado);
            return;
        }
        aplicarGravedadNewtoniana(ci, cj, r, distReal, softeningCuadrado);
        aplicarAtraccionArcade(ci, cj, distReal);
    }

    private void procesarConLunaOrbitando(CuerpoCeleste ci, CuerpoCeleste cj, Vector2D r, double distReal,
                                          double softeningCuadrado, AsistenciaOrbital asistenteOrbital,
                                          Consumer<String> listenerMensaje) {
        if (ci.getTipoCuerpo() == TipoCuerpo.LUNA && cj.getTipoCuerpo() == TipoCuerpo.LUNA) return;
        CuerpoCeleste luna = FILTRO_MENORES.esLunaOrbitandoPlaneta(ci) ? ci : cj;
        CuerpoCeleste otro = (luna == ci) ? cj : ci;
        if (otro != luna.getCuerpoOrbitado()) return;
        if (!procesarCapturaOrbital(ci, cj, distReal, asistenteOrbital, listenerMensaje) && FILTRO_MENORES.esParPlanetaLuna(ci, cj)) {
            procesarGravedadPlanetaLuna(ci, cj, r, distReal, softeningCuadrado);
        }
    }

    private void procesarGravedadPlanetaLuna(CuerpoCeleste ci, CuerpoCeleste cj, Vector2D r, double distReal, double softeningCuadrado) {
        CuerpoCeleste planeta = (ci.getTipoCuerpo() != null && ci.getTipoCuerpo().esPlaneta()) ? ci : cj;
        CuerpoCeleste luna = (planeta == ci) ? cj : ci;
        if (planeta.getMasa() >= ConfiguracionColisiones.RATIO_ABSORCION_PLANETA_LUNA * luna.getMasa()) {
            double radioComer = Math.max(planeta.getRadioAtraccion() * 3.0, 160.0);
            if (distReal <= radioComer) {
                Vector2D dirHaciaPlaneta = planeta.getPosicion().restar(luna.getPosicion()).normalizar();
                luna.setVelocidad(planeta.getVelocidad().sumar(dirHaciaPlaneta.multiplicar(12.0)));
                aplicarAtraccionArcade(ci, cj, distReal);
            }
        }
        aplicarGravedadNewtoniana(ci, cj, r, distReal, softeningCuadrado);
    }

    private boolean procesarCapturaOrbital(CuerpoCeleste ci, CuerpoCeleste cj, double distReal,
                                           AsistenciaOrbital asistenteOrbital, Consumer<String> listenerMensaje) {
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
        double factorDir = (G * ci.getMasa() * cj.getMasa() / r2) / Math.sqrt(r2);
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
        double masaBaseMayor = mayor.getTipoCuerpo() != null ? mayor.getTipoCuerpo().masaBase : 1.0;
        double factorMasaMayor = masaBaseMayor > 0 ? Math.max(0.1, mayor.getMasa() / masaBaseMayor) : 1.0;

        double aMenor = mayor.getMasa() >= 1e29
                ? (0.5 + 3.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor)
                : (0.35 + 2.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor);
        Vector2D fSobreMenor = dirHaciaMayor.multiplicar(menor.getMasa() * aMenor);
        menor.aplicarFuerza(fSobreMenor);
        mayor.aplicarFuerza(fSobreMenor.multiplicar(-1.0));
    }
}
