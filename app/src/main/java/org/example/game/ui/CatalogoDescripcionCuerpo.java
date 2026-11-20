package org.example.game.ui;

import javafx.scene.layout.VBox;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * Capacidad segregada de consulta descriptiva y ficha de características de un tipo de cuerpo (ISP).
 */
public interface CatalogoDescripcionCuerpo {
    String nombrePorDefecto(TipoCuerpo tipo);
    String icono(TipoCuerpo tipo);
    String subtitulo(TipoCuerpo tipo);
    String lore(TipoCuerpo tipo);
    VBox caracteristicas(TipoCuerpo tipo);
}
