package org.example.game.controlador;
import javafx.scene.canvas.Canvas; import javafx.scene.input.MouseButton; import javafx.scene.input.MouseEvent; import javafx.scene.input.ScrollEvent; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.Planeta; import org.example.game.motor.Vector2D; import org.example.game.render.CamaraZoom; import org.example.game.simulacion.MensajeEvento; import org.example.game.simulacion.SimulacionSolar; import org.example.game.ui.BarraInventarioHotbar; import java.util.List; import java.util.function.Consumer;
/**
 * Encapsula la gestión de eventos de mouse y la interacción sobre el canvas de simulación.
 *
 * Principios SOLID:
 * - SRP: Coordina los eventos de entrada de ratón delegando en componentes especializados (seleccionador, predictor, cámara).
 * - ISP + DIP: Implementa ProveedorEstadoInteraccion; desacoplado de modales concretos.
 */
public class ControladorMouse implements ProveedorEstadoInteraccion {

    private final SimulacionSolar simulacion;
    private final BarraInventarioHotbar hotbar;
    private final SeleccionadorCuerpoMouse seleccionador;
    private final PredictorPreviewMouse predictorPreview;
    private final ControlArrastreCamara controlArrastre = new ControlArrastreCamara();
    private Consumer<CuerpoCeleste> onInspeccionarCuerpo;
    private double anchoMundo;
    private double altoMundo;
    private CamaraZoom camaraZoom = new CamaraZoom();

    private CuerpoCeleste cuerpoSeleccionado = null;
    private Planeta planetaOrigenColonizacion = null;

    public ControladorMouse(SimulacionSolar simulacion,
                            BarraInventarioHotbar hotbar,
                            Consumer<CuerpoCeleste> onInspeccionarCuerpo,
                            double anchoMundo,
                            double altoMundo,
                            SeleccionadorCuerpoMouse seleccionador,
                            PredictorPreviewMouse predictorPreview) {
        this.simulacion = simulacion;
        this.hotbar = hotbar;
        this.onInspeccionarCuerpo = onInspeccionarCuerpo;
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
        this.seleccionador = seleccionador != null ? seleccionador : new SeleccionadorCuerpoMouse();
        this.predictorPreview = predictorPreview != null ? predictorPreview : new PredictorPreviewMouse();
    }

    public ControladorMouse(SimulacionSolar simulacion,
                            BarraInventarioHotbar hotbar,
                            Consumer<CuerpoCeleste> onInspeccionarCuerpo,
                            double anchoMundo,
                            double altoMundo) {
        this(simulacion, hotbar, onInspeccionarCuerpo, anchoMundo, altoMundo, null, null);
    }

    public ControladorMouse(SimulacionSolar simulacion,
                            BarraInventarioHotbar hotbar,
                            double anchoMundo,
                            double altoMundo) {
        this(simulacion, hotbar, (Consumer<CuerpoCeleste>) null, anchoMundo, altoMundo);
    }

    public void setOnInspeccionarCuerpo(Consumer<CuerpoCeleste> onInspeccionarCuerpo) {
        this.onInspeccionarCuerpo = onInspeccionarCuerpo;
    }

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

    public double transformarX(double mouseX) {
        return camaraZoom != null ? camaraZoom.pantallaAMundoX(mouseX, anchoMundo) : mouseX;
    }

    public double transformarY(double mouseY) {
        return camaraZoom != null ? camaraZoom.pantallaAMundoY(mouseY, altoMundo) : mouseY;
    }

    public void manejarMouseMovido(MouseEvent e) {
        actualizarPreviewColocacion(transformarX(e.getX()), transformarY(e.getY()));
    }

    public void iniciarModoColonizacion(Planeta origen) {
        this.planetaOrigenColonizacion = origen;
        if (simulacion != null) {
            simulacion.notificarEvento("Modo Colonización: Haz click en un planeta destino. [ESC] o click derecho para cancelar.", MensajeEvento.TipoMensaje.INFO);
        }
    }

    public boolean isModoColonizacion() {
        return planetaOrigenColonizacion != null;
    }

    public void cancelarModoColonizacion() {
        this.planetaOrigenColonizacion = null;
    }

    public void manejarMousePresionado(MouseEvent e) {
        if (e == null) return;
        double worldX = transformarX(e.getX());
        double worldY = transformarY(e.getY());

        if (planetaOrigenColonizacion != null) {
            controlArrastre.cancelar();
            procesarClickColonizacion(e, worldX, worldY);
            return;
        }

        if (e.getButton() == MouseButton.PRIMARY) {
            procesarClickIzquierdo(e.getX(), e.getY(), worldX, worldY);
        } else if (e.getButton() == MouseButton.SECONDARY) {
            controlArrastre.cancelar();
            procesarClickDerecho(worldX, worldY);
        }
    }

