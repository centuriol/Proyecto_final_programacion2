package org.example;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.game.controlador.*;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.guardado.TemporizadorAutoguardado;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.*;

/**
 * SRP: Ensambla y conecta todos los subsistemas, controladores, pantallas y capas UI de Centuriol.
 */
public class EnsambladorAplicacion {

    private static class ComponentesApp {
        SimulacionSolar simulacion;
        RenderizadorPixelArt renderizador;
        ResizableCanvas canvasJuego;
        BucleJuego bucleJuego;
        ControladorTeclado controladorTeclado;
        ControladorMouse controladorMouse;
        HUDRecursosTop hudRecursos;
        BarraInventarioHotbar hotbar;
        ControlTiempoWidget controlTiempo;
        PanelNotificaciones panelNotificaciones;
        CapaIndicadoresSol capaIndicadoresSol;
        HBox topBar;
        BorderPane bottomBar;
        Button btnArbolInferior;
        GestorGuardadoPartida gestorGuardado;
        TemporizadorAutoguardado autoguardado;
        CoordinadorSesionJuego coordinadorSesion;
        PantallaArbolProgresion pantallaArbol;
        PantallaMenuPausa pantallaMenuPausa;
        PantallaMenuPrincipal pantallaMenuPrincipal;
        PantallaGameOver pantallaGameOver;

        void sincronizarCanvas() {
            double w = canvasJuego.getWidth();
            double h = canvasJuego.getHeight();
            if (w > 0 && h > 0) {
                renderizador.setDimensiones(w, h);
                simulacion.setDimensionesMundo(w, h);
                controladorMouse.setDimensiones(w, h);
            }
        }

        void detenerTodo() {
            if (autoguardado != null) autoguardado.desactivar();
            if (bucleJuego != null) bucleJuego.detener();
        }
    }

    private final ComponentesApp comp = new ComponentesApp();
    private final VisualizadorDescripcion visualizadorDescripcion = new VentanaDescripcionCuerpo();

    public StackPane inicializar(Stage stage, double ancho, double alto, GestorGuardadoPartida gestorPersonalizado) {
        inicializarSimulacionYUI(ancho, alto);
        configurarControladores(stage, ancho, alto);
        configurarGuardadoYSesion(gestorPersonalizado);

        new ConfiguradorAtajosSesion().configurarAtajos(
                comp.controladorTeclado, comp.simulacion, comp.renderizador.getCamaraZoom(),
                comp.gestorGuardado, comp.coordinadorSesion, comp.controladorMouse, comp.hotbar, comp.pantallaArbol
        );

        var estructura = new ConstructorOverlayUI().construir(
                comp.hudRecursos, comp.hotbar, comp.controlTiempo,
                comp.panelNotificaciones, comp.pantallaArbol, comp.coordinadorSesion
        );
        comp.topBar = estructura.topBar();
        comp.bottomBar = estructura.bottomBar();
        comp.btnArbolInferior = estructura.btnArbolInferior();
        BorderPane overlayUI = estructura.overlay();

        comp.capaIndicadoresSol.visibleProperty().bind(overlayUI.visibleProperty());
        comp.capaIndicadoresSol.managedProperty().bind(overlayUI.managedProperty());
        comp.coordinadorSesion.vincularPantallas(comp.pantallaMenuPrincipal, comp.pantallaGameOver, overlayUI);
        comp.coordinadorSesion.vincularMenuPausa(comp.pantallaMenuPausa);
        comp.coordinadorSesion.mostrarMenuInicial();
        comp.simulacion.getBusEventos().agregarListenerUI(new SincronizadorUI(comp.hudRecursos, comp.controlTiempo));
        return ensamblarEscenaRaiz(overlayUI);
    }

    private void inicializarSimulacionYUI(double ancho, double alto) {
        comp.simulacion = new SimulacionSolar(ancho, alto);
        comp.renderizador = new RenderizadorPixelArt(ancho, alto);
        comp.renderizador.setProveedorNaves(() -> comp.simulacion.getGestorColonizacion().getNaves());
        comp.renderizador.setGestorEfectos(comp.simulacion.getGestorEfectos());
        comp.canvasJuego = new ResizableCanvas(ancho, alto);
        comp.hudRecursos = new HUDRecursosTop(comp.simulacion);
        comp.hotbar = new BarraInventarioHotbar(comp.simulacion);
        comp.controlTiempo = new ControlTiempoWidget(comp.simulacion);
        comp.panelNotificaciones = new PanelNotificaciones(comp.simulacion);
        comp.capaIndicadoresSol = new CapaIndicadoresSol(comp.renderizador.getCamaraZoom());
        comp.pantallaArbol = new PantallaArbolProgresion(comp.simulacion, null);
    }

