package org.example.game.controlador;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.example.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Encapsula la gestión de eventos de mouse y la interacción sobre el canvas de simulación.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de procesar entradas de ratón (movimiento, clicks, arrastres y scroll)
 *   y coordinar la selección de cuerpos y predicción de trayectorias.
 * - ISP + DIP: Implementa ProveedorEstadoInteraccion para ofrecer sólo el estado visual que requiere el render.
 *   Se desacopla completamente de Stage y de modales concretos mediante inyeccion de callback Consumer<CuerpoCeleste>.
 */
public class ControladorMouse implements ProveedorEstadoInteraccion {

    private final SimulacionSolar simulacion;
    private final BarraInventarioHotbar hotbar;
    private Consumer<CuerpoCeleste> onInspeccionarCuerpo;
    private double anchoMundo;
    private double altoMundo;
    private CamaraZoom camaraZoom;

    private List<Vector2D> trayectoriaPreview = new ArrayList<>();
    private CuerpoCeleste cuerpoSeleccionado = null;
    private double ultimoPreviewMouseX = Double.NaN;
    private double ultimoPreviewMouseY = Double.NaN;
    private long ultimoTiempoPrediccionMs = 0;

    public ControladorMouse(SimulacionSolar simulacion,
                            BarraInventarioHotbar hotbar,
                            Consumer<CuerpoCeleste> onInspeccionarCuerpo,
                            double anchoMundo,
                            double altoMundo) {
        this.simulacion = simulacion;
        this.hotbar = hotbar;
        this.onInspeccionarCuerpo = onInspeccionarCuerpo;
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
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

    /**
     * Vincula los listeners de eventos de ratón al Canvas especificado.
     */
    public void conectar(Canvas canvas) {
        if (canvas == null) return;
        canvas.setOnMouseMoved(this::manejarMouseMovido);
        canvas.setOnMousePressed(this::manejarMousePresionado);
        canvas.setOnMouseDragged(this::manejarMouseArrastrado);
        canvas.setOnMouseReleased(this::manejarMouseLiberado);
        canvas.setOnScroll(this::manejarScroll);
    }

    /**
     * Desvincula los listeners de eventos de ratón del Canvas.
     */
    public void desconectar(Canvas canvas) {
        if (canvas == null) return;
        canvas.setOnMouseMoved(null);
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
        canvas.setOnScroll(null);
    }

    public void manejarMouseMovido(MouseEvent e) {
        actualizarPreviewColocacion(e.getX(), e.getY());
    }

    private org.example.Planeta planetaOrigenColonizacion = null;

    public void iniciarModoColonizacion(org.example.Planeta origen) {
        this.planetaOrigenColonizacion = origen;
        if (simulacion != null) {
            simulacion.notificarEvento("Modo Colonización: Haz click en un planeta destino. [ESC] o click derecho para cancelar.", org.example.game.simulacion.MensajeEvento.TipoMensaje.INFO);
        }
    }

    public boolean isModoColonizacion() {
        return planetaOrigenColonizacion != null;
    }

    public void cancelarModoColonizacion() {
        this.planetaOrigenColonizacion = null;
    }

    public void manejarMousePresionado(MouseEvent e) {
        if (planetaOrigenColonizacion != null) {
            if (e.getButton() == MouseButton.PRIMARY) {
                seleccionarCuerpoBajoCursor(e.getX(), e.getY());
                if (cuerpoSeleccionado != null) {
                    simulacion.getGestorColonizacion().lanzar(planetaOrigenColonizacion, cuerpoSeleccionado, simulacion.getInventario(), simulacion::notificarEvento, simulacion.getTickActual());
                    cancelarModoColonizacion();
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                cancelarModoColonizacion();
                simulacion.notificarEvento("Misión de colonización cancelada.", org.example.game.simulacion.MensajeEvento.TipoMensaje.INFO);
            }
            return;
        }

        if (e.getButton() == MouseButton.PRIMARY) {
            if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
                // Colocación directa asistida (sin lanzamientos)
                simulacion.actualizarPosicionPreview(e.getX(), e.getY());
                simulacion.confirmarColocacionAutoOrbita();
                trayectoriaPreview.clear();
                resetThrottlePreview();
                if (hotbar != null) {
                    hotbar.deseleccionar();
                }
            } else {
                inspeccionarCuerpoBajoCursor(e.getX(), e.getY());
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {
            if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
                // Click derecho cancela modo colocación
                simulacion.salirModoColocacion();
                if (hotbar != null) {
                    hotbar.deseleccionar();
                }
                cuerpoSeleccionado = null;
                trayectoriaPreview.clear();
                resetThrottlePreview();
            } else {
                inspeccionarCuerpoBajoCursor(e.getX(), e.getY());
            }
        }
    }

    public void manejarMouseArrastrado(MouseEvent e) {
        actualizarPreviewColocacion(e.getX(), e.getY());
    }

    private void actualizarPreviewColocacion(double x, double y) {
        if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
            simulacion.actualizarPosicionPreview(x, y);
            actualizarPrediccionTrayectoriaConThrottle(x, y);
        }
    }

    private void inspeccionarCuerpoBajoCursor(double x, double y) {
        seleccionarCuerpoBajoCursor(x, y);
        if (cuerpoSeleccionado != null && onInspeccionarCuerpo != null) {
            onInspeccionarCuerpo.accept(cuerpoSeleccionado);
        }
    }

    private void actualizarPrediccionTrayectoriaConThrottle(double mouseX, double mouseY) {
        long ahora = System.currentTimeMillis();
        double dx = mouseX - ultimoPreviewMouseX;
        double dy = mouseY - ultimoPreviewMouseY;
        // Throttling: recalcular solo ante desplazamiento significativo (>=2px) o pasados 16ms (~60Hz)
        if (Double.isNaN(ultimoPreviewMouseX) || (dx * dx + dy * dy >= 4.0 && ahora - ultimoTiempoPrediccionMs >= 16)) {
            ultimoPreviewMouseX = mouseX;
            ultimoPreviewMouseY = mouseY;
            ultimoTiempoPrediccionMs = ahora;
            actualizarPrediccionTrayectoriaAuto();
        }
    }

    private void resetThrottlePreview() {
        ultimoPreviewMouseX = Double.NaN;
        ultimoPreviewMouseY = Double.NaN;
        ultimoTiempoPrediccionMs = 0;
    }

    public void manejarMouseLiberado(MouseEvent e) {
        // Se inhabilitan los lanzamientos manuales (slingshots)
    }

    public void manejarScroll(ScrollEvent e) {
        if (e.isControlDown() && camaraZoom != null) {
            double delta = e.getDeltaY() > 0 ? 0.1 : -0.1;
            camaraZoom.ajustarPorDelta(delta);
        } else {
            double delta = e.getDeltaY() > 0 ? 1.15 : 0.87;
            simulacion.setFactorMasaColocacion(simulacion.getFactorMasaColocacion() * delta);
        }
        e.consume();
    }

    public void actualizarPrediccionTrayectoriaAuto() {
        Vector2D pos = simulacion.getPosicionPreview();
        if (pos == null) return;

        Vector2D velOrbital = simulacion.calcularVelocidadOrbitalAsistida(pos);
        TipoCuerpo tipo = simulacion.getTipoColocacion();
        double masa = tipo != null ? tipo.masaBase * simulacion.getFactorMasaColocacion() : 1e24;

        trayectoriaPreview = simulacion.getPredictor().predecirTrayectoria(
                pos, velOrbital, masa,
                simulacion.getSistemaSolar().getCuerpos(), 75, 1.0
        );
    }

    public void seleccionarCuerpoBajoCursor(double mouseX, double mouseY) {
        double xFisica = ConstantesFisicas.javaFXAFisicaX(mouseX, anchoMundo);
        double yFisica = ConstantesFisicas.javaFXAFisica(mouseY, altoMundo);
        Vector2D clickPos = new Vector2D(xFisica, yFisica);

        CuerpoCeleste masCercano = null;
        double menorDistancia = Double.MAX_VALUE;

        for (CuerpoCeleste c : simulacion.getSistemaSolar().getCuerpos()) {
            double d = clickPos.distanciaA(c.getPosicion());
            double radioTolerancia = Math.max(28.0, c.getRadio() + 10.0);
            if (d <= radioTolerancia && d < menorDistancia) {
                menorDistancia = d;
                masCercano = c;
            }
        }
        this.cuerpoSeleccionado = masCercano;
    }

    public void limpiarSeleccion() {
        this.cuerpoSeleccionado = null;
        this.trayectoriaPreview.clear();
        resetThrottlePreview();
    }

    @Override
    public List<Vector2D> getTrayectoriaPreview() {
        return trayectoriaPreview;
    }

    @Override
    public CuerpoCeleste getCuerpoSeleccionado() {
        return cuerpoSeleccionado;
    }

    public void setCuerpoSeleccionado(CuerpoCeleste cuerpo) {
        this.cuerpoSeleccionado = cuerpo;
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
