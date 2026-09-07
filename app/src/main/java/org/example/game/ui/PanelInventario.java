package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.simulacion.ConfiguracionSimulacion;

/**
 * Panel lateral de inventario y controles de spawn.
 */
public class PanelInventario extends VBox {
    private final SimulacionSolar simulacion;
    private final InventarioJugador inventario;

    // UI Elements
    private final Label lblTitulo = new Label("INVENTARIO");
    private final VBox recursosBox = new VBox(5);
    private final Separator sep1 = new Separator();
    private final Label lblSpawn = new Label("CREAR CUERPO");
    private final VBox botonesSpawnBox = new VBox(5);
    private final Separator sep2 = new Separator();
    private final Label lblMasa = new Label("FACTOR MASA: 1.0x");
    private final Slider sliderMasa = new Slider(
            ConfiguracionSimulacion.MASA_FACTOR_MIN,
            ConfiguracionSimulacion.MASA_FACTOR_MAX,
            ConfiguracionSimulacion.MASA_FACTOR_MIN
    );
    private final Label lblInfoCosto = new Label("");
    private final Separator sep3 = new Separator();
    private final Label lblControles = new Label("CONTROLES");
    private final VBox controlesBox = new VBox(3);

    private TipoCuerpo tipoSeleccionado = null;

    public PanelInventario(SimulacionSolar simulacion) {
        this.simulacion = simulacion;
        this.inventario = simulacion.getInventario();

        setPrefWidth(ConfiguracionSimulacion.PANEL_LATERAL_ANCHO);
        setPadding(new Insets(10));
        setSpacing(10);
        setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #0f3460; -fx-border-width: 0 2 0 0;");

        // Estilo común para labels
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTitulo.setTextFill(Color.WHITE);
        lblSpawn.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblSpawn.setTextFill(Color.web("#00ffff"));
        lblMasa.setFont(Font.font("System", FontWeight.BOLD, 11));
        lblMasa.setTextFill(Color.web("#ffd700"));
        lblControles.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblControles.setTextFill(Color.web("#00ffff"));

        // Slider masa
        sliderMasa.setShowTickLabels(true);
        sliderMasa.setShowTickMarks(true);
        sliderMasa.setMajorTickUnit(1.0);
        sliderMasa.setMinorTickCount(4);
        sliderMasa.setSnapToTicks(false);
        sliderMasa.valueProperty().addListener((obs, old, neu) -> {
            double f = neu.doubleValue();
            simulacion.setFactorMasaColocacion(f);
            lblMasa.setText(String.format("FACTOR MASA: %.1fx", f));
            actualizarInfoCosto();
        });

        // Botones de spawn
        crearBotonesSpawn();

        // Controles de simulación
        crearControlesSimulacion();

        getChildren().addAll(
                lblTitulo, recursosBox, sep1,
                lblSpawn, botonesSpawnBox, lblMasa, sliderMasa, lblInfoCosto, sep2,
                lblControles, controlesBox
        );

        // Actualizar periódicamente
        simulacion.setOnTickCallback(this::actualizarRecursos);
        actualizarRecursos();
    }

    private void crearBotonesSpawn() {
        // Estrella
        Button btnEstrella = crearBotonSpawn("Estrella", TipoCuerpo.ESTRELLA,
                "Crea una estrella (fuente de gravedad y energia)");
        // Planeta Rocoso
        Button btnRocoso = crearBotonSpawn("Planeta Rocoso", TipoCuerpo.PLANETA_ROCOSO,
                "Planeta tipo Tierra, puede albergar civilizacion");
        // Luna
        Button btnLuna = crearBotonSpawn("Luna", TipoCuerpo.LUNA,
                "Satelite natural rocoso");

        botonesSpawnBox.getChildren().addAll(
                btnEstrella, btnRocoso, btnLuna
        );
    }

    private Button crearBotonSpawn(String texto, TipoCuerpo tipo, String tooltip) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(30);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-font-size: 11px;");

