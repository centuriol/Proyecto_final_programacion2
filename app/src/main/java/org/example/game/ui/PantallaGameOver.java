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
 * volver al ultimo guardado o volver al menu principal (SRP / DIP).
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

    private final Button btnVolverUltimoGuardado = new Button("Volver al último guardado");
    private final Button btnVolverMenu = new Button("Volver al menú");
    private final Label lblEstadoAccion = new Label("");

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
        configurarControles(accionVolverUltimoGuardado, accionVolverMenu);
        getChildren().add(construirPanelCentral());
    }

    private void configurarControles(BooleanSupplier accionVolverUltimoGuardado, Runnable accionVolverMenu) {
        lblEstadoAccion.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        lblEstadoAccion.setTextFill(TemaUI.COLOR_ORO);
        lblEstadoAccion.setVisible(false);
        lblEstadoAccion.setManaged(false);

        btnVolverUltimoGuardado.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        btnVolverUltimoGuardado.setMaxWidth(Double.MAX_VALUE);
        btnVolverUltimoGuardado.setStyle(ESTILO_BTN_ACCION);
        btnVolverUltimoGuardado.setOnAction(e -> ejecutarVolverUltimoGuardado(accionVolverUltimoGuardado));

        btnVolverMenu.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        btnVolverMenu.setMaxWidth(Double.MAX_VALUE);
        btnVolverMenu.setStyle(ESTILO_BTN_MENU);
        btnVolverMenu.setOnAction(e -> {
            ocultar();
            if (accionVolverMenu != null) {
                accionVolverMenu.run();
            }
        });
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

        VBox cajaBotones = new VBox(10, btnVolverUltimoGuardado, btnVolverMenu);
        cajaBotones.setAlignment(Pos.CENTER);
        cajaBotones.setFillWidth(true);
        panel.getChildren().addAll(lblTitulo, lblSubtitulo, lblEstadoAccion, cajaBotones);
        return panel;
    }

    private void ejecutarVolverUltimoGuardado(BooleanSupplier accionVolverUltimoGuardado) {
        boolean restaurado = accionVolverUltimoGuardado != null && accionVolverUltimoGuardado.getAsBoolean();
        if (restaurado) {
            ocultar();
        } else {
            lblEstadoAccion.setText("No hay partidas guardadas disponibles.");
            lblEstadoAccion.setVisible(true);
            lblEstadoAccion.setManaged(true);
        }
    }

    public void mostrar() {
        lblEstadoAccion.setText("");
        lblEstadoAccion.setVisible(false);
        lblEstadoAccion.setManaged(false);
        setVisible(true);
        setManaged(true);
        toFront();
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);
    }

    public Button getBtnVolverUltimoGuardado() { return btnVolverUltimoGuardado; }
    public Button getBtnVolverMenu() { return btnVolverMenu; }
    public Label getLblEstadoAccion() { return lblEstadoAccion; }
}
