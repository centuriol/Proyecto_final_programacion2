package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Ventana dedicada del Arbol de Investigacion y Progresion Cientifica.
 *
 * Principios SOLID:
 * - SRP: Encapsula el ciclo de vida y presentacion del arbol tecnologico en una clase cohesiva.
 * - DIP: Interactua con el modelo exclusivamente mediante abstracciones publicas.
 */
public class VentanaProgresion {

    private static Stage stageActivo = null;

    private final SimulacionSolar simulacion;
    private final Window owner;
    private Stage stage;

    private final Label lblCienciaPill = new Label();
    private final Label lblPlanetasPill = new Label();
    private final Label lblMasaPill = new Label();
    private final HBox ramasBox = new HBox(14);

    public record NodoInfo(int nivel, String nombre, String desc, double costo) {}

    private static final NodoInfo[] NODOS_PLANETAS = new NodoInfo[] {
            new NodoInfo(0, "Sol & Planeta de Agua", "Cuerpos elementales iniciales para fundar el sistema.", 0.0),
            new NodoInfo(1, "Luna", "Satelite rocoso orbital que genera minerales pasivos.", 100.0),
            new NodoInfo(2, "Planeta Porro", "Mundo habitable con civilizacion, poblacion y academias cientificas.", 200.0),
            new NodoInfo(3, "Escudo Planetario", "Cupula de defensa que absorbe impactos y despliega un satelite orbital de ciencia.", 300.0),
            new NodoInfo(4, "Planeta Lava", "Mundo magmatico extremo de alta extraccion de minerales pesados y datos termicos.", 400.0)
    };

    private static final NodoInfo[] NODOS_MASA = new NodoInfo[] {
            new NodoInfo(0, "Masa Inicial (0.3x)", "Factor base para cuerpos ligeros de baja perturbacion gravitatoria.", 0.0),
            new NodoInfo(1, "Masa Expandida (0.6x)", "Permite mundos de densidad intermedia mas estables.", 100.0),
            new NodoInfo(2, "Masa Estandar (1.0x)", "Masa completa equivalente a la Tierra y Sol base.", 200.0),
            new NodoInfo(3, "Mundos Masivos (2.0x)", "Generan campos gravitacionales amplios y mayor produccion.", 300.0),
            new NodoInfo(4, "Super-Planetas (5.0x)", "Colosos celestes con intensos pozos gravitatorios.", 400.0),
            new NodoInfo(5, "Titanes Cosmicos (10.0x)", "Limite superior maximo para astros masivos.", 500.0)
    };

    private static final NodoInfo[] NODOS_CANTIDAD = new NodoInfo[] {
            new NodoInfo(0, "Cupo Base (3 Planetas)", "Permite hasta 3 planetas coexistiendo en el sistema.", 0.0),
            new NodoInfo(1, "Capacidad (4 Planetas)", "Expande el sistema solar a un cuarto planeta.", 100.0),
            new NodoInfo(2, "Capacidad (5 Planetas)", "Habilita orbitas para un quinto planeta.", 200.0),
            new NodoInfo(3, "Capacidad (6 Planetas)", "Soporta hasta 6 planetas simultaneos en la simulacion.", 300.0),
            new NodoInfo(4, "Capacidad (8 Planetas)", "Permite densos sistemas multi-planetarios avanzados.", 400.0),
            new NodoInfo(5, "Gran Sistema (12 Planetas)", "Capacidad maxima para una mega-estructura solar.", 500.0)
    };

    public VentanaProgresion(SimulacionSolar simulacion, Window owner) {
        this.simulacion = simulacion;
        this.owner = owner;
    }

    public static void mostrar(SimulacionSolar simulacion, Window owner) {
        if (simulacion == null) {
            return;
        }
        if (stageActivo != null && stageActivo.isShowing()) {
            stageActivo.toFront();
            return;
        }
        new VentanaProgresion(simulacion, owner).abrir();
    }

