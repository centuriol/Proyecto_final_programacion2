package org.example.game.guardado;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
/**
 * Repositorio basado en archivos que escribe y lee partidas en una carpeta dedicada,
 * creandola automaticamente si no existe.
 *
 * Principios SOLID:
 * - SRP: Gestiona exclusivamente las operaciones de directorio y archivos de guardado.
 * - DIP: Depende de la abstraccion SerializadorPartida para transformar los datos.
 */
public class RepositorioPartidasArchivo implements RepositorioPartidas {

    public static final String CARPETA_POR_DEFECTO = "partidas_guardadas";
    public static final String EXTENSION_PARTIDA = ".orbita";

    private final Path directorioGuardado;
    private final SerializadorPartida serializador;

    public RepositorioPartidasArchivo() {
        this(Paths.get(CARPETA_POR_DEFECTO), new SerializadorPartidaTexto());
    }

    public RepositorioPartidasArchivo(Path directorioGuardado) {
        this(directorioGuardado, new SerializadorPartidaTexto());
    }

    public RepositorioPartidasArchivo(Path directorioGuardado, SerializadorPartida serializador) {
        Objects.requireNonNull(directorioGuardado, "El directorio de guardado no puede ser null");
        Objects.requireNonNull(serializador, "El serializador no puede ser null");
        this.directorioGuardado = directorioGuardado.toAbsolutePath().normalize();
        this.serializador = serializador;
        asegurarDirectorio();
    }

    @Override
    public Path guardar(String nombreSlot, DatosPartida datos) {
        if (datos == null) {
            throw new ExcepcionPersistenciaPartida("Los datos de la partida a guardar no pueden ser nulos");
        }
        Path archivoDestino = resolverRutaSegura(nombreSlot);
        String contenido = serializador.serializar(datos);
        asegurarDirectorio();
        try {
            Files.writeString(archivoDestino, contenido, StandardCharsets.UTF_8);
            return archivoDestino;
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo escribir el archivo de partida: " + archivoDestino, e);
        }
    }

    @Override
    public Optional<DatosPartida> cargar(String nombreSlot) {
        Path archivoOrigen = resolverRutaSegura(nombreSlot);
        if (!Files.isRegularFile(archivoOrigen)) {
            return Optional.empty();
        }
        try {
            String contenido = Files.readString(archivoOrigen, StandardCharsets.UTF_8);
            return Optional.of(serializador.deserializar(contenido));
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo leer el archivo de partida: " + archivoOrigen, e);
        }
    }

    @Override
    public List<String> listarPartidas() {
        if (!Files.isDirectory(directorioGuardado)) {
            return List.of();
        }
        try (Stream<Path> archivos = Files.list(directorioGuardado)) {
            return archivos
                    .filter(Files::isRegularFile)
                    .map(ruta -> ruta.getFileName().toString())
                    .filter(nombre -> nombre.endsWith(EXTENSION_PARTIDA))
                    .map(nombre -> nombre.substring(0, nombre.length() - EXTENSION_PARTIDA.length()))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo listar la carpeta de partidas guardadas", e);
        }
    }

    @Override
    public boolean existe(String nombreSlot) {
        return Files.isRegularFile(resolverRutaSegura(nombreSlot));
    }

    @Override
    public boolean eliminar(String nombreSlot) {
        Path archivo = resolverRutaSegura(nombreSlot);
        try {
            return Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo eliminar la partida: " + archivo, e);
        }
    }

    public Path getDirectorioGuardado() {
        return directorioGuardado;
    }

    private void asegurarDirectorio() {
        try {
            Files.createDirectories(directorioGuardado);
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo crear la carpeta dedicada de partidas: " + directorioGuardado, e);
        }
    }

    private Path resolverRutaSegura(String nombreSlot) {
        if (nombreSlot == null || nombreSlot.isBlank()) {
            throw new ExcepcionPersistenciaPartida("El identificador de la partida no puede estar vacio");
        }
        String limpio = nombreSlot.trim();
        if (limpio.endsWith(EXTENSION_PARTIDA)) {
            limpio = limpio.substring(0, limpio.length() - EXTENSION_PARTIDA.length());
        }
        String sanitizado = limpio.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitizado.isBlank() || ".".equals(sanitizado) || "..".equals(sanitizado)) {
            throw new ExcepcionPersistenciaPartida("Identificador de partida invalido: " + nombreSlot);
        }
        Path resuelto = directorioGuardado.resolve(sanitizado + EXTENSION_PARTIDA).normalize();
        if (!resuelto.startsWith(directorioGuardado)) {
            throw new ExcepcionPersistenciaPartida("Ruta de partida fuera del directorio permitido");
        }
        return resuelto;
    }
}