    private void configurarControladores(Stage stage, double ancho, double alto) {
        comp.controladorMouse = new ControladorMouse(
                comp.simulacion, comp.hotbar,
                cuerpo -> {
                    if (stage != null) {
                        visualizadorDescripcion.abrirParaInspeccionar(cuerpo, comp.simulacion, stage, comp.controladorMouse);
                    }
                },
                ancho, alto
        );
        comp.controladorMouse.conectar(comp.canvasJuego);
        comp.controladorMouse.setCamaraZoom(comp.renderizador.getCamaraZoom());

        comp.controladorTeclado = new ControladorTeclado();
        comp.controladorTeclado.configurarAtajosPorDefecto(
                comp.simulacion, comp.controlTiempo, comp.hotbar, comp.renderizador, comp.controladorMouse
        );
        comp.canvasJuego.widthProperty().addListener((o, a, b) -> comp.sincronizarCanvas());
        comp.canvasJuego.heightProperty().addListener((o, a, b) -> comp.sincronizarCanvas());
    }

    private void configurarGuardadoYSesion(GestorGuardadoPartida gestorPersonalizado) {
        comp.gestorGuardado = gestorPersonalizado != null ? gestorPersonalizado : new GestorGuardadoPartida();
        comp.autoguardado = new TemporizadorAutoguardado(comp.gestorGuardado, comp.simulacion, comp.renderizador.getCamaraZoom());

        Runnable limpiarVista = () -> {
            comp.renderizador.limpiarEstelas();
            comp.controladorMouse.limpiarSeleccion();
            comp.hotbar.deseleccionar();
            comp.pantallaArbol.ocultar();
        };

        comp.coordinadorSesion = new CoordinadorSesionJuego(comp.simulacion, comp.renderizador.getCamaraZoom(), comp.gestorGuardado, comp.autoguardado);
        comp.coordinadorSesion.configurarCallbacks(limpiarVista, () -> {
            if (comp.bucleJuego != null) comp.bucleJuego.detener();
            if (Platform.isFxApplicationThread()) Platform.exit();
        });
        comp.gestorGuardado.agregarListenerPostCarga(() -> {
            limpiarVista.run();
            if (!comp.simulacion.estaPerdida() && comp.pantallaGameOver != null) comp.pantallaGameOver.ocultar();
        });

        FabricaPantallasModales fabricaPantallas = new FabricaPantallasModales();
        comp.pantallaMenuPrincipal = fabricaPantallas.crearMenuPrincipal(comp.coordinadorSesion, comp.gestorGuardado);
        comp.pantallaMenuPausa = fabricaPantallas.crearMenuPausa(comp.coordinadorSesion);
        comp.pantallaGameOver = fabricaPantallas.crearGameOver(comp.coordinadorSesion);
    }

    private StackPane ensamblarEscenaRaiz(BorderPane overlayUI) {
        StackPane root = new StackPane(
                comp.canvasJuego, comp.capaIndicadoresSol, overlayUI,
                comp.pantallaArbol, comp.pantallaMenuPausa, comp.pantallaGameOver, comp.pantallaMenuPrincipal
        );
        root.setStyle("-fx-background-color: #0e1017;");
        root.setOnKeyPressed(evento -> {
            if (comp.coordinadorSesion.esPartidaEnCurso()) comp.controladorTeclado.handle(evento);
        });
        return root;
    }

    public void iniciarBucle() {
        comp.bucleJuego = new BucleJuego(comp.simulacion, comp.renderizador, comp.canvasJuego, comp.controladorMouse, comp.autoguardado);
        comp.bucleJuego.setListenerFrame(() -> comp.capaIndicadoresSol.actualizar(
                comp.simulacion.getSistemaSolar().getCuerpos(),
                comp.canvasJuego.getWidth(), comp.canvasJuego.getHeight()
        ));
        comp.bucleJuego.iniciar();
    }

    public void detener() { comp.detenerTodo(); }

    public SimulacionSolar getSimulacion() { return comp.simulacion; }
    public ControladorTeclado getControladorTeclado() { return comp.controladorTeclado; }
    public ControladorMouse getControladorMouse() { return comp.controladorMouse; }
    public BucleJuego getBucleJuego() { return comp.bucleJuego; }
    public RenderizadorPixelArt getRenderizador() { return comp.renderizador; }
    public GestorGuardadoPartida getGestorGuardado() { return comp.gestorGuardado; }
    public CoordinadorSesionJuego getCoordinadorSesion() { return comp.coordinadorSesion; }
    public PantallaMenuPrincipal getPantallaMenuPrincipal() { return comp.pantallaMenuPrincipal; }
    public PantallaMenuPausa getPantallaMenuPausa() { return comp.pantallaMenuPausa; }
    public PantallaArbolProgresion getPantallaArbol() { return comp.pantallaArbol; }
    public PantallaGameOver getPantallaGameOver() { return comp.pantallaGameOver; }
    public CapaIndicadoresSol getCapaIndicadoresSol() { return comp.capaIndicadoresSol; }
    public HBox getTopBar() { return comp.topBar; }
    public BorderPane getBottomBar() { return comp.bottomBar; }
    public Button getBtnArbolInferior() { return comp.btnArbolInferior; }
}
