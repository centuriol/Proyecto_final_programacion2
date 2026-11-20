package org.example.game.controlador;

import javafx.scene.Node;
import org.example.game.ui.PantallaGameOver;
import org.example.game.ui.PantallaMenuPausa;
import org.example.game.ui.PantallaMenuPrincipal;

/**
 * SRP: Controla la visibilidad y transiciones visuales entre menú principal, pausa, derrota y overlay de juego.
 */
public class GestorPantallasSesion {

    private static class ReferenciasPantallas {
        PantallaMenuPrincipal menuPrincipal;
        PantallaMenuPausa menuPausa;
        PantallaGameOver pantallaGameOver;
        Node overlayJuego;

        void ocultarPausa() {
            if (menuPausa != null) menuPausa.ocultar();
        }

        void cambiarVisibilidadOverlay(boolean visible) {
            if (overlayJuego != null) {
                overlayJuego.setVisible(visible);
                overlayJuego.setManaged(visible);
            }
        }
    }

    private final ReferenciasPantallas refs = new ReferenciasPantallas();

    public void vincularPantallas(PantallaMenuPrincipal menuPrincipal, PantallaGameOver pantallaGameOver, Node overlayJuego) {
        refs.menuPrincipal = menuPrincipal;
        refs.pantallaGameOver = pantallaGameOver;
        refs.overlayJuego = overlayJuego;
    }

    public void vincularMenuPausa(PantallaMenuPausa menuPausa) { refs.menuPausa = menuPausa; }

    public void mostrarMenuInicial() {
        refs.ocultarPausa();
        if (refs.pantallaGameOver != null) refs.pantallaGameOver.ocultar();
        refs.cambiarVisibilidadOverlay(false);
        if (refs.menuPrincipal != null) refs.menuPrincipal.mostrarMenuPrincipal();
    }

    public void entrarEnJuegoActivo() {
        refs.ocultarPausa();
        if (refs.menuPrincipal != null) refs.menuPrincipal.ocultar();
        if (refs.pantallaGameOver != null) refs.pantallaGameOver.ocultar();
        refs.cambiarVisibilidadOverlay(true);
    }

    public void mostrarPausa() { if (refs.menuPausa != null) refs.menuPausa.mostrar(); }
    public void ocultarPausa() { refs.ocultarPausa(); }

    public void mostrarDerrota() {
        refs.ocultarPausa();
        if (refs.pantallaGameOver != null) refs.pantallaGameOver.mostrar();
    }

    public boolean esPartidaEnCurso() {
        boolean menuVisible = refs.menuPrincipal != null && refs.menuPrincipal.isVisible();
        boolean derrotaVisible = refs.pantallaGameOver != null && refs.pantallaGameOver.isVisible();
        return !menuVisible && !derrotaVisible;
    }

    public boolean isEnMenuPausa() { return refs.menuPausa != null && refs.menuPausa.isVisible(); }
}
