package org.example;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import org.example.game.controlador.BucleJuego;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.Vector2D;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
public class RendimientoRegresionTest {

    @Test
    void testAvanzarTickNoDisparaCallbacksUIDirectamente() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        AtomicInteger conteoUI = new AtomicInteger(0);

        sim.agregarListenerUI(conteoUI::incrementAndGet);

        // Ejecutar 10 ticks de simulacion fisica
        for (int i = 0; i < 10; i++) {
            sim.avanzarTick();
        }

        // Regresion: avanzarTick no debe invocar callbacks de UI
        assertEquals(0, conteoUI.get(),
                "avanzarTick() no debe invocar callbacks de UI directamente; deben sincronizarse por frame");

        // Al invocar notificarListenersUI explicitamente (fin de frame), se debe disparar
        sim.notificarListenersUI();
        assertEquals(1, conteoUI.get(),
                "notificarListenersUI() debe invocar a los observadores registrados");
    }

    @Test
    void testMultiplesListenersUINoSePisan() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        AtomicInteger conteoListener1 = new AtomicInteger(0);
        AtomicInteger conteoListener2 = new AtomicInteger(0);
        AtomicInteger conteoListener3 = new AtomicInteger(0);

        sim.agregarListenerUI(conteoListener1::incrementAndGet);
        sim.agregarListenerUI(conteoListener2::incrementAndGet);
        sim.agregarListenerUI(conteoListener3::incrementAndGet);

        sim.notificarListenersUI();

        assertEquals(1, conteoListener1.get(), "Listener 1 debe recibir notificacion");
        assertEquals(1, conteoListener2.get(), "Listener 2 no debe haber sido pisado");
        assertEquals(1, conteoListener3.get(), "Listener 3 no debe haber sido pisado");

        // Remover listener 2
        sim.removerListenerUI(conteoListener2::incrementAndGet); // referencia distinta no remueve
        sim.removerListenerUI(null); // proteccion contra null
    }

    @Test
    void testBucleJuegoNotificaUIMaximoUnaVezPorFrameConMultiplesTicks() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(1280, 720);
        Canvas canvas = new Canvas(1280, 720);

        BucleJuego bucle = new BucleJuego(sim, renderizador, canvas, null);

        AtomicInteger conteoUI = new AtomicInteger(0);
        sim.agregarListenerUI(conteoUI::incrementAndGet);

        // Simular frame 1 (inicializa timestamp)
        bucle.handle(1_000_000_000L);
        assertEquals(0, conteoUI.get());

        // Simular frame 2 con salto de tiempo equivalente a 8 ticks (0.4s a 20 TPS)
        // 0.4s = 400_000_000 ns
        bucle.handle(1_400_000_000L);

        // Debe haberse ejecutado el lote de ticks en la fisica
        assertTrue(sim.getTickActual() >= 1, "La simulacion debio avanzar ticks en el frame");

        // Regresion: UI debe haberse notificado exactamente 1 vez para este frame
        assertEquals(1, conteoUI.get(),
                "La UI debe actualizarse exactamente 1 vez por frame aunque se hayan ejecutado multiples ticks de fisica");
    }

    @Test
    void testTrayectoriaPredictorCalculaCorrectamenteSinObjetosIntermedios() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        sim.agregarCuerpo(sol);

        TrayectoriaPredictor predictor = new TrayectoriaPredictor();
        Vector2D pos = new Vector2D(150, 0);
        Vector2D vel = new Vector2D(0, 25);

        List<Vector2D> tray = predictor.predecirTrayectoria(pos, vel, 1e24, sim.getSistemaSolar().getCuerpos(), 50, 1.0);

        assertNotNull(tray);
        assertEquals(51, tray.size(), "Debe contener el punto inicial mas 50 pasos futuros");
        assertEquals(150.0, tray.get(0).x(), 1e-6);
        assertEquals(0.0, tray.get(0).y(), 1e-6);
        assertTrue(tray.get(1).y() > 0.0, "La trayectoria debe avanzar en direccion Y con velocidad positiva");
    }
}
