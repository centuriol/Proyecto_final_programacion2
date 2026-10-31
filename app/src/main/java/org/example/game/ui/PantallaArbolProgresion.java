package org.example.game.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Apartado de Árbol de Investigación Científica con nodos en círculos y disposición orbital ajustada a pantalla.
 *
 * Principios SOLID:
 * - SRP: Coordinador principal de la pantalla del árbol tecnológico. Delega el dibujo orbital en LienzoArbolOrbital
 *        y los detalles e investigación en InspectorTecnologiaArbol.
 * - DIP: Desacoplado de la simulación mediante listeners e interfaces de dominio.
 */
public class PantallaArbolProgresion extends StackPane {

    public record NodoArbol(int nivel, String tag, String nombre, String desc, double costo) {}

    private final SimulacionSolar simulacion;
    private final Runnable onCerrarCallback;

    private final Label lblCienciaPill;
    private final Label lblPlanetasPill;
    private final Label lblMasaPill;
    private final Button btnCerrar;

    private final LienzoArbolOrbital lienzoArbol;
    private final InspectorTecnologiaArbol inspectorPanel;
    private Runnable listenerSimulacion;

    public PantallaArbolProgresion(SimulacionSolar simulacion, Runnable onCerrarCallback) {
        this.simulacion = simulacion;
        this.onCerrarCallback = onCerrarCallback;

        setVisible(false);
        setManaged(false);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: rgba(5, 7, 14, 0.88);");

        VBox dialogCard = new VBox(8);
        dialogCard.setMaxWidth(1080);
        dialogCard.setMaxHeight(530);
        dialogCard.setPrefWidth(1080);
        dialogCard.setPrefHeight(530);
        dialogCard.setPadding(new Insets(10, 16, 10, 16));
        dialogCard.setStyle("-fx-background-color: #070911; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 28, 0, 0, 0);");

        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.P) {
                ocultar();
                e.consume();
            }
        });

        // Top bar
        BorderPane topBar = new BorderPane();

        VBox titulosBox = new VBox(1);
        titulosBox.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label("ÁRBOL DE INVESTIGACIÓN CIENTÍFICA");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 15));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label("Nodos circulares orbitales: Cuerpos, Masa y Capacidad. Desbloquea con Ciencia.");
        lblSubtitulo.setFont(Font.font("Monospace", FontWeight.NORMAL, 9.5));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));
        titulosBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        topBar.setLeft(titulosBox);

        HBox controlesDer = new HBox(8);
        controlesDer.setAlignment(Pos.CENTER_RIGHT);

        lblCienciaPill = new Label("Ciencia: 0.0 CIE");
        lblCienciaPill.setFont(Font.font("Monospace", FontWeight.BOLD, 10.5));
        lblCienciaPill.setPadding(new Insets(4, 9, 4, 9));
        lblCienciaPill.setStyle("-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.2px; -fx-background-radius: 4px;");

        lblPlanetasPill = new Label("Planetas: 0 / 3");
        lblPlanetasPill.setFont(Font.font("Monospace", FontWeight.BOLD, 10.5));
        lblPlanetasPill.setPadding(new Insets(4, 9, 4, 9));
        lblPlanetasPill.setStyle("-fx-background-color: #2b2617; -fx-text-fill: #ffd700; -fx-border-color: #ffd700; -fx-border-width: 1px; -fx-background-radius: 4px;");

        lblMasaPill = new Label("Masa Máx: 0.3x");
        lblMasaPill.setFont(Font.font("Monospace", FontWeight.BOLD, 10.5));
        lblMasaPill.setPadding(new Insets(4, 9, 4, 9));
        lblMasaPill.setStyle("-fx-background-color: #261a38; -fx-text-fill: #d2a8ff; -fx-border-color: #d2a8ff; -fx-border-width: 1px; -fx-background-radius: 4px;");

        btnCerrar = new Button("X");
        btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        btnCerrar.setStyle("-fx-background-color: #26161c; -fx-text-fill: #ff6b6b; -fx-border-color: #ff6b6b; -fx-border-width: 1.2px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
        btnCerrar.setOnAction(e -> ocultar());

        controlesDer.getChildren().addAll(lblCienciaPill, lblPlanetasPill, lblMasaPill, btnCerrar);
        topBar.setRight(controlesDer);

        // Componentes especializados (SRP)
        this.inspectorPanel = new InspectorTecnologiaArbol(simulacion, () -> {
            refrescar();
            simulacion.getBusEventos().notificarListenersUI();
        });

        this.lienzoArbol = new LienzoArbolOrbital(
                simulacion,
                inspectorPanel::seleccionar,
                () -> {
                    refrescar();
                    simulacion.getBusEventos().notificarListenersUI();
                }
        );

        ScrollPane scroll = new ScrollPane(lienzoArbol);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        HBox areaCentral = new HBox(12);
        areaCentral.setAlignment(Pos.CENTER);
        VBox.setVgrow(areaCentral, Priority.ALWAYS);
        areaCentral.getChildren().addAll(scroll, inspectorPanel);

        dialogCard.getChildren().addAll(topBar, new Separator(), areaCentral);
        getChildren().add(dialogCard);
    }

    public void alternar() {
        if (isVisible()) {
            ocultar();
        } else {
            mostrar();
        }
    }

    public void mostrar() {
        setVisible(true);
        setManaged(true);
        toFront();
        requestFocus();

        refrescar();

        if (listenerSimulacion == null) {
            listenerSimulacion = () -> Platform.runLater(() -> {
                if (isVisible()) {
                    actualizarPills();
                    var inv = simulacion.getInventario();
                    double c = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;
                    lienzoArbol.actualizarEstadosBotones(c);
                    inspectorPanel.actualizar();
                }
            });
            simulacion.getBusEventos().agregarListenerUI(listenerSimulacion);
        }
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);

        if (listenerSimulacion != null) {
            simulacion.getBusEventos().removerListenerUI(listenerSimulacion);
            listenerSimulacion = null;
        }

        if (onCerrarCallback != null) {
            onCerrarCallback.run();
        }
    }

    public Button getBtnCerrar() {
        return btnCerrar;
    }

    public InspectorTecnologiaArbol getInspectorPanel() {
        return inspectorPanel;
    }

    public LienzoArbolOrbital getLienzoArbol() {
        return lienzoArbol;
    }

    private void actualizarPills() {
        double ciencia = simulacion.getInventario() != null ? simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA) : 0.0;
        lblCienciaPill.setText("Ciencia: " + String.format("%.1f", ciencia) + " CIE");
        lblPlanetasPill.setText("Planetas: " + simulacion.getGestorCicloVida().getCantidadPlanetasActuales() + " / " + simulacion.getProgresion().getLimitePlanetas());
        lblMasaPill.setText("Masa Máx: " + String.format("%.1f", simulacion.getProgresion().getLimiteMasaActual()) + "x");
    }

    public void refrescar() {
        actualizarPills();
        lienzoArbol.refrescar();

        if (inspectorPanel.getNodoSeleccionado() != null) {
            inspectorPanel.actualizar();
        } else {
            inspectorPanel.seleccionar(RamaProgresion.PLANETAS, LienzoArbolOrbital.NODOS_PLANETAS[0]);
        }
    }
}
