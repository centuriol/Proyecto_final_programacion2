package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.BooleanSupplier;

/**
 * Menú modal de pausa invocado con la tecla Escape durante la partida (SRP / DIP).
 */
public class PantallaMenuPausa extends StackPane {

    private static final String ESTILO_OPCION_PAUSA =
            "-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; "
            + "-fx-border-width: 1.4px; -fx-background-radius: 4px; -fx-border-radius: 4px; "
            + "-fx-padding: 10 22 10 22; -fx-cursor: hand;";
    private static final String ESTILO_OPCION_SALIR =
            "-fx-background-color: #1f1720; -fx-text-fill: #ff6b6b; -fx-border-color: #ff6b6b; "
            + "-fx-border-width: 1.4px; -fx-background-radius: 4px; -fx-border-radius: 4px; "
            + "-fx-padding: 10 22 10 22; -fx-cursor: hand;";

    private static class ControlesPausa {
        final Button btnContinuar;
        final Button btnGuardar;
        final Button btnCargar;
        final Button btnSalirMenu;
        final Label lblEstado = new Label("");

        ControlesPausa(Runnable onContinuar, Runnable onGuardar, Runnable onCargar, Runnable onSalir) {
            this.btnContinuar = fabricarBoton("Continuar", ESTILO_OPCION_PAUSA, onContinuar);
            this.btnGuardar = fabricarBoton("Guardar", ESTILO_OPCION_PAUSA, onGuardar);
            this.btnCargar = fabricarBoton("Cargar", ESTILO_OPCION_PAUSA, onCargar);
            this.btnSalirMenu = fabricarBoton("Salir al menú principal", ESTILO_OPCION_SALIR, onSalir);
            this.lblEstado.setFont(Font.font("Monospace", FontWeight.BOLD, 11.5));
            limpiarEstado();
        }

        private static Button fabricarBoton(String etiqueta, String estilo, Runnable accion) {
            Button boton = new Button(etiqueta);
            boton.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setStyle(estilo);
            boton.setOnAction(evento -> accion.run());
            return boton;
        }

        void mostrarEstado(String texto, Color color) {
            lblEstado.setText(texto);
            lblEstado.setTextFill(color);
            lblEstado.setVisible(true);
            lblEstado.setManaged(true);
        }

        void limpiarEstado() {
            lblEstado.setText("");
            lblEstado.setVisible(false);
            lblEstado.setManaged(false);
        }
    }

    private final ControlesPausa controles;

    public PantallaMenuPausa(Runnable onContinuar,
                             BooleanSupplier onGuardar,
                             BooleanSupplier onCargar,
                             Runnable onSalirMenu) {
        setBackground(new Background(new BackgroundFill(Color.rgb(8, 10, 18, 0.82), CornerRadii.EMPTY, Insets.EMPTY)));
        setAlignment(Pos.CENTER);
        setPickOnBounds(true);
        setVisible(false);
        setManaged(false);

        this.controles = new ControlesPausa(
                () -> { if (onContinuar != null) onContinuar.run(); },
                () -> ejecutarGuardado(onGuardar),
                () -> ejecutarCarga(onCargar),
                () -> { ocultar(); if (onSalirMenu != null) onSalirMenu.run(); }
        );
        getChildren().add(ensamblarTarjeta());
    }

    private VBox ensamblarTarjeta() {
        VBox tarjeta = new VBox(14);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setMaxWidth(400);
        tarjeta.setPadding(new Insets(28, 36, 28, 36));
        tarjeta.setBackground(new Background(new BackgroundFill(TemaUI.COLOR_PANEL_BASE, new CornerRadii(6), Insets.EMPTY)));
        tarjeta.setBorder(new Border(new BorderStroke(TemaUI.COLOR_ACENTO_CYAN, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(1.8))));

        Label encabezado = new Label("PAUSA");
        encabezado.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        encabezado.setTextFill(TemaUI.COLOR_ACENTO_CYAN);

        VBox listaBotones = new VBox(10, controles.btnContinuar, controles.btnGuardar, controles.btnCargar, controles.btnSalirMenu);
        listaBotones.setAlignment(Pos.CENTER);
        listaBotones.setFillWidth(true);

        tarjeta.getChildren().addAll(encabezado, controles.lblEstado, listaBotones);
        return tarjeta;
    }

    private void ejecutarGuardado(BooleanSupplier onGuardar) {
        boolean ok = onGuardar != null && onGuardar.getAsBoolean();
        controles.mostrarEstado(
                ok ? "Partida guardada correctamente." : "Error al guardar la partida.",
                ok ? TemaUI.COLOR_VERDE_EXITO : TemaUI.COLOR_ROJO_PELIGRO
        );
    }

    private void ejecutarCarga(BooleanSupplier onCargar) {
        if (onCargar != null && onCargar.getAsBoolean()) {
            ocultar();
        } else {
            controles.mostrarEstado("No hay partidas guardadas disponibles.", TemaUI.COLOR_ORO);
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

    public Button getBtnContinuar() { return controles.btnContinuar; }
    public Button getBtnGuardar() { return controles.btnGuardar; }
    public Button getBtnCargar() { return controles.btnCargar; }
    public Button getBtnSalirMenu() { return controles.btnSalirMenu; }
    public Label getLblEstado() { return controles.lblEstado; }
}
