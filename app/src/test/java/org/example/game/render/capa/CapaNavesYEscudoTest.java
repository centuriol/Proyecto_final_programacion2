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
}
