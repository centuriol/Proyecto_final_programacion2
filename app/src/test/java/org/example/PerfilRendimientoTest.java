package org.example;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.example.game.controlador.ControladorMouse;
import org.example.game.controlador.SincronizadorUI;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.Vector2D;
import org.example.game.render.GestorImagenes;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.ControlTiempoWidget;
import org.example.game.ui.HUDRecursosTop;
import org.example.game.ui.HUDSimulacion;
import org.example.game.ui.PanelInventario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PerfilRendimientoTest {

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @Test
    void medirHotspotsAntesDeOptimizar() {
        double w = 1920;
        double h = 1080;
        SimulacionSolar sim = new SimulacionSolar(w, h);
        RenderizadorPixelArt renderer = new RenderizadorPixelArt(w, h);
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Escena estandar: 10 cuerpos celestes (1 sol, 6 planetas con/sin civilizacion, 3 lunas)
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        sim.agregarCuerpo(sol);
        for (int i = 1; i <= 6; i++) {
            Planeta p = CuerpoCelesteFactory.crearPlanetaRocoso(120 + i * 45, 0, 1.0, new Vector2D(0, 20 + i * 4));
            if (i % 2 == 0) p.desarrollarCivilizacion();
            sim.agregarCuerpo(p);
        }
        for (int i = 1; i <= 3; i++) {
            sim.agregarCuerpo(CuerpoCelesteFactory.crearLuna(140 + i * 45, 12, 0.5, new Vector2D(0, 15)));
        }

        int cantidadCuerpos = sim.getSistemaSolar().getCuerpos().size();

        // 1. Medir HUDSimulacion.actualizar()
        HUDSimulacion hud = new HUDSimulacion(sim);
        int itersHud = 1000;
        // Warmup
        for (int i = 0; i < 50; i++) hud.actualizarParaPerfil();
        long tInicioHud = System.nanoTime();
        for (int i = 0; i < itersHud; i++) {
            hud.actualizarParaPerfil();
        }
        long tFinHud = System.nanoTime();
        double msPorLlamadaHud = (tFinHud - tInicioHud) / 1_000_000.0 / itersHud;

        // 2. Medir PanelInventario.actualizarRecursos()
        PanelInventario panelInv = new PanelInventario(sim);
        int itersInv = 1000;
        for (int i = 0; i < 50; i++) panelInv.actualizarRecursosParaPerfil();
        long tInicioInv = System.nanoTime();
        for (int i = 0; i < itersInv; i++) {
            panelInv.actualizarRecursosParaPerfil();
        }
        long tFinInv = System.nanoTime();
        double msPorLlamadaInv = (tFinInv - tInicioInv) / 1_000_000.0 / itersInv;

        // 3. Medir SincronizadorUI.run() (HUDRecursosTop + ControlTiempoWidget)
        HUDRecursosTop hudRecursos = new HUDRecursosTop(sim);
        ControlTiempoWidget ctrlTiempo = new ControlTiempoWidget(sim);
        SincronizadorUI sincUI = new SincronizadorUI(hudRecursos, ctrlTiempo);
        int itersSinc = 2000;
        for (int i = 0; i < 50; i++) sincUI.run();
        long tInicioSinc = System.nanoTime();
        for (int i = 0; i < itersSinc; i++) {
            sincUI.run();
        }
        long tFinSinc = System.nanoTime();
        double msPorLlamadaSinc = (tFinSinc - tInicioSinc) / 1_000_000.0 / itersSinc;

        // 4. Medir TrayectoriaPredictor (en cada movimiento de mouse en COLOCANDO)
        TrayectoriaPredictor predictor = sim.getPredictor();
        Vector2D posPreview = new Vector2D(200, 200);
        Vector2D velPreview = new Vector2D(0, 30);
        int itersPredictor = 1000;
        for (int i = 0; i < 50; i++) predictor.predecirTrayectoria(posPreview, velPreview, 1e24, sim.getSistemaSolar().getCuerpos(), 75, 1.0);
        long tInicioPred = System.nanoTime();
        for (int i = 0; i < itersPredictor; i++) {
            predictor.predecirTrayectoria(posPreview, velPreview, 1e24, sim.getSistemaSolar().getCuerpos(), 75, 1.0);
        }
        long tFinPred = System.nanoTime();
        double msPorLlamadaPred = (tFinPred - tInicioPred) / 1_000_000.0 / itersPredictor;

        // 5. Medir MotorFisicaPermisiva (avanzarPaso)
        MotorFisicaPermisiva motor = new MotorFisicaPermisiva();
        int itersFisica = 2000;
        for (int i = 0; i < 50; i++) motor.avanzarPaso(sim.getSistemaSolar().getCuerpos());
        long tInicioFisica = System.nanoTime();
        for (int i = 0; i < itersFisica; i++) {
            motor.avanzarPaso(sim.getSistemaSolar().getCuerpos());
        }
        long tFinFisica = System.nanoTime();
        double msPorTickFisica = (tFinFisica - tInicioFisica) / 1_000_000.0 / itersFisica;
        double msPor8Ticks = msPorTickFisica * 8.0;

        // 6. Medir RenderizadorPixelArt (renderizarTodo)
        int itersRender = 500;
        for (int i = 0; i < 20; i++) renderer.renderizarTodo(gc, sim.getSistemaSolar().getCuerpos(), sim.getTickActual(), null, null, 1.0, null, null, null);
        long tInicioRender = System.nanoTime();
        for (int i = 0; i < itersRender; i++) {
            renderer.renderizarTodo(gc, sim.getSistemaSolar().getCuerpos(), sim.getTickActual(), null, null, 1.0, null, null, null);
        }
        long tFinRender = System.nanoTime();
        double msPorFrameRender = (tFinRender - tInicioRender) / 1_000_000.0 / itersRender;

        System.out.println("================================================================================");
        System.out.println("=== COMPARATIVA DE RENDIMIENTO Y PERFILADO DE HOTSPOTS (ANTES VS DESPUES) ===");
        System.out.println("Escena de prueba estandar: 1 estrella central, 6 planetas con/sin civ, 3 lunas (" + cantidadCuerpos + " cuerpos)");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("HOTSPOT 1: PanelInventario.actualizarRecursos()");
        System.out.println("  - Antes:   0.2201 ms/llamada | Freq fisica: 80-160/s | Total CPU: 17.60 - 35.21 ms/s");
        System.out.println(String.format("  - Despues: %.4f ms/llamada | Freq frame:  60/s     | Total CPU: %.2f ms/s (Reutilizacion de nodos)",
                msPorLlamadaInv, msPorLlamadaInv * 60));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("HOTSPOT 2: HUDSimulacion.actualizar()");
        System.out.println("  - Antes:   0.0597 ms/llamada | Freq fisica: 80-160/s | Total CPU: 4.78 - 9.56 ms/s");
        System.out.println(String.format("  - Despues: %.4f ms/llamada | Freq frame:  60/s     | Total CPU: %.2f ms/s (1 solo bucle O(n))",
                msPorLlamadaHud, msPorLlamadaHud * 60));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("HOTSPOT 3: TrayectoriaPredictor.predecirTrayectoria()");
        System.out.println("  - Antes:   0.0971 ms/llamada | Freq eventos: 100-200/s| Total CPU: 9.71 - 19.41 ms/s");
        System.out.println(String.format("  - Despues: %.4f ms/llamada | Freq throttled: <=60/s | Total CPU: %.2f ms/s (Primitivos sin allocs)",
                msPorLlamadaPred, msPorLlamadaPred * 60));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("HOTSPOT 4: SincronizadorUI.run() (ControlTiempoWidget + HUDRecursosTop)");
        System.out.println("  - Antes:   0.0193 ms/llamada | Freq fisica: 80-160/s | Total CPU: 1.54 - 3.09 ms/s");
        System.out.println(String.format("  - Despues: %.4f ms/llamada | Freq frame:  60/s     | Total CPU: %.2f ms/s (Estilos CSS cacheados)",
                msPorLlamadaSinc, msPorLlamadaSinc * 60));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("HOTSPOT 5: Fisica por tick (MotorFisicaPermisiva)");
        System.out.println(String.format("  - Costo actual: %.4f ms/tick | Por 8 ticks/frame: %.4f ms (Listas y vectores optimizados)",
                msPorTickFisica, msPor8Ticks));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("HOTSPOT 6: Render por frame (RenderizadorPixelArt)");
        System.out.println(String.format("  - Costo actual: %.4f ms/frame (Presupuesto 60 FPS: 16.66 ms)",
                msPorFrameRender));
        System.out.println("================================================================================");
    }
}
