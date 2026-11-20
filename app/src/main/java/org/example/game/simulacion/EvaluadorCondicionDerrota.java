package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.GestorAgujeroNegro;

import java.util.List;

/**
 * Responsable exclusivamente de evaluar las condiciones astrofísicas de fin de partida (derrota) (SRP).
 */
public class EvaluadorCondicionDerrota implements EvaluadorDerrota {

    public boolean estaPerdida(List<CuerpoCeleste> cuerpos) {
        if (cuerpos == null) return false;
        boolean hayAgujeroNegro = false;
        boolean hayPlanetaOLuna = false;
        for (CuerpoCeleste c : cuerpos) {
            if (c == null || !c.esSimulado()) continue;
            if (esAgujeroNegro(c)) {
                hayAgujeroNegro = true;
            } else if (esPlanetaOLunaSobreviviente(c)) {
                hayPlanetaOLuna = true;
            }
        }
        return hayAgujeroNegro && !hayPlanetaOLuna;
    }

    private boolean esAgujeroNegro(CuerpoCeleste c) {
        TipoCuerpo tipo = c.getTipoCuerpo();
        return tipo == TipoCuerpo.AGUJERO_NEGRO
                || tipo == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO
                || GestorAgujeroNegro.esAgujeroNegro(c);
    }

    private boolean esPlanetaOLunaSobreviviente(CuerpoCeleste c) {
        TipoCuerpo tipo = c.getTipoCuerpo();
        return tipo != TipoCuerpo.ESTRELLA
                && tipo != TipoCuerpo.METEORITO
                && tipo != TipoCuerpo.SATELITE
                && c.getMasa() >= 1e22;
    }
}
