package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;

import java.util.List;

/**
 * Responsable exclusivamente de evaluar las condiciones astrofísicas de fin de partida (derrota) (SRP).
 */
public class EvaluadorCondicionDerrota {

    public boolean estaPerdida(List<CuerpoCeleste> cuerpos) {
        if (cuerpos == null) return false;
        boolean hayAgujeroNegro = false;
        boolean hayPlanetaOLuna = false;
        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.esSimulado()) {
                if (c.getMasa() >= 1e31) {
                    hayAgujeroNegro = true;
                } else if (c.permiteReembolso() && c.getMasa() >= 1e22 && c.getMasa() <= 1e28) {
                    hayPlanetaOLuna = true;
                }
            }
        }
        return hayAgujeroNegro && !hayPlanetaOLuna;
    }
}
