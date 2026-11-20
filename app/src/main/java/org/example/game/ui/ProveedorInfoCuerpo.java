package org.example.game.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Contrato compuesto para generación de diálogos modales, secciones visuales y descriptores de cuerpos (ISP / DIP).
 */
public interface ProveedorInfoCuerpo extends ProveedorDescriptoresCuerpo, ConstructorSeccionesCuerpo {
    VBox inicializarDialogo(Stage stage, String titulo, Window owner);
    void vincularCierrePorEscape(Scene scene, Stage stage);
    HBox crearHeader(Node iconoNodo, String titulo, String subtitulo);
    void agregarFilaPropiedad(GridPane grid, int fila, String clave, String valor, String colorValorHex);
}
