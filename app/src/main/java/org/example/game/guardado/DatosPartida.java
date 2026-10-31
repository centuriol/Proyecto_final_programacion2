package org.example.game.guardado;
import java.util.List;
import java.util.Objects;
/**
 * Modelo raiz inmutable de los datos guardados de una partida completa.
 *
 * Principios SOLID:
 * - SRP: Representa exclusivamente la instantanea completa de datos persistibles de una partida,
 *   desacoplado del formato de serializacion y del medio de almacenamiento.
 */
public record DatosPartida( String nombrePartida, long marcaTiempoGuardado, EstadoRelojGuardado reloj, EstadoJugadorGuardado jugador, List<DatosCuerpo> cuerpos, List<DatosNaveColonizadora> naves ) {
    public DatosPartida {
        Objects.requireNonNull(reloj, "El estado del reloj no puede ser null");
        Objects.requireNonNull(jugador, "El estado del jugador no puede ser null");
        if (nombrePartida == null || nombrePartida.isBlank()) {
            throw new IllegalArgumentException("El nombre de la partida no puede estar vacio");
        }
        nombrePartida = nombrePartida.trim();
        marcaTiempoGuardado = Math.max(0L, marcaTiempoGuardado);
        cuerpos = cuerpos != null ? List.copyOf(cuerpos) : List.of();
        naves = naves != null ? List.copyOf(naves) : List.of();
    }

    public DatosPartida(
            String nombrePartida,
            EstadoRelojGuardado reloj,
            EstadoJugadorGuardado jugador,
            List<DatosCuerpo> cuerpos,
            List<DatosNaveColonizadora> naves
    ) {
        this(nombrePartida, 0L, reloj, jugador, cuerpos, naves);
    }

    public DatosPartida conMarcaTiempo(long nuevaMarcaTiempo) {
        return new DatosPartida(nombrePartida, nuevaMarcaTiempo, reloj, jugador, cuerpos, naves);
    }

    public int cantidadCuerpos() {
        return cuerpos.size();
    }

    public int cantidadNaves() {
        return naves.size();
    }
}
