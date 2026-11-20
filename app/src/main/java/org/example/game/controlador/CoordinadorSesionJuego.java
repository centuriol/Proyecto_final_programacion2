package org.example.game.controlador;

import javafx.scene.Node;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.guardado.TemporizadorAutoguardado;
import org.example.game.render.ControlZoom;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.PantallaGameOver;
import org.example.game.ui.PantallaMenuPausa;
import org.example.game.ui.PantallaMenuPrincipal;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Coordina las transiciones entre menú principal, sesión activa, pausa y derrota (SRP / DIP).
 */
public class CoordinadorSesionJuego {

    private static class ContextoSesion {
        final SimulacionSolar simulacion;
        final ControlZoom controlZoom;
        final GestorGuardadoPartida gestorGuardado;
        final TemporizadorAutoguardado autoguardado;
        Runnable accionLimpiarVista = () -> {};
        Runnable accionSalir = () -> {};
        boolean enPausaPrevioMenu = false;

        ContextoSesion(SimulacionSolar sim, ControlZoom zoom, GestorGuardadoPartida gestor, TemporizadorAutoguardado auto) {
            this.simulacion = Objects.requireNonNull(sim, "La simulacion no puede ser null");
            this.controlZoom = Objects.requireNonNull(zoom, "El control de camara no puede ser null");
            this.gestorGuardado = Objects.requireNonNull(gestor, "El gestor de guardado no puede ser null");
            this.autoguardado = auto != null ? auto : new TemporizadorAutoguardado(gestor, sim, zoom);
        }

        void pausarYDesactivarAutoguardado() {
            autoguardado.desactivar();
            simulacion.getReloj().pause();
        }
    }

    private final ContextoSesion ctx;
    private final GestorPantallasSesion pantallas = new GestorPantallasSesion();

    public CoordinadorSesionJuego(SimulacionSolar simulacion, ControlZoom controlZoom,
                                  GestorGuardadoPartida gestorGuardado, TemporizadorAutoguardado autoguardado) {
        this.ctx = new ContextoSesion(simulacion, controlZoom, gestorGuardado, autoguardado);
    }

    public void configurarCallbacks(Runnable accionLimpiarVista, Runnable accionSalir) {
        if (accionLimpiarVista != null) ctx.accionLimpiarVista = accionLimpiarVista;
        if (accionSalir != null) ctx.accionSalir = accionSalir;
    }

    public void vincularPantallas(PantallaMenuPrincipal menuPrincipal, PantallaGameOver pantallaGameOver, Node overlayJuego) {
        pantallas.vincularPantallas(menuPrincipal, pantallaGameOver, overlayJuego);
        ctx.simulacion.getBusEventos().setOnGameOver(() -> {
            ctx.autoguardado.desactivar();
            pantallas.mostrarDerrota();
        });
    }

    public void vincularMenuPausa(PantallaMenuPausa menuPausa) { pantallas.vincularMenuPausa(menuPausa); }

    public void mostrarMenuInicial() {
        ctx.pausarYDesactivarAutoguardado();
        ctx.enPausaPrevioMenu = false;
        ctx.accionLimpiarVista.run();
        pantallas.mostrarMenuInicial();
    }

    public void iniciarNuevaPartida() {
        ctx.simulacion.reiniciar();
        ctx.controlZoom.resetear();
        ctx.gestorGuardado.seleccionarRanuraParaNuevaPartida();
        ctx.accionLimpiarVista.run();
        entrarEnJuegoActivo();
    }

    public void abrirMenuPausa() {
        if (!esPartidaEnCurso() || isEnMenuPausa()) return;
        ctx.enPausaPrevioMenu = ctx.simulacion.getReloj().isEnPausa();
        ctx.pausarYDesactivarAutoguardado();
        pantallas.mostrarPausa();
    }

    public void reanudarDesdePausa() {
        pantallas.ocultarPausa();
        if (!ctx.enPausaPrevioMenu) ctx.simulacion.getReloj().play();
        ctx.enPausaPrevioMenu = false;
        if (esPartidaEnCurso()) ctx.autoguardado.activar();
    }

    public boolean guardarDesdePausa() {
        if (isEnMenuPausa() && !ctx.enPausaPrevioMenu) ctx.simulacion.getReloj().play();
        Path archivo = ctx.gestorGuardado.guardarPartidaRapida(ctx.simulacion, ctx.controlZoom);
        if (isEnMenuPausa()) ctx.simulacion.getReloj().pause();
        return archivo != null;
    }

    public boolean cargarDesdePausa() { return volverAlUltimoGuardado(); }

    public boolean cargarRanura(int numeroRanura) {
        boolean exito = ctx.gestorGuardado.cargarRanura(numeroRanura, ctx.simulacion, ctx.controlZoom);
        if (exito) entrarEnJuegoActivo();
        return exito;
    }

    public boolean volverAlUltimoGuardado() {
        boolean exito = ctx.gestorGuardado.cargarUltimoGuardado(ctx.simulacion, ctx.controlZoom);
        if (exito && !ctx.simulacion.estaPerdida()) {
            entrarEnJuegoActivo();
            return true;
        }
        return false;
    }

    public void volverAlMenuPrincipal() { mostrarMenuInicial(); }

    public void salir() {
        ctx.pausarYDesactivarAutoguardado();
        pantallas.ocultarPausa();
        ctx.accionSalir.run();
    }

    public boolean esPartidaEnCurso() { return pantallas.esPartidaEnCurso(); }
    public boolean isEnMenuPausa() { return pantallas.isEnMenuPausa(); }
    public TemporizadorAutoguardado getAutoguardado() { return ctx.autoguardado; }

    private void entrarEnJuegoActivo() {
        ctx.enPausaPrevioMenu = false;
        pantallas.entrarEnJuegoActivo();
        ctx.autoguardado.activar();
    }
}
