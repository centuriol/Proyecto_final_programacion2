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
 * Menu modal de pausa invocado con la tecla Escape durante la partida.
 * Ofrece las cuatro acciones requeridas: Continuar, Guardar, Cargar y Salir al menu principal (SRP / DIP).
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

    private final Button btnContinuar;
    private final Button btnGuardar;
    private final Button btnCargar;
    private final Button btnSalirMenu;
    private final Label lblEstado = new Label("");

    public PantallaMenuPausa(Runnable onContinuar,
                             BooleanSupplier onGuardar,
                             BooleanSupplier onCargar,
                             Runnable onSalirMenu) {
        setBackground(new Background(new BackgroundFill(Color.rgb(8, 10, 18, 0.82), CornerRadii.EMPTY, Insets.EMPTY)));
        setAlignment(Pos.CENTER);
        setPickOnBounds(true);
        setVisible(false);
        setManaged(false);

        this.btnContinuar = fabricarBoton("Continuar", ESTILO_OPCION_PAUSA, () -> {
            if (onContinuar != null) onContinuar.run();
        });
        this.btnGuardar = fabricarBoton("Guardar", ESTILO_OPCION_PAUSA, () -> ejecutarGuardado(onGuardar));
        this.btnCargar = fabricarBoton("Cargar", ESTILO_OPCION_PAUSA, () -> ejecutarCarga(onCargar));
        this.btnSalirMenu = fabricarBoton("Salir al menú principal", ESTILO_OPCION_SALIR, () -> {
            ocultar();
            if (onSalirMenu != null) onSalirMenu.run();
        });

        configurarEtiquetaEstado();
        getChildren().add(ensamblarTarjeta());
    }

    private void configurarEtiquetaEstado() {
        lblEstado.setFont(Font.font("Monospace", FontWeight.BOLD, 11.5));
        lblEstado.setVisible(false);
        lblEstado.setManaged(false);
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

        VBox listaBotones = new VBox(10, btnContinuar, btnGuardar, btnCargar, btnSalirMenu);
        listaBotones.setAlignment(Pos.CENTER);
        listaBotones.setFillWidth(true);

        tarjeta.getChildren().addAll(encabezado, lblEstado, listaBotones);
        return tarjeta;
    }

    private Button fabricarBoton(String etiqueta, String estilo, Runnable accion) {
        Button boton = new Button(etiqueta);
        boton.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setStyle(estilo);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    private void ejecutarGuardado(BooleanSupplier onGuardar) {
        boolean ok = onGuardar != null && onGuardar.getAsBoolean();
        mostrarMensajeEstado(
                ok ? "Partida guardada correctamente." : "Error al guardar la partida.",
                ok ? TemaUI.COLOR_VERDE_EXITO : TemaUI.COLOR_ROJO_PELIGRO
        );
    }

    private void ejecutarCarga(BooleanSupplier onCargar) {
        boolean cargado = onCargar != null && onCargar.getAsBoolean();
        if (cargado) {
            ocultar();
        } else {
            mostrarMensajeEstado("No hay partidas guardadas disponibles.", TemaUI.COLOR_ORO);
        }
    }

    private void mostrarMensajeEstado(String texto, Color color) {
        lblEstado.setText(texto);
        lblEstado.setTextFill(color);
        lblEstado.setVisible(true);
        lblEstado.setManaged(true);
    }

    public void mostrar() {
        lblEstado.setText("");
        lblEstado.setVisible(false);
        lblEstado.setManaged(false);
        setVisible(true);
        setManaged(true);
        toFront();
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);
    }

    public Button getBtnContinuar() { return btnContinuar; }
    public Button getBtnGuardar() { return btnGuardar; }
    public Button getBtnCargar() { return btnCargar; }
    public Button getBtnSalirMenu() { return btnSalirMenu; }
    public Label getLblEstado() { return lblEstado; }
}
