package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.CuerpoCeleste;
import org.example.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Map;

/**
 * Ventana emergente interactiva para:
 * 1. Configurar y colocar un nuevo cuerpo celeste (nombre, masa seleccionable,
 *    cálculo dinámico en tiempo real de costos y producción por tick).
 * 2. Inspeccionar la información y lore de un cuerpo celeste existente en el sistema.
 */
public class VentanaDescripcionCuerpo {

    /**
     * Abre la ventana interactiva para configurar y colocar un nuevo cuerpo celeste.
     * Permite elegir el nombre, regular la masa mediante un slider, y ver en tiempo real
     * cómo escalan los costos de materiales necesarios y la producción por tick generada.
     *
     * @param tipo Tipo de cuerpo celeste a colocar.
     * @param simulacion Instancia de la simulación activa.
     * @param owner Ventana propietaria (Stage principal).
     * @param onConfirmado Callback ejecutado tras confirmar la colocación.
     */
    public static void mostrarParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        if (tipo == null) return;

        Stage stage = new Stage();
        stage.setTitle("Configurar y Colocar: " + tipo.nombre);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.NONE);

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #13151f; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

        // 1. Cabecera: Icono y Nombre del Tipo
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblIcono = new Label(obtenerIcono(tipo));
        lblIcono.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        lblIcono.setTextFill(Color.web(tipo.getColorHexString()));

        VBox titulosBox = new VBox(2);
        Label lblTitulo = new Label(tipo.nombre.toUpperCase());
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label(obtenerSubtitulo(tipo) + " • Configuración de Colocación");
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));

        titulosBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        header.getChildren().addAll(lblIcono, titulosBox);

        // 2. Descripción narrativa (Lore del ítem)
        VBox descBox = new VBox(4);
        Label lblDescTitulo = new Label("DESCRIPCIÓN DEL CUERPO");
        lblDescTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDescTitulo.setTextFill(Color.web("#5be3ff"));

        Label lblDescripcion = new Label(obtenerLore(tipo));
        lblDescripcion.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblDescripcion.setTextFill(Color.web("#d1d5db"));
        lblDescripcion.setWrapText(true);
        lblDescripcion.setLineSpacing(2);

        descBox.getChildren().addAll(lblDescTitulo, lblDescripcion);

        // 3. Formulario de Personalización: Nombre y Masa
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(10));
        formBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // Campo de Nombre
        VBox campoNombreBox = new VBox(4);
        Label lblNombrePrompt = new Label("NOMBRE DEL CUERPO:");
        lblNombrePrompt.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblNombrePrompt.setTextFill(Color.web("#5be3ff"));

        TextField txtNombre = new TextField(obtenerNombrePorDefecto(tipo));
        txtNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        txtNombre.setStyle("-fx-background-color: #0d0e15; -fx-text-fill: #ffffff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-padding: 5 8 5 8;");
        campoNombreBox.getChildren().addAll(lblNombrePrompt, txtNombre);

        // Selector de Masa
        VBox campoMasaBox = new VBox(4);
        HBox masaLabelBox = new HBox(10);
        masaLabelBox.setAlignment(Pos.CENTER_LEFT);

        Label lblMasaPrompt = new Label("MASA DEL CUERPO:");
        lblMasaPrompt.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblMasaPrompt.setTextFill(Color.web("#5be3ff"));

        double masaInicial = 1.0;
        if (simulacion != null) {
            masaInicial = Math.max(ConfiguracionSimulacion.MASA_FACTOR_MIN,
                    Math.min(ConfiguracionSimulacion.MASA_FACTOR_MAX, simulacion.getFactorMasaColocacion()));
        }

        Label lblMasaVal = new Label(String.format("%.1fx (%.2e kg)", masaInicial, tipo.masaBase * masaInicial));
        lblMasaVal.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lblMasaVal.setTextFill(Color.web("#ffd700"));

        masaLabelBox.getChildren().addAll(lblMasaPrompt, lblMasaVal);

        Slider sliderMasa = new Slider(
                ConfiguracionSimulacion.MASA_FACTOR_MIN,
                ConfiguracionSimulacion.MASA_FACTOR_MAX,
                masaInicial
        );
        sliderMasa.setShowTickMarks(true);
        sliderMasa.setMajorTickUnit(1.0);
        sliderMasa.setMinorTickCount(1);
        sliderMasa.setBlockIncrement(0.5);

        campoMasaBox.getChildren().addAll(masaLabelBox, sliderMasa);
        formBox.getChildren().addAll(campoNombreBox, campoMasaBox);

        // 4. Ficha Técnica Dinámica: Costos y Producción Escalados
        HBox econBox = new HBox(12);
        econBox.setAlignment(Pos.CENTER);

        // Columna Izquierda: Costos escalados
        VBox costoBox = new VBox(6);
        costoBox.setPadding(new Insets(10));
        costoBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        HBox.setHgrow(costoBox, Priority.ALWAYS);

        Label lblCostoTitulo = new Label("MATERIALES PARA PONERLO");
        lblCostoTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblCostoTitulo.setTextFill(Color.web("#ffd700"));

        VBox listaCostos = new VBox(4);
        costoBox.getChildren().addAll(lblCostoTitulo, listaCostos);

        // Columna Derecha: Producción por tick escalada
        VBox prodBox = new VBox(6);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        HBox.setHgrow(prodBox, Priority.ALWAYS);

        Label lblProdTitulo = new Label("MATERIALES QUE TE DA POR TICK");
        lblProdTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblProdTitulo.setTextFill(Color.web("#50fa7b"));

        VBox listaProd = new VBox(4);
        prodBox.getChildren().addAll(lblProdTitulo, listaProd);

        econBox.getChildren().addAll(costoBox, prodBox);

        // 5. Mensaje de Estado / Asequibilidad
        Label lblEstado = new Label();
        lblEstado.setFont(Font.font("System", FontWeight.NORMAL, 11));
        lblEstado.setWrapText(true);

        // 6. Botones de Acción (Cancelar y Colocar en Órbita)
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCancelar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #8c92a4; -fx-border-color: #3b3d4f; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6 16 6 16;");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnColocar = new Button("Colocar en Órbita");
        btnColocar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnColocar.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 18 6 18;");

        footer.getChildren().addAll(btnCancelar, btnColocar);

        // Lógica de Actualización Dinámica
        Runnable actualizarDatosDinamicos = () -> {
            double factorMasa = sliderMasa.getValue();
            lblMasaVal.setText(String.format("%.1fx (%.2e kg)", factorMasa, tipo.masaBase * factorMasa));

            // Actualizar lista de costos
            listaCostos.getChildren().clear();
            Map<TipoRecurso, Double> costosEscalados = InventarioJugador.getCostosEscalados(tipo, factorMasa);

            boolean puedePagarTodo = true;
            InventarioJugador inv = simulacion != null ? simulacion.getInventario() : null;

            if (costosEscalados.isEmpty()) {
                Label lblSinCosto = new Label("Sin costo de colocación");
                lblSinCosto.setFont(Font.font("System", FontWeight.NORMAL, 11));
                lblSinCosto.setTextFill(Color.web("#8c92a4"));
                listaCostos.getChildren().add(lblSinCosto);
            } else {
                for (Map.Entry<TipoRecurso, Double> e : costosEscalados.entrySet()) {
                    TipoRecurso tr = e.getKey();
                    double requerido = e.getValue();
                    double disponible = inv != null ? inv.getRecurso(tr) : 0;
                    boolean alcanza = disponible >= requerido;
                    if (!alcanza) puedePagarTodo = false;

                    Label lblItem = new Label(String.format("• %s: %.0f (Tienes: %.0f)",
                            tr.nombre, requerido, disponible));
                    lblItem.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
                    lblItem.setTextFill(Color.web(alcanza ? tr.getColorHex() : "#ff5555"));
                    listaCostos.getChildren().add(lblItem);
                }
            }

            // Actualizar lista de producción
            listaProd.getChildren().clear();
            Map<TipoRecurso, Double> prodsEscaladas = InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa);
            if (prodsEscaladas.isEmpty()) {
                Label lblSinProd = new Label("No genera recursos pasivos");
                lblSinProd.setFont(Font.font("System", FontWeight.NORMAL, 11));
                lblSinProd.setTextFill(Color.web("#8c92a4"));
                listaProd.getChildren().add(lblSinProd);
            } else {
                for (Map.Entry<TipoRecurso, Double> e : prodsEscaladas.entrySet()) {
                    Label lblItem = new Label(String.format("• %s: +%.2f / tick", e.getKey().nombre, e.getValue()));
                    lblItem.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
                    lblItem.setTextFill(Color.web(e.getKey().getColorHex()));
                    listaProd.getChildren().add(lblItem);
                }
            }

            // Actualizar estado del botón de colocación y mensaje
            if (inv != null && !inv.puedeCrear(tipo, factorMasa)) {
                lblEstado.setText("⚠ Recursos insuficientes para este tamaño de cuerpo.");
                lblEstado.setTextFill(Color.web("#ff5555"));
                btnColocar.setDisable(true);
                btnColocar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #555866; -fx-border-color: #3b3d4f; -fx-border-width: 1px; -fx-padding: 6 18 6 18;");
            } else {
                lblEstado.setText("✓ Recursos disponibles. Haz clic en 'Colocar en Órbita' y sitúa el cuerpo con el mouse.");
                lblEstado.setTextFill(Color.web("#50fa7b"));
                btnColocar.setDisable(false);
                btnColocar.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 18 6 18;");
            }
        };

        sliderMasa.valueProperty().addListener((obs, old, neu) -> actualizarDatosDinamicos.run());
        actualizarDatosDinamicos.run();

        // Acción confirmar colocación
        btnColocar.setOnAction(e -> {
            String nombreFinal = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
            if (nombreFinal.isBlank()) {
                nombreFinal = obtenerNombrePorDefecto(tipo);
            }
            double factorMasaFinal = sliderMasa.getValue();

            if (simulacion != null) {
                simulacion.entrarModoColocacion(tipo, factorMasaFinal, nombreFinal);
            }

            if (onConfirmado != null) {
                onConfirmado.run();
            }
            stage.close();
        });

        root.getChildren().addAll(header, new Separator(), descBox, formBox, econBox, lblEstado, new Separator(), footer);

        Scene scene = new Scene(root, 560, 560);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Muestra la ventana de información y descripción completa para un cuerpo celeste existente en el juego.
     * Se abre al hacer click derecho sobre un planeta, estrella o luna en el lienzo de simulación.
     */
    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, Window owner) {
        if (cuerpo == null) return;
        TipoCuerpo tipo = cuerpo.getTipoCuerpo();
        if (tipo == null) return;

        Stage stage = new Stage();
        stage.setTitle("Inspeccionar: " + cuerpo.getNombre());
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.NONE);

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #13151f; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

        // Cabecera: Icono y Nombre del cuerpo
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblIcono = new Label(obtenerIcono(tipo));
        lblIcono.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        lblIcono.setTextFill(Color.web(tipo.getColorHexString()));

        VBox titulosBox = new VBox(2);
        Label lblTitulo = new Label(cuerpo.getNombre().toUpperCase());
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label(tipo.nombre + " • " + obtenerSubtitulo(tipo));
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));

        titulosBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        header.getChildren().addAll(lblIcono, titulosBox);

        // Lore
        VBox descBox = new VBox(4);
        Label lblDescTitulo = new Label("DESCRIPCIÓN DEL CUERPO");
        lblDescTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDescTitulo.setTextFill(Color.web("#5be3ff"));

        Label lblDescripcion = new Label(obtenerLore(tipo));
        lblDescripcion.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblDescripcion.setTextFill(Color.web("#d1d5db"));
        lblDescripcion.setWrapText(true);
        lblDescripcion.setLineSpacing(2);

        descBox.getChildren().addAll(lblDescTitulo, lblDescripcion);

        // Datos Físicos Reales
        VBox datosFisicos = new VBox(4);
        datosFisicos.setPadding(new Insets(10));
        datosFisicos.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        Label lblDatosTitulo = new Label("TELEMETRÍA Y PROPIEDADES FÍSICAS");
        lblDatosTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDatosTitulo.setTextFill(Color.web("#ffd700"));

        Label lblMasa = new Label(String.format("• Masa: %.2e kg", cuerpo.getMasa()));
        lblMasa.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblMasa.setTextFill(Color.web("#d1d5db"));

        Label lblVel = new Label(String.format("• Velocidad: %.2f km/s", cuerpo.getVelocidad().magnitud()));
        lblVel.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblVel.setTextFill(Color.web("#d1d5db"));

        Label lblPos = new Label(String.format("• Posición: (%.1f, %.1f)", cuerpo.getPosicionX(), cuerpo.getPosicionY()));
        lblPos.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblPos.setTextFill(Color.web("#d1d5db"));

        datosFisicos.getChildren().addAll(lblDatosTitulo, lblMasa, lblVel, lblPos);

        // Producción actual del cuerpo
        VBox prodBox = new VBox(4);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        Label lblProdTitulo = new Label("GENERACIÓN ACTUAL POR TICK");
        lblProdTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblProdTitulo.setTextFill(Color.web("#50fa7b"));
        prodBox.getChildren().add(lblProdTitulo);

        double factorMasa = tipo.masaBase > 0 ? Math.max(0.1, cuerpo.getMasa() / tipo.masaBase) : 1.0;
        Map<TipoRecurso, Double> prod = InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa);
        if (prod.isEmpty()) {
            Label lblSinProd = new Label("No genera recursos pasivos");
            lblSinProd.setFont(Font.font("System", FontWeight.NORMAL, 11));
            lblSinProd.setTextFill(Color.web("#8c92a4"));
            prodBox.getChildren().add(lblSinProd);
        } else {
            for (Map.Entry<TipoRecurso, Double> e : prod.entrySet()) {
                Label lblItem = new Label(String.format("• %s: +%.2f / tick", e.getKey().nombre, e.getValue()));
                lblItem.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
                lblItem.setTextFill(Color.web(e.getKey().getColorHex()));
                prodBox.getChildren().add(lblItem);
            }
        }

        // Civilización si es planeta
        if (cuerpo instanceof Planeta p && p.tieneCivilizacion() && p.getCivilizacion() != null) {
            VBox civBox = new VBox(4);
            civBox.setPadding(new Insets(10));
            civBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

            Label lblCivTitulo = new Label("CIVILIZACIÓN PLANETARIA");
            lblCivTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblCivTitulo.setTextFill(Color.web("#5be3ff"));

            Label lblPob = new Label(String.format("• Población: %.0f habitantes", p.getCivilizacion().getPoblacion()));
            lblPob.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblPob.setTextFill(Color.web("#d1d5db"));

            Label lblEst = new Label("• Estado: " + p.getCivilizacion().getEstado().name());
            lblEst.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblEst.setTextFill(Color.web("#50fa7b"));

            civBox.getChildren().addAll(lblCivTitulo, lblPob, lblEst);
            datosFisicos.getChildren().add(civBox);
        }

        // Botón Cerrar
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCerrar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6 18 6 18;");
        btnCerrar.setOnAction(e -> stage.close());
        footer.getChildren().add(btnCerrar);

        root.getChildren().addAll(header, new Separator(), descBox, datosFisicos, prodBox, new Separator(), footer);

        Scene scene = new Scene(root, 520, 520);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Muestra la ventana en modo descripción informativa básica (compatibilidad hacia atrás).
     */
    public static void mostrar(TipoCuerpo tipo, Window owner) {
        mostrarParaColocar(tipo, null, owner, null);
    }

    public static String obtenerNombrePorDefecto(TipoCuerpo tipo) {
        if (tipo == null) return "Cuerpo";
        return switch (tipo) {
            case ESTRELLA -> "Sol Central";
            case PLANETA_ROCOSO -> "Terra Nova";
            case PLANETA_GASEOSO -> "Joviano";
            case PLANETA_HELADO -> "Boreas";
            case LUNA -> "Selene";
            case SATELITE -> "Sonda-1";
            case ESCUDO_DOME -> "Aegis";
            case METEORITO -> "Bólido";
            case AGUJERO_NEGRO -> "Singularidad";
            case AGUJERO_NEGRO_SUPERMASIVO -> "Leviatán";
            default -> tipo.nombre != null ? tipo.nombre : "Cuerpo";
        };
    }

    public static String obtenerIcono(TipoCuerpo tipo) {
        if (tipo == null) return "[???]";
        return switch (tipo) {
            case ESTRELLA -> "[EST]";
            case PLANETA_ROCOSO -> "[ROC]";
            case LUNA -> "[LUN]";
            case PLANETA_GASEOSO -> "[GAS]";
            case PLANETA_HELADO -> "[HEL]";
            case SATELITE -> "[SAT]";
            case ESCUDO_DOME -> "[ESC]";
            case METEORITO -> "[MET]";
            case AGUJERO_NEGRO -> "[BH]";
            case AGUJERO_NEGRO_SUPERMASIVO -> "[SMBH]";
            default -> "[???]";
        };
    }

    public static String obtenerSubtitulo(TipoCuerpo tipo) {
        if (tipo == null) return "Objeto Astronómico";
        return switch (tipo) {
            case ESTRELLA -> "Estrella Central • Reactor Termonuclear Natural";
            case PLANETA_ROCOSO, PLANETA_GASEOSO, PLANETA_HELADO -> "Cuerpo Planetario • Fuente de Población y Ciencia";
            case LUNA -> "Satélite Natural • Cantera de Extracción Mineral";
            default -> "Objeto Astronómico";
        };
    }

    public static String obtenerLore(TipoCuerpo tipo) {
        if (tipo == null) return "Cuerpo celeste en el sistema.";
        return switch (tipo) {
            case ESTRELLA ->
                "Una colosal masa de plasma autosostenida por equilibrio hidrostático. En su núcleo ardiente se desarrollan procesos de fusión que emiten inmensos torrentes de radiación, convirtiéndola en la generadora primaria de energía para todo el sistema solar. Sus ráfagas y vientos estelares cargados de iones pesados dispersan además minerales útiles a través del espacio.";
            case PLANETA_ROCOSO, PLANETA_GASEOSO, PLANETA_HELADO ->
                "Un mundo geológicamente diferenciado provisto de corteza densa, atmósfera y capas ricas en compuestos pesados. Alberga biomas y condiciones idóneas para el desarrollo de vida organizada. A medida que su población colonizadora prolifera, se establecen academias de ciencia para la investigación e industrias mineras que extraen minerales valiosos para la expansión.";
            case LUNA ->
                "Un satélite natural sólido y estéril que gira en torno a su cuerpo huésped. Su superficie no erosionada conserva el registro intacto de miles de millones de años de bombardeo astronómico, concentrando depósitos superficiales de minerales puros. Constituye una cantera inagotable y segura para la extracción de minerales brutos sin perturbar biosferas.";
            default ->
                "Cuerpo celeste en órbita dentro del sistema.";
        };
    }
}
