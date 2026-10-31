package org.example.game.guardado;
import org.example.game.cuerpo.TipoCuerpo; import java.util.Objects;
/**
 * Datos guardados inmutables de un cuerpo celeste individual (SRP).
 */
public record DatosCuerpo( TipoCuerpo tipo, String nombre, double masa, boolean simulado, EstadoCinematica cinematica, DetalleCuerpo detalle ) {
    public DatosCuerpo {
        Objects.requireNonNull(tipo, "El tipo de cuerpo no puede ser null");
        Objects.requireNonNull(cinematica, "El estado cinematico no puede ser null");
        Objects.requireNonNull(detalle, "El detalle del cuerpo no puede ser null");
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cuerpo no puede estar vacio");
        }
        nombre = nombre.trim();
        if (!Double.isFinite(masa) || masa < 0.0) {
            throw new IllegalArgumentException("La masa del cuerpo debe ser finita y no negativa");
        }
    }

    public boolean poseeOrbitaRegistrada() {
        return cinematica.tieneCuerpoOrbitado();
    }
}
