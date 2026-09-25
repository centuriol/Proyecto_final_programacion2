package org.example.game.controlador.comando;

import org.example.game.controlador.ControladorMouse;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;

/**
 * SRP: Encapsula la acción de cancelar el modo colocación actual,
 * deseleccionar ítems en la hotbar y limpiar la selección activa de cuerpos celestes.
 */
public class ComandoCancelar implements Comando {

    private final SimulacionSolar simulacion;
    private final BarraInventarioHotbar hotbar;
    private final ControladorMouse controladorMouse;

    public ComandoCancelar(SimulacionSolar simulacion, BarraInventarioHotbar hotbar, ControladorMouse controladorMouse) {
        this.simulacion = simulacion;
        this.hotbar = hotbar;
        this.controladorMouse = controladorMouse;
    }

    @Override
    public void ejecutar() {
        if (simulacion != null) {
            simulacion.salirModoColocacion();
        }
        if (hotbar != null) {
            hotbar.deseleccionar();
        }
        if (controladorMouse != null) {
            controladorMouse.limpiarSeleccion();
        }
    }
}
