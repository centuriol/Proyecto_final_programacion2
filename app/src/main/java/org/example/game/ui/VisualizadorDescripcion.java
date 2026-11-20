package org.example.game.ui;

import javafx.stage.Window;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Contrato compuesto para apertura de ventanas modales de colocación, inspección y consulta de descripciones (ISP / DIP).
 */
public interface VisualizadorDescripcion extends CatalogoDescripcionCuerpo {
    void abrirParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado);
    void abrirParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse);
    void abrir(TipoCuerpo tipo, Window owner);
}
