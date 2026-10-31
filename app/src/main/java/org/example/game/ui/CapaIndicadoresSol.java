package org.example.game.ui;
import javafx.scene.Cursor; import javafx.scene.control.Button; import javafx.scene.control.ContentDisplay; import javafx.scene.layout.Pane; import javafx.scene.paint.Color; import javafx.scene.shape.Polygon; import javafx.scene.text.Font; import javafx.scene.text.FontWeight; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.render.CalculadorIndicadoresSol; import org.example.game.render.ControlZoom; import org.example.game.render.IndicadorSolBorde; import java.util.ArrayList; import java.util.Collections; import java.util.List; import java.util.Objects;
/**
 * Capa visual superpuesta que muestra un indicador interactivo en el borde de la pantalla
 * por cada sol fuera de vista y permite viajar hasta el con un clic.
 *
 * Principios SOLID:
 * - SRP: Renderiza y gestiona la interaccion de los indicadores de borde delegando el calculo geometrico.
 * - DIP: Depende de ControlZoom y CalculadorIndicadoresSol.
 */
public class CapaIndicadoresSol extends Pane {

    private static final double ANCHO_ESTIMADO_BOTON = 84.0;
    private static final double ALTO_ESTIMADO_BOTON = 26.0;
    private static final String ESTILO_INDICADOR =
            "-fx-background-color: rgba(26, 22, 14, 0.92); -fx-text-fill: #ffd700; "
            + "-fx-border-color: #ffd700; -fx-border-width: 1.4px; -fx-background-radius: 4px; "
            + "-fx-border-radius: 4px; -fx-padding: 3 8 3 8;";

    private final ControlZoom camara;
    private final CalculadorIndicadoresSol calculador;
    private final List<IndicadorSolBorde> indicadoresActivos = new ArrayList<>();
    private final List<Button> botonesIndicadores = new ArrayList<>();

    private List<CuerpoCeleste> ultimosCuerpos = Collections.emptyList();
    private double ultimoAncho = 0.0;
    private double ultimoAlto = 0.0;

    public CapaIndicadoresSol(ControlZoom camara) {
        this(camara, new CalculadorIndicadoresSol());
    }

    public CapaIndicadoresSol(ControlZoom camara, CalculadorIndicadoresSol calculador) {
        this.camara = Objects.requireNonNull(camara, "El control de camara no puede ser null");
        this.calculador = calculador != null ? calculador : new CalculadorIndicadoresSol();
        setPickOnBounds(false);
    }

    public void actualizar(List<CuerpoCeleste> cuerpos, double anchoVista, double altoVista) {
        this.ultimosCuerpos = cuerpos != null ? new ArrayList<>(cuerpos) : Collections.emptyList();
        this.ultimoAncho = anchoVista;
        this.ultimoAlto = altoVista;

        List<IndicadorSolBorde> nuevos = calculador.calcularIndicadores(this.ultimosCuerpos, camara, anchoVista, altoVista);
        indicadoresActivos.clear();
        indicadoresActivos.addAll(nuevos);
        sincronizarBotones(nuevos, anchoVista, altoVista);
    }

    public void viajarASol(CuerpoCeleste sol) {
        if (sol == null) {
            return;
        }
        camara.enfocarPuntoFisico(sol.getPosicionX(), sol.getPosicionY());
        if (ultimoAncho > 0.0 && ultimoAlto > 0.0) {
            actualizar(ultimosCuerpos, ultimoAncho, ultimoAlto);
        }
    }

    public List<IndicadorSolBorde> getIndicadoresActivos() {
        return Collections.unmodifiableList(indicadoresActivos);
    }

    public List<Button> getBotonesIndicadores() {
        return Collections.unmodifiableList(botonesIndicadores);
    }

    private void sincronizarBotones(List<IndicadorSolBorde> indicadores, double anchoVista, double altoVista) {
        while (botonesIndicadores.size() > indicadores.size()) {
            Button removido = botonesIndicadores.remove(botonesIndicadores.size() - 1);
            getChildren().remove(removido);
        }
        while (botonesIndicadores.size() < indicadores.size()) {
            Button creado = crearBotonBase();
            botonesIndicadores.add(creado);
            getChildren().add(creado);
        }
        for (int i = 0; i < indicadores.size(); i++) {
            configurarBoton(botonesIndicadores.get(i), indicadores.get(i), anchoVista, altoVista);
        }
    }

    private Button crearBotonBase() {
        Polygon flecha = new Polygon(-5.0, -4.5, 6.0, 0.0, -5.0, 4.5);
        flecha.setFill(Color.web("#ffd700"));

        Button boton = new Button("Sol", flecha);
        boton.setContentDisplay(ContentDisplay.LEFT);
        boton.setGraphicTextGap(6.0);
        boton.setFont(Font.font("Monospace", FontWeight.BOLD, 10.5));
        boton.setStyle(ESTILO_INDICADOR);
        boton.setCursor(Cursor.HAND);
        boton.setFocusTraversable(false);
        return boton;
    }

    private void configurarBoton(Button boton, IndicadorSolBorde indicador, double anchoVista, double altoVista) {
        boton.setText(indicador.etiqueta());
        if (boton.getGraphic() != null) {
            boton.getGraphic().setRotate(indicador.anguloGrados());
        }
        boton.setOnAction(evento -> viajarASol(indicador.sol()));

        double anchoBtn = boton.getWidth() > 0.0 ? boton.getWidth() : ANCHO_ESTIMADO_BOTON;
        double altoBtn = boton.getHeight() > 0.0 ? boton.getHeight() : ALTO_ESTIMADO_BOTON;
        double x = Math.max(6.0, Math.min(anchoVista - anchoBtn - 6.0, indicador.pantallaX() - anchoBtn / 2.0));
        double y = Math.max(6.0, Math.min(altoVista - altoBtn - 6.0, indicador.pantallaY() - altoBtn / 2.0));
        boton.setLayoutX(x);
        boton.setLayoutY(y);
    }
}
