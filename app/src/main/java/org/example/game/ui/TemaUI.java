package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Paleta de colores, bordes y estilos CSS unificados de la interfaz estilo RimWorld (DRY / SRP).
 */
public final class TemaUI {

    private TemaUI() {}

    // Colores base
    public static final Color COLOR_FONDO_OSCURO = Color.web("#0e1017");
    public static final Color COLOR_PANEL_BASE = Color.web("#1a1c26");
    public static final Color COLOR_PANEL_SLOT = Color.web("#2b2d3a");
    public static final Color COLOR_BORDE_NEGRO = Color.BLACK;
    public static final Color COLOR_BORDE_SECUNDARIO = Color.web("#2b2d3a");
    public static final Color COLOR_ACENTO_CYAN = Color.web("#5be3ff");
    public static final Color COLOR_TEXTO_TITULO = Color.web("#ffffff");
    public static final Color COLOR_TEXTO_MUTED = Color.web("#8c92a4");
    public static final Color COLOR_ORO = Color.web("#ffd700");
    public static final Color COLOR_VERDE_EXITO = Color.web("#50fa7b");
    public static final Color COLOR_ROJO_PELIGRO = Color.web("#ff5555");

    // Fondos y bordes JavaFX
    public static final Background BG_PANEL = new Background(new BackgroundFill(COLOR_PANEL_BASE, new CornerRadii(4), Insets.EMPTY));
    public static final Background BG_SLOT_NORMAL = new Background(new BackgroundFill(COLOR_PANEL_SLOT, new CornerRadii(4), Insets.EMPTY));
    public static final Background BG_SLOT_HOVER = new Background(new BackgroundFill(Color.web("#3b3d4f"), new CornerRadii(4), Insets.EMPTY));
    public static final Border BORDE_PANEL_NEGRO = new Border(new BorderStroke(COLOR_BORDE_NEGRO, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2)));
    public static final Border BORDE_SLOT_NORMAL = new Border(new BorderStroke(COLOR_BORDE_NEGRO, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2)));
    public static final Border BORDE_SLOT_HOVER = new Border(new BorderStroke(COLOR_ACENTO_CYAN, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2)));

    // Estilos CSS reutilizables
    public static final String CSS_MODAL_ROOT = "-fx-background-color: #0e1017; -fx-border-color: #000000; -fx-border-width: 2px;";
    public static final String CSS_SECCION_CARD = "-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;";
    public static final String CSS_TEXTFIELD = "-fx-background-color: #0d0e15; -fx-text-fill: #ffffff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-padding: 5 8 5 8;";
    public static final String CSS_BTN_PRIMARIO = "-fx-background-color: #1a1c26; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand; -fx-padding: 6 12 6 12;";
    public static final String CSS_BTN_SECUNDARIO = "-fx-background-color: #1a1c26; -fx-text-fill: #8c92a4; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand; -fx-padding: 6 12 6 12;";
    public static final String CSS_BTN_PELIGRO = "-fx-background-color: #1a1c26; -fx-text-fill: #ff5555; -fx-border-color: #ff5555; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-cursor: hand; -fx-padding: 6 12 6 12;";
}
