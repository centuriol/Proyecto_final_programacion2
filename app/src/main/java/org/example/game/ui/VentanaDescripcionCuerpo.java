package org.example.game.ui;
import javafx.scene.layout.VBox; import javafx.stage.Window; import org.example.game.controlador.ControladorMouse; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.simulacion.SimulacionSolar;
/**
 * Fachada para las ventanas modales de inspección y colocación de cuerpos celestes (Facade / SRP).
 */
public class VentanaDescripcionCuerpo {

    public static void mostrarParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        VentanaColocacion.mostrar(tipo, simulacion, owner, onConfirmado);
    }

    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, Window owner) {
        VentanaInspeccion.mostrar(cuerpo, null, owner, null);
    }

    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner) {
        VentanaInspeccion.mostrar(cuerpo, simulacion, owner, null);
    }

    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse) {
        VentanaInspeccion.mostrar(cuerpo, simulacion, owner, controladorMouse);
    }

    public static void mostrar(TipoCuerpo tipo, Window owner) {
        VentanaColocacion.mostrar(tipo, null, owner, null);
    }

    public static String obtenerNombrePorDefecto(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerNombrePorDefecto(tipo);
    }

    public static String obtenerIcono(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerIcono(tipo);
    }

    public static String obtenerSubtitulo(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerSubtitulo(tipo);
    }

    public static String obtenerLore(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerLore(tipo);
    }

    public static VBox crearSeccionCaracteristicas(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.crearSeccionCaracteristicas(tipo);
    }
}
