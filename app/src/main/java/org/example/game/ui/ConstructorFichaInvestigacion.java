package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Responsable de construir visualmente las columnas y tarjetas de investigación tecnológica (SRP).
 */
public class ConstructorFichaInvestigacion {

    public record NodoInfo(int nivel, String nombre, String desc, double costo) {}

    public VBox construirColumnaRama(RamaProgresion rama,
                                     String tagTitulo,
                                     String colorHex,
                                     NodoInfo[] nodos,
                                     int nivelActual,
                                     double cienciaDisponible,
                                     SimulacionSolar simulacion,
                                     Runnable onInvestigado) {
        VBox col = new VBox(10);
        col.setPadding(new Insets(12));
        col.setPrefWidth(315);
        col.setStyle("-fx-background-color: #13151f; -fx-border-color: " + colorHex + "; -fx-border-width: 1px; -fx-background-radius: 6px;");

        Label lblTag = new Label(tagTitulo);
        lblTag.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        lblTag.setTextFill(Color.web(colorHex));

        Label lblNombreRama = new Label(rama.nombre);
        lblNombreRama.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        lblNombreRama.setTextFill(Color.web("#e8e4d8"));

        Label lblDescRama = new Label(rama.descripcion);
        lblDescRama.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        lblDescRama.setTextFill(Color.web("#8c92a4"));
        lblDescRama.setWrapText(true);

        col.getChildren().addAll(lblTag, lblNombreRama, lblDescRama, new Separator());

        for (NodoInfo nodo : nodos) {
            col.getChildren().add(construirTarjetaNodo(rama, nodo, nivelActual, cienciaDisponible, simulacion, onInvestigado));
        }
        return col;
    }

    public VBox construirTarjetaNodo(RamaProgresion rama,
                                     NodoInfo nodo,
                                     int nivelActual,
                                     double cienciaDisponible,
                                     SimulacionSolar simulacion,
                                     Runnable onInvestigado) {
        int nodeLevel = nodo.nivel();
        String nodeName = nodo.nombre();
        String nodeDesc = nodo.desc();
        double nodeCost = nodo.costo();

        VBox cardNodo = new VBox(6);
        cardNodo.setPadding(new Insets(10));

        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblNodeTitle = new Label(String.format("%d. %s", nodeLevel, nodeName));
        lblNodeTitle.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        HBox.setHgrow(lblNodeTitle, Priority.ALWAYS);

        Label lblNodeDesc = new Label(nodeDesc);
        lblNodeDesc.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
        lblNodeDesc.setWrapText(true);

        if (nodeLevel <= nivelActual) {
            cardNodo.setStyle("-fx-background-color: #102619; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-background-radius: 4px;");
            lblNodeTitle.setTextFill(Color.web("#50fa7b"));
            lblNodeDesc.setTextFill(Color.web("#99f6b4"));

            Label lblBadge = new Label("[INVESTIGADO]");
            lblBadge.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblBadge.setTextFill(Color.web("#50fa7b"));
            cardHeader.getChildren().addAll(lblNodeTitle, lblBadge);
            cardNodo.getChildren().addAll(cardHeader, lblNodeDesc);
        } else if (nodeLevel == nivelActual + 1) {
            cardNodo.setStyle("-fx-background-color: #1c2233; -fx-border-color: #ffd700; -fx-border-width: 1.5px; -fx-background-radius: 4px;");
            lblNodeTitle.setTextFill(Color.web("#ffd700"));
            lblNodeDesc.setTextFill(Color.web("#d1d5db"));

            Label lblCosto = new Label(String.format("[Costo: %.0f CIE]", nodeCost));
            lblCosto.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblCosto.setTextFill(Color.web("#ffd700"));
            cardHeader.getChildren().addAll(lblNodeTitle, lblCosto);

            Button btnInvestigar = new Button(String.format("Investigar (%.0f CIE)", nodeCost));
            btnInvestigar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            btnInvestigar.setMaxWidth(Double.MAX_VALUE);

            boolean puedePagar = cienciaDisponible >= nodeCost;
            btnInvestigar.setDisable(!puedePagar);
            btnInvestigar.setStyle(puedePagar
                    ? "-fx-background-color: #1e3a47; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6;"
                    : "-fx-background-color: #1a1c26; -fx-text-fill: #666666; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 6;");

            btnInvestigar.setOnAction(e -> {
                if (simulacion != null) {
                    SistemaProgresion prog = simulacion.getProgresion();
                    if (prog != null && prog.investigar(rama, simulacion.getInventario())) {
                        simulacion.notificarEvento("Tecnologia investigada con exito: " + nodeName, MensajeEvento.TipoMensaje.EXITO);
                        if (onInvestigado != null) {
                            onInvestigado.run();
                        }
                        simulacion.getBusEventos().notificarListenersUI();
                    }
                }
            });

            cardNodo.getChildren().addAll(cardHeader, lblNodeDesc, btnInvestigar);
        } else {
            cardNodo.setStyle("-fx-background-color: #151720; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px;");
            lblNodeTitle.setTextFill(Color.web("#6b7280"));
            lblNodeDesc.setTextFill(Color.web("#4b5563"));

            Label lblBloq = new Label(String.format("[BLOQ - %.0f CIE]", nodeCost));
            lblBloq.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
            lblBloq.setTextFill(Color.web("#6b7280"));
            cardHeader.getChildren().addAll(lblNodeTitle, lblBloq);
            cardNodo.getChildren().addAll(cardHeader, lblNodeDesc);
        }

        return cardNodo;
    }
}
