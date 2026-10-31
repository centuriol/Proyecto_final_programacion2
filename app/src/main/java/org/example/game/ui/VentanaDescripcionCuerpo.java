package org.example.game.ui;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;
/**
 * Fachada para las ventanas modales de inspección y colocación de cuerpos celestes (Facade / SRP).
 */
public class VentanaDescripcionCuerpo {

    private static final VentanaDescripcionCuerpo INSTANCIA_POR_DEFECTO = new VentanaDescripcionCuerpo();

    public VentanaDescripcionCuerpo() {}

    public static VentanaDescripcionCuerpo getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public void abrirParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        VentanaColocacion.mostrar(tipo, simulacion, owner, onConfirmado);
    }

    public void abrirParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse) {
        VentanaInspeccion.mostrar(cuerpo, simulacion, owner, controladorMouse);
    }

    public void abrir(TipoCuerpo tipo, Window owner) {
        VentanaColocacion.mostrar(tipo, null, owner, null);
    }

    public String nombrePorDefecto(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerNombrePorDefecto(tipo);
    }

    public String icono(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerIcono(tipo);
    }

    public String subtitulo(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerSubtitulo(tipo);
    }

    public String lore(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.obtenerLore(tipo);
    }

    public VBox caracteristicas(TipoCuerpo tipo) {
        return SeccionInfoCuerpo.crearSeccionCaracteristicas(tipo);
    }

    public static void mostrarParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        INSTANCIA_POR_DEFECTO.abrirParaColocar(tipo, simulacion, owner, onConfirmado);
    }

    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, Window owner) {
        INSTANCIA_POR_DEFECTO.abrirParaInspeccionar(cuerpo, null, owner, null);
    }

    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner) {
        INSTANCIA_POR_DEFECTO.abrirParaInspeccionar(cuerpo, simulacion, owner, null);
    }

    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse) {
        INSTANCIA_POR_DEFECTO.abrirParaInspeccionar(cuerpo, simulacion, owner, controladorMouse);
    }

    public static void mostrar(TipoCuerpo tipo, Window owner) {
        INSTANCIA_POR_DEFECTO.abrir(tipo, owner);
    }

    public static String obtenerNombrePorDefecto(TipoCuerpo tipo) {
        return INSTANCIA_POR_DEFECTO.nombrePorDefecto(tipo);
    }

    public static String obtenerIcono(TipoCuerpo tipo) {
        return INSTANCIA_POR_DEFECTO.icono(tipo);
    }

    public static String obtenerSubtitulo(TipoCuerpo tipo) {
        return INSTANCIA_POR_DEFECTO.subtitulo(tipo);
    }

    public static String obtenerLore(TipoCuerpo tipo) {
        return INSTANCIA_POR_DEFECTO.lore(tipo);
    }

    public static VBox crearSeccionCaracteristicas(TipoCuerpo tipo) {
        return INSTANCIA_POR_DEFECTO.caracteristicas(tipo);
    }
}
