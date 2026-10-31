package org.example.game.guardado;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;
/**
 * Politica de gestion de las tres ranuras de guardado de Centuriol.
 * Ocupa primero las ranuras vacias y, cuando las tres estan ocupadas,
 * reemplaza la mas antigua segun su marca de tiempo monotonica (SRP / OCP).
 */
public class PoliticaRanurasGuardado {

    public static final int TOTAL_RANURAS = 3;
    public static final String PREFIJO_RANURA = "ranura_";

    private final LongSupplier proveedorTiempo;

    public PoliticaRanurasGuardado() {
        this(System::currentTimeMillis);
    }

    public PoliticaRanurasGuardado(LongSupplier proveedorTiempo) {
        this.proveedorTiempo = proveedorTiempo != null ? proveedorTiempo : System::currentTimeMillis;
    }

    public String identificadorRanura(int numeroRanura) {
        if (numeroRanura < 1 || numeroRanura > TOTAL_RANURAS) {
            throw new ExcepcionPersistenciaPartida("Numero de ranura fuera de rango (1-" + TOTAL_RANURAS + "): " + numeroRanura);
        }
        return PREFIJO_RANURA + numeroRanura;
    }

    public List<InfoRanuraGuardado> consultarRanuras(RepositorioPartidas repositorio) {
        Objects.requireNonNull(repositorio, "El repositorio no puede ser null");
        List<InfoRanuraGuardado> ranuras = new ArrayList<>(TOTAL_RANURAS);
        for (int i = 1; i <= TOTAL_RANURAS; i++) {
            String slot = identificadorRanura(i);
            DatosPartida cargada = cargarSeguro(repositorio, slot);
            ranuras.add(new InfoRanuraGuardado(i, slot, cargada));
        }
        return List.copyOf(ranuras);
    }

    public InfoRanuraGuardado seleccionarRanuraDestino(RepositorioPartidas repositorio) {
        List<InfoRanuraGuardado> ranuras = consultarRanuras(repositorio);
        for (InfoRanuraGuardado ranura : ranuras) {
            if (!ranura.estaOcupada()) {
                return ranura;
            }
        }
        return ranuras.stream()
                .min(Comparator.comparingLong(InfoRanuraGuardado::marcaTiempo)
                        .thenComparingInt(InfoRanuraGuardado::numeroRanura))
                .orElseThrow();
    }

    public Optional<InfoRanuraGuardado> obtenerUltimoGuardado(RepositorioPartidas repositorio) {
        return consultarRanuras(repositorio).stream()
                .filter(InfoRanuraGuardado::estaOcupada)
                .max(Comparator.comparingLong(InfoRanuraGuardado::marcaTiempo)
                        .thenComparingInt(InfoRanuraGuardado::numeroRanura));
    }

    public long siguienteMarcaTiempo(RepositorioPartidas repositorio) {
        long maxExistente = consultarRanuras(repositorio).stream()
                .filter(InfoRanuraGuardado::estaOcupada)
                .mapToLong(InfoRanuraGuardado::marcaTiempo)
                .max()
                .orElse(0L);
        return Math.max(proveedorTiempo.getAsLong(), maxExistente + 1L);
    }

    private DatosPartida cargarSeguro(RepositorioPartidas repositorio, String slot) {
        try {
            return repositorio.cargar(slot).orElse(null);
        } catch (ExcepcionPersistenciaPartida ignored) {
            return null;
        }
    }
}
