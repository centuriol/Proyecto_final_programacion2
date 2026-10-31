package org.example.game.ui;
import javafx.animation.AnimationTimer; import javafx.application.Platform; import javafx.geometry.Insets; import javafx.geometry.Pos; import javafx.scene.control.Button; import javafx.scene.control.Label; import javafx.scene.input.MouseButton; import javafx.scene.layout.*; import javafx.scene.paint.Color; import javafx.scene.text.Font; import javafx.scene.text.FontWeight; import org.example.game.guardado.InfoRanuraGuardado; import org.example.game.guardado.PoliticaRanurasGuardado; import org.example.game.render.EscenaFondoMenu; import java.util.ArrayList; import java.util.Collections; import java.util.List; import java.util.function.IntConsumer; import java.util.function.Supplier;
/**
 * Primera pantalla de Centuriol con fondo espacial animado permanente, un Sol a la izquierda,
 * un Planeta Porro a la derecha y las tres opciones principales:
 * Iniciar partida, Cargar partida (mostrando las tres ranuras de guardado) y Salir (SRP / DIP).
 */
public class PantallaMenuPrincipal extends StackPane {

    private static final String ESTILO_BOTON_MENU =
            "-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; "
            + "-fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-border-radius: 4px; "
            + "-fx-padding: 12 24 12 24; -fx-cursor: hand;";
    private static final String ESTILO_RANURA_VACIA =
            "-fx-background-color: #14161f; -fx-text-fill: #6c7284; -fx-border-color: #2b2d3a; "
            + "-fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-padding: 10 18 10 18;";

    private final Supplier<List<InfoRanuraGuardado>> proveedorRanuras;
    private final IntConsumer onCargarRanura;
    private final EscenaFondoMenu escenaFondo;
    private final CampoSatelitesMenu campoSatelites = new CampoSatelitesMenu();
    private final ResizableCanvas canvasFondo = new ResizableCanvas(1280.0, 720.0);
    private final AnimationTimer bucleFondo;
    private final VBox vistaOpciones = new VBox(14);
    private final VBox vistaRanuras = new VBox(12);
    private final List<Button> botonesRanuras = new ArrayList<>(PoliticaRanurasGuardado.TOTAL_RANURAS);

    private boolean animacionActiva = false;
    private long ultimoPulsoNs = 0L;
    private Button btnIniciarPartida;
    private Button btnCargarPartida;
    private Button btnSalir;
    private Button btnVolver;

    public PantallaMenuPrincipal(Runnable onIniciarPartida,
                                 Supplier<List<InfoRanuraGuardado>> proveedorRanuras,
                                 IntConsumer onCargarRanura,
                                 Runnable onSalir) {
        this.proveedorRanuras = proveedorRanuras != null ? proveedorRanuras : List::of;
        this.onCargarRanura = onCargarRanura;
        this.escenaFondo = new EscenaFondoMenu();
        this.bucleFondo = crearBucleFondo();

        setBackground(new Background(new BackgroundFill(Color.web("#080a12"), CornerRadii.EMPTY, Insets.EMPTY)));
        setAlignment(Pos.CENTER);
        setPickOnBounds(true);

        configurarCanvasFondo();
        VBox tarjetaCentral = construirTarjetaCentral(onIniciarPartida, onSalir);
        getChildren().addAll(canvasFondo, tarjetaCentral);

        sceneProperty().addListener((obs, anterior, actual) -> sincronizarCicloAnimacion());
        visibleProperty().addListener((obs, anterior, visible) -> sincronizarCicloAnimacion());
        mostrarMenuPrincipal();
    }

    private void configurarCanvasFondo() {
        canvasFondo.setMouseTransparent(false);
        canvasFondo.setOnMousePressed(evento -> {
            if (evento.getButton() == MouseButton.PRIMARY) {
                manejarClicSatelite(evento.getX(), evento.getY());
            }
        });
        canvasFondo.widthProperty().addListener((obs, ant, act) -> renderizarFondoActual());
        canvasFondo.heightProperty().addListener((obs, ant, act) -> renderizarFondoActual());
        renderizarFondoActual();
    }

    private AnimationTimer crearBucleFondo() {
        return new AnimationTimer() {
            @Override
            public void handle(long ahoraNs) {
                if (ultimoPulsoNs == 0L) {
                    ultimoPulsoNs = ahoraNs;
                    renderizarFondoActual();
                    return;
                }
                double deltaSeg = (ahoraNs - ultimoPulsoNs) / 1_000_000_000.0;
                ultimoPulsoNs = ahoraNs;
                avanzarYRenderizarFondo(deltaSeg);
            }
        };
    }

    private void sincronizarCicloAnimacion() {
        boolean debeAnimar = isVisible() && getScene() != null;
        if (!Platform.isFxApplicationThread()) {
            return;
        }
        if (debeAnimar && !animacionActiva) {
            ultimoPulsoNs = 0L;
            animacionActiva = true;
            bucleFondo.start();
        } else if (!debeAnimar && animacionActiva) {
            animacionActiva = false;
            ultimoPulsoNs = 0L;
            bucleFondo.stop();
        }
    }

    public void avanzarYRenderizarFondo(double deltaSegundos) {
        escenaFondo.avanzar(deltaSegundos);
        campoSatelites.avanzar(deltaSegundos);
        renderizarFondoActual();
    }

    public boolean manejarClicSatelite(double x, double y) {
        boolean activado = campoSatelites.manejarClic(x, y, canvasFondo.getWidth(), canvasFondo.getHeight());
        if (activado) {
            renderizarFondoActual();
        }
        return activado;
    }

