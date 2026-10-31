package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Map;

/**
 * Ventana modal para configurar y colocar un nuevo cuerpo celeste en la simulacion.
 *
 * Principios SOLID:
 * - SRP: Encapsula el formulario de parametrizacion y balance economico previo a la colocacion.
 * - DIP: Depende de abstracciones y componentes modulares de interfaz.
 */
public class VentanaColocacion {

    private final TipoCuerpo tipo;
    private final SimulacionSolar simulacion;
    private final Window owner;
    private final Runnable onConfirmado;

    private Stage stage;
    private TextField txtNombre;
    private WidgetFactorMasa widgetMasa;
    private VBox listaCostos;
    private VBox listaProd;
    private Label lblEstado;
    private Button btnColocar;

    public VentanaColocacion(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        this.tipo = tipo;
        this.simulacion = simulacion;
        this.owner = owner;
        this.onConfirmado = onConfirmado;
    }

    public static void mostrar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        if (tipo == null) {
            return;
        }
        new VentanaColocacion(tipo, simulacion, owner, onConfirmado).abrir();
    }

    public void abrir() {
        stage = new Stage();
        VBox root = SeccionInfoCuerpo.inicializarDialogo(stage, "Configurar y Colocar: " + tipo.nombre, owner);

        HBox header = SeccionInfoCuerpo.crearHeader(
                SeccionInfoCuerpo.crearNodoIcono(tipo),
                tipo.nombre,
                SeccionInfoCuerpo.obtenerSubtitulo(tipo) + " - Configuracion de Colocacion"
        );

        VBox descBox = SeccionInfoCuerpo.crearSeccionDescripcion(tipo);
        VBox caracBox = SeccionInfoCuerpo.crearSeccionCaracteristicas(tipo);
        VBox formBox = construirFormulario();
        HBox econBox = construirFichaEconomica();
        lblEstado = construirEtiquetaEstado();
        HBox footer = construirFooter();

        actualizarDatosDinamicos();

        root.getChildren().addAll(header, new Separator(), descBox, caracBox, formBox, econBox, lblEstado, new Separator(), footer);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #13151f; -fx-background-color: #13151f;");

        Scene scene = new Scene(scroll, 580, 620);
        SeccionInfoCuerpo.vincularCierrePorEscape(scene, stage);

        stage.setScene(scene);
        stage.show();
    }

    private VBox construirFormulario() {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(10));
        formBox.setStyle(TemaUI.CSS_SECCION_CARD);

        VBox campoNombreBox = new VBox(4);
        Label lblNombrePrompt = new Label("NOMBRE DEL CUERPO:");
        lblNombrePrompt.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblNombrePrompt.setTextFill(TemaUI.COLOR_ACENTO_CYAN);

        txtNombre = new TextField(SeccionInfoCuerpo.obtenerNombrePorDefecto(tipo));
        txtNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        txtNombre.setStyle(TemaUI.CSS_TEXTFIELD);
        campoNombreBox.getChildren().addAll(lblNombrePrompt, txtNombre);

        VBox campoMasaBox = new VBox(4);
        double masaInicial = 1.0;
        if (simulacion != null) {
            masaInicial = Math.max(ConfiguracionSimulacion.MASA_FACTOR_MIN,
                    Math.min(ConfiguracionSimulacion.MASA_FACTOR_MAX, simulacion.getFactorMasaColocacion()));
        }
        double masaMax = (simulacion != null && simulacion.getProgresion() != null)
                ? simulacion.getProgresion().getLimiteMasaActual()
                : ConfiguracionSimulacion.MASA_FACTOR_MAX;

        widgetMasa = new WidgetFactorMasa("MASA DEL CUERPO:", masaInicial, tipo.masaBase, masaMax);
        if (tipo == TipoCuerpo.SATELITE) {
            Label lblSateliteFijo = new Label("ESTRUCTURA DE MASA FIJA: 1.0 t (No modificable)");
            lblSateliteFijo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblSateliteFijo.setTextFill(TemaUI.COLOR_ACENTO_CYAN);
            lblSateliteFijo.setPadding(new Insets(8));
            lblSateliteFijo.setStyle(TemaUI.CSS_SECCION_CARD);
            campoMasaBox.getChildren().add(lblSateliteFijo);
        } else {
            campoMasaBox.getChildren().add(widgetMasa);
            widgetMasa.setOnFactorCambiado(val -> actualizarDatosDinamicos());
        }
        formBox.getChildren().addAll(campoNombreBox, campoMasaBox);
        return formBox;
    }

    private HBox construirFichaEconomica() {
        HBox econBox = new HBox(12);
        econBox.setAlignment(Pos.CENTER);

        VBox costoBox = new VBox(6);
        costoBox.setPadding(new Insets(10));
        costoBox.setStyle(TemaUI.CSS_SECCION_CARD);
        HBox.setHgrow(costoBox, Priority.ALWAYS);

        Label lblCostoTitulo = new Label("MATERIALES PARA PONERLO");
        lblCostoTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblCostoTitulo.setTextFill(TemaUI.COLOR_ORO);
        listaCostos = new VBox(4);
        costoBox.getChildren().addAll(lblCostoTitulo, listaCostos);

        VBox prodBox = new VBox(6);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle(TemaUI.CSS_SECCION_CARD);
        HBox.setHgrow(prodBox, Priority.ALWAYS);

        Label lblProdTitulo = new Label("MATERIALES QUE TE DA POR TICK");
        lblProdTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblProdTitulo.setTextFill(TemaUI.COLOR_VERDE_EXITO);
        listaProd = new VBox(4);
        prodBox.getChildren().addAll(lblProdTitulo, listaProd);

        econBox.getChildren().addAll(costoBox, prodBox);
        return econBox;
    }

    private Label construirEtiquetaEstado() {
        Label lbl = new Label();
        lbl.setFont(Font.font("System", FontWeight.NORMAL, 11));
        lbl.setWrapText(true);
        return lbl;
    }

    private HBox construirFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCancelar.setStyle(TemaUI.CSS_BTN_SECUNDARIO);
        btnCancelar.setOnAction(e -> stage.close());

        btnColocar = new Button("Colocar en Orbita");
        btnColocar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnColocar.setStyle(TemaUI.CSS_BTN_PRIMARIO);
        btnColocar.setOnAction(e -> confirmarColocacion());

        footer.getChildren().addAll(btnCancelar, btnColocar);
        return footer;
    }

    private void actualizarDatosDinamicos() {
        double factor = (tipo == TipoCuerpo.SATELITE) ? 1.0 : widgetMasa.getFactorMasa();
        Map<TipoRecurso, Double> costosEscalados = InventarioJugador.getCostosEscalados(tipo, factor);
        SeccionInfoCuerpo.poblarListaCostos(listaCostos, costosEscalados);

        Map<TipoRecurso, Double> prodEscalada = InventarioJugador.getProduccionEscaladaPorTick(tipo, factor);
        SeccionInfoCuerpo.poblarListaProduccion(listaProd, prodEscalada);

        boolean puedeCrear = simulacion == null || simulacion.getInventario().puedeCrear(tipo, factor);
        if (!puedeCrear) {
            lblEstado.setText("Recursos insuficientes para colocar este cuerpo con esta masa.");
            lblEstado.setTextFill(TemaUI.COLOR_ROJO_PELIGRO);
            btnColocar.setDisable(true);
        } else {
            lblEstado.setText("Recursos disponibles. Haz clic en 'Colocar en Orbita' y situa el cuerpo con el mouse.");
            lblEstado.setTextFill(TemaUI.COLOR_VERDE_EXITO);
            btnColocar.setDisable(false);
        }
    }

    private void confirmarColocacion() {
        String nombreFinal = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (nombreFinal.isBlank()) {
            nombreFinal = SeccionInfoCuerpo.obtenerNombrePorDefecto(tipo);
        }
        double factorMasaFinal = (tipo == TipoCuerpo.SATELITE) ? 1.0 : widgetMasa.getFactorMasa();
        if (simulacion != null) {
            simulacion.entrarModoColocacion(tipo, factorMasaFinal, nombreFinal);
        }
        if (onConfirmado != null) {
            onConfirmado.run();
        }
        stage.close();
    }
}
