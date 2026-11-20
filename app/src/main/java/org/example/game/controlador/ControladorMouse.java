package org.example.game.controlador;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.motor.Vector2D;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.ControladorColocacion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;

import java.util.List;
import java.util.function.Consumer;

/**
 * Encapsula la gestión de eventos de mouse y la interacción sobre el canvas de simulación (SRP / ISP / DIP).
 */
public class ControladorMouse implements ProveedorEstadoInteraccion {

    private static final FabricaControlador FABRICA = new FabricaControlador();

    private static class EstadoMouse {
        final SimulacionSolar simulacion;
        final BarraInventarioHotbar hotbar;
        final SeleccionadorCuerpo seleccionador;
        final PredictorPreview predictorPreview;
        Consumer<CuerpoCeleste> onInspeccionarCuerpo;
        double anchoMundo;
        double altoMundo;
        CamaraZoom camaraZoom = FABRICA.crearCamaraZoom();
        CuerpoCeleste cuerpoSeleccionado = null;
        Planeta planetaOrigenColonizacion = null;

        EstadoMouse(SimulacionSolar sim, BarraInventarioHotbar hotbar, Consumer<CuerpoCeleste> onInsp,
                    double ancho, double alto, SeleccionadorCuerpo sel, PredictorPreview pred) {
            this.simulacion = sim;
            this.hotbar = hotbar;
            this.onInspeccionarCuerpo = onInsp;
            this.anchoMundo = ancho;
            this.altoMundo = alto;
            this.seleccionador = sel != null ? sel : FABRICA.crearSeleccionadorCuerpo();
            this.predictorPreview = pred != null ? pred : FABRICA.crearPredictorPreview();
        }

        double aMundoX(double mouseX) { return camaraZoom != null ? camaraZoom.pantallaAMundoX(mouseX, anchoMundo) : mouseX; }
        double aMundoY(double mouseY) { return camaraZoom != null ? camaraZoom.pantallaAMundoY(mouseY, altoMundo) : mouseY; }

        void actualizarPreview(double x, double y) {
            if (simulacion.getGestorColocacion().getModoColocacion() == ControladorColocacion.ModoColocacion.COLOCANDO) {
                simulacion.getGestorColocacion().actualizarPosicionPreview(x, y, anchoMundo, altoMundo);
                predictorPreview.actualizarConThrottle(x, y, simulacion);
            }
        }
    }

    private final EstadoMouse estado;
    private final ManejadorClicksCanvas manejadorClicks = new ManejadorClicksCanvas();

    public ControladorMouse(SimulacionSolar simulacion, BarraInventarioHotbar hotbar,
                            Consumer<CuerpoCeleste> onInspeccionarCuerpo, double anchoMundo, double altoMundo,
                            SeleccionadorCuerpo seleccionador, PredictorPreview predictorPreview) {
        this.estado = new EstadoMouse(simulacion, hotbar, onInspeccionarCuerpo, anchoMundo, altoMundo, seleccionador, predictorPreview);
    }

    public ControladorMouse(SimulacionSolar simulacion, BarraInventarioHotbar hotbar,
                            Consumer<CuerpoCeleste> onInspeccionarCuerpo, double anchoMundo, double altoMundo) {
        this(simulacion, hotbar, onInspeccionarCuerpo, anchoMundo, altoMundo, null, null);
    }

    public ControladorMouse(SimulacionSolar simulacion, BarraInventarioHotbar hotbar, double anchoMundo, double altoMundo) {
        this(simulacion, hotbar, (Consumer<CuerpoCeleste>) null, anchoMundo, altoMundo);
    }

    public void setOnInspeccionarCuerpo(Consumer<CuerpoCeleste> cb) { estado.onInspeccionarCuerpo = cb; }

    public void conectar(Canvas canvas) {
        if (canvas == null) return;
        canvas.setOnMouseMoved(this::manejarMouseMovido);
        canvas.setOnMousePressed(this::manejarMousePresionado);
        canvas.setOnMouseDragged(this::manejarMouseArrastrado);
        canvas.setOnMouseReleased(this::manejarMouseLiberado);
        canvas.setOnScroll(this::manejarScroll);
    }

