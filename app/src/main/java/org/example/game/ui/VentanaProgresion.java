package org.example.game.ui;
import javafx.geometry.Insets; import javafx.geometry.Pos; import javafx.scene.Scene; import javafx.scene.control.Button; import javafx.scene.control.Label; import javafx.scene.control.ScrollPane; import javafx.scene.control.Separator; import javafx.scene.input.KeyCode; import javafx.scene.layout.HBox; import javafx.scene.layout.Priority; import javafx.scene.layout.VBox; import javafx.scene.paint.Color; import javafx.scene.text.Font; import javafx.scene.text.FontWeight; import javafx.stage.Modality; import javafx.stage.Stage; import javafx.stage.Window; import org.example.game.jugador.TipoRecurso; import org.example.game.progresion.RamaProgresion; import org.example.game.progresion.SistemaProgresion; import org.example.game.simulacion.ConfiguracionSimulacion; import org.example.game.simulacion.MensajeEvento; import org.example.game.simulacion.SimulacionSolar;
/**
 * Pantalla dedicada del Árbol de Investigación y Progresión Científica.
 * Dividido en tres ramas independientes:
 * 1. Planetas y Cuerpos celestes.
 * 2. Límite de Masa de colocación.
 * 3. Cantidad máxima de planetas simultáneos.
 *
 * Principios SOLID:
 * - SRP: Encapsula la visualización e interacción del árbol tecnológico.
 * - DIP: Desacoplado; interactúa con SistemaProgresion y SimulacionSolar mediante sus abstracciones públicas.
 */
public class VentanaProgresion {

    private static Stage stageActivo = null;

    private record NodoInfo(int nivel, String nombre, String desc, double costo) {}

