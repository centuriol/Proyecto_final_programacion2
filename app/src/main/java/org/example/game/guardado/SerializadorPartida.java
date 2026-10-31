package org.example.game.guardado;
/**
 * Contrato para la serializacion y deserializacion de DatosPartida.
 *
 * Principios SOLID:
 * - SRP: Define unicamente la conversion bidireccional entre DatosPartida y su representacion textual.
 * - OCP / DIP: Permite intercambiar formatos (propiedades, JSON, XML) sin modificar el repositorio.
 */
public interface SerializadorPartida {

    String serializar(DatosPartida datos);

    DatosPartida deserializar(String contenido);
}
