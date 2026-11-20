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
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Apartado de Árbol de Investigación Científica con disposición neuronal (SRP / DIP).
 */
public class PantallaArbolProgresion extends StackPane {

    public record NodoArbol(int nivel, String tag, String nombre, String desc, double costo) {}

    private static class ComponentesArbol {
        final SimulacionSolar simulacion;
        final Runnable onCerrarCallback;
        final BarraResumenProgresion barraResumen = new BarraResumenProgresion(true);
        final Button btnCerrar = new Button("X");
        final LienzoArbolOrbital lienzoArbol;
        final InspectorTecnologiaArbol inspectorPanel;
        Runnable listenerSimulacion;

        ComponentesArbol(SimulacionSolar sim, Runnable onCerrar, Runnable onRefrescar) {
            this.simulacion = sim;
            this.onCerrarCallback = onCerrar;
            Runnable accionInvestigado = () -> {
                onRefrescar.run();
                sim.getBusEventos().notificarListenersUI();
            };
            this.inspectorPanel = new InspectorTecnologiaArbol(sim, accionInvestigado);
            this.lienzoArbol = new LienzoArbolOrbital(
                    sim, inspectorPanel::seleccionar, inspectorPanel::seleccionarSecundaria,
                    accionInvestigado, new ConstructorOrbeNodoArbol()
            );
        }

        void desuscribir() {
            if (listenerSimulacion != null) {
                simulacion.getBusEventos().removerListenerUI(listenerSimulacion);
                listenerSimulacion = null;
            }
        }
    }

    private final ComponentesArbol comp;

    public PantallaArbolProgresion(SimulacionSolar simulacion, Runnable onCerrarCallback) {
        this.comp = new ComponentesArbol(simulacion, onCerrarCallback, this::refrescar);

        setVisible(false);
        setManaged(false);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: rgba(5, 7, 14, 0.88);");
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.P) {
                ocultar();
                e.consume();
            }
        });

        getChildren().add(construirTarjetaDialogo());
    }

    private VBox construirTarjetaDialogo() {
        VBox dialogCard = new VBox(8);
        dialogCard.setMaxWidth(1080);
        dialogCard.setMaxHeight(530);
        dialogCard.setPrefWidth(1080);
        dialogCard.setPrefHeight(530);
        dialogCard.setPadding(new Insets(10, 16, 10, 16));
        dialogCard.setStyle("-fx-background-color: #070911; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 28, 0, 0, 0);");

        ScrollPane scroll = new ScrollPane(comp.lienzoArbol);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        HBox areaCentral = new HBox(12, scroll, comp.inspectorPanel);
        areaCentral.setAlignment(Pos.CENTER);
        VBox.setVgrow(areaCentral, Priority.ALWAYS);

        dialogCard.getChildren().addAll(construirTopBar(), new Separator(), areaCentral);
        return dialogCard;
    }

    private BorderPane construirTopBar() {
        BorderPane topBar = new BorderPane();
        Label lblTitulo = new Label("ÁRBOL NEURONAL DE INVESTIGACIÓN CIENTÍFICA");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 15));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label("Soma central con 3 dendritas (Cuerpos, Masa, Capacidad) y sinapsis secundarias opcionales.");
        lblSubtitulo.setFont(Font.font("Monospace", FontWeight.NORMAL, 9.5));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));

        VBox titulosBox = new VBox(1, lblTitulo, lblSubtitulo);
        titulosBox.setAlignment(Pos.CENTER_LEFT);
        topBar.setLeft(titulosBox);

        comp.btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        comp.btnCerrar.setStyle("-fx-background-color: #26161c; -fx-text-fill: #ff6b6b; -fx-border-color: #ff6b6b; -fx-border-width: 1.2px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
        comp.btnCerrar.setOnAction(e -> ocultar());

        HBox controlesDer = new HBox(8, comp.barraResumen, comp.btnCerrar);
        controlesDer.setAlignment(Pos.CENTER_RIGHT);
        topBar.setRight(controlesDer);
        return topBar;
    }

    public void alternar() {
        if (isVisible()) ocultar();
        else mostrar();
    }

    public void mostrar() {
        setVisible(true);
        setManaged(true);
        toFront();
        requestFocus();
        refrescar();

        if (comp.listenerSimulacion == null) {
            comp.listenerSimulacion = () -> Platform.runLater(() -> {
                if (isVisible()) {
                    double ciencia = comp.barraResumen.actualizar(comp.simulacion);
                    comp.lienzoArbol.actualizarEstadosBotones(ciencia);
                    comp.inspectorPanel.actualizar();
                }
            });
            comp.simulacion.getBusEventos().agregarListenerUI(comp.listenerSimulacion);
        }
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);
        comp.desuscribir();
        if (comp.onCerrarCallback != null) comp.onCerrarCallback.run();
    }

    public void refrescar() {
        comp.barraResumen.actualizar(comp.simulacion);
        comp.lienzoArbol.refrescar();
        if (comp.inspectorPanel.getNodoSeleccionado() != null || comp.inspectorPanel.getMejoraSecundariaSeleccionada() != null) {
            comp.inspectorPanel.actualizar();
        } else {
            comp.inspectorPanel.seleccionar(RamaProgresion.PLANETAS, LienzoArbolOrbital.NODOS_PLANETAS[0]);
        }
    }

    public Button getBtnCerrar() { return comp.btnCerrar; }
    public InspectorTecnologiaArbol getInspectorPanel() { return comp.inspectorPanel; }
    public LienzoArbolOrbital getLienzoArbol() { return comp.lienzoArbol; }
}
