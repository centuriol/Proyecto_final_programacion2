package org.example.game.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.util.EnumMap;
import java.util.Map;

/**
 * Apartado de Árbol de Investigación Científica con nodos en círculos y disposición orbital ajustada a pantalla.
 *
 * Principios SOLID:
 * - SRP: Encapsula la visualización e interacción del árbol tecnológico con nodos circulares y panel lateral.
 * - DIP: Desacoplado de la simulación mediante listeners e interfaces de dominio.
 */
public class PantallaArbolProgresion extends StackPane {

    private final SimulacionSolar simulacion;
    private final Runnable onCerrarCallback;

    private final Label lblCienciaPill;
    private final Label lblPlanetasPill;
    private final Label lblMasaPill;
    private final Pane arbolCanvas;
    private Runnable listenerSimulacion;

    // Panel Lateral de Inspección
    private final VBox inspectorPanel;
    private final Label lblInspRama;
    private final Label lblInspTitulo;
    private final StackPane inspCirclePreview;
    private final Label lblInspTagPreview;
    private final Label lblInspDesc;
    private final Label lblInspCosto;
    private final ProgressBar inspBarraCiencia;
    private final Label lblInspProgresoCiencia;
    private final Button btnInspInvestigar;

    private NodoArbol nodoSeleccionado = null;
    private RamaProgresion ramaSeleccionada = null;

    private final Map<RamaProgresion, Button> botonesInvestigar = new EnumMap<>(RamaProgresion.class);
    private final Map<RamaProgresion, Double> costosBotones = new EnumMap<>(RamaProgresion.class);

    public record NodoArbol(int nivel, String tag, String nombre, String desc, double costo) {}

    private static final NodoArbol[] NODOS_PLANETAS = new NodoArbol[] {
            new NodoArbol(1, "[LUN]", "Luna", "Satélite rocoso natural. Proporciona una extracción continua y segura de minerales sin perturbar biosferas.", 100.0),
            new NodoArbol(2, "[POR]", "Planeta Porro", "Mundo templado habitable con civilización, crecimiento demográfico, colonización y academias científicas.", 500.0),
            new NodoArbol(3, "[SAT]", "Satélite", "Sonda orbital artificial no tripulada de masa fija (1.0 t). Genera ciencia pasiva continua (+0.08/t) y energía (+0.03/t).", 1500.0),
            new NodoArbol(4, "[ESC]", "Escudo", "Cúpula de defensa planetaria que absorbe impactos de meteoritos destructivos y despliega satélites protectores.", 3500.0),
            new NodoArbol(5, "[LAV]", "Planeta Lava", "Mundo magmático extremo de altísima extracción mineral (+0.09/t) y datos térmicos intensivos (+0.06/t).", 8000.0)
    };

    private static final NodoArbol[] NODOS_MASA = new NodoArbol[] {
            new NodoArbol(1, "0.6x", "Masa x0.6", "Habilita planetas con densidad intermedia y órbitas más estables ante perturbaciones gravitacionales.", 100.0),
            new NodoArbol(2, "1.0x", "Masa x1.0", "Masa estándar completa (1.0x) equivalente a la Tierra y Sol base, incrementando la producción proporcional.", 500.0),
            new NodoArbol(3, "2.0x", "Masa x2.0", "Cuerpos de doble densidad con pozos de gravedad más profundos que retienen satélites con mayor fuerza.", 1500.0),
            new NodoArbol(4, "5.0x", "Masa x5.0", "Colosos celestes masivos de alto rendimiento industrial y amplias zonas de influencia gravitatoria.", 3500.0),
            new NodoArbol(5, "10.0x", "Masa x10.0", "Límite superior absoluto de masa para astros ultra-masivos capaces de deformar el espacio circundante.", 8000.0)
    };

