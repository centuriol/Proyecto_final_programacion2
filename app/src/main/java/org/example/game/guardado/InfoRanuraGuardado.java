package org.example.game.guardado;
import java.time.Instant; import java.time.ZoneId; import java.time.format.DateTimeFormatter; import java.util.Objects;
/**
 * Representa el estado e informacion descriptiva de una ranura fija de guardado (SRP).
 */
public record InfoRanuraGuardado( int numeroRanura, String identificadorSlot, DatosPartida datos ) {
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public InfoRanuraGuardado {
        if (numeroRanura < 1) {
            throw new IllegalArgumentException("El numero de ranura debe ser positivo");
        }
        Objects.requireNonNull(identificadorSlot, "El identificador de ranura no puede ser null");
        if (identificadorSlot.isBlank()) {
            throw new IllegalArgumentException("El identificador de ranura no puede estar vacio");
        }
    }

    public boolean estaOcupada() {
        return datos != null;
    }

    public long marcaTiempo() {
        return datos != null ? datos.marcaTiempoGuardado() : Long.MIN_VALUE;
    }

    public String tituloRanura() {
        return "Ranura " + numeroRanura;
    }

    public String formatearResumen() {
        if (!estaOcupada()) {
            return "Vacia";
        }
        String detalleBase = "Tick " + datos.reloj().tickActual()
                + " | Cuerpos: " + datos.cantidadCuerpos();
        if (datos.marcaTiempoGuardado() > 0L) {
            return detalleBase + " | " + FORMATO_FECHA.format(Instant.ofEpochMilli(datos.marcaTiempoGuardado()));
        }
        return detalleBase;
    }
}
