package org.example.game.guardado;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
/**
 * Contrato de persistencia para almacenar, recuperar y consultar partidas guardadas.
 *
 * Principios SOLID:
 * - SRP: Abstrae el almacenamiento de partidas sin exponer detalles del sistema de archivos ni formato.
 * - DIP / ISP: Permite sustituir el almacenamiento en disco por implementaciones en memoria para pruebas.
 */
public interface RepositorioPartidas {

    Path guardar(String nombreSlot, DatosPartida datos);

    Optional<DatosPartida> cargar(String nombreSlot);

    List<String> listarPartidas();

    boolean existe(String nombreSlot);

    boolean eliminar(String nombreSlot);
}
