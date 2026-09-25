package org.example.game.controlador;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import org.example.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.VentanaDescripcionCuerpo;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula la gestión de eventos de mouse y la interacción sobre el canvas de simulación.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de procesar entradas de ratón (movimiento, clicks, arrastres y scroll)
 *   y coordinar la selección de cuerpos y predicción de trayectorias.
 * - ISP + DIP: Implementa ProveedorEstadoInteraccion para ofrecer sólo el estado visual que requiere el render.
 */
public class ControladorMouse implements ProveedorEstadoInteraccion {

    private final SimulacionSolar simulacion;
    private final BarraInventarioHotbar hotbar;
    private final Stage stage;
    private final double anchoMundo;
    private final double altoMundo;

    private List<Vector2D> trayectoriaPreview = new ArrayList<>();
    private CuerpoCeleste cuerpoSeleccionado = null;

    public ControladorMouse(SimulacionSolar simulacion,
                            BarraInventarioHotbar hotbar,
                            Stage stage,
                            double anchoMundo,
                            double altoMundo) {
        this.simulacion = simulacion;
        this.hotbar = hotbar;
        this.stage = stage;
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
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
        if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
            simulacion.actualizarPosicionPreview(e.getX(), e.getY());
            actualizarPrediccionTrayectoriaAuto();
        }
    }

    public void manejarMousePresionado(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
                // Colocación directa asistida (sin lanzamientos)
                simulacion.actualizarPosicionPreview(e.getX(), e.getY());
                simulacion.confirmarColocacionAutoOrbita();
                trayectoriaPreview.clear();
                if (hotbar != null) {
                    hotbar.deseleccionar();
                }
            } else {
                // Seleccionar cuerpo bajo el cursor y abrir descripción para inspección/eliminación
                seleccionarCuerpoBajoCursor(e.getX(), e.getY());
                if (cuerpoSeleccionado != null) {
                    VentanaDescripcionCuerpo.mostrarParaInspeccionar(cuerpoSeleccionado, simulacion, stage);
                }
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
            } else {
                // Click derecho sobre un cuerpo abre ventana de descripción e inspección del ítem
                seleccionarCuerpoBajoCursor(e.getX(), e.getY());
                if (cuerpoSeleccionado != null) {
                    VentanaDescripcionCuerpo.mostrarParaInspeccionar(cuerpoSeleccionado, simulacion, stage);
                }
            }
        }
    }

    public void manejarMouseArrastrado(MouseEvent e) {
        if (simulacion.getModoColocacion() == SimulacionSolar.ModoColocacion.COLOCANDO) {
            simulacion.actualizarPosicionPreview(e.getX(), e.getY());
            actualizarPrediccionTrayectoriaAuto();
        }
    }

    public void manejarMouseLiberado(MouseEvent e) {
        // Se inhabilitan los lanzamientos manuales (slingshots)
    }

    public void manejarScroll(ScrollEvent e) {
        double delta = e.getDeltaY() > 0 ? 1.15 : 0.87;
        simulacion.setFactorMasaColocacion(simulacion.getFactorMasaColocacion() * delta);
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
}