    private void renderizarFondoActual() {
        var gc = canvasFondo.getGraphicsContext2D();
        double ancho = canvasFondo.getWidth();
        double alto = canvasFondo.getHeight();
        escenaFondo.dibujar(gc, ancho, alto);
        campoSatelites.dibujar(gc, ancho, alto);
    }

    private VBox construirTarjetaCentral(Runnable onIniciarPartida, Runnable onSalir) {
        VBox tarjeta = new VBox(20);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setMaxSize(470, Region.USE_PREF_SIZE);
        tarjeta.setPadding(new Insets(34, 42, 34, 42));
        tarjeta.setBackground(new Background(new BackgroundFill(Color.rgb(22, 25, 36, 0.85), new CornerRadii(6), Insets.EMPTY)));
        tarjeta.setBorder(new Border(new BorderStroke(TemaUI.COLOR_ACENTO_CYAN, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(2))));

        Label titulo = new Label("CENTURIOL");
        titulo.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
        titulo.setTextFill(TemaUI.COLOR_ACENTO_CYAN);

        Label subtitulo = new Label("Simulador de Sistemas Solares y Civilizaciones");
        subtitulo.setFont(Font.font("Monospace", FontWeight.NORMAL, 13));
        subtitulo.setTextFill(TemaUI.COLOR_TEXTO_MUTED);

        construirVistaOpciones(onIniciarPartida, onSalir);
        construirVistaRanuras();

        StackPane contenedorVistas = new StackPane(vistaOpciones, vistaRanuras);
        tarjeta.getChildren().addAll(titulo, subtitulo, contenedorVistas);
        return tarjeta;
    }

    private void construirVistaOpciones(Runnable onIniciarPartida, Runnable onSalir) {
        vistaOpciones.setAlignment(Pos.CENTER);
        btnIniciarPartida = crearBotonAccion("Iniciar partida", () -> {
            if (onIniciarPartida != null) onIniciarPartida.run();
        });
        btnCargarPartida = crearBotonAccion("Cargar partida", this::mostrarVistaRanuras);
        btnSalir = crearBotonAccion("Salir", () -> {
            if (onSalir != null) onSalir.run();
        });
        btnSalir.setStyle(TemaUI.CSS_BTN_PELIGRO + " -fx-padding: 12 24 12 24;");
        vistaOpciones.getChildren().addAll(btnIniciarPartida, btnCargarPartida, btnSalir);
    }

    private void construirVistaRanuras() {
        vistaRanuras.setAlignment(Pos.CENTER);
        Label encabezado = new Label("SELECCIONAR RANURA DE GUARDADO");
        encabezado.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        encabezado.setTextFill(TemaUI.COLOR_TEXTO_TITULO);
        vistaRanuras.getChildren().add(encabezado);

        for (int i = 1; i <= PoliticaRanurasGuardado.TOTAL_RANURAS; i++) {
            final int numeroRanura = i;
            Button btnRanura = crearBotonAccion("Ranura " + numeroRanura, () -> {
                if (onCargarRanura != null) onCargarRanura.accept(numeroRanura);
            });
            botonesRanuras.add(btnRanura);
            vistaRanuras.getChildren().add(btnRanura);
        }

        btnVolver = crearBotonAccion("Volver", this::mostrarVistaOpciones);
        btnVolver.setStyle(TemaUI.CSS_BTN_SECUNDARIO + " -fx-padding: 10 20 10 20;");
        vistaRanuras.getChildren().add(btnVolver);
    }

    private Button crearBotonAccion(String texto, Runnable accion) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(ESTILO_BOTON_MENU);
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    public void refrescarRanuras() {
        List<InfoRanuraGuardado> ranuras = proveedorRanuras.get();
        for (int i = 0; i < botonesRanuras.size(); i++) {
            Button btn = botonesRanuras.get(i);
            InfoRanuraGuardado info = (ranuras != null && i < ranuras.size())
                    ? ranuras.get(i)
                    : new InfoRanuraGuardado(i + 1, "ranura_" + (i + 1), null);
            btn.setText(info.tituloRanura() + " - " + info.formatearResumen());
            btn.setDisable(!info.estaOcupada());
            btn.setStyle(info.estaOcupada() ? ESTILO_BOTON_MENU : ESTILO_RANURA_VACIA);
        }
    }

    public void mostrarMenuPrincipal() {
        campoSatelites.reiniciar();
        mostrarVistaOpciones();
        setVisible(true);
        setManaged(true);
        toFront();
        renderizarFondoActual();
        sincronizarCicloAnimacion();
    }

    public void mostrarVistaRanuras() {
        refrescarRanuras();
        vistaOpciones.setVisible(false);
        vistaOpciones.setManaged(false);
        vistaRanuras.setVisible(true);
        vistaRanuras.setManaged(true);
    }

    private void mostrarVistaOpciones() {
        vistaRanuras.setVisible(false);
        vistaRanuras.setManaged(false);
        vistaOpciones.setVisible(true);
        vistaOpciones.setManaged(true);
    }

    public void ocultar() {
        setVisible(false);
        setManaged(false);
        sincronizarCicloAnimacion();
    }

    public boolean isMostrandoRanuras() { return vistaRanuras.isVisible(); }
    public boolean isAnimacionFondoActiva() { return animacionActiva; }
    public EscenaFondoMenu getEscenaFondo() { return escenaFondo; }
    public CampoSatelitesMenu getCampoSatelites() { return campoSatelites; }
    public ResizableCanvas getCanvasFondo() { return canvasFondo; }
    public Button getBtnIniciarPartida() { return btnIniciarPartida; }
    public Button getBtnCargarPartida() { return btnCargarPartida; }
    public Button getBtnSalir() { return btnSalir; }
    public Button getBtnVolver() { return btnVolver; }
    public List<Button> getBotonesRanuras() { return Collections.unmodifiableList(botonesRanuras); }
}
