package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstracción para captura gravitacional, estabilización orbital y asistencia a cuerpos libres (DIP / ISP).
 */
public interface AsistenciaOrbital {
    CuerpoCeleste[] verificarYCapturarOrbita(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje);
    void inicializarVelocidadOrbital(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal);
    void aplicarDinamicaOrbitalEstable(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal);
    void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos, double asistenciaOrbital);
    double calcularAsistenciaEfectiva(double masa, double asistenciaOrbital);
    double calcularVelocidadOrbitalIdeal(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double rOrb);
}
