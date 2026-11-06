package org.example.game.render.capa;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.motor.Vector2D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CapaNavesYEscudoTest {

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testDibujarNavesYEscudoSinErrores() {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        CapaEscudoPlanetario capaEscudo = new CapaEscudoPlanetario();
        assertDoesNotThrow(() -> capaEscudo.dibujarCupula(gc, 400, 300, 20, 10));

        CapaNavesColonizadoras capaNaves = new CapaNavesColonizadoras();
        NaveColonizadora nave = new NaveColonizadora(Vector2D.cero(), null, 50.0);
        assertDoesNotThrow(() -> capaNaves.dibujar(gc, () -> List.of(nave), new TransformadorCoordenadas(), 800, 600));
    }

    @Test
    void testEscaladoPersonasPorPoblacionEnPlanetaPorro() {
        assertEquals(0, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(50));
        assertEquals(0, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(99));
        assertEquals(1, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(100));
        assertEquals(1, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(999));
        assertEquals(2, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(1000));
        assertEquals(3, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(2000));
        assertEquals(24, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(1_000_000));
        assertEquals(24, org.example.game.render.cuerpo.RenderizadorPlaneta.calcularCantidadPersonasVisibles(5_000_000));

        Canvas canvas = new Canvas(200, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        org.example.game.cuerpo.Planeta porro = new org.example.game.cuerpo.CuerpoCelesteFactory()
                .crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        org.example.game.render.cuerpo.RenderizadorPlaneta renderizador =
                new org.example.game.render.cuerpo.RenderizadorPlaneta(new org.example.game.render.GestorImagenes());

        porro.getCivilizacion().setPoblacion(1000);
        assertDoesNotThrow(() -> renderizador.dibujar(gc, porro, org.example.game.cuerpo.TipoCuerpo.PLANETA_ROCOSO, porro.getRadio(), 1));
    }
}
