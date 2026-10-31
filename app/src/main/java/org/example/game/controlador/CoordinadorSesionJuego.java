package org.example.game.controlador;
import javafx.scene.Node; import org.example.game.guardado.GestorGuardadoPartida; import org.example.game.guardado.TemporizadorAutoguardado; import org.example.game.render.ControlZoom; import org.example.game.simulacion.SimulacionSolar; import org.example.game.ui.PantallaGameOver; import org.example.game.ui.PantallaMenuPausa; import org.example.game.ui.PantallaMenuPrincipal; import java.nio.file.Path; import java.util.Objects;
/**
 * Coordina las transiciones entre el menu principal, la sesion de juego activa,
 * el menu de pausa y la pantalla de derrota.
 *
 * Principios SOLID:
 * - SRP: Orquesta exclusivamente el ciclo de vida de pantallas, autoguardado y reinicio/carga de sesion.
 * - DIP: Recibe la accion de salida y la limpieza de vista mediante abstracciones funcionales.
 */
public class CoordinadorSesionJuego {

    private final SimulacionSolar simulacion;
    private final ControlZoom controlZoom;
    private final GestorGuardadoPartida gestorGuardado;
    private final TemporizadorAutoguardado autoguardado;

    private Runnable accionLimpiarVista = () -> {};
    private Runnable accionSalir = () -> {};
    private PantallaMenuPrincipal menuPrincipal;
    private PantallaMenuPausa menuPausa;
    private PantallaGameOver pantallaGameOver;
    private Node overlayJuego;
    private boolean enPausaPrevioMenu = false;

    public CoordinadorSesionJuego(SimulacionSolar simulacion,
                                  ControlZoom controlZoom,
                                  GestorGuardadoPartida gestorGuardado,
                                  TemporizadorAutoguardado autoguardado) {
        this.simulacion = Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        this.controlZoom = Objects.requireNonNull(controlZoom, "El control de camara no puede ser null");
        this.gestorGuardado = Objects.requireNonNull(gestorGuardado, "El gestor de guardado no puede ser null");
        this.autoguardado = autoguardado != null
                ? autoguardado
                : new TemporizadorAutoguardado(gestorGuardado, simulacion, controlZoom);
    }

    public void configurarCallbacks(Runnable accionLimpiarVista, Runnable accionSalir) {
        if (accionLimpiarVista != null) {
            this.accionLimpiarVista = accionLimpiarVista;
        }
        if (accionSalir != null) {
            this.accionSalir = accionSalir;
        }
    }

    public void vincularPantallas(PantallaMenuPrincipal menuPrincipal,
                                  PantallaGameOver pantallaGameOver,
                                  Node overlayJuego) {
        this.menuPrincipal = menuPrincipal;
        this.pantallaGameOver = pantallaGameOver;
        this.overlayJuego = overlayJuego;
        this.simulacion.setOnGameOver(this::manejarDerrota);
    }

    public void vincularMenuPausa(PantallaMenuPausa menuPausa) {
        this.menuPausa = menuPausa;
    }

    public void mostrarMenuInicial() {
        autoguardado.desactivar();
        simulacion.pause();
        enPausaPrevioMenu = false;
        accionLimpiarVista.run();
        ocultarMenuPausa();
        if (pantallaGameOver != null) {
            pantallaGameOver.ocultar();
        }
        cambiarVisibilidadOverlay(false);
        if (menuPrincipal != null) {
            menuPrincipal.mostrarMenuPrincipal();
        }
    }

    public void iniciarNuevaPartida() {
        simulacion.reiniciar();
        controlZoom.resetear();
        gestorGuardado.seleccionarRanuraParaNuevaPartida();
        accionLimpiarVista.run();
        entrarEnJuegoActivo();
    }

    public void abrirMenuPausa() {
        if (!esPartidaEnCurso() || isEnMenuPausa()) {
            return;
        }
        enPausaPrevioMenu = simulacion.isEnPausa();
        autoguardado.desactivar();
        simulacion.pause();
        if (menuPausa != null) {
            menuPausa.mostrar();
        }
    }

    public void reanudarDesdePausa() {
        ocultarMenuPausa();
        if (!enPausaPrevioMenu) {
            simulacion.play();
        }
        enPausaPrevioMenu = false;
        if (esPartidaEnCurso()) {
            autoguardado.activar();
        }
    }

    public boolean guardarDesdePausa() {
        boolean pausadoPorMenu = isEnMenuPausa() && !enPausaPrevioMenu;
        if (pausadoPorMenu) {
            simulacion.play();
        }
        Path archivo = gestorGuardado.guardarPartidaRapida(simulacion, controlZoom);
        if (isEnMenuPausa()) {
            simulacion.pause();
        }
        return archivo != null;
    }

    public boolean cargarDesdePausa() {
        return volverAlUltimoGuardado();
    }

    public boolean cargarRanura(int numeroRanura) {
        boolean exito = gestorGuardado.cargarRanura(numeroRanura, simulacion, controlZoom);
        if (exito) {
            entrarEnJuegoActivo();
        }
        return exito;
    }

    public boolean volverAlUltimoGuardado() {
        boolean exito = gestorGuardado.cargarUltimoGuardado(simulacion, controlZoom);
        if (exito && !simulacion.estaPerdida()) {
            entrarEnJuegoActivo();
            return true;
        }
        return false;
    }

    public void volverAlMenuPrincipal() {
        mostrarMenuInicial();
    }

    public void salir() {
        autoguardado.desactivar();
        simulacion.pause();
        ocultarMenuPausa();
        accionSalir.run();
    }

    public boolean esPartidaEnCurso() {
        boolean menuVisible = menuPrincipal != null && menuPrincipal.isVisible();
        boolean derrotaVisible = pantallaGameOver != null && pantallaGameOver.isVisible();
        return !menuVisible && !derrotaVisible;
    }

    public boolean isEnMenuPausa() {
        return menuPausa != null && menuPausa.isVisible();
    }

    public TemporizadorAutoguardado getAutoguardado() {
        return autoguardado;
    }

    private void entrarEnJuegoActivo() {
        enPausaPrevioMenu = false;
        ocultarMenuPausa();
        if (menuPrincipal != null) {
            menuPrincipal.ocultar();
        }
        if (pantallaGameOver != null) {
            pantallaGameOver.ocultar();
        }
        cambiarVisibilidadOverlay(true);
        autoguardado.activar();
    }

    private void manejarDerrota() {
        autoguardado.desactivar();
        ocultarMenuPausa();
        if (pantallaGameOver != null) {
            pantallaGameOver.mostrar();
        }
    }

    private void ocultarMenuPausa() {
        if (menuPausa != null) {
            menuPausa.ocultar();
        }
    }

    private void cambiarVisibilidadOverlay(boolean visible) {
        if (overlayJuego != null) {
            overlayJuego.setVisible(visible);
            overlayJuego.setManaged(visible);
        }
    }
}
