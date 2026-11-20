package org.example.game.controlador;

import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.render.CamaraZoom;

/**
 * Fábrica de composición para los colaboradores por defecto del subsistema de controladores (DIP).
 */
public class FabricaControlador {

    public ConversorCoordenadas crearConversorCoordenadas() {
        return new TransformadorCoordenadas();
    }

    public SeleccionadorCuerpo crearSeleccionadorCuerpo() {
        return new SeleccionadorCuerpoMouse(crearConversorCoordenadas());
    }

    public PredictorPreview crearPredictorPreview() {
        return new PredictorPreviewMouse();
    }

    public CamaraZoom crearCamaraZoom() {
        return new CamaraZoom();
    }
}
