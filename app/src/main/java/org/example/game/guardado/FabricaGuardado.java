package org.example.game.guardado;

import java.nio.file.Path;

/**
 * Fábrica de ensamblaje por defecto del subsistema de persistencia (Composition Root para DIP).
 */
public class FabricaGuardado {

    public RepositorioPartidas crearRepositorioPorDefecto() {
        return new RepositorioPartidasArchivo();
    }

    public MapeadorPartida crearMapeadorPorDefecto() {
        return new MapeadorEstadoSimulacion(crearReconstructorPorDefecto());
    }

    public PoliticaRanuras crearPoliticaPorDefecto() {
        return new PoliticaRanurasGuardado();
    }

    public ReconstructorCuerpo crearReconstructorPorDefecto() {
        return new ReconstructorCuerpos();
    }

    public SerializadorPartida crearSerializadorPorDefecto() {
        return new SerializadorPartidaTexto(crearCodificadorPorDefecto());
    }

    public CodificadorEntidades crearCodificadorPorDefecto() {
        return new CodificadorEntidadesPartida();
    }

    public ResolvedorRutas crearResolvedorRutas(Path directorio) {
        return new ResolvedorRutaPartida(directorio);
    }

    public org.example.game.cuerpo.GeneradorIdentidadCuerpos crearGeneradorIdentidadPorDefecto() {
        return new org.example.game.cuerpo.CuerpoCelesteFactory();
    }
}
