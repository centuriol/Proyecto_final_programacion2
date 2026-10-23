package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Pantalla superpuesta de Game Over al colapsar el sistema solar y perder todos los mundos habitables.
 *
 * Principios SOLID:
 * - SRP: Modela exclusivamente la visualización del fin de partida y la acción de reinicio completo.
 * - DIP: Delega el reinicio al orquestador SimulacionSolar mediante contrato.
 */
public class PantallaGameOver extends StackPane {

    private final SimulacionSolar simulacion;
    private final VBox panelCentral;

    public PantallaGameOver(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        // Fondo oscuro translúcido que cubre todo el canvas
        setBackground(new Background(new BackgroundFill(Color.rgb(10, 10, 15, 0.88), CornerRadii.EMPTY, Insets.EMPTY)));
        setAlignment(Pos.CENTER);
        setVisible(false);
        setManaged(false);

        // Panel modal central
        panelCentral = new VBox(16);
        panelCentral.setAlignment(Pos.CENTER);
        panelCentral.setMaxWidth(460);
        panelCentral.setPadding(new Insets(30, 40, 30, 40));
        panelCentral.setBackground(new Background(new BackgroundFill(Color.web("#1a1c26"), new CornerRadii(6), Insets.EMPTY)));
        panelCentral.setBorder(new Border(new BorderStroke(Color.web("#ff4444"), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(2))));

        // Título trágico
        Label lblTitulo = new Label("PERDISTE TODO...");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#ff4444"));

        // Subtítulo explicativo
        Label lblSubtitulo = new Label("El agujero negro ha devorado todos los planetas y lunas del sistema solar. La civilización y la vida han desaparecido en la singularidad.");
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 13));
        lblSubtitulo.setTextFill(Color.web("#e8e4d8"));
        lblSubtitulo.setWrapText(true);
        lblSubtitulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Botón reiniciar
        Button btnReiniciar = new Button("Empezar de nuevo");
        btnReiniciar.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        btnReiniciar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 10 24 10 24; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        btnReiniciar.setOnAction(e -> {
            ocultar();
            if (simulacion != null) {
                simulacion.reiniciar();
            }
        });

        panelCentral.getChildren().addAll(lblTitulo, lblSubtitulo, btnReiniciar);
        getChildren().add(panelCentral);
    }

    public void mostrar() {
        setVisible(true);
        setManaged(true);
        toFront();
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);
    }
}
