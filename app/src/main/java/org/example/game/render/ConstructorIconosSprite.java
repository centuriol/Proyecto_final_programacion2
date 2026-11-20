package org.example.game.render;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * Capacidad segregada para construir nodos gráficos JavaFX (íconos y contenedores) desde sprites (ISP).
 */
public interface ConstructorIconosSprite {
    ImageView crearVistaIcono(TipoCuerpo tipo, double ancho, double alto);
    StackPane crearContenedorSprite(TipoCuerpo tipo, double ancho, double alto);
}
