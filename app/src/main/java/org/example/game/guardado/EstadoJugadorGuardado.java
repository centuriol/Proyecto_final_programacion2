package org.example.game.guardado;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.RamaProgresion;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
/**
 * Datos guardados inmutables de recursos economicos y progresion tecnologica del jugador (SRP).
 */
public record EstadoJugadorGuardado( Map<TipoRecurso, Double> recursos, Map<RamaProgresion, Integer> nivelesProgresion, int nivelEscudo ) {
    public EstadoJugadorGuardado {
        Map<TipoRecurso, Double> copiaRecursos = new EnumMap<>(TipoRecurso.class);
        if (recursos != null) {
            for (Map.Entry<TipoRecurso, Double> entry : recursos.entrySet()) {
                double val = entry.getValue() != null && Double.isFinite(entry.getValue())
                        ? Math.max(0.0, entry.getValue())
                        : 0.0;
                copiaRecursos.put(entry.getKey(), val);
            }
        }
        Map<RamaProgresion, Integer> copiaNiveles = new EnumMap<>(RamaProgresion.class);
        if (nivelesProgresion != null) {
            for (Map.Entry<RamaProgresion, Integer> entry : nivelesProgresion.entrySet()) {
                int niv = entry.getValue() != null ? Math.max(0, entry.getValue()) : 0;
                copiaNiveles.put(entry.getKey(), niv);
            }
        }
        recursos = Collections.unmodifiableMap(copiaRecursos);
        nivelesProgresion = Collections.unmodifiableMap(copiaNiveles);
        nivelEscudo = Math.max(0, nivelEscudo);
    }

    public double obtenerRecurso(TipoRecurso tipo) {
        return tipo != null ? recursos.getOrDefault(tipo, 0.0) : 0.0;
    }

    public int obtenerNivelRama(RamaProgresion rama) {
        return rama != null ? nivelesProgresion.getOrDefault(rama, 0) : 0;
    }
}
