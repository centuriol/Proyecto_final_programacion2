package org.example.game.guardado;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * SRP: Resuelve, sanitiza y asegura rutas seguras dentro del directorio de guardado de partidas.
 */
public class ResolvedorRutaPartida implements ResolvedorRutas {

    private final Path directorioGuardado;

    public ResolvedorRutaPartida(Path directorioGuardado) {
        Objects.requireNonNull(directorioGuardado, "El directorio de guardado no puede ser null");
        this.directorioGuardado = directorioGuardado.toAbsolutePath().normalize();
        asegurarDirectorio();
    }

    public Path getDirectorioGuardado() { return directorioGuardado; }

    public void asegurarDirectorio() {
        try {
            Files.createDirectories(directorioGuardado);
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo crear la carpeta dedicada de partidas: " + directorioGuardado, e);
        }
    }

    public Path resolverRutaSegura(String nombreSlot, String extension) {
        if (nombreSlot == null || nombreSlot.isBlank()) {
            throw new ExcepcionPersistenciaPartida("El identificador de la partida no puede estar vacio");
        }
        String limpio = nombreSlot.trim();
        if (limpio.endsWith(extension)) {
            limpio = limpio.substring(0, limpio.length() - extension.length());
        }
        String sanitizado = limpio.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitizado.isBlank() || ".".equals(sanitizado) || "..".equals(sanitizado)) {
            throw new ExcepcionPersistenciaPartida("Identificador de partida invalido: " + nombreSlot);
        }
        Path resuelto = directorioGuardado.resolve(sanitizado + extension).normalize();
        if (!resuelto.startsWith(directorioGuardado)) {
            throw new ExcepcionPersistenciaPartida("Ruta de partida fuera del directorio permitido");
        }
        return resuelto;
    }

    public List<String> listarSlots(String extension) {
        if (!Files.isDirectory(directorioGuardado)) return List.of();
        try (Stream<Path> archivos = Files.list(directorioGuardado)) {
            return archivos
                    .filter(Files::isRegularFile)
                    .map(ruta -> ruta.getFileName().toString())
                    .filter(nombre -> nombre.endsWith(extension))
                    .map(nombre -> nombre.substring(0, nombre.length() - extension.length()))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo listar la carpeta de partidas guardadas", e);
        }
    }
}
