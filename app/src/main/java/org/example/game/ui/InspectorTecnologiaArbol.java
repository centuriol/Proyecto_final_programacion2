package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Panel lateral de inspección y acción para el Árbol de Investigación Científica (SRP).
 * Principios SOLID:
 * - SRP: Responsable exclusivo de presentar el detalle del nodo tecnológico seleccionado y ejecutar su desbloqueo.
 */
public class InspectorTecnologiaArbol extends VBox {

    private final SimulacionSolar simulacion;
    private final Runnable onInvestigadoCallback;

    private final Label lblInspRama;
    private final Label lblInspTitulo;
    private final StackPane inspCirclePreview;
    private final Label lblInspTagPreview;
    private final Label lblInspDesc;
    private final Label lblInspCosto;
    private final ProgressBar inspBarraCiencia;
    private final Label lblInspProgresoCiencia;
    private final Button btnInspInvestigar;

    private PantallaArbolProgresion.NodoArbol nodoSeleccionado = null;
    private RamaProgresion ramaSeleccionada = null;

    public InspectorTecnologiaArbol(SimulacionSolar simulacion, Runnable onInvestigadoCallback) {
        super(8);
        this.simulacion = simulacion;
        this.onInvestigadoCallback = onInvestigadoCallback;

        setPrefWidth(260);
        setMinWidth(260);
        setMaxWidth(260);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #0f121d; -fx-border-color: #2b3149; -fx-border-width: 1.5px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

        lblInspRama = new Label("RAMA TECNOLÓGICA");
        lblInspRama.setFont(Font.font("Monospace", FontWeight.BOLD, 9.5));
        lblInspRama.setTextFill(Color.web("#8c92a4"));

        lblInspTitulo = new Label("Selecciona un Nodo");
        lblInspTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        lblInspTitulo.setTextFill(Color.web("#e8e4d8"));
        lblInspTitulo.setWrapText(true);

        inspCirclePreview = new StackPane();
        inspCirclePreview.setPrefSize(58, 58);
        inspCirclePreview.setMaxSize(58, 58);
        inspCirclePreview.setStyle("-fx-background-color: #172a38; -fx-background-radius: 50%; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-border-radius: 50%;");

        lblInspTagPreview = new Label("[?]");
        lblInspTagPreview.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        lblInspTagPreview.setTextFill(Color.web("#5be3ff"));
        inspCirclePreview.getChildren().add(lblInspTagPreview);

        lblInspDesc = new Label("Haz clic sobre cualquier círculo del árbol para inspeccionar sus características, beneficios y coste.");
        lblInspDesc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        lblInspDesc.setTextFill(Color.web("#a0a5b5"));
        lblInspDesc.setWrapText(true);
        VBox.setVgrow(lblInspDesc, Priority.ALWAYS);

        VBox costoBox = new VBox(3);
        lblInspCosto = new Label("Costo: -- CIE");
        lblInspCosto.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lblInspCosto.setTextFill(Color.web("#ffd700"));

        inspBarraCiencia = new ProgressBar(0.0);
        inspBarraCiencia.setMaxWidth(Double.MAX_VALUE);
        inspBarraCiencia.setPrefHeight(10);
        inspBarraCiencia.setStyle("-fx-accent: #5be3ff;");

        lblInspProgresoCiencia = new Label("Ciencia: -- / --");
        lblInspProgresoCiencia.setFont(Font.font("Monospace", FontWeight.NORMAL, 9.5));
        lblInspProgresoCiencia.setTextFill(Color.web("#8c92a4"));
        costoBox.getChildren().addAll(lblInspCosto, inspBarraCiencia, lblInspProgresoCiencia);

        btnInspInvestigar = new Button("Investigar Tecnología");
        btnInspInvestigar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnInspInvestigar.setMaxWidth(Double.MAX_VALUE);
        btnInspInvestigar.setDisable(true);
        btnInspInvestigar.setStyle("-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 6 10 6 10;");

        btnInspInvestigar.setOnAction(e -> ejecutarInvestigacion());

        getChildren().addAll(lblInspRama, lblInspTitulo, inspCirclePreview, new Separator(), lblInspDesc, costoBox, btnInspInvestigar);
    }

    public void seleccionar(RamaProgresion rama, PantallaArbolProgresion.NodoArbol nodo) {
        this.ramaSeleccionada = rama;
        this.nodoSeleccionado = nodo;
        actualizar();
    }

