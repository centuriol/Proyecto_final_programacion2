package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Responsable exclusivo de construir el componente visual e interactivo de un orbe en el árbol orbital (SRP).
 */
public class ConstructorOrbeNodoArbol {

    public VBox construir(RamaProgresion rama,
                          PantallaArbolProgresion.NodoArbol nodo,
                          int nivelActual,
                          double ciencia,
                          SimulacionSolar simulacion,
                          BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSeleccionarNodo,
                          Runnable onInvestigado,
                          Map<RamaProgresion, Button> botonesInvestigar,
                          Map<RamaProgresion, Double> costosBotones) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(86);

        double diameter = 52;
        StackPane orbCircle = new StackPane();
        orbCircle.setPrefSize(diameter, diameter);
        orbCircle.setMaxSize(diameter, diameter);
        orbCircle.setCursor(Cursor.HAND);

        Label lblTierBadge = new Label("T" + nodo.nivel());
        lblTierBadge.setFont(Font.font("Monospace", FontWeight.BOLD, 8));
        lblTierBadge.setTextFill(Color.web("#8c92a4"));
        StackPane.setAlignment(lblTierBadge, Pos.TOP_CENTER);
        StackPane.setMargin(lblTierBadge, new Insets(4, 0, 0, 0));

        Label lblTag = new Label(nodo.tag());
        lblTag.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        StackPane.setAlignment(lblTag, Pos.CENTER);

        orbCircle.getChildren().addAll(lblTag, lblTierBadge);

        Label lblNombre = new Label(nodo.nombre());
        lblNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        lblNombre.setAlignment(Pos.CENTER);
        lblNombre.setMaxWidth(86);

        if (nodo.nivel() <= nivelActual) {
            orbCircle.setStyle("-fx-background-color: #0d2516; -fx-background-radius: 50%; -fx-border-color: #50fa7b; -fx-border-width: 2px; -fx-border-radius: 50%;");
            lblTag.setTextFill(Color.web("#50fa7b"));
            lblNombre.setTextFill(Color.web("#50fa7b"));

            Label lblOk = new Label("INVESTIGADO");
            lblOk.setFont(Font.font("Monospace", FontWeight.BOLD, 7.5));
            lblOk.setTextFill(Color.web("#50fa7b"));
            lblOk.setPadding(new Insets(1, 4, 1, 4));
            lblOk.setStyle("-fx-background-color: #102619; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-background-radius: 3px; -fx-border-radius: 3px;");

            box.getChildren().addAll(orbCircle, lblNombre, lblOk);
        } else if (nodo.nivel() == nivelActual + 1) {
            orbCircle.setStyle("-fx-background-color: #142238; -fx-background-radius: 50%; -fx-border-color: #ffd700; -fx-border-width: 2px; -fx-border-radius: 50%;");
            lblTag.setTextFill(Color.web("#ffd700"));
            lblNombre.setTextFill(Color.web("#e8e4d8"));

            Button btnInvestigar = new Button(String.format("Inv (%.0f)", nodo.costo()));
            btnInvestigar.setFont(Font.font("Monospace", FontWeight.BOLD, 8.5));
            btnInvestigar.setCursor(Cursor.HAND);

            boolean puedePagar = ciencia >= nodo.costo();
            btnInvestigar.setDisable(!puedePagar);
            btnInvestigar.setStyle(puedePagar
                    ? "-fx-background-color: #1e3a47; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.2px; -fx-padding: 2 4 2 4; -fx-background-radius: 3px; -fx-border-radius: 3px;"
                    : "-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 2 4 2 4; -fx-background-radius: 3px; -fx-border-radius: 3px;");

            btnInvestigar.setOnAction(e -> {
                if (simulacion != null) {
                    var inv = simulacion.getInventario();
                    var prog = simulacion.getProgresion();
                    if (prog != null && prog.investigar(rama, inv)) {
                        simulacion.notificarEvento("Investigación completada: " + nodo.nombre(), MensajeEvento.TipoMensaje.EXITO);
                        if (onInvestigado != null) {
                            onInvestigado.run();
                        }
                        if (onSeleccionarNodo != null) {
                            onSeleccionarNodo.accept(rama, nodo);
                        }
                    }
                }
            });

            if (botonesInvestigar != null) {
                botonesInvestigar.put(rama, btnInvestigar);
            }
            if (costosBotones != null) {
                costosBotones.put(rama, nodo.costo());
            }

            box.getChildren().addAll(orbCircle, lblNombre, btnInvestigar);
        } else {
            orbCircle.setStyle("-fx-background-color: #10121a; -fx-background-radius: 50%; -fx-border-color: #2b2d3a; -fx-border-width: 1.2px; -fx-border-radius: 50%;");
            lblTag.setTextFill(Color.web("#555a6d"));
            lblNombre.setTextFill(Color.web("#6b7280"));

            Label lblBloq = new Label(String.format("[%.0f CIE]", nodo.costo()));
            lblBloq.setFont(Font.font("Monospace", FontWeight.NORMAL, 8));
            lblBloq.setTextFill(Color.web("#6b7280"));

            box.getChildren().addAll(orbCircle, lblNombre, lblBloq);
        }

        orbCircle.setOnMouseClicked(e -> {
            if (onSeleccionarNodo != null) onSeleccionarNodo.accept(rama, nodo);
        });
        box.setOnMouseClicked(e -> {
            if (onSeleccionarNodo != null) onSeleccionarNodo.accept(rama, nodo);
        });

        return box;
    }
}