        btn.setOnAction(e -> {
            if (inventario.puedeCrear(tipo, simulacion.getFactorMasaColocacion())) {
                simulacion.entrarModoColocacion(tipo, simulacion.getFactorMasaColocacion());
                tipoSeleccionado = tipo;
                actualizarSeleccionVisual(tipo);
            } else {
                // Feedback visual: color rojo temporal
                btn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                                evt -> btn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white;"))
                ).play();
            }
        });

        return btn;
    }

    private void actualizarSeleccionVisual(TipoCuerpo seleccionado) {
        for (javafx.scene.Node n : botonesSpawnBox.getChildren()) {
            if (n instanceof Button) {
                Button b = (Button) n;
                if (b.getText().contains(seleccionado.nombre.split(" ")[0]) ||
                    (seleccionado == TipoCuerpo.AGUJERO_NEGRO && b.getText().contains("Agujero")) ||
                    (seleccionado == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO && b.getText().contains("SMBH"))) {
                    b.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    b.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white;");
                }
            }
        }
    }

    private void crearControlesSimulacion() {
        Button btnPlayPause = new Button("Play/Pause (Espacio)");
        btnPlayPause.setMaxWidth(Double.MAX_VALUE);
        btnPlayPause.setOnAction(e -> simulacion.togglePause());

        Button btnStep = new Button("Paso (S)");
        btnStep.setMaxWidth(Double.MAX_VALUE);
        btnStep.setOnAction(e -> simulacion.step());

        Button btnLimpiar = new Button("Limpiar Todo");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        btnLimpiar.setOnAction(e -> limpiarTodo());

        HBox velocidadBox = new HBox(5);
        Label lblVel = new Label("Velocidad:");
        lblVel.setTextFill(Color.WHITE);
        lblVel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        Slider sliderVel = new Slider(0.1, 5.0, 1.0);
        sliderVel.setPrefWidth(120);
        sliderVel.valueProperty().addListener((obs, o, n) ->
                simulacion.setVelocidadSimulacion(n.doubleValue()));

        velocidadBox.getChildren().addAll(lblVel, sliderVel);

        controlesBox.getChildren().addAll(btnPlayPause, btnStep, velocidadBox, btnLimpiar);
    }

    private void limpiarTodo() {
        // Confirmar
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Limpiar simulacion");
        alert.setHeaderText("Eliminar todos los cuerpos?");
        alert.setContentText("Se perdera el progreso actual.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                simulacion.getSistemaSolar().getCuerpos().clear();
                simulacion.getVista().getPane().getChildren().clear();
                simulacion.notificarEvento("Simulacion reiniciada.", org.example.game.simulacion.MensajeEvento.TipoMensaje.INFO);
            }
        });
    }

    private void actualizarRecursos() {
        recursosBox.getChildren().clear();

        for (TipoRecurso tr : TipoRecurso.values()) {
            double cant = inventario.getRecurso(tr);
            if (cant > 0 || tr == TipoRecurso.POBLACION || tr == TipoRecurso.CIENCIA || tr == TipoRecurso.MINERALES || tr == TipoRecurso.ENERGIA) {
                HBox row = new HBox(5);
                row.setAlignment(Pos.CENTER_LEFT);

                Label lblNombre = new Label(tr.nombre);
                lblNombre.setTextFill(Color.WHITE);
                lblNombre.setPrefWidth(120);
                lblNombre.setFont(Font.font("System", FontWeight.NORMAL, 11));

                Label lblCant = new Label(String.format("%.1f", cant));
                lblCant.setTextFill(Color.web(tr.getColorHex()));
                lblCant.setFont(Font.font("Monospace", FontWeight.BOLD, 12));

                row.getChildren().addAll(lblNombre, lblCant);
                recursosBox.getChildren().add(row);
            }
        }
    }

    private void actualizarInfoCosto() {
        if (tipoSeleccionado != null) {
            var costosEscalados = inventario.calcularCostoTotal(tipoSeleccionado, simulacion.getFactorMasaColocacion());
            if (!costosEscalados.isEmpty()) {
                StringBuilder sb = new StringBuilder("Costo (x" + String.format("%.1f", simulacion.getFactorMasaColocacion()) + "): ");
                for (var e : costosEscalados.entrySet()) {
                    double total = e.getValue();
                    double disponible = inventario.getRecurso(e.getKey());
                    String color = disponible >= total ? "#32CD32" : "#FF4444";
                    sb.append(e.getKey().nombre).append(": ")
                      .append(String.format("%.0f", total))
                      .append("  ");
                }
                lblInfoCosto.setText(sb.toString());
                lblInfoCosto.setTextFill(Color.web("#cccccc"));
            }
        } else {
            lblInfoCosto.setText("Selecciona un cuerpo para ver costo");
            lblInfoCosto.setTextFill(Color.web("#888888"));
        }
    }
}