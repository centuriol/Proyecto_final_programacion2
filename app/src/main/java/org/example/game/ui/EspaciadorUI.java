package org.example.game.ui;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * SRP: Componente utilitario de interfaz para generar separadores horizontales expansibles
 * en layouts basados en HBox, transparentes a la interacción con el mouse.
 */
public final class EspaciadorUI {

    private EspaciadorUI() {}

    public static HBox crearHorizontal() {
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMouseTransparent(true);
        return spacer;
    }
}
