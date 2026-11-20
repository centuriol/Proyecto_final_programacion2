package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.efectos.GestorEfectos;
import org.example.game.motor.Vector2D;
import org.example.game.render.capa.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * Orquestador principal de renderizado Pixel Art para "Órbita" (SRP / OCP).
 */
public class RenderizadorPixelArt {

    private static final FabricaRender FABRICA = new FabricaRender();

    private static class EstadoLienzo {
        double anchoCanvas;
        double altoCanvas;
        double tiempoAnimacionSegundos = 0.0;
        final CamaraZoom camaraZoom = FABRICA.crearCamaraZoom();
        final RenderizadorFondo renderizadorFondo;

        EstadoLienzo(double ancho, double alto, RenderizadorFondo fondo) {
            this.anchoCanvas = ancho;
            this.altoCanvas = alto;
            this.renderizadorFondo = fondo != null ? fondo : FABRICA.crearRenderizadorFondo(ancho, alto);
            this.renderizadorFondo.setCamaraZoom(this.camaraZoom);
        }

        void redimensionar(double w, double h) {
            if (w > 0 && h > 0 && (w != anchoCanvas || h != altoCanvas)) {
                anchoCanvas = w;
                altoCanvas = h;
                renderizadorFondo.generarEstrellasFondo(w, h);
                renderizadorFondo.normalizarDesplazamiento(w, h);
            }
        }
    }

    private final EstadoLienzo lienzo;
    private final CapasVisualesSimulacion capas;

    public RenderizadorPixelArt(double ancho, double alto,
                                RenderizadorFondo renderizadorFondo,
                                GestorEstelas gestorEstelas,
                                RepositorioSprites repositorioSprites,
                                CapaZonasHabitables capaZonasHabitables,
                                CapaAreaAtraccion capaAreaAtraccion,
                                CapaSeleccion capaSeleccion,
                                CapaTrayectorias capaTrayectorias,
                                CapaGhostPreview capaGhostPreview) {
        this.lienzo = new EstadoLienzo(ancho, alto, renderizadorFondo);
        this.capas = new CapasVisualesSimulacion(
                gestorEstelas, repositorioSprites, capaZonasHabitables, capaAreaAtraccion,
                capaSeleccion, capaTrayectorias, capaGhostPreview
        );
    }

    public RenderizadorPixelArt(double ancho, double alto, RenderizadorFondo renderizadorFondo, GestorEstelas gestorEstelas) {
        this(ancho, alto, renderizadorFondo, gestorEstelas, null, null, null, null, null, null);
    }

    public RenderizadorPixelArt(double ancho, double alto) {
        this(ancho, alto, null, null);
    }

    public void registrarRenderizadorCuerpo(TipoCuerpo tipo, RenderizadorCuerpo renderizador) {
        capas.registrarRenderizadorCuerpo(tipo, renderizador);
    }

    public void setProveedorNaves(Supplier<List<NaveColonizadora>> proveedorNaves) {
        capas.setProveedorNaves(proveedorNaves);
    }

    public void setGestorEfectos(GestorEfectos gestorEfectos) {
        capas.setGestorEfectos(gestorEfectos);
    }

    public void renderizarTodo(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick,
                               Vector2D previewPosFisica, TipoCuerpo previewTipo, double previewFactorMasa,
                               Vector2D dragInicioFisica, List<Vector2D> trayectoriaPreview,
                               CuerpoCeleste cuerpoSeleccionado) {
        renderizarTodo(gc, cuerpos, tick, previewPosFisica, previewTipo, previewFactorMasa,
                null, dragInicioFisica, trayectoriaPreview, cuerpoSeleccionado);
    }

