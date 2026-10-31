package org.example.game.guardado;
import org.example.game.motor.Vector2D;
import java.util.Objects;
/**
 * Datos guardados inmutables de una nave colonizadora en transito (SRP).
 */
public record DatosNaveColonizadora( Vector2D posicion, int indiceDestino, double poblacionColonos ) {
    public DatosNaveColonizadora {
        Objects.requireNonNull(posicion, "La posicion de la nave no puede ser null");
        if (indiceDestino < 0) {
            throw new IllegalArgumentException("El indice de destino debe ser no negativo");
        }
        poblacionColonos = Double.isFinite(poblacionColonos) ? Math.max(0.0, poblacionColonos) : 0.0;
    }

    public boolean esDestinoValido(int totalCuerpos) {
        return indiceDestino >= 0 && indiceDestino < totalCuerpos;
    }
}