    private void procesarClickColonizacion(MouseEvent e, double worldX, double worldY) {
        if (e.getButton() == MouseButton.PRIMARY) {
            seleccionarCuerpoBajoCursor(worldX, worldY);
            if (cuerpoSeleccionado != null) {
                simulacion.getGestorColonizacion().lanzar(planetaOrigenColonizacion, cuerpoSeleccionado, simulacion.getInventario(), simulacion::notificarEvento, simulacion.getTickActual());
                cancelarModoColonizacion();
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {
            cancelarModoColonizacion();
            simulacion.notificarEvento("Misión de colonización cancelada.", MensajeEvento.TipoMensaje.INFO);
        }
    }

    private void procesarClickIzquierdo(double screenX, double screenY, double worldX, double worldY) {
        if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
            controlArrastre.cancelar();
            simulacion.actualizarPosicionPreview(worldX, worldY);
            simulacion.confirmarColocacionAutoOrbita();
            limpiarSeleccion();
            if (hotbar != null) {
                hotbar.deseleccionar();
            }
        } else {
            controlArrastre.iniciar(screenX, screenY);
            seleccionarCuerpoBajoCursor(worldX, worldY);
        }
    }

    private void procesarClickDerecho(double worldX, double worldY) {
        if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
            simulacion.salirModoColocacion();
            if (hotbar != null) {
                hotbar.deseleccionar();
            }
            limpiarSeleccion();
        } else {
            inspeccionarCuerpoBajoCursor(worldX, worldY);
        }
    }

    public void manejarMouseArrastrado(MouseEvent e) {
        if (e == null) return;
        if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO || isModoColonizacion()) {
            controlArrastre.cancelar();
            actualizarPreviewColocacion(transformarX(e.getX()), transformarY(e.getY()));
            return;
        }
        if (e.getButton() != MouseButton.SECONDARY && controlArrastre.estaActivo()) {
            controlArrastre.arrastrar(e.getX(), e.getY(), camaraZoom);
        }
    }

    private void actualizarPreviewColocacion(double x, double y) {
        if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
            simulacion.actualizarPosicionPreview(x, y);
            predictorPreview.actualizarConThrottle(x, y, simulacion);
        }
    }

    private void inspeccionarCuerpoBajoCursor(double x, double y) {
        seleccionarCuerpoBajoCursor(x, y);
        if (cuerpoSeleccionado != null && cuerpoSeleccionado.esInteractuablePorUsuario() && onInspeccionarCuerpo != null) {
            onInspeccionarCuerpo.accept(cuerpoSeleccionado);
        }
    }

    public void manejarMouseLiberado(MouseEvent e) {
        if (e == null) return;
        if (e.getButton() == MouseButton.PRIMARY && controlArrastre.finalizarYEsClickSimple()) {
            inspeccionarCuerpoBajoCursor(transformarX(e.getX()), transformarY(e.getY()));
        }
    }

    public void manejarScroll(ScrollEvent e) {
        if (e == null) return;
        if (camaraZoom != null) {
            camaraZoom.aplicarRueda(e.getDeltaY());
            actualizarPreviewColocacion(transformarX(e.getX()), transformarY(e.getY()));
        }
        e.consume();
    }

    public void actualizarPrediccionTrayectoriaAuto() {
        predictorPreview.actualizarPrediccion(simulacion);
    }

    public void seleccionarCuerpoBajoCursor(double mouseX, double mouseY) {
        this.cuerpoSeleccionado = seleccionador.encontrarCuerpo(
                mouseX, mouseY, anchoMundo, altoMundo, simulacion.getSistemaSolar().getCuerpos()
        );
    }

    public void limpiarSeleccion() {
        this.cuerpoSeleccionado = null;
        this.predictorPreview.limpiar();
    }

    @Override
    public List<Vector2D> getTrayectoriaPreview() {
        return predictorPreview.getTrayectoriaPreview();
    }

    @Override
    public CuerpoCeleste getCuerpoSeleccionado() {
        return cuerpoSeleccionado;
    }

    public void setCuerpoSeleccionado(CuerpoCeleste cuerpo) {
        this.cuerpoSeleccionado = (cuerpo != null && cuerpo.esInteractuablePorUsuario()) ? cuerpo : null;
    }

    public void setDimensiones(double ancho, double alto) {
        if (ancho > 0 && alto > 0) {
            this.anchoMundo = ancho;
            this.altoMundo = alto;
        }
    }

    public CamaraZoom getCamaraZoom() {
        return camaraZoom;
    }

    public void setCamaraZoom(CamaraZoom camaraZoom) {
        this.camaraZoom = camaraZoom;
    }
}
