package org.example.game.controlador;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Bucle principal de juego (Game Loop).
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivamente de la temporización (fixed-timestep a 20 TPS base con multiplicador),
 *   ejecución de ticks físicos y disparo del renderizado en cada cuadro de animación.
 * - DIP: Recibe dependencias desacopladas (SimulacionSolar, RenderizadorPixelArt, Canvas, ProveedorEstadoInteraccion)
 *   en lugar de crearlas o acoplarlas rígidamente.
 */
public class BucleJuego extends AnimationTimer {

    private final SimulacionSolar simulacion;
    private final RenderizadorPixelArt renderizador;
    private final Canvas canvas;
    private final ProveedorEstadoInteraccion proveedorEstado;

    private long lastTickTime = 0;
    private double tickAcumulado = 0;

    public BucleJuego(SimulacionSolar simulacion,
                      RenderizadorPixelArt renderizador,
                      Canvas canvas,
                      ProveedorEstadoInteraccion proveedorEstado) {
        this.simulacion = simulacion;
        this.renderizador = renderizador;
        this.canvas = canvas;
        this.proveedorEstado = proveedorEstado;
    }

    @Override
    public void handle(long now) {
        if (lastTickTime == 0) {
            lastTickTime = now;
            return;
        }

        double deltaTime = (now - lastTickTime) / 1_000_000_000.0;
        lastTickTime = now;

        // Ticks fijos a 20 TPS base * multiplicador de velocidad
        double ticksPorSegundo = 20.0 * simulacion.getVelocidadSimulacion();
        double tiempoPorTick = 1.0 / ticksPorSegundo;

        tickAcumulado += deltaTime;
        int ticksAEjecutar = (int) (tickAcumulado / tiempoPorTick);

        if (ticksAEjecutar > 0) {
            tickAcumulado -= ticksAEjecutar * tiempoPorTick;
            ticksAEjecutar = Math.min(ticksAEjecutar, 8); // Evitar espiral de retraso
            for (int i = 0; i < ticksAEjecutar; i++) {
                simulacion.avanzarTick();
            }
        }

        // Renderizar frame completo en Pixel Art
        renderizador.renderizarTodo(
                canvas.getGraphicsContext2D(),
                simulacion.getSistemaSolar().getCuerpos(),
                simulacion.getTickActual(),
                simulacion.getPosicionPreview(),
                simulacion.getTipoColocacion(),
                simulacion.getFactorMasaColocacion(),
                simulacion.getNombreColocacion(),
                null, // Sin vector de impulso/lanzamiento
                proveedorEstado != null ? proveedorEstado.getTrayectoriaPreview() : java.util.Collections.emptyList(),
                proveedorEstado != null ? proveedorEstado.getCuerpoSeleccionado() : null
        );
    }

    public void iniciar() {
        start();
    }

    public void detener() {
        stop();
    }
}
