package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.function.Consumer;

/**
 * Orquesta la captura gravitacional y delega límites y estabilización orbital (SRP / DIP / OCP).
 */
public class AsistenteOrbital implements AsistenciaOrbital {

    private static final LimitesOrbitales LIMITES_DEFECTO = new LimitesOrbitales();
    private static final FabricaMotorFisica FABRICA = new FabricaMotorFisica();

    private final List<EstrategiaCapturaOrbital> estrategiasCaptura;
    private final EstabilizadorOrbital estabilizador;

    public AsistenteOrbital(double G, List<EstrategiaCapturaOrbital> estrategiasCaptura, CalculadorOrbital calculadorOrbital) {
        this.estrategiasCaptura = estrategiasCaptura != null ? estrategiasCaptura : FABRICA.crearEstrategiasCapturaPorDefecto();
        this.estabilizador = new EstabilizadorOrbital(G, calculadorOrbital, LIMITES_DEFECTO);
    }

    public AsistenteOrbital(double G, List<EstrategiaCapturaOrbital> estrategiasCaptura) {
        this(G, estrategiasCaptura, new CalculadorOrbital());
    }

    public AsistenteOrbital(double G) {
        this(G, null, new CalculadorOrbital());
    }

    public AsistenteOrbital() {
        this(ConstantesFisicas.G_ESCALADO, null, new CalculadorOrbital());
    }

    public CuerpoCeleste[] verificarYCapturarOrbita(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje) {
        if (LIMITES_DEFECTO.debeRomperOrbitaPorMasa(ci, cj)) {
            if (ci.estaOrbitando(cj)) ci.setCuerpoOrbitado(null);
            if (cj.estaOrbitando(ci)) cj.setCuerpoOrbitado(null);
            return null;
        }
        if (ci.estaOrbitando(cj)) return new CuerpoCeleste[]{ci, cj};
        if (cj.estaOrbitando(ci)) return new CuerpoCeleste[]{cj, ci};

        for (EstrategiaCapturaOrbital estrategia : estrategiasCaptura) {
            if (estrategia.puedeCapturar(ci, cj, distReal)) {
                return estrategia.ejecutarCaptura(ci, cj, distReal, listenerMensaje, this);
            }
        }
        return null;
    }

    public static double calcularRadioOrbitalMinimo(CuerpoCeleste orbitando, CuerpoCeleste anfitrion) {
        return LIMITES_DEFECTO.calcularRadioMinimo(orbitando, anfitrion);
    }

    public static double calcularRadioOrbitalMaximo(CuerpoCeleste orbitando, CuerpoCeleste anfitrion) {
        return LIMITES_DEFECTO.calcularRadioMaximo(orbitando, anfitrion);
    }

    public static double acotarRadioOrbital(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double radioDeseado) {
        return LIMITES_DEFECTO.acotarRadio(orbitando, anfitrion, radioDeseado);
    }

    public void inicializarVelocidadOrbital(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        estabilizador.inicializarVelocidadOrbital(orbitando, anfitrion, distReal);
    }

    public void aplicarDinamicaOrbitalEstable(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        estabilizador.aplicarDinamicaOrbitalEstable(orbitando, anfitrion, distReal);
    }

    public void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos, double asistenciaOrbital) {
        estabilizador.aplicarAsistenciaOrbital(cuerpos, asistenciaOrbital);
    }

    public double calcularAsistenciaEfectiva(double masa, double asistenciaOrbital) {
        return estabilizador.calcularAsistenciaEfectiva(masa, asistenciaOrbital);
    }

    public double calcularVelocidadOrbitalIdeal(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double rOrb) {
        return estabilizador.calcularVelocidadOrbitalIdeal(orbitando, anfitrion, rOrb);
    }
}
