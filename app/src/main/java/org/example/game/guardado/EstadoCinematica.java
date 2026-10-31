package org.example.game.guardado;
import org.example.game.motor.Vector2D; import java.util.Objects;
/**
 * Instantanea inmutable del estado cinematico y orbital de un cuerpo celeste (SRP).
 */
public record EstadoCinematica( Vector2D posicion, Vector2D velocidad, int indiceOrbitado, double radioOrbita ) {
    public EstadoCinematica {
        Objects.requireNonNull(posicion, "La posicion no puede ser null");
        Objects.requireNonNull(velocidad, "La velocidad no puede ser null");
        indiceOrbitado = Math.max(-1, indiceOrbitado);
        radioOrbita = Double.isFinite(radioOrbita) ? Math.max(0.0, radioOrbita) : 0.0;
    }

    public boolean tieneCuerpoOrbitado() {
        return indiceOrbitado >= 0;
    }
}