    public void abrir() {
        stage = new Stage();
        stageActivo = stage;
        stage.initModality(Modality.NONE);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setTitle("Arbol de Investigacion Cientifica - Proyecto Orbita");

        VBox root = construirContenedorPrincipal();
        Scene scene = new Scene(root, 1020, 680);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.P) {
                stage.close();
            }
        });

        Runnable listenerSimulacion = () -> javafx.application.Platform.runLater(() -> {
            if (stage.isShowing()) {
                refrescar();
            }
        });
        simulacion.agregarListenerUI(listenerSimulacion);

        stage.setOnHidden(e -> {
            simulacion.removerListenerUI(listenerSimulacion);
            stageActivo = null;
        });

        refrescar();
        stage.setScene(scene);
        stage.show();
    }

    private VBox construirContenedorPrincipal() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));
        root.setStyle("-fx-background-color: #0d0f17; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-background-radius: 4px;");

        VBox cabecera = construirCabecera();
        HBox pillsBar = construirPillsBar();

        ramasBox.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(ramasBox, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(ramasBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scroll.setPrefHeight(450);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox bottomBar = construirBottomBar();
        root.getChildren().addAll(cabecera, pillsBar, new Separator(), scroll, new Separator(), bottomBar);
        return root;
    }

    private VBox construirCabecera() {
        Label lblTitulo = new Label("ARBOL DE INVESTIGACION Y DESARROLLO CIENTIFICO");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 17));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label("Elegi libremente que rama tecnologica investigar con Ciencia [CIE]. Cada avance potencia tu sistema solar.");
        lblSubtitulo.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));

        return new VBox(4, lblTitulo, lblSubtitulo);
    }

    private HBox construirPillsBar() {
        HBox pillsBar = new HBox(12);
        pillsBar.setAlignment(Pos.CENTER_LEFT);

        configurarPill(lblCienciaPill, "#172a38", "#5be3ff");
        configurarPill(lblPlanetasPill, "#2b2617", "#ffd700");
        configurarPill(lblMasaPill, "#261a38", "#d2a8ff");

        pillsBar.getChildren().addAll(lblCienciaPill, lblPlanetasPill, lblMasaPill);
        return pillsBar;
    }

    private void configurarPill(Label label, String fondo, String colorBorde) {
        label.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        label.setPadding(new Insets(4, 10, 4, 10));
        label.setStyle("-fx-background-color: " + fondo + "; -fx-text-fill: " + colorBorde + "; -fx-border-color: " + colorBorde + "; -fx-border-width: 1px; -fx-background-radius: 4px;");
    }

    private HBox construirBottomBar() {
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        Button btnCerrar = new Button("Cerrar [ESC / P]");
        btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCerrar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6 16 6 16;");
        btnCerrar.setOnAction(e -> {
            if (stage != null) {
                stage.close();
            }
        });

        bottomBar.getChildren().add(btnCerrar);
        return bottomBar;
    }

    private void refrescar() {
        var inv = simulacion.getInventario();
        var prog = simulacion.getProgresion();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;

        lblCienciaPill.setText(String.format("Ciencia: %.1f CIE", ciencia));
        lblPlanetasPill.setText(String.format("Planetas: %d / %d", simulacion.getCantidadPlanetasActuales(), prog.getLimitePlanetas()));
        lblMasaPill.setText(String.format("Limite Masa: %.1fx", prog.getLimiteMasaActual()));

        ramasBox.getChildren().clear();
        ramasBox.getChildren().add(construirColumnaRama(RamaProgresion.PLANETAS, "[RAMA PLANETAS]", "#5be3ff", NODOS_PLANETAS, prog.getNivel(RamaProgresion.PLANETAS), ciencia));
        ramasBox.getChildren().add(construirColumnaRama(RamaProgresion.MASA, "[RAMA MASA]", "#d2a8ff", NODOS_MASA, prog.getNivel(RamaProgresion.MASA), ciencia));
        ramasBox.getChildren().add(construirColumnaRama(RamaProgresion.CANTIDAD_PLANETAS, "[RAMA CAPACIDAD]", "#ffd700", NODOS_CANTIDAD, prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS), ciencia));
    }

    private VBox construirColumnaRama(RamaProgresion rama, String tagTitulo, String colorHex, NodoInfo[] nodos, int nivelActual, double cienciaDisponible) {
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
            col.getChildren().add(construirTarjetaNodo(rama, nodo, nivelActual, cienciaDisponible));
        }
        return col;
    }

    private VBox construirTarjetaNodo(RamaProgresion rama, NodoInfo nodo, int nivelActual, double cienciaDisponible) {
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
                SistemaProgresion prog = simulacion.getProgresion();
                if (prog.investigar(rama, simulacion.getInventario())) {
                    simulacion.notificarEvento("Tecnologia investigada con exito: " + nodeName, MensajeEvento.TipoMensaje.EXITO);
                    refrescar();
                    simulacion.notificarListenersUI();
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
