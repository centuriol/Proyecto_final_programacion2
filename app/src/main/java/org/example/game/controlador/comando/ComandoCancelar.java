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
    private Runnable accionExtra;

    public ComandoCancelar(SimulacionSolar simulacion, BarraInventarioHotbar hotbar, ControladorMouse controladorMouse) {
        this.simulacion = simulacion;
        this.hotbar = hotbar;
        this.controladorMouse = controladorMouse;
    }

    public void setAccionExtra(Runnable accionExtra) {
        this.accionExtra = accionExtra;
    }

    @Override
    public void ejecutar() {
        if (accionExtra != null) {
            accionExtra.run();
        }
        if (simulacion != null) {
            simulacion.getGestorColocacion().salirModoColocacion();
        }
        if (hotbar != null) {
            hotbar.deseleccionar();
        }
        if (controladorMouse != null) {
            controladorMouse.limpiarSeleccion();
            controladorMouse.cancelarModoColonizacion();
        }
    }
}
