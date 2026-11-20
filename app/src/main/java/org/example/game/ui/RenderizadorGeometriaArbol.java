package org.example.game.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.progresion.RamaProgresion;

import java.util.ArrayList;
import java.util.List;

/**
 * SRP: Construye la geometría visual del árbol neuronal (soma, anillos sinápticos, dendritas y espinas).
 */
public class RenderizadorGeometriaArbol {

    public static final double SOMA_X = 68.0;
    public static final double SOMA_Y = 305.0;
    public static final double SOMA_DIAMETRO = 66.0;
    public static final double START_X = 185.0;
    public static final double STEP_X = 148.0;

    public List<Node> crearAnillosSinapticosFondo() {
        List<Node> nodos = new ArrayList<>();
        double[] radiosAura = {46.0, 62.0};
        for (double r : radiosAura) {
            Circle halo = new Circle(SOMA_X, SOMA_Y, r);
            halo.setFill(Color.rgb(80, 250, 123, 0.04));
            halo.setStroke(Color.web("#1c3b2b"));
            halo.setStrokeWidth(1.0);
            halo.getStrokeDashArray().addAll(4.0, 6.0);
            nodos.add(halo);
        }
        for (int i = 0; i < 5; i++) {
            double xTier = START_X + i * STEP_X + 43.0;
            Circle pulso = new Circle(SOMA_X, SOMA_Y, xTier - SOMA_X);
            pulso.setFill(Color.TRANSPARENT);
            pulso.setStroke(Color.web("#131926"));
            pulso.setStrokeWidth(1.0);
            pulso.getStrokeDashArray().addAll(3.0, 8.0);
            nodos.add(pulso);

            Label lblTier = new Label("SINAPSIS T" + (i + 1));
            lblTier.setFont(Font.font("Monospace", FontWeight.BOLD, 8.0));
            lblTier.setTextFill(Color.web("#39435e"));
            lblTier.setLayoutX(xTier - 32.0);
            lblTier.setLayoutY(12.0);
            nodos.add(lblTier);
        }
        return nodos;
    }

    public double[] calcularOffsetBifurcacion(RamaProgresion rama, int tier, int idx, int totalEnTier) {
        if (rama == RamaProgresion.PLANETAS) {
            return totalEnTier == 1 ? new double[]{26.0, 68.0} : parHorizontal(idx, 70.0);
        }
        if (rama == RamaProgresion.MASA) {
            return calcularOffsetMasa(tier, idx, totalEnTier);
        }
        return totalEnTier == 1 ? new double[]{26.0, -68.0} : parHorizontal(idx, -70.0);
    }

    private double[] parHorizontal(int idx, double dy) {
        return idx == 0 ? new double[]{-44.0, dy} : new double[]{42.0, dy};
    }

    private double[] calcularOffsetMasa(int tier, int idx, int totalEnTier) {
        if (totalEnTier == 1) {
            return new double[]{26.0, (tier % 2 == 1) ? -68.0 : 68.0};
        }
        return idx == 0 ? new double[]{32.0, -68.0} : new double[]{32.0, 68.0};
    }

    public CubicCurve crearDendritaCurva(double x1, double y1, double x2, double y2, double curvaturaY, boolean activa) {
        double dx = x2 - x1;
        CubicCurve curva = new CubicCurve(
                x1, y1,
                x1 + dx * 0.35, y1 + curvaturaY,
                x1 + dx * 0.68, y2 - curvaturaY * 0.6,
                x2, y2
        );
        curva.setFill(Color.TRANSPARENT);
        curva.setStrokeLineCap(StrokeLineCap.ROUND);
        curva.setStrokeWidth(activa ? 2.6 : 1.5);
        curva.setStroke(activa ? Color.web("#50fa7b") : Color.web("#2b3149"));
        if (!activa) {
            curva.getStrokeDashArray().addAll(6.0, 6.0);
        }
        return curva;
    }

    public QuadCurve crearEspinaBifurcacion(double x1, double y1, double x2, double y2,
                                            boolean investigada, boolean desbloqueada, String colorRamaHex) {
        QuadCurve espina = new QuadCurve(x1, y1, x1 + (x2 - x1) * 0.25, y2, x2, y2);
        espina.setFill(Color.TRANSPARENT);
        espina.setStrokeLineCap(StrokeLineCap.ROUND);
        espina.setStrokeWidth(investigada ? 2.0 : (desbloqueada ? 1.5 : 1.1));
        if (investigada) {
            espina.setStroke(Color.web("#50fa7b"));
        } else if (desbloqueada) {
            espina.setStroke(Color.web(colorRamaHex));
            espina.getStrokeDashArray().addAll(4.0, 4.0);
        } else {
            espina.setStroke(Color.web("#23283b"));
            espina.getStrokeDashArray().addAll(3.0, 5.0);
        }
        return espina;
    }

    public VBox crearSomaNeuronal() {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(SOMA_DIAMETRO + 24);

        StackPane circle = new StackPane();
        circle.setPrefSize(SOMA_DIAMETRO, SOMA_DIAMETRO);
        circle.setMaxSize(SOMA_DIAMETRO, SOMA_DIAMETRO);
        circle.setStyle("-fx-background-color: #102619; -fx-background-radius: 50%; -fx-border-color: #50fa7b; -fx-border-width: 2.8px; -fx-border-radius: 50%;");

        VBox inside = new VBox(1);
        inside.setAlignment(Pos.CENTER);

        Label lblIcon = new Label("[SOMA]");
        lblIcon.setFont(Font.font("Monospace", FontWeight.BOLD, 11.5));
        lblIcon.setTextFill(Color.web("#ffd700"));

        Label lblBase = new Label("NÚCLEO");
        lblBase.setFont(Font.font("Monospace", FontWeight.BOLD, 8.0));
        lblBase.setTextFill(Color.web("#50fa7b"));

        inside.getChildren().addAll(lblIcon, lblBase);
        circle.getChildren().add(inside);

        Label lblNombre = new Label("Sol & Agua");
        lblNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 9.5));
        lblNombre.setTextFill(Color.web("#e8e4d8"));

        box.getChildren().addAll(circle, lblNombre);
        box.setLayoutX(SOMA_X - (SOMA_DIAMETRO + 24.0) / 2.0);
        box.setLayoutY(SOMA_Y - SOMA_DIAMETRO / 2.0);
        return box;
    }
}
