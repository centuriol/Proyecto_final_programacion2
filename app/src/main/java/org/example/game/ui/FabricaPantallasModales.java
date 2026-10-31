package org.example.game.ui;

import org.example.game.controlador.CoordinadorSesionJuego;
import org.example.game.guardado.GestorGuardadoPartida;

/**
 * Responsable de instanciar y cablear las pantallas modales del juego con el coordinador de sesión (SRP).
 */
public class FabricaPantallasModales {

    public PantallaMenuPrincipal crearMenuPrincipal(CoordinadorSesionJuego coordinadorSesion, GestorGuardadoPartida gestorGuardado) {
        return new PantallaMenuPrincipal(
                coordinadorSesion::iniciarNuevaPartida,
                gestorGuardado::obtenerRanuras,
                coordinadorSesion::cargarRanura,
                coordinadorSesion::salir
        );
    }

    public PantallaMenuPausa crearMenuPausa(CoordinadorSesionJuego coordinadorSesion) {
        return new PantallaMenuPausa(
                coordinadorSesion::reanudarDesdePausa,
                coordinadorSesion::guardarDesdePausa,
                coordinadorSesion::cargarDesdePausa,
                coordinadorSesion::volverAlMenuPrincipal
        );
    }

    public PantallaGameOver crearGameOver(CoordinadorSesionJuego coordinadorSesion) {
        return new PantallaGameOver(
                coordinadorSesion::volverAlUltimoGuardado,
                coordinadorSesion::volverAlMenuPrincipal
        );
    }
}
