package org.example.game.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Panel visual de notificaciones de eventos en tiempo real.
 * SRP: Muestra alertas y mensajes importantes en la interfaz de juego.
 * DIP: Escucha eventos emitidos por SimulacionSolar sin acoplar la logica de simulacion a la vista.
 */
public class PanelNotificaciones extends VBox {

    private static final int MAX_MENSAJES_VISIBLES = 5;

    public PanelNotificaciones(SimulacionSolar simulacion) {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(6);
        setPadding(new Insets(8));
        setMaxWidth(380);
        setMouseTransparent(true);

        simulacion.agregarListenerEvento(this::mostrarEvento);
    }

    public void mostrarEvento(MensajeEvento evento) {
        if (Platform.isFxApplicationThread()) {
            agregarElementoVisual(evento);
        } else {
            Platform.runLater(() -> agregarElementoVisual(evento));
        }
    }

    private void agregarElementoVisual(MensajeEvento evento) {
        Label item = new Label("[" + evento.tipo().name() + "] " + evento.mensaje());
        item.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        item.setTextFill(Color.web(evento.tipo().colorHex));
        item.setWrapText(true);
        item.setMaxWidth(360);
        item.setPadding(new Insets(5, 10, 5, 10));
        item.setStyle("-fx-background-color: rgba(26, 28, 38, 0.92); -fx-border-color: "
                + evento.tipo().colorHex + "; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // Limitar cantidad visible
        while (getChildren().size() >= MAX_MENSAJES_VISIBLES) {
            getChildren().remove(0);
        }

        getChildren().add(item);

        // Desvanecimiento automatico tras 4.5 segundos
        PauseTransition pausa = new PauseTransition(Duration.seconds(4.5));
        FadeTransition fade = new FadeTransition(Duration.millis(800), item);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        SequentialTransition seq = new SequentialTransition(pausa, fade);
        seq.setOnFinished(e -> getChildren().remove(item));
        seq.play();
    }
}
