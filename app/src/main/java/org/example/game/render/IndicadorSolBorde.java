package org.example.game.render;
import org.example.game.cuerpo.CuerpoCeleste;
import java.util.Objects;
/**
 * Datos inmutables de proyeccion en el borde de la pantalla para un sol fuera de vista (SRP).
 */
public record IndicadorSolBorde( CuerpoCeleste sol, double pantallaX, double pantallaY, double anguloGrados ) {

    public IndicadorSolBorde {
        Objects.requireNonNull(sol, "El sol asociado al indicador no puede ser null");
    }

    public String etiqueta() {
        String nombre = sol.getNombre();
        return (nombre != null && !nombre.isBlank()) ? nombre : "Sol";
    }
}
