package org.example.game.simulacion;

import org.example.game.cuerpo.*;
import org.example.game.motor.CalculadorOrbital;
import org.example.game.motor.Vector2D;

/**
 * Generador de presets y configuraciones iniciales de cuerpos celestes (SRP / DIP).
 */
public class PresetsSistema {

    private final FabricaCuerpos fabricaCuerpos;

    public PresetsSistema() {
        this(new CuerpoCelesteFactory());
    }

    public PresetsSistema(FabricaCuerpos fabricaCuerpos) {
        this.fabricaCuerpos = fabricaCuerpos != null ? fabricaCuerpos : new CuerpoCelesteFactory();
    }

    public void cargarPresetSistemaBasico(SimulacionSolar simulacion) {
        simulacion.limpiarCuerpos();

        // 1. Estrella Sol central
        Estrella sol = fabricaCuerpos.crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        // 2. Planeta Rocoso (Tierra) con civilización próspera
        Vector2D posTierra = new Vector2D(160, 0);
        Vector2D velTierra = CalculadorOrbital.calcularVelocidadOrbitalVector(sol, posTierra, false);
        Planeta tierra = fabricaCuerpos.crearPlaneta(TipoCuerpo.PLANETA_ROCOSO, posTierra.x(), posTierra.y(), 1.0, velTierra);
        tierra.setCuerpoOrbitado(sol);
        tierra.desarrollarCivilizacion();
        simulacion.agregarCuerpo(tierra);

        // 3. Luna orbitando a la Tierra (órbita lejana para dejar espacio interior a satélites)
        Vector2D posLuna = new Vector2D(160 + 65, 0);
        Vector2D velLuna = velTierra.sumar(new Vector2D(0, -1.07));
        Luna luna = fabricaCuerpos.crearLuna(posLuna.x(), posLuna.y(), 0.8, velLuna);
        luna.setCuerpoOrbitado(tierra);
        simulacion.agregarCuerpo(luna);
    }

    public void cargarPresetEstrellaBinaria(SimulacionSolar simulacion) {
        simulacion.limpiarCuerpos();

        // Dos estrellas orbitándose
        Estrella sol1 = fabricaCuerpos.crearEstrella(-100, 0, 0.8);
        sol1.setVelocidad(new Vector2D(0, 55));
        simulacion.agregarCuerpo(sol1);

        Estrella sol2 = fabricaCuerpos.crearEstrella(100, 0, 0.8);
        sol2.setVelocidad(new Vector2D(0, -55));
        simulacion.agregarCuerpo(sol2);

        // Planeta circumbinario exterior
        Planeta planetaExt = fabricaCuerpos.crearPlaneta(TipoCuerpo.PLANETA_ROCOSO, 0, 360, 1.0, new Vector2D(-88, 0));
        planetaExt.desarrollarCivilizacion();
        simulacion.agregarCuerpo(planetaExt);
    }
}
