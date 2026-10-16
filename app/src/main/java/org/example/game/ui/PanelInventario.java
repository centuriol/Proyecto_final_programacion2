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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final WidgetFactorMasa widgetMasa = new WidgetFactorMasa("FACTOR MASA:", 1.0, 0.0);
    private final Label lblInfoCosto = new Label("");
    private final Separator sep3 = new Separator();
    private final Label lblControles = new Label("CONTROLES");
    private final VBox controlesBox = new VBox(3);

    private final Map<TipoRecurso, HBox> mapaFilasRecursos = new java.util.EnumMap<>(TipoRecurso.class);
    private final Map<TipoRecurso, Label> mapaLabelsRecursos = new java.util.EnumMap<>(TipoRecurso.class);
    private final List<Button> listaBotonesSpawn = new ArrayList<>();
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
        lblControles.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblControles.setTextFill(Color.web("#00ffff"));

        // Slider masa reutilizable
        widgetMasa.setOnFactorCambiado(f -> {
            simulacion.setFactorMasaColocacion(f);
            actualizarInfoCosto();
        });

        // Inicializar estructura fija de filas de recursos para evitar recrear nodos en cada actualización
        for (TipoRecurso tr : TipoRecurso.values()) {
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER_LEFT);

            Label lblNombre = new Label(tr.nombre);
            lblNombre.setTextFill(Color.WHITE);
            lblNombre.setPrefWidth(120);
            lblNombre.setFont(Font.font("System", FontWeight.NORMAL, 11));

            Label lblCant = new Label("0.0");
            lblCant.setTextFill(Color.web(tr.getColorHex()));
            lblCant.setFont(Font.font("Monospace", FontWeight.BOLD, 12));

            row.getChildren().addAll(lblNombre, lblCant);
            mapaFilasRecursos.put(tr, row);
            mapaLabelsRecursos.put(tr, lblCant);
            recursosBox.getChildren().add(row);
        }

        // Botones de spawn
        crearBotonesSpawn();

        // Controles de simulación
        crearControlesSimulacion();

        getChildren().addAll(
                lblTitulo, recursosBox, sep1,
                lblSpawn, botonesSpawnBox, widgetMasa, lblInfoCosto, sep2,
                lblControles, controlesBox
        );

        // Actualizar periódicamente desacoplado de los ticks de física
        simulacion.agregarListenerUI(this::actualizarRecursos);
        actualizarRecursos();
    }

    private void crearBotonesSpawn() {
        // Sol
        Button btnEstrella = crearBotonSpawn("Sol", TipoCuerpo.ESTRELLA,
                "Crea el sol (fuente de gravedad y energia)");
        // Planeta Porro
        Button btnRocoso = crearBotonSpawn("Planeta Porro", TipoCuerpo.PLANETA_ROCOSO,
                "Planeta habitable, puede albergar civilizacion");
        // Luna
        Button btnLuna = crearBotonSpawn("Luna", TipoCuerpo.LUNA,
                "Satelite natural rocoso");
        // Planeta de Agua
        Button btnAgua = crearBotonSpawn("Planeta de Agua", TipoCuerpo.PLANETA_AGUA,
                "Planeta oceanico de bajo costo que produce minerales y ciencia");
        // Planeta Lava
        Button btnLava = crearBotonSpawn("Planeta Lava", TipoCuerpo.PLANETA_LAVA,
                "Planeta volcanico de alto rendimiento en minerales y ciencia");

        botonesSpawnBox.getChildren().addAll(
                btnEstrella, btnRocoso, btnLuna, btnAgua, btnLava
        );
    }

    private Button crearBotonSpawn(String texto, TipoCuerpo tipo, String tooltip) {
        Button btn = new Button(texto);
        javafx.scene.image.ImageView iv = org.example.game.render.GestorImagenes.crearImageView(tipo, 18, 18);
        if (iv != null) {
            btn.setGraphic(iv);
        }
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

        listaBotonesSpawn.add(btn);
        return btn;
    }

    private void actualizarSeleccionVisual(TipoCuerpo seleccionado) {
        for (Button b : listaBotonesSpawn) {
            if (b.getText().contains(seleccionado.nombre.split(" ")[0]) ||
                (seleccionado == TipoCuerpo.AGUJERO_NEGRO && b.getText().contains("Agujero")) ||
                (seleccionado == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO && b.getText().contains("SMBH"))) {
                b.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                b.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white;");
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

    public void actualizarRecursosParaPerfil() {
        actualizarRecursos();
    }

    private void actualizarRecursos() {
        for (TipoRecurso tr : TipoRecurso.values()) {
            double cant = inventario.getRecurso(tr);
            HBox row = mapaFilasRecursos.get(tr);
            Label lblCant = mapaLabelsRecursos.get(tr);
            if (row != null && lblCant != null) {
                boolean visible = cant > 0 || tr == TipoRecurso.POBLACION || tr == TipoRecurso.CIENCIA || tr == TipoRecurso.MINERALES || tr == TipoRecurso.ENERGIA;
                if (row.isVisible() != visible) {
                    row.setVisible(visible);
                    row.setManaged(visible);
                }
                if (visible) {
                    lblCant.setText(String.format(java.util.Locale.US, "%.1f", cant));
                }
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