    public void actualizar() {
        if (nodoSeleccionado == null || ramaSeleccionada == null) return;

        var prog = simulacion.getProgresion();
        var inv = simulacion.getInventario();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;
        int nivelActual = prog.getNivel(ramaSeleccionada);

        lblInspRama.setText("RAMA: " + ramaSeleccionada.name());
        lblInspTitulo.setText(nodoSeleccionado.nombre());
        lblInspTagPreview.setText(nodoSeleccionado.tag());
        lblInspDesc.setText(nodoSeleccionado.desc());

        if (nodoSeleccionado.nivel() <= nivelActual) {
            lblInspCosto.setText("Estado: ¡INVESTIGADO!");
            lblInspCosto.setTextFill(Color.web("#50fa7b"));
            inspBarraCiencia.setProgress(1.0);
            lblInspProgresoCiencia.setText("Tecnología activa en el sistema.");
            btnInspInvestigar.setDisable(true);
            btnInspInvestigar.setText("Tecnología Desbloqueada");
            btnInspInvestigar.setStyle("-fx-background-color: #102619; -fx-text-fill: #50fa7b; -fx-border-color: #50fa7b; -fx-border-width: 1.2px; -fx-padding: 6 10 6 10;");
            inspCirclePreview.setStyle("-fx-background-color: #102619; -fx-background-radius: 50%; -fx-border-color: #50fa7b; -fx-border-width: 2.5px; -fx-border-radius: 50%;");
            lblInspTagPreview.setTextFill(Color.web("#50fa7b"));
        } else if (nodoSeleccionado.nivel() == nivelActual + 1) {
            lblInspCosto.setText(String.format("Costo: %.0f CIE", nodoSeleccionado.costo()));
            lblInspCosto.setTextFill(Color.web("#ffd700"));

            double progreso = Math.min(1.0, ciencia / nodoSeleccionado.costo());
            inspBarraCiencia.setProgress(progreso);
            lblInspProgresoCiencia.setText(String.format("Ciencia: %.1f / %.0f CIE", ciencia, nodoSeleccionado.costo()));

            boolean puedePagar = ciencia >= nodoSeleccionado.costo();
            btnInspInvestigar.setDisable(!puedePagar);
            btnInspInvestigar.setText(puedePagar ? "INVESTIGAR AHORA" : "Ciencia Insuficiente");
            btnInspInvestigar.setStyle(puedePagar
                    ? "-fx-background-color: #1e3a5f; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 10 6 10; -fx-background-radius: 4px; -fx-border-radius: 4px;"
                    : "-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 6 10 6 10; -fx-background-radius: 4px; -fx-border-radius: 4px;");
            inspCirclePreview.setStyle("-fx-background-color: #1a233a; -fx-background-radius: 50%; -fx-border-color: #ffd700; -fx-border-width: 2.5px; -fx-border-radius: 50%;");
            lblInspTagPreview.setTextFill(Color.web("#ffd700"));
        } else {
            lblInspCosto.setText(String.format("Bloqueado (Requiere Nivel %d)", nodoSeleccionado.nivel() - 1));
            lblInspCosto.setTextFill(Color.web("#777777"));
            inspBarraCiencia.setProgress(0.0);
            lblInspProgresoCiencia.setText("Investiga primero la tecnología previa.");
            btnInspInvestigar.setDisable(true);
            btnInspInvestigar.setText("Tecnología Bloqueada");
            btnInspInvestigar.setStyle("-fx-background-color: #141620; -fx-text-fill: #555555; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-padding: 6 10 6 10;");
            inspCirclePreview.setStyle("-fx-background-color: #10121a; -fx-background-radius: 50%; -fx-border-color: #2b2d3a; -fx-border-width: 1.5px; -fx-border-radius: 50%;");
            lblInspTagPreview.setTextFill(Color.web("#555555"));
        }
    }

    private void ejecutarInvestigacion() {
        if (nodoSeleccionado != null && ramaSeleccionada != null) {
            var inv = simulacion.getInventario();
            var prog = simulacion.getProgresion();
            if (prog.investigar(ramaSeleccionada, inv)) {
                simulacion.notificarEvento("Investigación completada: " + nodoSeleccionado.nombre(), MensajeEvento.TipoMensaje.EXITO);
                if (onInvestigadoCallback != null) {
                    onInvestigadoCallback.run();
                }
                actualizar();
            }
        }
    }

    public PantallaArbolProgresion.NodoArbol getNodoSeleccionado() {
        return nodoSeleccionado;
    }

    public RamaProgresion getRamaSeleccionada() {
        return ramaSeleccionada;
    }
}
