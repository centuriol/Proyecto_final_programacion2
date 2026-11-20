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
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Ventana dedicada del Árbol de Investigación y Progresión Científica (SRP / DIP).
 */
public class VentanaProgresion {

    private static Stage stageActivo = null;

    private static final ConstructorFichaInvestigacion.NodoInfo[] NODOS_PLANETAS = new ConstructorFichaInvestigacion.NodoInfo[] {
            new ConstructorFichaInvestigacion.NodoInfo(0, "Sol & Planeta de Agua", "Cuerpos elementales iniciales para fundar el sistema.", 0.0),
            new ConstructorFichaInvestigacion.NodoInfo(1, "Luna", "Satelite rocoso orbital que genera minerales pasivos.", 100.0),
            new ConstructorFichaInvestigacion.NodoInfo(2, "Planeta Porro", "Mundo habitable con civilizacion, poblacion y academias cientificas.", 200.0),
            new ConstructorFichaInvestigacion.NodoInfo(3, "Escudo Planetario", "Cupula de defensa que absorbe impactos y despliega un satelite orbital de ciencia.", 300.0),
            new ConstructorFichaInvestigacion.NodoInfo(4, "Planeta Lava", "Mundo magmatico extremo de alta extraccion de minerales pesados y datos termicos.", 400.0)
    };

    private static final ConstructorFichaInvestigacion.NodoInfo[] NODOS_MASA = new ConstructorFichaInvestigacion.NodoInfo[] {
            new ConstructorFichaInvestigacion.NodoInfo(0, "Masa Inicial (0.3x)", "Factor base para cuerpos ligeros de baja perturbacion gravitatoria.", 0.0),
            new ConstructorFichaInvestigacion.NodoInfo(1, "Masa Expandida (0.6x)", "Permite mundos de densidad intermedia mas estables.", 100.0),
            new ConstructorFichaInvestigacion.NodoInfo(2, "Masa Estandar (1.0x)", "Masa completa equivalente a la Tierra y Sol base.", 200.0),
            new ConstructorFichaInvestigacion.NodoInfo(3, "Mundos Masivos (2.0x)", "Generan campos gravitacionales amplios y mayor produccion.", 300.0),
            new ConstructorFichaInvestigacion.NodoInfo(4, "Super-Planetas (5.0x)", "Colosos celestes con intensos pozos gravitatorios.", 400.0),
            new ConstructorFichaInvestigacion.NodoInfo(5, "Titanes Cosmicos (10.0x)", "Limite superior maximo para astros masivos.", 500.0)
    };

    private static final ConstructorFichaInvestigacion.NodoInfo[] NODOS_CANTIDAD = new ConstructorFichaInvestigacion.NodoInfo[] {
            new ConstructorFichaInvestigacion.NodoInfo(0, "Cupo Base (3 Planetas)", "Permite hasta 3 planetas coexistiendo en el sistema.", 0.0),
            new ConstructorFichaInvestigacion.NodoInfo(1, "Capacidad (4 Planetas)", "Expande el sistema solar a un cuarto planeta.", 100.0),
            new ConstructorFichaInvestigacion.NodoInfo(2, "Capacidad (5 Planetas)", "Habilita orbitas para un quinto planeta.", 200.0),
            new ConstructorFichaInvestigacion.NodoInfo(3, "Capacidad (6 Planetas)", "Soporta hasta 6 planetas simultaneos en la simulacion.", 300.0),
            new ConstructorFichaInvestigacion.NodoInfo(4, "Capacidad (8 Planetas)", "Permite densos sistemas multi-planetarios avanzados.", 400.0),
            new ConstructorFichaInvestigacion.NodoInfo(5, "Gran Sistema (12 Planetas)", "Capacidad maxima para una mega-estructura solar.", 500.0)
    };

    private static class ContextoVentanaProgresion {
        final SimulacionSolar simulacion;
        final Window owner;
        final ConstructorFichaInvestigacion constructorFichas;
        final BarraResumenProgresion barraResumen = new BarraResumenProgresion(false);
        final HBox ramasBox = new HBox(14);
        Stage stage;

