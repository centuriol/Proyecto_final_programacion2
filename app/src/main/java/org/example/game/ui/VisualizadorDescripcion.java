package org.example.game.ui;

import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Contrato para ventanas de inspección y descripción de cuerpos (ISP / DIP).
 */
public interface VisualizadorDescripcion {

    void abrirParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado);

    void abrirParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse);

    void abrir(TipoCuerpo tipo, Window owner);

    String nombrePorDefecto(TipoCuerpo tipo);

    String icono(TipoCuerpo tipo);

    String subtitulo(TipoCuerpo tipo);

    String lore(TipoCuerpo tipo);

    VBox caracteristicas(TipoCuerpo tipo);
}
