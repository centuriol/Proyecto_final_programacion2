package org.example.game.civilizacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import java.util.List;

/**
 * Abstracción para evaluar condiciones astrofísicas de habitabilidad alrededor de un planeta (DIP / ISP).
 */
public interface AnalisisHabitabilidad {
    CondicionesHabitabilidad evaluar(Planeta planeta, List<CuerpoCeleste> cuerpos);
}
