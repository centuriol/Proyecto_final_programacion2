package org.example;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.example.game.render.GestorImagenes;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MedicionRendimientoTest {

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @Test
    void medirRendimientoBase() {
        double w = 1920;
        double h = 1080;
        SimulacionSolar sim = new SimulacionSolar(w, h);
        RenderizadorPixelArt renderer = new RenderizadorPixelArt(w, h);
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Configurar 10 cuerpos celestes (estrella, varios planetas, lunas, satelites)
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        sim.agregarCuerpo(sol);
        for (int i = 1; i <= 6; i++) {
            Planeta p = CuerpoCelesteFactory.crearPlanetaRocoso(100 + i * 40, 0, 1.0, new Vector2D(0, 20 + i * 5));
            sim.agregarCuerpo(p);
        }
        for (int i = 1; i <= 3; i++) {
            sim.agregarCuerpo(CuerpoCelesteFactory.crearLuna(120 + i * 40, 10, 0.5, new Vector2D(0, 15)));
        }

        int cantidadCuerpos = sim.getSistemaSolar().getCuerpos().size();

        // Warmup
        for (int i = 0; i < 20; i++) {
            sim.avanzarTick();
            renderer.renderizarTodo(gc, sim.getSistemaSolar().getCuerpos(), sim.getTickActual(),
                    null, null, 1.0, null, null, null);
        }

        // 1. Medir tiempo de ticks de fisica/simulacion
        int iteracionesTicks = 1000;
        long tInicioTicks = System.nanoTime();
        for (int i = 0; i < iteracionesTicks; i++) {
            sim.avanzarTick();
        }
        long tFinTicks = System.nanoTime();
        double msPorTick = (tFinTicks - tInicioTicks) / 1_000_000.0 / iteracionesTicks;
        double msPor8Ticks = msPorTick * 8.0;

        // 2. Medir tiempo de render por frame
        int iteracionesRender = 500;
        long tInicioRender = System.nanoTime();
        for (int i = 0; i < iteracionesRender; i++) {
            renderer.renderizarTodo(gc, sim.getSistemaSolar().getCuerpos(), sim.getTickActual(),
                    null, null, 1.0, null, null, null);
        }
        long tFinRender = System.nanoTime();
        double msPorFrameRender = (tFinRender - tInicioRender) / 1_000_000.0 / iteracionesRender;

        // 3a. Medir busquedas fallidas a disco sin cache (miss forzado con limpiarCache)
        int iteracionesSinCache = 200;
        long tInicioSinCache = System.nanoTime();
        for (int i = 0; i < iteracionesSinCache; i++) {
            GestorImagenes.limpiarCache();
            GestorImagenes.getImagenPorNombre("fondo_inexistente_bench");
            GestorImagenes.getImagenPorNombre("sprite_inexistente_bench");
        }
        long tFinSinCache = System.nanoTime();
        double msSinCacheGestor = (tFinSinCache - tInicioSinCache) / 1_000_000.0 / iteracionesSinCache;

        // 3b. Medir busquedas fallidas con cache de recursos fallidos activa
        int iteracionesConCache = 20000;
        long tInicioConCache = System.nanoTime();
        for (int i = 0; i < iteracionesConCache; i++) {
            GestorImagenes.getImagenPorNombre("fondo_inexistente_bench");
            GestorImagenes.getImagenPorNombre("sprite_inexistente_bench");
        }
        long tFinConCache = System.nanoTime();
        double msConCacheGestor = (tFinConCache - tInicioConCache) / 1_000_000.0 / iteracionesConCache;

        // 4a. Medir Color.web directo (sin cache)
        int iteracionesColor = 20000;
        long tInicioColorDirecto = System.nanoTime();
        for (int i = 0; i < iteracionesColor; i++) {
            javafx.scene.paint.Color.web("#0e1017");
            javafx.scene.paint.Color.web("#2b2d3a");
            javafx.scene.paint.Color.web("#5be3ff");
            javafx.scene.paint.Color.web("#e8e4d8");
            javafx.scene.paint.Color.web("#4169E1");
        }
        long tFinColorDirecto = System.nanoTime();
        double msColorDirectoPor5k = (tFinColorDirecto - tInicioColorDirecto) / 1_000_000.0 / (iteracionesColor / 1000.0);

        // 4b. Medir Color.web optimizado (con cache en RenderizadorPixelArt)
        long tInicioColorCache = System.nanoTime();
        for (int i = 0; i < iteracionesColor; i++) {
            RenderizadorPixelArt.colorWeb("#0e1017");
            RenderizadorPixelArt.colorWeb("#2b2d3a");
            RenderizadorPixelArt.colorWeb("#5be3ff");
            RenderizadorPixelArt.colorWeb("#e8e4d8");
            RenderizadorPixelArt.colorWeb("#4169E1");
        }
        long tFinColorCache = System.nanoTime();
        double msColorCachePor5k = (tFinColorCache - tInicioColorCache) / 1_000_000.0 / (iteracionesColor / 1000.0);

        System.out.println("=== MEDICION RENDIMIENTO (ANTES VS DESPUES) ===");
        System.out.println("Cantidad de cuerpos en simulacion: " + cantidadCuerpos);
        System.out.println("Fisica por tick: " + String.format("%.4f", msPorTick) + " ms");
        System.out.println("Fisica por 8 ticks (peor caso/frame en BucleJuego): " + String.format("%.4f", msPor8Ticks) + " ms");
        System.out.println("Render por frame: " + String.format("%.4f", msPorFrameRender) + " ms");
        System.out.println("GestorImagenes busqueda fallida ANTES (disco/classpath): " + String.format("%.4f", msSinCacheGestor) + " ms");
        System.out.println("GestorImagenes busqueda fallida DESPUES (cache negativo): " + String.format("%.6f", msConCacheGestor) + " ms");
        System.out.println("Color.web 5000 operaciones ANTES (parseo directo): " + String.format("%.4f", msColorDirectoPor5k) + " ms");
        System.out.println("Color.web 5000 operaciones DESPUES (cache Map): " + String.format("%.4f", msColorCachePor5k) + " ms");
        System.out.println("================================================");
    }
}
