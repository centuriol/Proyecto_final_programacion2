package org.example;

import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private InventarioJugador inventario;
    private MotorFisicaPermisiva motorFisica;
    private TrayectoriaPredictor predictor;

    @BeforeEach
    void setUp() {
        inventario = new InventarioJugador();
        motorFisica = new MotorFisicaPermisiva(1000.0);
        predictor = new TrayectoriaPredictor();
    }

    @Test
    void testInventarioRecursosIniciales() {
        assertTrue(inventario.getRecurso(TipoRecurso.MASA_ESTELAR) > 0);
        assertTrue(inventario.getRecurso(TipoRecurso.MATERIA_PLANETARIA) > 0);
        assertTrue(inventario.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
    }

    @Test
    void testCuerpoCelesteFactory() {
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        assertNotNull(estrella);
        assertEquals(TipoCuerpo.ESTRELLA, estrella.getTipoCuerpo());

        Vector2D pos = new Vector2D(150, 0);
        Vector2D velOrbital = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, pos, false);
        assertNotNull(velOrbital);
        assertTrue(velOrbital.magnitud() > 0);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(pos.x, pos.y, 1.0, velOrbital);
        assertNotNull(planeta);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, planeta.getTipoCuerpo());

        Luna luna = CuerpoCelesteFactory.crearLuna(170, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.LUNA, luna.getTipoCuerpo());

        Satelite sat = CuerpoCelesteFactory.crearSatelite(140, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.SATELITE, sat.getTipoCuerpo());

        EscudoProtector escudo = CuerpoCelesteFactory.crearEscudo(150, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.ESCUDO_DOME, escudo.getTipoCuerpo());
    }

    @Test
    void testFisicaPermisivaAvancePaso() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        cuerpos.add(estrella);

        Vector2D pos = new Vector2D(150, 0);
        Vector2D vel = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, pos, false);
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(pos.x, pos.y, 1.0, vel);
        cuerpos.add(planeta);

        Vector2D posInicial = new Vector2D(planeta.getPosicionX(), planeta.getPosicionY());

        // Ejecutar pasos de física
        for (int i = 0; i < 20; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        Vector2D posFinal = new Vector2D(planeta.getPosicionX(), planeta.getPosicionY());
        assertNotEquals(posInicial.y, posFinal.y);
        assertTrue(cuerpos.contains(planeta));
    }

    @Test
    void testPredictorTrayectoria() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        cuerpos.add(estrella);

        Vector2D posInicial = new Vector2D(150, 0);
        Vector2D velInicial = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, posInicial, false);

        List<Vector2D> tray = predictor.predecirTrayectoria(posInicial, velInicial, 5.97e24, cuerpos, 50, 1.0);
        assertNotNull(tray);
        assertEquals(51, tray.size()); // Posición inicial + 50 pasos
    }
}