    public void renderizarTodo(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick,
                               Vector2D previewPosFisica, TipoCuerpo previewTipo, double previewFactorMasa,
                               String previewNombre,
                               Vector2D dragInicioFisica, List<Vector2D> trayectoriaPreview,
                               CuerpoCeleste cuerpoSeleccionado) {
        lienzo.renderizadorFondo.dibujarFondo(gc, lienzo.anchoCanvas, lienzo.altoCanvas, tick);
        if (capas.getRepositorioSprites().obtenerImagenFondo() == null) {
            lienzo.renderizadorFondo.dibujarEstrellasFondo(gc, tick);
        }

        boolean vistaTransformada = aplicarTransformacionCamara(gc);
        capas.dibujarCapasInferiores(gc, cuerpos, lienzo.renderizadorFondo, lienzo.camaraZoom, lienzo.anchoCanvas, lienzo.altoCanvas);
        capas.dibujarCuerposYEntidades(gc, cuerpos, tick, lienzo.tiempoAnimacionSegundos, lienzo.anchoCanvas, lienzo.altoCanvas);
        capas.dibujarOverlaysInteraccion(
                gc, cuerpos, cuerpoSeleccionado, trayectoriaPreview, previewPosFisica, previewTipo,
                previewFactorMasa, previewNombre, tick, lienzo.tiempoAnimacionSegundos, lienzo.anchoCanvas, lienzo.altoCanvas
        );
        if (vistaTransformada) {
            gc.restore();
        }
    }

    private boolean aplicarTransformacionCamara(GraphicsContext gc) {
        if (lienzo.camaraZoom.esVistaNeutra()) return false;
        double zoom = lienzo.camaraZoom.getNivelZoom();
        gc.save();
        gc.translate(
                lienzo.camaraZoom.calcularTraslacionX(lienzo.anchoCanvas),
                lienzo.camaraZoom.calcularTraslacionY(lienzo.altoCanvas)
        );
        gc.scale(zoom, zoom);
        return true;
    }

    public void actualizarEstelas(List<CuerpoCeleste> cuerpos) { capas.actualizarEstelas(cuerpos); }
    public void limpiarEstelas() { capas.limpiarEstelas(); }
    public void toggleGrilla() { capas.toggleGrilla(); }
    public void toggleEstelas() { capas.toggleEstelas(); }
    public void toggleZonasHabitables() { capas.toggleZonasHabitables(); }

    public CamaraZoom getCamaraZoom() { return lienzo.camaraZoom; }
    public double getAnchoCanvas() { return lienzo.anchoCanvas; }
    public double getAltoCanvas() { return lienzo.altoCanvas; }

    public void setDimensiones(double w, double h) {
        lienzo.redimensionar(w, h);
    }

    public void avanzarAnimaciones(double deltaTime, double multiplicadorVelocidad) {
        if (deltaTime <= 0 || multiplicadorVelocidad <= 0) return;
        lienzo.tiempoAnimacionSegundos += deltaTime * multiplicadorVelocidad;
        avanzarFondo(deltaTime, multiplicadorVelocidad);
    }

    public void avanzarFondo(double deltaTime, double multiplicadorVelocidad) {
        if (deltaTime <= 0 || multiplicadorVelocidad <= 0) return;
        lienzo.renderizadorFondo.avanzarDesplazamiento(deltaTime, multiplicadorVelocidad, lienzo.anchoCanvas);
    }

    public Image getImagenFondo() { return capas.getRepositorioSprites().obtenerImagenFondo(); }
    public double getDesplazamientoFondoX() { return lienzo.renderizadorFondo.getDesplazamientoFondoX(); }
    public double getDesplazamientoFondoY() { return lienzo.renderizadorFondo.getDesplazamientoFondoY(); }
    public double getFactorParallaxFondo() { return lienzo.renderizadorFondo.getFactorParallax(); }
    public RenderizadorFondo getRenderizadorFondo() { return lienzo.renderizadorFondo; }
    public double getVelocidadBaseFondo() { return lienzo.renderizadorFondo.getVelocidadBaseFondo(); }
    public double getTiempoAnimacionSegundos() { return lienzo.tiempoAnimacionSegundos; }
}
