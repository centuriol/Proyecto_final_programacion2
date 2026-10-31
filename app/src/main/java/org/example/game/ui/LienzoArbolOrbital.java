package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.ConfiguracionEconomia;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Lienzo orbital con nodos circulares y geometría cósmica para el Árbol de Investigación (SRP).
 */
public class LienzoArbolOrbital extends Pane {

    public static final PantallaArbolProgresion.NodoArbol[] NODOS_PLANETAS = new PantallaArbolProgresion.NodoArbol[] {
            new PantallaArbolProgresion.NodoArbol(1, "[LUN]", "Luna", "Satélite rocoso natural. Proporciona una extracción continua y segura de minerales sin perturbar biosferas.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1),
            new PantallaArbolProgresion.NodoArbol(2, "[POR]", "Planeta Porro", "Mundo templado habitable con civilización, crecimiento demográfico, colonización y academias científicas.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2),
            new PantallaArbolProgresion.NodoArbol(3, "[SAT]", "Satélite", "Sonda orbital defensiva y científica de masa fija (1.0 t). Intercepta meteoritos entrantes y genera ciencia y energía.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3),
            new PantallaArbolProgresion.NodoArbol(4, "[ESC]", "Escudo", "Cúpula de defensa planetaria que absorbe impactos destructivos adicionales.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4),
            new PantallaArbolProgresion.NodoArbol(5, "[LAV]", "Planeta Lava", "Mundo magmático extremo de altísima extracción mineral y datos térmicos intensivos.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5)
    };

    public static final PantallaArbolProgresion.NodoArbol[] NODOS_MASA = new PantallaArbolProgresion.NodoArbol[] {
            new PantallaArbolProgresion.NodoArbol(1, "0.6x", "Masa x0.6", "Habilita planetas con densidad intermedia y órbitas más estables ante perturbaciones gravitacionales.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1),
            new PantallaArbolProgresion.NodoArbol(2, "1.0x", "Masa x1.0", "Masa estándar completa (1.0x) equivalente a la Tierra y Sol base, incrementando la producción proporcional.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2),
            new PantallaArbolProgresion.NodoArbol(3, "2.0x", "Masa x2.0", "Cuerpos de doble densidad con pozos de gravedad más profundos que retienen satélites con mayor fuerza.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3),
            new PantallaArbolProgresion.NodoArbol(4, "5.0x", "Masa x5.0", "Colosos celestes masivos de alto rendimiento industrial y amplias zonas de influencia gravitatoria.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4),
            new PantallaArbolProgresion.NodoArbol(5, "10.0x", "Masa x10.0", "Límite superior absoluto de masa para astros ultra-masivos capaces de deformar el espacio circundante.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5)
    };

    public static final PantallaArbolProgresion.NodoArbol[] NODOS_CANTIDAD = new PantallaArbolProgresion.NodoArbol[] {
            new PantallaArbolProgresion.NodoArbol(1, "4 PLAN", "Capacidad (4)", "Amplía la infraestructura del sistema solar para albergar hasta 4 planetas en coexistencia orbital simultánea.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1),
            new PantallaArbolProgresion.NodoArbol(2, "5 PLAN", "Capacidad (5)", "Permite coordinar trayectorias seguras para sostener hasta 5 planetas activos en el sistema solar.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2),
            new PantallaArbolProgresion.NodoArbol(3, "6 PLAN", "Capacidad (6)", "Expansión orbital avanzada para alojar 6 planetas con órbitas concéntricas sin colapsos mutuos.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3),
            new PantallaArbolProgresion.NodoArbol(4, "8 PLAN", "Capacidad (8)", "Soporte de navegación multi-planetaria para albergar hasta 8 planetas simultáneos en el mapa.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4),
            new PantallaArbolProgresion.NodoArbol(5, "12 PLAN", "Gran Sistema (12)", "Capacidad máxima de civilización interestelar: permite hasta 12 mundos girando en armonía cósmica.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5)
    };

    private final SimulacionSolar simulacion;
    private final BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSeleccionarNodo;
    private final Runnable onInvestigado;

    private final Map<RamaProgresion, Button> botonesInvestigar = new EnumMap<>(RamaProgresion.class);
    private final Map<RamaProgresion, Double> costosBotones = new EnumMap<>(RamaProgresion.class);

    public LienzoArbolOrbital(SimulacionSolar simulacion,
                              BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSeleccionarNodo,
                              Runnable onInvestigado) {
        this.simulacion = simulacion;
        this.onSeleccionarNodo = onSeleccionarNodo;
        this.onInvestigado = onInvestigado;

        setStyle("-fx-background-color: transparent;");
        setPrefSize(780, 430);
        setMinSize(780, 430);
    }

    public void refrescar() {
        botonesInvestigar.clear();
        costosBotones.clear();
        getChildren().clear();

        var inv = simulacion.getInventario();
        var prog = simulacion.getProgresion();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;

        double rootCenterX = 55;
        double rootCenterY = 215;
        double rootDiameter = 62;

        double[] radiosOrbitales = {120, 245, 370, 495, 620};
        for (int i = 0; i < radiosOrbitales.length; i++) {
            Circle orbitaPunteada = new Circle(rootCenterX, rootCenterY, radiosOrbitales[i]);
            orbitaPunteada.setFill(Color.TRANSPARENT);
            orbitaPunteada.setStroke(Color.web("#171d2b"));
            orbitaPunteada.setStrokeWidth(1.0);
            orbitaPunteada.getStrokeDashArray().addAll(5.0, 7.0);
            getChildren().add(orbitaPunteada);

            Label lblOrbita = new Label("T" + (i + 1));
            lblOrbita.setFont(Font.font("Monospace", FontWeight.NORMAL, 8.5));
            lblOrbita.setTextFill(Color.web("#30384d"));
            lblOrbita.setLayoutX(rootCenterX + radiosOrbitales[i] - 15);
            lblOrbita.setLayoutY(rootCenterY - 10);
            getChildren().add(lblOrbita);
        }

        VBox rootNode = crearNodoCircularRaiz(rootDiameter);
        rootNode.setLayoutX(rootCenterX - rootDiameter / 2.0);
        rootNode.setLayoutY(rootCenterY - rootDiameter / 2.0);

        double startX = 145;
        double stepX = 125;

        double yRamaPlanetas = 65;
        double yRamaMasa = 190;
        double yRamaCantidad = 315;

        getChildren().add(crearLineaConectora(rootCenterX, rootCenterY, startX + 26, yRamaPlanetas + 26, true));
        getChildren().add(crearLineaConectora(rootCenterX, rootCenterY, startX + 26, yRamaMasa + 26, true));
        getChildren().add(crearLineaConectora(rootCenterX, rootCenterY, startX + 26, yRamaCantidad + 26, true));

        renderizarRamaCircular(RamaProgresion.PLANETAS, NODOS_PLANETAS, startX, yRamaPlanetas, stepX,
                prog.getNivel(RamaProgresion.PLANETAS), ciencia);

        renderizarRamaCircular(RamaProgresion.MASA, NODOS_MASA, startX, yRamaMasa, stepX,
                prog.getNivel(RamaProgresion.MASA), ciencia);

        renderizarRamaCircular(RamaProgresion.CANTIDAD_PLANETAS, NODOS_CANTIDAD, startX, yRamaCantidad, stepX,
                prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS), ciencia);

        getChildren().add(rootNode);
    }

    public void actualizarEstadosBotones(double ciencia) {
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

    private void renderizarRamaCircular(RamaProgresion rama, PantallaArbolProgresion.NodoArbol[] nodos,
                                        double startX, double y, double stepX, int nivelActual, double ciencia) {
        for (int i = 0; i < nodos.length; i++) {
            PantallaArbolProgresion.NodoArbol nodo = nodos[i];
            double x = startX + i * stepX;

            if (i > 0) {
                double prevX = startX + (i - 1) * stepX;
                boolean activa = nodo.nivel() <= nivelActual;
                getChildren().add(crearLineaConectora(prevX + 26, y + 26, x + 26, y + 26, activa));
            }

            VBox circularCard = crearOrbeCircularNodo(rama, nodo, nivelActual, ciencia);
            circularCard.setLayoutX(x);
            circularCard.setLayoutY(y);

            getChildren().add(circularCard);
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

    private VBox crearOrbeCircularNodo(RamaProgresion rama, PantallaArbolProgresion.NodoArbol nodo, int nivelActual, double ciencia) {
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
                var inv = simulacion.getInventario();
                var prog = simulacion.getProgresion();
                if (prog.investigar(rama, inv)) {
                    simulacion.notificarEvento("Investigación completada: " + nodo.nombre(), MensajeEvento.TipoMensaje.EXITO);
                    if (onInvestigado != null) {
                        onInvestigado.run();
                    }
                    if (onSeleccionarNodo != null) {
                        onSeleccionarNodo.accept(rama, nodo);
                    }
                }
            });

            botonesInvestigar.put(rama, btnInvestigar);
            costosBotones.put(rama, nodo.costo());

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
