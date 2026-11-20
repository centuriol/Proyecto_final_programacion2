package org.example.game.guardado;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repositorio basado en archivos que escribe y lee partidas delegando rutas en ResolvedorRutaPartida (SRP / DIP).
 */
public class RepositorioPartidasArchivo implements RepositorioPartidas {

    public static final String CARPETA_POR_DEFECTO = "partidas_guardadas";
    public static final String EXTENSION_PARTIDA = ".orbita";

    private static final FabricaGuardado FABRICA = new FabricaGuardado();

    private final ResolvedorRutas resolvedorRutas;
    private final SerializadorPartida serializador;

    public RepositorioPartidasArchivo() {
        this(Paths.get(CARPETA_POR_DEFECTO), FABRICA.crearSerializadorPorDefecto());
    }

    public RepositorioPartidasArchivo(Path directorioGuardado) {
        this(directorioGuardado, FABRICA.crearSerializadorPorDefecto());
    }

    public RepositorioPartidasArchivo(Path directorioGuardado, SerializadorPartida serializador) {
        this(FABRICA.crearResolvedorRutas(directorioGuardado), serializador);
    }

    public RepositorioPartidasArchivo(ResolvedorRutas resolvedorRutas, SerializadorPartida serializador) {
        this.resolvedorRutas = Objects.requireNonNull(resolvedorRutas, "El resolvedor de rutas no puede ser null");
        this.serializador = Objects.requireNonNull(serializador, "El serializador no puede ser null");
    }

    @Override
    public Path guardar(String nombreSlot, DatosPartida datos) {
        if (datos == null) throw new ExcepcionPersistenciaPartida("Los datos de la partida a guardar no pueden ser nulos");
        Path archivoDestino = resolvedorRutas.resolverRutaSegura(nombreSlot, EXTENSION_PARTIDA);
        String contenido = serializador.serializar(datos);
        resolvedorRutas.asegurarDirectorio();
        try {
            Files.writeString(archivoDestino, contenido, StandardCharsets.UTF_8);
            return archivoDestino;
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo escribir el archivo de partida: " + archivoDestino, e);
        }
    }

    @Override
    public Optional<DatosPartida> cargar(String nombreSlot) {
        Path archivoOrigen = resolvedorRutas.resolverRutaSegura(nombreSlot, EXTENSION_PARTIDA);
        if (!Files.isRegularFile(archivoOrigen)) return Optional.empty();
        try {
            return Optional.of(serializador.deserializar(Files.readString(archivoOrigen, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo leer el archivo de partida: " + archivoOrigen, e);
        }
    }

    @Override
    public List<String> listarPartidas() {
        return resolvedorRutas.listarSlots(EXTENSION_PARTIDA);
    }

    @Override
    public boolean existe(String nombreSlot) {
        return Files.isRegularFile(resolvedorRutas.resolverRutaSegura(nombreSlot, EXTENSION_PARTIDA));
    }

    @Override
    public boolean eliminar(String nombreSlot) {
        Path archivo = resolvedorRutas.resolverRutaSegura(nombreSlot, EXTENSION_PARTIDA);
        try {
            return Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo eliminar la partida: " + archivo, e);
        }
    }

    public Path getDirectorioGuardado() { return resolvedorRutas.getDirectorioGuardado(); }
}
