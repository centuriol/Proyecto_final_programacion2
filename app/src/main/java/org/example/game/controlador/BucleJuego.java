package org.example.game.controlador;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import org.example.game.guardado.TemporizadorAutoguardado;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Collections;

/**
 * Bucle principal de juego (Game Loop) (SRP / DIP).
 */
public class BucleJuego extends AnimationTimer {

    private static class ContextoFrame {
        final SimulacionSolar simulacion;
        final RenderizadorPixelArt renderizador;
        final Canvas canvas;
        final ProveedorEstadoInteraccion proveedorEstado;
        final TemporizadorAutoguardado autoguardado;
        Runnable listenerFrame;

        ContextoFrame(SimulacionSolar sim, RenderizadorPixelArt rend, Canvas canvas,
                      ProveedorEstadoInteraccion prov, TemporizadorAutoguardado auto) {
            this.simulacion = sim;
            this.renderizador = rend;
            this.canvas = canvas;
            this.proveedorEstado = prov;
            this.autoguardado = auto;
        }

        void sincronizarDimensiones() {
            double w = canvas.getWidth();
            double h = canvas.getHeight();
            if (w > 0 && h > 0 && (w != renderizador.getAnchoCanvas() || h != renderizador.getAltoCanvas())) {
                renderizador.setDimensiones(w, h);
                simulacion.setDimensionesMundo(w, h);
                if (proveedorEstado != null) proveedorEstado.setDimensiones(w, h);
            }
        }

        void renderizar() {
            renderizador.renderizarTodo(
                    canvas.getGraphicsContext2D(),
                    simulacion.getSistemaSolar().getCuerpos(),
                    simulacion.getReloj().getTickActual(),
                    simulacion.getGestorColocacion().getPosicionPreview(),
                    simulacion.getGestorColocacion().getTipoColocacion(),
                    simulacion.getGestorColocacion().getFactorMasaColocacion(),
                    simulacion.getGestorColocacion().getNombreColocacion(),
                    null,
                    proveedorEstado != null ? proveedorEstado.getTrayectoriaPreview() : Collections.emptyList(),
                    proveedorEstado != null ? proveedorEstado.getCuerpoSeleccionado() : null
            );
        }
    }

    private static class AcumuladorTiempo {
        long lastTickTime = 0;
        double tickAcumulado = 0;

        double calcularDelta(long now) {
            if (lastTickTime == 0) {
                lastTickTime = now;
                return -1.0;
            }
            double dt = (now - lastTickTime) / 1_000_000_000.0;
            lastTickTime = now;
            return dt;
        }

        int consumirTicks(double deltaTime, double velocidadSimulacion) {
            double tiempoPorTick = 1.0 / (20.0 * velocidadSimulacion);
            tickAcumulado += deltaTime;
            int ticks = (int) (tickAcumulado / tiempoPorTick);
            if (ticks > 0) {
                tickAcumulado -= ticks * tiempoPorTick;
                return Math.min(ticks, 8);
            }
            return 0;
        }
    }

    private final ContextoFrame ctx;
    private final AcumuladorTiempo tiempo = new AcumuladorTiempo();

    public BucleJuego(SimulacionSolar simulacion, RenderizadorPixelArt renderizador,
                      Canvas canvas, ProveedorEstadoInteraccion proveedorEstado) {
        this(simulacion, renderizador, canvas, proveedorEstado, null);
    }

    public BucleJuego(SimulacionSolar simulacion, RenderizadorPixelArt renderizador,
                      Canvas canvas, ProveedorEstadoInteraccion proveedorEstado,
                      TemporizadorAutoguardado autoguardado) {
        this.ctx = new ContextoFrame(simulacion, renderizador, canvas, proveedorEstado, autoguardado);
    }

    @Override
    public void handle(long now) {
        double deltaTime = tiempo.calcularDelta(now);
        if (deltaTime < 0) return;

        int ticks = tiempo.consumirTicks(deltaTime, ctx.simulacion.getReloj().getVelocidadSimulacion());
        for (int i = 0; i < ticks; i++) {
            ctx.simulacion.avanzarTick();
            ctx.renderizador.actualizarEstelas(ctx.simulacion.getSistemaSolar().getCuerpos());
        }
        if (ctx.autoguardado != null) ctx.autoguardado.actualizar(Math.min(deltaTime, 1.0));
        ctx.simulacion.getBusEventos().notificarListenersUI();
        ctx.sincronizarDimensiones();

        double mult = ctx.simulacion.getReloj().isEnPausa() ? 0.0 : ctx.simulacion.getReloj().getVelocidadSimulacion();
        ctx.renderizador.avanzarAnimaciones(Math.min(deltaTime, 0.1), mult);
        ctx.renderizar();
        if (ctx.listenerFrame != null) ctx.listenerFrame.run();
    }

    public void setListenerFrame(Runnable listenerFrame) { ctx.listenerFrame = listenerFrame; }
    public void iniciar() { start(); }
    public void detener() { stop(); }
}
