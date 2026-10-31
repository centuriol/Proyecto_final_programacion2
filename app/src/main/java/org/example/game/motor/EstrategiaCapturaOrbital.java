package org.example.game.motor;
import org.example.game.cuerpo.CuerpoCeleste; import java.util.function.Consumer;
/**
 * Estrategia de detección y captura gravitacional de órbitas relativas (Strategy / OCP).
 */
public interface EstrategiaCapturaOrbital {
    boolean puedeCapturar(CuerpoCeleste ci, CuerpoCeleste cj, double distReal);
    CuerpoCeleste[] ejecutarCaptura(CuerpoCeleste ci, CuerpoCeleste cj, double distReal, Consumer<String> listenerMensaje, AsistenteOrbital asistente);
}
