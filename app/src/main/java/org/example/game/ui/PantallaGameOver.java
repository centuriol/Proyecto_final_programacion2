package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.game.simulacion.SimulacionSolar;
import java.util.function.BooleanSupplier;

/**
 * Pantalla superpuesta de derrota con dos opciones:
 * volver al último guardado o volver al menú principal (SRP / DIP).
 */
public class PantallaGameOver extends StackPane {

    private static final String ESTILO_BTN_ACCION =
            "-fx-background-color: #2b2d3a; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; "
            + "-fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 10 22 10 22; "
            + "-fx-background-radius: 4px; -fx-border-radius: 4px;";
    private static final String ESTILO_BTN_MENU =
            "-fx-background-color: #1a1c26; -fx-text-fill: #e8e4d8; -fx-border-color: #8c92a4; "
            + "-fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 10 22 10 22; "
            + "-fx-background-radius: 4px; -fx-border-radius: 4px;";

    private static class ControlesGameOver {
        final Button btnVolverUltimoGuardado = new Button("Volver al último guardado");
        final Button btnVolverMenu = new Button("Volver al menú");
        final Label lblEstadoAccion = new Label("");

        void configurar(Runnable onUltimoGuardado, Runnable onMenu) {
            lblEstadoAccion.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
            lblEstadoAccion.setTextFill(TemaUI.COLOR_ORO);
            limpiarEstado();

            btnVolverUltimoGuardado.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            btnVolverUltimoGuardado.setMaxWidth(Double.MAX_VALUE);
            btnVolverUltimoGuardado.setStyle(ESTILO_BTN_ACCION);
            btnVolverUltimoGuardado.setOnAction(e -> onUltimoGuardado.run());

            btnVolverMenu.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            btnVolverMenu.setMaxWidth(Double.MAX_VALUE);
            btnVolverMenu.setStyle(ESTILO_BTN_MENU);
            btnVolverMenu.setOnAction(e -> onMenu.run());
        }

        void mostrarSinGuardados() {
            lblEstadoAccion.setText("No hay partidas guardadas disponibles.");
            lblEstadoAccion.setVisible(true);
            lblEstadoAccion.setManaged(true);
        }

        void limpiarEstado() {
            lblEstadoAccion.setText("");
            lblEstadoAccion.setVisible(false);
            lblEstadoAccion.setManaged(false);
        }
    }

    private final ControlesGameOver controles = new ControlesGameOver();

    public PantallaGameOver(SimulacionSolar simulacion) {
        this(() -> {
            if (simulacion != null) {
                simulacion.reiniciar();
                return true;
            }
            return false;
        }, () -> {});
    }

    public PantallaGameOver(BooleanSupplier accionVolverUltimoGuardado, Runnable accionVolverMenu) {
        setBackground(new Background(new BackgroundFill(Color.rgb(10, 10, 15, 0.88), CornerRadii.EMPTY, Insets.EMPTY)));
        setAlignment(Pos.CENTER);
        setPickOnBounds(true);
        setVisible(false);
        setManaged(false);
        controles.configurar(
                () -> ejecutarVolverUltimoGuardado(accionVolverUltimoGuardado),
                () -> { ocultar(); if (accionVolverMenu != null) accionVolverMenu.run(); }
        );
        getChildren().add(construirPanelCentral());
    }

    private VBox construirPanelCentral() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(480);
        panel.setPadding(new Insets(30, 40, 30, 40));
        panel.setBackground(new Background(new BackgroundFill(Color.web("#1a1c26"), new CornerRadii(6), Insets.EMPTY)));
        panel.setBorder(new Border(new BorderStroke(Color.web("#ff4444"), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(2))));

        Label lblTitulo = new Label("PERDISTE LA PARTIDA");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#ff4444"));

        Label lblSubtitulo = new Label("El agujero negro ha devorado todos los planetas y lunas del sistema solar. Selecciona una opción para continuar.");
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 13));
        lblSubtitulo.setTextFill(Color.web("#e8e4d8"));
        lblSubtitulo.setWrapText(true);
        lblSubtitulo.setTextAlignment(TextAlignment.CENTER);

        VBox cajaBotones = new VBox(10, controles.btnVolverUltimoGuardado, controles.btnVolverMenu);
        cajaBotones.setAlignment(Pos.CENTER);
        cajaBotones.setFillWidth(true);
        panel.getChildren().addAll(lblTitulo, lblSubtitulo, controles.lblEstadoAccion, cajaBotones);
        return panel;
    }

    private void ejecutarVolverUltimoGuardado(BooleanSupplier accionVolverUltimoGuardado) {
        if (accionVolverUltimoGuardado != null && accionVolverUltimoGuardado.getAsBoolean()) {
            ocultar();
        } else {
            controles.mostrarSinGuardados();
        }
    }

    public void mostrar() {
        controles.limpiarEstado();
        setVisible(true);
        setManaged(true);
        toFront();
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);
    }

    public Button getBtnVolverUltimoGuardado() { return controles.btnVolverUltimoGuardado; }
    public Button getBtnVolverMenu() { return controles.btnVolverMenu; }
    public Label getLblEstadoAccion() { return controles.lblEstadoAccion; }
}
