package org.example.game.guardado;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * SRP: Administra la ranura activa seleccionada y la sincronización de identificadores con la política de ranuras.
 */
public class SelectorRanuraActiva {

    private final PoliticaRanuras politica;
    private int ranuraSeleccionada = 1;

    public SelectorRanuraActiva(PoliticaRanuras politica) {
        this.politica = Objects.requireNonNull(politica, "La politica de ranuras no puede ser null");
    }

    public int getRanuraSeleccionada() { return ranuraSeleccionada; }

    public void setRanuraSeleccionada(int numeroRanura) {
        politica.identificadorRanura(numeroRanura);
        this.ranuraSeleccionada = numeroRanura;
    }

    public InfoRanuraGuardado seleccionarDestino(RepositorioPartidas repositorio) {
        InfoRanuraGuardado destino = politica.seleccionarRanuraDestino(repositorio);
        this.ranuraSeleccionada = destino.numeroRanura();
        return destino;
    }

    public String identificadorActiva() { return politica.identificadorRanura(ranuraSeleccionada); }
    public String identificador(int numeroRanura) { return politica.identificadorRanura(numeroRanura); }
    public long siguienteMarcaTiempo(RepositorioPartidas repo) { return politica.siguienteMarcaTiempo(repo); }
    public Optional<InfoRanuraGuardado> obtenerUltimoGuardado(RepositorioPartidas repo) { return politica.obtenerUltimoGuardado(repo); }
    public List<InfoRanuraGuardado> consultarRanuras(RepositorioPartidas repo) { return politica.consultarRanuras(repo); }

    public void sincronizarDesdeIdentificador(String slot) {
        if (slot == null || !slot.startsWith(PoliticaRanurasGuardado.PREFIJO_RANURA)) return;
        String sufijo = slot.substring(PoliticaRanurasGuardado.PREFIJO_RANURA.length());
        try {
            int numero = Integer.parseInt(sufijo);
            if (numero >= 1 && numero <= PoliticaRanurasGuardado.TOTAL_RANURAS) {
                this.ranuraSeleccionada = numero;
            }
        } catch (NumberFormatException e) {
            System.err.println("[INFO] Ranura personalizada no numerica: " + slot);
        }
    }
}
