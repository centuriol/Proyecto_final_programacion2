package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstracción para cálculo de fuerzas gravitacionales entre cuerpos simulados (DIP / ISP).
 */
public interface CalculadorFuerzaGravitatoria {
    void calcularGravedad(List<CuerpoCeleste> cuerpos, AsistenciaOrbital asistenteOrbital, Consumer<String> listenerMensaje);
}