        ContextoVentanaProgresion(SimulacionSolar sim, Window owner, ConstructorFichaInvestigacion fichas) {
            this.simulacion = sim;
            this.owner = owner;
            this.constructorFichas = fichas != null ? fichas : new ConstructorFichaInvestigacion();
        }

        void cerrarSiAbierta() {
            if (stage != null) stage.close();
        }
    }

    private final ContextoVentanaProgresion ctx;

    public VentanaProgresion(SimulacionSolar simulacion, Window owner) {
        this(simulacion, owner, new ConstructorFichaInvestigacion());
    }

    public VentanaProgresion(SimulacionSolar simulacion, Window owner, ConstructorFichaInvestigacion constructorFichas) {
        this.ctx = new ContextoVentanaProgresion(simulacion, owner, constructorFichas);
    }

    public static void mostrar(SimulacionSolar simulacion, Window owner) {
        if (simulacion == null) return;
        if (stageActivo != null && stageActivo.isShowing()) {
            stageActivo.toFront();
            return;
        }
        new VentanaProgresion(simulacion, owner).abrir();
    }

    public void abrir() {
        ctx.stage = new Stage();
        stageActivo = ctx.stage;
        ctx.stage.initModality(Modality.NONE);
        if (ctx.owner != null) ctx.stage.initOwner(ctx.owner);
        ctx.stage.setTitle("Arbol de Investigacion Cientifica - Proyecto Orbita");

        Scene scene = new Scene(construirContenedorPrincipal(), 1020, 680);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.P) ctx.cerrarSiAbierta();
        });

        Runnable listenerSimulacion = () -> javafx.application.Platform.runLater(() -> {
            if (ctx.stage.isShowing()) refrescar();
        });
        ctx.simulacion.getBusEventos().agregarListenerUI(listenerSimulacion);
        ctx.stage.setOnHidden(e -> {
            ctx.simulacion.getBusEventos().removerListenerUI(listenerSimulacion);
            stageActivo = null;
        });

        refrescar();
        ctx.stage.setScene(scene);
        ctx.stage.show();
    }

    private VBox construirContenedorPrincipal() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 20, 16, 20));
        root.setStyle("-fx-background-color: #0d0f17; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-background-radius: 4px;");

        ctx.ramasBox.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(ctx.ramasBox, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(ctx.ramasBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scroll.setPrefHeight(450);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(construirCabecera(), ctx.barraResumen, new Separator(), scroll, new Separator(), construirBottomBar());
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

    private HBox construirBottomBar() {
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        Button btnCerrar = new Button("Cerrar [ESC / P]");
        btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCerrar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6 16 6 16;");
        btnCerrar.setOnAction(e -> ctx.cerrarSiAbierta());

        bottomBar.getChildren().add(btnCerrar);
        return bottomBar;
    }

    private void refrescar() {
        double ciencia = ctx.barraResumen.actualizar(ctx.simulacion);
        var prog = ctx.simulacion.getProgresion();
        ctx.ramasBox.getChildren().clear();
        ctx.ramasBox.getChildren().add(ctx.constructorFichas.construirColumnaRama(RamaProgresion.PLANETAS, "[RAMA PLANETAS]", "#5be3ff", NODOS_PLANETAS, prog.getNivel(RamaProgresion.PLANETAS), ciencia, ctx.simulacion, this::refrescar));
        ctx.ramasBox.getChildren().add(ctx.constructorFichas.construirColumnaRama(RamaProgresion.MASA, "[RAMA MASA]", "#d2a8ff", NODOS_MASA, prog.getNivel(RamaProgresion.MASA), ciencia, ctx.simulacion, this::refrescar));
        ctx.ramasBox.getChildren().add(ctx.constructorFichas.construirColumnaRama(RamaProgresion.CANTIDAD_PLANETAS, "[RAMA CAPACIDAD]", "#ffd700", NODOS_CANTIDAD, prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS), ciencia, ctx.simulacion, this::refrescar));
    }
}
