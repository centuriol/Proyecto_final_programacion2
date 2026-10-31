package org.example.game.guardado;
/**
 * Excepcion de dominio para errores de serializacion, deserializacion o acceso al repositorio de partidas.
 */
public class ExcepcionPersistenciaPartida extends RuntimeException {

    public ExcepcionPersistenciaPartida(String mensaje) {
        super(mensaje);
    }

    public ExcepcionPersistenciaPartida(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