    private static final NodoArbol[] NODOS_CANTIDAD = new NodoArbol[] {
            new NodoArbol(1, "4 PLAN", "Capacidad (4)", "Amplía la infraestructura del sistema solar para albergar hasta 4 planetas en coexistencia orbital simultánea.", 100.0),
            new NodoArbol(2, "5 PLAN", "Capacidad (5)", "Permite coordinar trayectorias seguras para sostener hasta 5 planetas activos en el sistema solar.", 500.0),
            new NodoArbol(3, "6 PLAN", "Capacidad (6)", "Expansión orbital avanzada para alojar 6 planetas con órbitas concéntricas sin colapsos mutuos.", 1500.0),
            new NodoArbol(4, "8 PLAN", "Capacidad (8)", "Soporte de navegación multi-planetaria para albergar hasta 8 planetas simultáneos en el mapa.", 3500.0),
            new NodoArbol(5, "12 PLAN", "Gran Sistema (12)", "Capacidad máxima de civilización interestelar: permite hasta 12 mundos girando en armonía cósmica.", 8000.0)
    };

    public PantallaArbolProgresion(SimulacionSolar simulacion, Runnable onCerrarCallback) {
        this.simulacion = simulacion;
        this.onCerrarCallback = onCerrarCallback;

        setVisible(false);
        setManaged(false);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: rgba(5, 7, 14, 0.88);");

        // Contenedor principal estilizado tipo ventana / tarjeta de mando
        VBox dialogCard = new VBox(8);
        dialogCard.setMaxWidth(1080);
        dialogCard.setMaxHeight(530);
        dialogCard.setPrefWidth(1080);
        dialogCard.setPrefHeight(530);
        dialogCard.setPadding(new Insets(10, 16, 10, 16));
        dialogCard.setStyle("-fx-background-color: #070911; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 28, 0, 0, 0);");

        // Teclas rápidas [ESC / P]
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.P) {
                ocultar();
                e.consume();
            }
        });

        // ===== Barra Superior =====
        BorderPane topBar = new BorderPane();

        HBox cabeceraIzq = new HBox(12);
        cabeceraIzq.setAlignment(Pos.CENTER_LEFT);

        HBox navTabs = new HBox(6);
        navTabs.setAlignment(Pos.CENTER_LEFT);

        Button btnTabSim = new Button("[SIMULADOR]");
        btnTabSim.setFont(Font.font("Monospace", FontWeight.BOLD, 10.5));
        btnTabSim.setStyle("-fx-background-color: #1a1c26; -fx-text-fill: #8c92a4; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        btnTabSim.setOnAction(e -> ocultar());

        Button btnTabTree = new Button("[ÁRBOL DE INVESTIGACIÓN]");
        btnTabTree.setFont(Font.font("Monospace", FontWeight.BOLD, 10.5));
        btnTabTree.setStyle("-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-padding: 4 10 4 10;");

        navTabs.getChildren().addAll(btnTabSim, btnTabTree);

        VBox titulosBox = new VBox(1);
        Label lblTitulo = new Label("ÁRBOL DE INVESTIGACIÓN CIENTÍFICA");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 15));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label("Nodos circulares orbitales: Cuerpos, Masa y Capacidad. Desbloquea con Ciencia.");
        lblSubtitulo.setFont(Font.font("Monospace", FontWeight.NORMAL, 9.5));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));
        titulosBox.getChildren().addAll(lblTitulo, lblSubtitulo);

        cabeceraIzq.getChildren().addAll(navTabs, new Separator(javafx.geometry.Orientation.VERTICAL), titulosBox);
        topBar.setLeft(cabeceraIzq);

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

        Button btnVolver = new Button("Volver [ESC / P]");
        btnVolver.setFont(Font.font("Monospace", FontWeight.BOLD, 10.5));
        btnVolver.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.2px; -fx-cursor: hand; -fx-padding: 4 12 4 12;");
        btnVolver.setOnAction(e -> ocultar());

        controlesDer.getChildren().addAll(lblCienciaPill, lblPlanetasPill, lblMasaPill, btnVolver);
        topBar.setRight(controlesDer);

        // ===== Área Central (Lienzo Orbital + Panel de Inspección Lateral) =====
        HBox areaCentral = new HBox(12);
        areaCentral.setAlignment(Pos.CENTER);
        VBox.setVgrow(areaCentral, Priority.ALWAYS);

        arbolCanvas = new Pane();
        arbolCanvas.setStyle("-fx-background-color: transparent;");
        arbolCanvas.setPrefSize(780, 430);
        arbolCanvas.setMinSize(780, 430);

        ScrollPane scroll = new ScrollPane(arbolCanvas);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        HBox.setHgrow(scroll, Priority.ALWAYS);

        // Panel Lateral de Inspección
        inspectorPanel = new VBox(8);
        inspectorPanel.setPrefWidth(260);
        inspectorPanel.setMinWidth(260);
        inspectorPanel.setMaxWidth(260);
        inspectorPanel.setPadding(new Insets(12));
        inspectorPanel.setStyle("-fx-background-color: #0f121d; -fx-border-color: #2b3149; -fx-border-width: 1.5px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

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

        btnInspInvestigar.setOnAction(e -> {
            if (nodoSeleccionado != null && ramaSeleccionada != null) {
                var inv = simulacion.getInventario();
                var prog = simulacion.getProgresion();
                if (prog.investigar(ramaSeleccionada, inv)) {
                    simulacion.notificarEvento("Investigación completada: " + nodoSeleccionado.nombre, MensajeEvento.TipoMensaje.EXITO);
                    refrescar();
                    simulacion.notificarListenersUI();
                    actualizarInspector();
                }
            }
        });

        inspectorPanel.getChildren().addAll(lblInspRama, lblInspTitulo, inspCirclePreview, new Separator(), lblInspDesc, costoBox, btnInspInvestigar);
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
                    actualizarEstadosBotones(c);
                    actualizarInspector();
                }
            });
            simulacion.agregarListenerUI(listenerSimulacion);
        }
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);

        if (listenerSimulacion != null) {
            simulacion.removerListenerUI(listenerSimulacion);
            listenerSimulacion = null;
        }

        if (onCerrarCallback != null) {
            onCerrarCallback.run();
        }
    }

    private void actualizarPills() {
        double ciencia = simulacion.getInventario() != null ? simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA) : 0.0;
        lblCienciaPill.setText("Ciencia: " + String.format("%.1f", ciencia) + " CIE");
        lblPlanetasPill.setText("Planetas: " + simulacion.getCantidadPlanetasActuales() + " / " + simulacion.getProgresion().getLimitePlanetas());
        lblMasaPill.setText("Masa Máx: " + String.format("%.1f", simulacion.getProgresion().getLimiteMasaActual()) + "x");
    }

    private void actualizarEstadosBotones(double ciencia) {
        for (var entry : botonesInvestigar.entrySet()) {
            RamaProgresion rama = entry.getKey();
            Button btn = entry.getValue();
            Double costo = costosBotones.get(rama);
            if (btn != null && costo != null) {
                boolean puedePagar = ciencia >= costo;
                if (btn.isDisable() == puedePagar) {
                    btn.setDisable(!puedePagar);
                    btn.setStyle(puedePagar
                            ? "-fx-background-color: #1e3a47; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.2px; -fx-cursor: hand; -fx-padding: 2 5 2 5; -fx-background-radius: 4px; -fx-border-radius: 4px;"
                            : "-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 2 5 2 5; -fx-background-radius: 4px; -fx-border-radius: 4px;");
                }
            }
        }
    }

    private void actualizarInspector() {
        if (nodoSeleccionado == null || ramaSeleccionada == null) return;

        var prog = simulacion.getProgresion();
        var inv = simulacion.getInventario();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;
        int nivelActual = prog.getNivel(ramaSeleccionada);

        lblInspRama.setText("RAMA: " + ramaSeleccionada.name());
        lblInspTitulo.setText(nodoSeleccionado.nombre);
        lblInspTagPreview.setText(nodoSeleccionado.tag);
        lblInspDesc.setText(nodoSeleccionado.desc);

        if (nodoSeleccionado.nivel <= nivelActual) {
            // Ya investigado
            lblInspCosto.setText("Estado: ¡INVESTIGADO!");
            lblInspCosto.setTextFill(Color.web("#50fa7b"));
            inspBarraCiencia.setProgress(1.0);
            lblInspProgresoCiencia.setText("Tecnología activa en el sistema.");
            btnInspInvestigar.setDisable(true);
            btnInspInvestigar.setText("Tecnología Desbloqueada");
            btnInspInvestigar.setStyle("-fx-background-color: #102619; -fx-text-fill: #50fa7b; -fx-border-color: #50fa7b; -fx-border-width: 1.2px; -fx-padding: 6 10 6 10;");
            inspCirclePreview.setStyle("-fx-background-color: #102619; -fx-background-radius: 50%; -fx-border-color: #50fa7b; -fx-border-width: 2.5px; -fx-border-radius: 50%;");
            lblInspTagPreview.setTextFill(Color.web("#50fa7b"));

        } else if (nodoSeleccionado.nivel == nivelActual + 1) {
            // Próximo disponible
            lblInspCosto.setText(String.format("Costo: %.0f CIE", nodoSeleccionado.costo));
            lblInspCosto.setTextFill(Color.web("#ffd700"));

            double progreso = Math.min(1.0, ciencia / nodoSeleccionado.costo);
            inspBarraCiencia.setProgress(progreso);
            lblInspProgresoCiencia.setText(String.format("Ciencia: %.1f / %.0f CIE", ciencia, nodoSeleccionado.costo));

            boolean puedePagar = ciencia >= nodoSeleccionado.costo;
            btnInspInvestigar.setDisable(!puedePagar);
            btnInspInvestigar.setText(puedePagar ? "INVESTIGAR AHORA" : "Ciencia Insuficiente");
            btnInspInvestigar.setStyle(puedePagar
                    ? "-fx-background-color: #1e3a5f; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 10 6 10; -fx-background-radius: 4px; -fx-border-radius: 4px;"
                    : "-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 6 10 6 10; -fx-background-radius: 4px; -fx-border-radius: 4px;");
            inspCirclePreview.setStyle("-fx-background-color: #1a233a; -fx-background-radius: 50%; -fx-border-color: #ffd700; -fx-border-width: 2.5px; -fx-border-radius: 50%;");
            lblInspTagPreview.setTextFill(Color.web("#ffd700"));

        } else {
            // Bloqueado por nivel previo
            lblInspCosto.setText(String.format("Bloqueado (Requiere Nivel %d)", nodoSeleccionado.nivel - 1));
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

    public void refrescar() {
        actualizarPills();
        botonesInvestigar.clear();
        costosBotones.clear();
        arbolCanvas.getChildren().clear();

        var inv = simulacion.getInventario();
        var prog = simulacion.getProgresion();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;

        // ===== Orbe Central / Núcleo Solar =====
        double rootCenterX = 55;
        double rootCenterY = 215;
        double rootDiameter = 62;

        // Anillos orbitales concéntricos de guía (fondos cósmicos circulares)
        double[] radiosOrbitales = {120, 245, 370, 495, 620};
        for (int i = 0; i < radiosOrbitales.length; i++) {
            Circle orbitaPunteada = new Circle(rootCenterX, rootCenterY, radiosOrbitales[i]);
            orbitaPunteada.setFill(Color.TRANSPARENT);
            orbitaPunteada.setStroke(Color.web("#171d2b"));
            orbitaPunteada.setStrokeWidth(1.0);
            orbitaPunteada.getStrokeDashArray().addAll(5.0, 7.0);
            arbolCanvas.getChildren().add(orbitaPunteada);

            Label lblOrbita = new Label("T" + (i + 1));
            lblOrbita.setFont(Font.font("Monospace", FontWeight.NORMAL, 8.5));
            lblOrbita.setTextFill(Color.web("#30384d"));
            lblOrbita.setLayoutX(rootCenterX + radiosOrbitales[i] - 15);
            lblOrbita.setLayoutY(rootCenterY - 10);
            arbolCanvas.getChildren().add(lblOrbita);
        }

        VBox rootNode = crearNodoCircularRaiz(rootDiameter);
        rootNode.setLayoutX(rootCenterX - rootDiameter / 2.0);
        rootNode.setLayoutY(rootCenterY - rootDiameter / 2.0);

        // Posiciones orbitales para las 3 ramas (5 niveles cada una)
        double startX = 145;
        double stepX = 125;

        double yRamaPlanetas = 65;
        double yRamaMasa = 190;
        double yRamaCantidad = 315;

        // Conectar núcleo con primer nodo de cada rama
        arbolCanvas.getChildren().add(crearLineaConectora(rootCenterX, rootCenterY, startX + 26, yRamaPlanetas + 26, true));
        arbolCanvas.getChildren().add(crearLineaConectora(rootCenterX, rootCenterY, startX + 26, yRamaMasa + 26, true));
        arbolCanvas.getChildren().add(crearLineaConectora(rootCenterX, rootCenterY, startX + 26, yRamaCantidad + 26, true));

        // Renderizar las 3 Ramas con Orbes Circulares
        renderizarRamaCircular(RamaProgresion.PLANETAS, NODOS_PLANETAS, startX, yRamaPlanetas, stepX,
                prog.getNivel(RamaProgresion.PLANETAS), ciencia, "#5be3ff");

        renderizarRamaCircular(RamaProgresion.MASA, NODOS_MASA, startX, yRamaMasa, stepX,
                prog.getNivel(RamaProgresion.MASA), ciencia, "#d2a8ff");

        renderizarRamaCircular(RamaProgresion.CANTIDAD_PLANETAS, NODOS_CANTIDAD, startX, yRamaCantidad, stepX,
                prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS), ciencia, "#ffd700");

        arbolCanvas.getChildren().add(rootNode);

        // Si hay nodo seleccionado, actualizar el inspector lateral
        if (nodoSeleccionado != null) {
            actualizarInspector();
        } else {
            // Por defecto seleccionar el primer nodo desbloqueable
            seleccionarNodo(RamaProgresion.PLANETAS, NODOS_PLANETAS[0]);
        }
    }

    private void renderizarRamaCircular(RamaProgresion rama, NodoArbol[] nodos, double startX, double y, double stepX,
                                        int nivelActual, double ciencia, String colorHex) {
        for (int i = 0; i < nodos.length; i++) {
            NodoArbol nodo = nodos[i];
            double x = startX + i * stepX;

            // Línea conectora entre círculos de la misma rama
            if (i > 0) {
                double prevX = startX + (i - 1) * stepX;
                boolean activa = nodo.nivel <= nivelActual;
                arbolCanvas.getChildren().add(crearLineaConectora(prevX + 26, y + 26, x + 26, y + 26, activa));
            }

            VBox circularCard = crearOrbeCircularNodo(rama, nodo, nivelActual, ciencia, colorHex);
            circularCard.setLayoutX(x);
            circularCard.setLayoutY(y);

            arbolCanvas.getChildren().add(circularCard);
        }
    }

    private VBox crearNodoCircularRaiz(double diameter) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(diameter + 24);

        StackPane circle = new StackPane();
        circle.setPrefSize(diameter, diameter);
        circle.setMaxSize(diameter, diameter);
        circle.setStyle("-fx-background-color: #102619; -fx-background-radius: 50%; -fx-border-color: #50fa7b; -fx-border-width: 2.5px; -fx-border-radius: 50%;");

        VBox inside = new VBox(1);
        inside.setAlignment(Pos.CENTER);

        Label lblIcon = new Label("[SOL]");
        lblIcon.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        lblIcon.setTextFill(Color.web("#ffd700"));

        Label lblBase = new Label("BASE");
        lblBase.setFont(Font.font("Monospace", FontWeight.BOLD, 8.5));
        lblBase.setTextFill(Color.web("#50fa7b"));

        inside.getChildren().addAll(lblIcon, lblBase);
        circle.getChildren().add(inside);

        Label lblNombre = new Label("Sol & Agua");
        lblNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 9.5));
        lblNombre.setTextFill(Color.web("#e8e4d8"));

        box.getChildren().addAll(circle, lblNombre);
        return box;
    }

    private VBox crearOrbeCircularNodo(RamaProgresion rama, NodoArbol nodo, int nivelActual, double ciencia, String colorHex) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(86);

        double diameter = 52;
        StackPane orbCircle = new StackPane();
        orbCircle.setPrefSize(diameter, diameter);
        orbCircle.setMaxSize(diameter, diameter);
        orbCircle.setCursor(Cursor.HAND);

        // Insignia superior de Nivel (T1, T2, etc.)
        Label lblTierBadge = new Label("T" + nodo.nivel);
        lblTierBadge.setFont(Font.font("Monospace", FontWeight.BOLD, 8));
        lblTierBadge.setTextFill(Color.web("#8c92a4"));
        StackPane.setAlignment(lblTierBadge, Pos.TOP_CENTER);
        StackPane.setMargin(lblTierBadge, new Insets(4, 0, 0, 0));

        Label lblTag = new Label(nodo.tag);
        lblTag.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        StackPane.setAlignment(lblTag, Pos.CENTER);

        orbCircle.getChildren().addAll(lblTag, lblTierBadge);

        // Nombre de la tecnología
        Label lblNombre = new Label(nodo.nombre);
        lblNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        lblNombre.setAlignment(Pos.CENTER);
        lblNombre.setMaxWidth(86);

        // Estado / Botón
        if (nodo.nivel <= nivelActual) {
            // Ya investigado
            orbCircle.setStyle("-fx-background-color: #0d2516; -fx-background-radius: 50%; -fx-border-color: #50fa7b; -fx-border-width: 2px; -fx-border-radius: 50%;");
            lblTag.setTextFill(Color.web("#50fa7b"));
            lblNombre.setTextFill(Color.web("#50fa7b"));

            Label lblOk = new Label("INVESTIGADO");
            lblOk.setFont(Font.font("Monospace", FontWeight.BOLD, 7.5));
            lblOk.setTextFill(Color.web("#50fa7b"));
            lblOk.setPadding(new Insets(1, 4, 1, 4));
            lblOk.setStyle("-fx-background-color: #102619; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-background-radius: 3px; -fx-border-radius: 3px;");

            box.getChildren().addAll(orbCircle, lblNombre, lblOk);

        } else if (nodo.nivel == nivelActual + 1) {
            // Desbloqueable ahora
            orbCircle.setStyle("-fx-background-color: #142238; -fx-background-radius: 50%; -fx-border-color: #ffd700; -fx-border-width: 2px; -fx-border-radius: 50%;");
            lblTag.setTextFill(Color.web("#ffd700"));
            lblNombre.setTextFill(Color.web("#e8e4d8"));

            Button btnInvestigar = new Button(String.format("Inv (%.0f)", nodo.costo));
            btnInvestigar.setFont(Font.font("Monospace", FontWeight.BOLD, 8.5));
            btnInvestigar.setCursor(Cursor.HAND);

            boolean puedePagar = ciencia >= nodo.costo;
            btnInvestigar.setDisable(!puedePagar);
            btnInvestigar.setStyle(puedePagar
                    ? "-fx-background-color: #1e3a47; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.2px; -fx-padding: 2 4 2 4; -fx-background-radius: 3px; -fx-border-radius: 3px;"
                    : "-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 2 4 2 4; -fx-background-radius: 3px; -fx-border-radius: 3px;");

            btnInvestigar.setOnAction(e -> {
                var inv = simulacion.getInventario();
                var prog = simulacion.getProgresion();
                if (prog.investigar(rama, inv)) {
                    simulacion.notificarEvento("Investigación completada: " + nodo.nombre, MensajeEvento.TipoMensaje.EXITO);
                    refrescar();
                    simulacion.notificarListenersUI();
                    seleccionarNodo(rama, nodo);
                }
            });

            botonesInvestigar.put(rama, btnInvestigar);
            costosBotones.put(rama, nodo.costo);

            box.getChildren().addAll(orbCircle, lblNombre, btnInvestigar);

        } else {
            // Bloqueado
            orbCircle.setStyle("-fx-background-color: #10121a; -fx-background-radius: 50%; -fx-border-color: #2b2d3a; -fx-border-width: 1.2px; -fx-border-radius: 50%;");
            lblTag.setTextFill(Color.web("#555a6d"));
            lblNombre.setTextFill(Color.web("#6b7280"));

            Label lblBloq = new Label(String.format("[%.0f CIE]", nodo.costo));
            lblBloq.setFont(Font.font("Monospace", FontWeight.NORMAL, 8));
            lblBloq.setTextFill(Color.web("#6b7280"));

            box.getChildren().addAll(orbCircle, lblNombre, lblBloq);
        }

        // Al hacer clic sobre el círculo, se selecciona en el panel lateral de inspección
        orbCircle.setOnMouseClicked(e -> seleccionarNodo(rama, nodo));
        box.setOnMouseClicked(e -> seleccionarNodo(rama, nodo));

        return box;
    }

    private void seleccionarNodo(RamaProgresion rama, NodoArbol nodo) {
        this.ramaSeleccionada = rama;
        this.nodoSeleccionado = nodo;
        actualizarInspector();
    }

    private Line crearLineaConectora(double x1, double y1, double x2, double y2, boolean activa) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStrokeWidth(activa ? 2.0 : 1.2);
        line.setStroke(activa ? Color.web("#50fa7b") : Color.web("#2b3149"));
        if (!activa) {
            line.getStrokeDashArray().addAll(5.0, 5.0);
        }
        return line;
    }
}
