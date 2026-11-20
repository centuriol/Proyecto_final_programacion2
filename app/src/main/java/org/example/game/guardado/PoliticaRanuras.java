package org.example.game.guardado;

import java.util.List;
import java.util.Optional;

/**
 * Abstracción de política de ranuras de guardado y ordenamiento temporal (DIP / ISP).
 */
public interface PoliticaRanuras {
    String identificadorRanura(int numeroRanura);
    List<InfoRanuraGuardado> consultarRanuras(RepositorioPartidas repositorio);
    InfoRanuraGuardado seleccionarRanuraDestino(RepositorioPartidas repositorio);
    Optional<InfoRanuraGuardado> obtenerUltimoGuardado(RepositorioPartidas repositorio);
    long siguienteMarcaTiempo(RepositorioPartidas repositorio);
}
