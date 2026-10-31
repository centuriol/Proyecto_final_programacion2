package org.example.game.guardado;
import org.example.game.civilizacion.EstadoCivilizacion; import java.util.Objects;
/**
 * Instantanea inmutable del estado de una civilizacion planetaria para persistencia (SRP).
 */
public record DatosCivilizacion( double poblacion, int nivelTecnologico, double estabilidad, double felicidad, EstadoCivilizacion estado, int ticksEfectoEvento ) {
    public DatosCivilizacion {
        Objects.requireNonNull(estado, "El estado de civilizacion no puede ser null");
        poblacion = Double.isFinite(poblacion) ? Math.max(0.0, poblacion) : 0.0;
        nivelTecnologico = Math.max(0, Math.min(10, nivelTecnologico));
        estabilidad = Double.isFinite(estabilidad) ? Math.max(0.0, Math.min(1.0, estabilidad)) : 0.0;
        felicidad = Double.isFinite(felicidad) ? Math.max(0.0, Math.min(1.0, felicidad)) : 0.0;
        ticksEfectoEvento = Math.max(0, ticksEfectoEvento);
    }

    public boolean estaViva() {
        return estado != EstadoCivilizacion.EXTINGUIDA && poblacion > 0.0;
    }
}
