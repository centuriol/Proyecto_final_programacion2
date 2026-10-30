package org.example.game.controlador;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Bucle principal de juego (Game Loop).
 *
 * Principios SOLID:
 * - SRP: Temporización, ejecución de ticks y disparo de renderizado estructurado en métodos breves y legibles.
 * - DIP: Recibe dependencias desacopladas (SimulacionSolar, RenderizadorPixelArt, Canvas, ProveedorEstadoInteraccion).
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

        actualizarTicksFisicos(deltaTime);
        simulacion.notificarListenersUI();
        sincronizarDimensiones();
        actualizarAnimaciones(deltaTime);
        renderizarFrame();
    }

    private void actualizarTicksFisicos(double deltaTime) {
        double ticksPorSegundo = 20.0 * simulacion.getVelocidadSimulacion();
        double tiempoPorTick = 1.0 / ticksPorSegundo;

        tickAcumulado += deltaTime;
        int ticksAEjecutar = (int) (tickAcumulado / tiempoPorTick);

        if (ticksAEjecutar > 0) {
            tickAcumulado -= ticksAEjecutar * tiempoPorTick;
            ticksAEjecutar = Math.min(ticksAEjecutar, 8);
            for (int i = 0; i < ticksAEjecutar; i++) {
                simulacion.avanzarTick();
                renderizador.actualizarEstelas(simulacion.getSistemaSolar().getCuerpos());
            }
        }
    }

    private void sincronizarDimensiones() {
        double anchoActual = canvas.getWidth();
        double altoActual = canvas.getHeight();
        if (anchoActual > 0 && altoActual > 0 &&
                (anchoActual != renderizador.getAnchoCanvas() || altoActual != renderizador.getAltoCanvas())) {
            renderizador.setDimensiones(anchoActual, altoActual);
            simulacion.setDimensionesMundo(anchoActual, altoActual);
            if (proveedorEstado != null) {
                proveedorEstado.setDimensiones(anchoActual, altoActual);
            }
        }
    }

    private void actualizarAnimaciones(double deltaTime) {
        double multVelocidad = simulacion.isEnPausa() ? 0.0 : simulacion.getVelocidadSimulacion();
        renderizador.avanzarAnimaciones(Math.min(deltaTime, 0.1), multVelocidad);
    }

    private void renderizarFrame() {
        renderizador.renderizarTodo(
                canvas.getGraphicsContext2D(),
                simulacion.getSistemaSolar().getCuerpos(),
                simulacion.getTickActual(),
                simulacion.getPosicionPreview(),
                simulacion.getTipoColocacion(),
                simulacion.getFactorMasaColocacion(),
                simulacion.getNombreColocacion(),
                null,
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
