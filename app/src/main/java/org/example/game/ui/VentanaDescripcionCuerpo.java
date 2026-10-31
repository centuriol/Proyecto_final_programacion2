package org.example.game.ui;

import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Fachada para las ventanas modales de inspección y colocación de cuerpos celestes (Facade / SRP / DIP).
 */
public class VentanaDescripcionCuerpo implements VisualizadorDescripcion {

    private final ProveedorInfoCuerpo infoCuerpo;

    public VentanaDescripcionCuerpo() {
        this(new SeccionInfoCuerpo());
    }

    public VentanaDescripcionCuerpo(ProveedorInfoCuerpo infoCuerpo) {
        this.infoCuerpo = infoCuerpo != null ? infoCuerpo : new SeccionInfoCuerpo();
    }

    @Override
    public void abrirParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        VentanaColocacion.mostrar(tipo, simulacion, owner, onConfirmado);
    }

    @Override
    public void abrirParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse) {
        VentanaInspeccion.mostrar(cuerpo, simulacion, owner, controladorMouse);
    }

    @Override
    public void abrir(TipoCuerpo tipo, Window owner) {
        VentanaColocacion.mostrar(tipo, null, owner, null);
    }

    @Override
    public String nombrePorDefecto(TipoCuerpo tipo) {
        return infoCuerpo.obtenerNombrePorDefecto(tipo);
    }

    @Override
    public String icono(TipoCuerpo tipo) {
        return infoCuerpo.obtenerIcono(tipo);
    }

    @Override
    public String subtitulo(TipoCuerpo tipo) {
        return infoCuerpo.obtenerSubtitulo(tipo);
    }

    @Override
    public String lore(TipoCuerpo tipo) {
        return infoCuerpo.obtenerLore(tipo);
    }

    @Override
    public VBox caracteristicas(TipoCuerpo tipo) {
        return infoCuerpo.crearSeccionCaracteristicas(tipo);
    }
}