    public void desconectar(Canvas canvas) {
        if (canvas == null) return;
        canvas.setOnMouseMoved(null);
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
        canvas.setOnScroll(null);
    }

    public double transformarX(double mouseX) { return estado.aMundoX(mouseX); }
    public double transformarY(double mouseY) { return estado.aMundoY(mouseY); }
    public void manejarMouseMovido(MouseEvent e) { estado.actualizarPreview(transformarX(e.getX()), transformarY(e.getY())); }

    public void iniciarModoColonizacion(Planeta origen) {
        estado.planetaOrigenColonizacion = origen;
        if (estado.simulacion != null) {
            estado.simulacion.notificarEvento("Modo Colonización: Haz click en un planeta destino. [ESC] o click derecho para cancelar.", MensajeEvento.TipoMensaje.INFO);
        }
    }

    public boolean isModoColonizacion() { return estado.planetaOrigenColonizacion != null; }
    public void cancelarModoColonizacion() { estado.planetaOrigenColonizacion = null; }

    public void manejarMousePresionado(MouseEvent e) {
        if (e == null) return;
        manejadorClicks.procesarPresionado(e, transformarX(e.getX()), transformarY(e.getY()), this,
                estado.simulacion, estado.hotbar, estado.planetaOrigenColonizacion, estado.onInspeccionarCuerpo);
    }

    public void manejarMouseArrastrado(MouseEvent e) {
        if (e == null) return;
        boolean bloqueado = estado.simulacion.getGestorColocacion().getModoColocacion() == ControladorColocacion.ModoColocacion.COLOCANDO || isModoColonizacion();
        manejadorClicks.procesarArrastre(e, bloqueado,
                () -> estado.actualizarPreview(transformarX(e.getX()), transformarY(e.getY())), estado.camaraZoom);
    }

    public void manejarMouseLiberado(MouseEvent e) {
        if (e == null) return;
        manejadorClicks.procesarLiberado(e, transformarX(e.getX()), transformarY(e.getY()), this, estado.onInspeccionarCuerpo);
    }

    public void manejarScroll(ScrollEvent e) {
        if (e == null) return;
        if (estado.camaraZoom != null) {
            estado.camaraZoom.aplicarRueda(e.getDeltaY());
            estado.actualizarPreview(transformarX(e.getX()), transformarY(e.getY()));
        }
        e.consume();
    }

    public void actualizarPrediccionTrayectoriaAuto() { estado.predictorPreview.actualizarPrediccion(estado.simulacion); }

    public void seleccionarCuerpoBajoCursor(double mouseX, double mouseY) {
        estado.cuerpoSeleccionado = estado.seleccionador.encontrarCuerpo(
                mouseX, mouseY, estado.anchoMundo, estado.altoMundo, estado.simulacion.getSistemaSolar().getCuerpos());
    }

    public void limpiarSeleccion() {
        estado.cuerpoSeleccionado = null;
        estado.predictorPreview.limpiar();
    }

    @Override public List<Vector2D> getTrayectoriaPreview() { return estado.predictorPreview.getTrayectoriaPreview(); }
    @Override public CuerpoCeleste getCuerpoSeleccionado() { return estado.cuerpoSeleccionado; }
    public void setCuerpoSeleccionado(CuerpoCeleste c) { estado.cuerpoSeleccionado = (c != null && c.esInteractuablePorUsuario()) ? c : null; }

    public void setDimensiones(double ancho, double alto) {
        if (ancho > 0 && alto > 0) {
            estado.anchoMundo = ancho;
            estado.altoMundo = alto;
        }
    }

    public CamaraZoom getCamaraZoom() { return estado.camaraZoom; }
    public void setCamaraZoom(CamaraZoom camaraZoom) { estado.camaraZoom = camaraZoom; }
}