    public static void mostrar(SimulacionSolar simulacion, Window owner) {
        if (simulacion == null) return;

        if (stageActivo != null && stageActivo.isShowing()) {
            stageActivo.toFront();
            return;
        }

        Stage stage = new Stage();
        stageActivo = stage;
        stage.initModality(Modality.NONE);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setTitle("Árbol de Investigación Científica - Proyecto Órbita");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));
        root.setStyle("-fx-background-color: #0d0f17; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-background-radius: 4px;");

        // ===== Cabecera =====
        Label lblTitulo = new Label("ÁRBOL DE INVESTIGACIÓN Y DESARROLLO CIENTÍFICO");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 17));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label("Elegí libremente qué rama tecnológica investigar con Ciencia [CIE]. Cada avance potencia tu sistema solar.");
        lblSubtitulo.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));

        // Pills de Estado
        HBox pillsBar = new HBox(12);
        pillsBar.setAlignment(Pos.CENTER_LEFT);

        Label lblCienciaPill = new Label();
        lblCienciaPill.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        lblCienciaPill.setPadding(new Insets(4, 10, 4, 10));
        lblCienciaPill.setStyle("-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px;");

        Label lblPlanetasPill = new Label();
        lblPlanetasPill.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        lblPlanetasPill.setPadding(new Insets(4, 10, 4, 10));
        lblPlanetasPill.setStyle("-fx-background-color: #2b2617; -fx-text-fill: #ffd700; -fx-border-color: #ffd700; -fx-border-width: 1px; -fx-background-radius: 4px;");

        Label lblMasaPill = new Label();
        lblMasaPill.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        lblMasaPill.setPadding(new Insets(4, 10, 4, 10));
        lblMasaPill.setStyle("-fx-background-color: #261a38; -fx-text-fill: #d2a8ff; -fx-border-color: #d2a8ff; -fx-border-width: 1px; -fx-background-radius: 4px;");

        pillsBar.getChildren().addAll(lblCienciaPill, lblPlanetasPill, lblMasaPill);

        root.getChildren().addAll(new VBox(4, lblTitulo, lblSubtitulo), pillsBar, new Separator());

        // ===== Contenedor de las 3 Ramas =====
        HBox ramasBox = new HBox(14);
        ramasBox.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(ramasBox, Priority.ALWAYS);

        // Definiciones de Nodos por Rama
        NodoInfo[] nodosPlanetas = new NodoInfo[] {
                new NodoInfo(0, "Sol & Planeta de Agua", "Cuerpos elementales iniciales para fundar el sistema.", 0.0),
                new NodoInfo(1, "Luna", "Satélite rocoso orbital que genera minerales pasivos.", 100.0),
                new NodoInfo(2, "Planeta Porro", "Mundo habitable con civilización, población y academias científicas.", 200.0),
                new NodoInfo(3, "Escudo Planetario", "Cúpula de defensa que absorbe impactos y despliega un satélite orbital de ciencia.", 300.0),
                new NodoInfo(4, "Planeta Lava", "Mundo magmático extremo de alta extracción de minerales pesados y datos térmicos.", 400.0)
        };

        NodoInfo[] nodosMasa = new NodoInfo[] {
                new NodoInfo(0, "Masa Inicial (0.3x)", "Factor base para cuerpos ligeros de baja perturbación gravitatoria.", 0.0),
                new NodoInfo(1, "Masa Expandida (0.6x)", "Permite mundos de densidad intermedia más estables.", 100.0),
                new NodoInfo(2, "Masa Estándar (1.0x)", "Masa completa equivalente a la Tierra y Sol base.", 200.0),
                new NodoInfo(3, "Mundos Masivos (2.0x)", "Generan campos gravitacionales amplios y mayor producción.", 300.0),
                new NodoInfo(4, "Super-Planetas (5.0x)", "Colosos celestes con intensos pozos gravitatorios.", 400.0),
                new NodoInfo(5, "Titanes Cósmicos (10.0x)", "Límite superior máximo para astros masivos.", 500.0)
        };

        NodoInfo[] nodosCantidad = new NodoInfo[] {
                new NodoInfo(0, "Cupo Base (3 Planetas)", "Permite hasta 3 planetas coexistiendo en el sistema.", 0.0),
                new NodoInfo(1, "Capacidad (4 Planetas)", "Expande el sistema solar a un cuarto planeta.", 100.0),
                new NodoInfo(2, "Capacidad (5 Planetas)", "Habilita órbitas para un quinto planeta.", 200.0),
                new NodoInfo(3, "Capacidad (6 Planetas)", "Soporta hasta 6 planetas simultáneos en la simulación.", 300.0),
                new NodoInfo(4, "Capacidad (8 Planetas)", "Permite densos sistemas multi-planetarios avanzados.", 400.0),
                new NodoInfo(5, "Gran Sistema (12 Planetas)", "Capacidad máxima para una mega-estructura solar.", 500.0)
        };

        // Función de refresco reactivo
        Runnable[] refrescarRef = new Runnable[1];
        refrescarRef[0] = () -> {
            var inv = simulacion.getInventario();
            var prog = simulacion.getProgresion();
            double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;

            lblCienciaPill.setText(String.format("Ciencia: %.1f CIE", ciencia));
            lblPlanetasPill.setText(String.format("Planetas: %d / %d", simulacion.getCantidadPlanetasActuales(), prog.getLimitePlanetas()));
            lblMasaPill.setText(String.format("Límite Masa: %.1fx", prog.getLimiteMasaActual()));

            ramasBox.getChildren().clear();

            // Renderizar cada una de las 3 ramas
            ramasBox.getChildren().add(construirColumnaRama(
                    RamaProgresion.PLANETAS,
                    "[RAMA PLANETAS]",
                    "#5be3ff",
                    nodosPlanetas,
                    prog.getNivel(RamaProgresion.PLANETAS),
                    ciencia,
                    simulacion,
                    () -> refrescarRef[0].run()
            ));

            ramasBox.getChildren().add(construirColumnaRama(
                    RamaProgresion.MASA,
                    "[RAMA MASA]",
                    "#d2a8ff",
                    nodosMasa,
                    prog.getNivel(RamaProgresion.MASA),
                    ciencia,
                    simulacion,
                    () -> refrescarRef[0].run()
            ));

            ramasBox.getChildren().add(construirColumnaRama(
                    RamaProgresion.CANTIDAD_PLANETAS,
                    "[RAMA CAPACIDAD]",
                    "#ffd700",
                    nodosCantidad,
                    prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS),
                    ciencia,
                    simulacion,
                    () -> refrescarRef[0].run()
            ));
        };

        refrescarRef[0].run();

        ScrollPane scroll = new ScrollPane(ramasBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scroll.setPrefHeight(450);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Barra inferior con botón de cerrar
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        Button btnCerrar = new Button("Cerrar [ESC / P]");
        btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCerrar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6 16 6 16;");
        btnCerrar.setOnAction(e -> stage.close());

        bottomBar.getChildren().add(btnCerrar);

        root.getChildren().addAll(scroll, new Separator(), bottomBar);

        Scene scene = new Scene(root, 1020, 680);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.P) {
                stage.close();
            }
        });

        // Conectar listener reactivo de simulación para actualizar ciencia en vivo
        Runnable listenerSimulacion = () -> javafx.application.Platform.runLater(() -> {
            if (stage.isShowing()) {
                refrescarRef[0].run();
            }
        });
        simulacion.agregarListenerUI(listenerSimulacion);

        stage.setOnHidden(e -> {
            simulacion.removerListenerUI(listenerSimulacion);
            stageActivo = null;
        });

        stage.setScene(scene);
        stage.show();
    }

    private static VBox construirColumnaRama(
            RamaProgresion rama,
            String tagTitulo,
            String colorHex,
            NodoInfo[] nodos,
            int nivelActual,
            double cienciaDisponible,
            SimulacionSolar simulacion,
            Runnable onActualizar
    ) {
        VBox col = new VBox(10);
        col.setPadding(new Insets(12));
        col.setPrefWidth(315);
        col.setStyle("-fx-background-color: #13151f; -fx-border-color: " + colorHex + "; -fx-border-width: 1px; -fx-background-radius: 6px;");

        // Cabecera de la rama
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

        // Renderizado de nodos
        for (int i = 0; i < nodos.length; i++) {
            NodoInfo nodo = nodos[i];
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
                // Nodo ya investigado
                cardNodo.setStyle("-fx-background-color: #102619; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-background-radius: 4px;");
                lblNodeTitle.setTextFill(Color.web("#50fa7b"));
                lblNodeDesc.setTextFill(Color.web("#99f6b4"));

                Label lblBadge = new Label("[INVESTIGADO]");
                lblBadge.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
                lblBadge.setTextFill(Color.web("#50fa7b"));
                cardHeader.getChildren().addAll(lblNodeTitle, lblBadge);
                cardNodo.getChildren().addAll(cardHeader, lblNodeDesc);
            } else if (nodeLevel == nivelActual + 1) {
                // Nodo disponible para investigar
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
                    var inv = simulacion.getInventario();
                    var prog = simulacion.getProgresion();
                    if (prog.investigar(rama, inv)) {
                        simulacion.notificarEvento("¡Tecnología investigada con éxito: " + nodeName + "!", MensajeEvento.TipoMensaje.EXITO);
                        onActualizar.run();
                        simulacion.notificarListenersUI();
                    }
                });

                cardNodo.getChildren().addAll(cardHeader, lblNodeDesc, btnInvestigar);
            } else {
                // Nodo bloqueado
                cardNodo.setStyle("-fx-background-color: #151720; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px;");
                lblNodeTitle.setTextFill(Color.web("#6b7280"));
                lblNodeDesc.setTextFill(Color.web("#4b5563"));

                Label lblBloq = new Label(String.format("[BLOQ - %.0f CIE]", nodeCost));
                lblBloq.setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
                lblBloq.setTextFill(Color.web("#6b7280"));
                cardHeader.getChildren().addAll(lblNodeTitle, lblBloq);
                cardNodo.getChildren().addAll(cardHeader, lblNodeDesc);
            }

            col.getChildren().add(cardNodo);
        }

        return col;
    }
}
