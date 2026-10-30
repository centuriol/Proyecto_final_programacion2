package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Luna;
import org.example.game.cuerpo.Planeta;
import org.example.game.motor.Vector2D;

/**
 * Generador de presets y configuraciones iniciales de cuerpos celestes (SRP).
 */
public class PresetsSistema {

    public static void cargarPresetSistemaBasico(SimulacionSolar simulacion) {
        simulacion.limpiarCuerpos();

        // 1. Estrella Sol central
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        // 2. Planeta Rocoso (Tierra) con civilización próspera
        Vector2D posTierra = new Vector2D(160, 0);
        Vector2D velTierra = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(sol, posTierra, false);
        Planeta tierra = CuerpoCelesteFactory.crearPlanetaRocoso(posTierra.x(), posTierra.y(), 1.0, velTierra);
        tierra.setCuerpoOrbitado(sol);
        tierra.desarrollarCivilizacion();
        simulacion.agregarCuerpo(tierra);

        // 3. Luna orbitando a la Tierra (órbita lejana para dejar espacio interior a satélites)
        Vector2D posLuna = new Vector2D(160 + 65, 0);
        Vector2D velLuna = velTierra.sumar(new Vector2D(0, -1.07));
        Luna luna = CuerpoCelesteFactory.crearLuna(posLuna.x(), posLuna.y(), 0.8, velLuna);
        luna.setCuerpoOrbitado(tierra);
        simulacion.agregarCuerpo(luna);
    }

    public static void cargarPresetEstrellaBinaria(SimulacionSolar simulacion) {
        simulacion.limpiarCuerpos();

        // Dos estrellas orbitándose
        Estrella sol1 = CuerpoCelesteFactory.crearEstrella(-100, 0, 0.8);
        sol1.setVelocidad(new Vector2D(0, 55));
        simulacion.agregarCuerpo(sol1);

        Estrella sol2 = CuerpoCelesteFactory.crearEstrella(100, 0, 0.8);
        sol2.setVelocidad(new Vector2D(0, -55));
        simulacion.agregarCuerpo(sol2);

        // Planeta circumbinario exterior
        Planeta planetaExt = CuerpoCelesteFactory.crearPlanetaRocoso(0, 360, 1.0, new Vector2D(-88, 0));
        planetaExt.desarrollarCivilizacion();
        simulacion.agregarCuerpo(planetaExt);
    }
}
