package org.example.game.controlador;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.render.NavegacionCamara;
import org.example.game.simulacion.ControladorColocacion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;

import java.util.function.Consumer;

/**
 * SRP: Resuelve los clics de colonización, colocación, selección e inspección sobre el canvas.
 */
public class ManejadorClicksCanvas {

    private final ControlArrastreCamara controlArrastre = new ControlArrastreCamara();

    public void procesarPresionado(MouseEvent e, double worldX, double worldY, ControladorMouse ctrl,
                                   SimulacionSolar simulacion, BarraInventarioHotbar hotbar,
                                   Planeta planetaOrigen, Consumer<CuerpoCeleste> onInspeccionar) {
        if (planetaOrigen != null) {
            controlArrastre.cancelar();
            procesarClickColonizacion(e, worldX, worldY, ctrl, simulacion, planetaOrigen);
            return;
        }
        if (e.getButton() == MouseButton.PRIMARY) {
            procesarClickIzquierdo(e.getX(), e.getY(), worldX, worldY, ctrl, simulacion, hotbar);
        } else if (e.getButton() == MouseButton.SECONDARY) {
            controlArrastre.cancelar();
            procesarClickDerecho(worldX, worldY, ctrl, simulacion, hotbar, onInspeccionar);
        }
    }

    private void procesarClickColonizacion(MouseEvent e, double worldX, double worldY, ControladorMouse ctrl,
                                           SimulacionSolar simulacion, Planeta origen) {
        if (e.getButton() == MouseButton.PRIMARY) {
            ctrl.seleccionarCuerpoBajoCursor(worldX, worldY);
            if (ctrl.getCuerpoSeleccionado() != null) {
                simulacion.getGestorColonizacion().lanzar(origen, ctrl.getCuerpoSeleccionado(),
                        simulacion.getInventario(), simulacion.getBusEventos()::notificarEvento,
                        simulacion.getReloj().getTickActual());
                ctrl.cancelarModoColonizacion();
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {
            ctrl.cancelarModoColonizacion();
            simulacion.notificarEvento("Misión de colonización cancelada.", MensajeEvento.TipoMensaje.INFO);
        }
    }

    private void procesarClickIzquierdo(double screenX, double screenY, double worldX, double worldY,
                                        ControladorMouse ctrl, SimulacionSolar simulacion, BarraInventarioHotbar hotbar) {
        if (simulacion.getGestorColocacion().getModoColocacion() == ControladorColocacion.ModoColocacion.COLOCANDO) {
            controlArrastre.cancelar();
            simulacion.getGestorColocacion().actualizarPosicionPreview(worldX, worldY);
            simulacion.getGestorColocacion().confirmarColocacionAutoOrbita(simulacion);
            ctrl.limpiarSeleccion();
            if (hotbar != null) hotbar.deseleccionar();
        } else {
            controlArrastre.iniciar(screenX, screenY);
            ctrl.seleccionarCuerpoBajoCursor(worldX, worldY);
        }
    }

    private void procesarClickDerecho(double worldX, double worldY, ControladorMouse ctrl,
                                      SimulacionSolar simulacion, BarraInventarioHotbar hotbar,
                                      Consumer<CuerpoCeleste> onInspeccionar) {
        if (simulacion.getGestorColocacion().getModoColocacion() == ControladorColocacion.ModoColocacion.COLOCANDO) {
            simulacion.getGestorColocacion().salirModoColocacion();
            if (hotbar != null) hotbar.deseleccionar();
            ctrl.limpiarSeleccion();
        } else {
            inspeccionarBajoCursor(worldX, worldY, ctrl, onInspeccionar);
        }
    }

    public void procesarArrastre(MouseEvent e, boolean bloqueado, Runnable actualizarPreview, NavegacionCamara camaraZoom) {
        if (bloqueado) {
            controlArrastre.cancelar();
            actualizarPreview.run();
            return;
        }
        if (e.getButton() != MouseButton.SECONDARY && controlArrastre.estaActivo()) {
            controlArrastre.arrastrar(e.getX(), e.getY(), camaraZoom);
        }
    }

    public void procesarLiberado(MouseEvent e, double worldX, double worldY, ControladorMouse ctrl,
                                 Consumer<CuerpoCeleste> onInspeccionar) {
        if (e.getButton() == MouseButton.PRIMARY && controlArrastre.finalizarYEsClickSimple()) {
            inspeccionarBajoCursor(worldX, worldY, ctrl, onInspeccionar);
        }
    }

    private void inspeccionarBajoCursor(double x, double y, ControladorMouse ctrl, Consumer<CuerpoCeleste> onInspeccionar) {
        ctrl.seleccionarCuerpoBajoCursor(x, y);
        CuerpoCeleste sel = ctrl.getCuerpoSeleccionado();
        if (sel != null && sel.esInteractuablePorUsuario() && onInspeccionar != null) {
            onInspeccionar.accept(sel);
        }
    }
}
