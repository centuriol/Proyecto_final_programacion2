package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.controlador.CoordinadorSesionJuego;

/**
 * Constructor de capas de interfaz superpuesta (HUD, barras superior e inferior) (SRP).
 */
public class ConstructorOverlayUI {

    private static final String ESTILO_BTN_ARBOL_DISCRETO =
            "-fx-background-color: rgba(24, 27, 38, 0.88); -fx-text-fill: #9aa3b8; "
            + "-fx-border-color: #363c52; -fx-border-width: 1px; -fx-background-radius: 4px; "
            + "-fx-border-radius: 4px; -fx-padding: 6 14 6 14; -fx-cursor: hand;";

    public record EstructuraOverlay(
            BorderPane overlay,
            HBox topBar,
            BorderPane bottomBar,
            Button btnArbolInferior
    ) {}

    public EstructuraOverlay construir(
            HUDRecursosTop hudRecursos,
            BarraInventarioHotbar hotbar,
            ControlTiempoWidget controlTiempo,
            PanelNotificaciones panelNotificaciones,
            PantallaArbolProgresion arbol,
            CoordinadorSesionJuego coordinadorSesion
    ) {
        BorderPane overlay = new BorderPane();
        overlay.setPadding(new Insets(12));
        overlay.setPickOnBounds(false);

        HBox topBar = new HBox(EspaciadorUI.crearHorizontal(), hudRecursos);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPickOnBounds(false);

        BorderPane bottomBar = new BorderPane();
        bottomBar.setPickOnBounds(false);

        BorderPane.setAlignment(controlTiempo, Pos.BOTTOM_LEFT);
        bottomBar.setLeft(controlTiempo);

        VBox contenedorHotbar = new VBox(2, hotbar.getBotonToggle(), hotbar);
        contenedorHotbar.setAlignment(Pos.BOTTOM_CENTER);
        contenedorHotbar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        contenedorHotbar.setPickOnBounds(false);
        BorderPane.setAlignment(contenedorHotbar, Pos.BOTTOM_CENTER);
        bottomBar.setCenter(contenedorHotbar);

        HBox esquinaDerecha = new HBox();
        esquinaDerecha.setAlignment(Pos.BOTTOM_RIGHT);
        esquinaDerecha.setPickOnBounds(false);
        esquinaDerecha.minWidthProperty().bind(controlTiempo.widthProperty());
        esquinaDerecha.prefWidthProperty().bind(controlTiempo.widthProperty());

        Button btnArbolInferior = new Button("Árbol");
        btnArbolInferior.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnArbolInferior.setStyle(ESTILO_BTN_ARBOL_DISCRETO);
        btnArbolInferior.setFocusTraversable(false);
        btnArbolInferior.setOnAction(evento -> {
            if (coordinadorSesion != null && !coordinadorSesion.isEnMenuPausa()) {
                arbol.mostrar();
            }
        });
        esquinaDerecha.getChildren().add(btnArbolInferior);
        BorderPane.setAlignment(esquinaDerecha, Pos.BOTTOM_RIGHT);
        bottomBar.setRight(esquinaDerecha);

        overlay.setTop(topBar);
        overlay.setLeft(panelNotificaciones);
        overlay.setBottom(bottomBar);

        return new EstructuraOverlay(overlay, topBar, bottomBar, btnArbolInferior);
    }
}
