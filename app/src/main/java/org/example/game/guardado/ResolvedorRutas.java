package org.example.game.guardado;

import java.nio.file.Path;
import java.util.List;

/**
 * Abstracción para resolución, validación y listado seguro de rutas de archivos de guardado (DIP / ISP).
 */
public interface ResolvedorRutas {
    Path getDirectorioGuardado();
    void asegurarDirectorio();
    Path resolverRutaSegura(String nombreSlot, String extension);
    List<String> listarSlots(String extension);
}
