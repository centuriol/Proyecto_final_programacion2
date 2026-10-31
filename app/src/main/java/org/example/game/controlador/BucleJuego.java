package org.example.game.controlador;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import org.example.game.guardado.TemporizadorAutoguardado;
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
    private final TemporizadorAutoguardado autoguardado;
    private Runnable listenerFrame;

    private long lastTickTime = 0;
    private double tickAcumulado = 0;

    public BucleJuego(SimulacionSolar simulacion,
                      RenderizadorPixelArt renderizador,
                      Canvas canvas,
                      ProveedorEstadoInteraccion proveedorEstado) {
        this(simulacion, renderizador, canvas, proveedorEstado, null);
    }

    public BucleJuego(SimulacionSolar simulacion,
                      RenderizadorPixelArt renderizador,
                      Canvas canvas,
                      ProveedorEstadoInteraccion proveedorEstado,
                      TemporizadorAutoguardado autoguardado) {
        this.simulacion = simulacion;
        this.renderizador = renderizador;
        this.canvas = canvas;
        this.proveedorEstado = proveedorEstado;
        this.autoguardado = autoguardado;
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
        if (autoguardado != null) {
            autoguardado.actualizar(Math.min(deltaTime, 1.0));
        }
        simulacion.getBusEventos().notificarListenersUI();
        sincronizarDimensiones();
        actualizarAnimaciones(deltaTime);
        renderizarFrame();
        if (listenerFrame != null) {
            listenerFrame.run();
        }
    }

    public void setListenerFrame(Runnable listenerFrame) {
        this.listenerFrame = listenerFrame;
    }

    private void actualizarTicksFisicos(double deltaTime) {
        double ticksPorSegundo = 20.0 * simulacion.getReloj().getVelocidadSimulacion();
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
        double multVelocidad = simulacion.getReloj().isEnPausa() ? 0.0 : simulacion.getReloj().getVelocidadSimulacion();
        renderizador.avanzarAnimaciones(Math.min(deltaTime, 0.1), multVelocidad);
    }

    private void renderizarFrame() {
        renderizador.renderizarTodo(
                canvas.getGraphicsContext2D(),
                simulacion.getSistemaSolar().getCuerpos(),
                simulacion.getReloj().getTickActual(),
                simulacion.getGestorColocacion().getPosicionPreview(),
                simulacion.getGestorColocacion().getTipoColocacion(),
                simulacion.getGestorColocacion().getFactorMasaColocacion(),
                simulacion.getGestorColocacion().getNombreColocacion(),
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
