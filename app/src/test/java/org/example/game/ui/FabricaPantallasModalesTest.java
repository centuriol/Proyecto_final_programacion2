package org.example.game.ui;

import org.example.game.controlador.CoordinadorSesionJuego;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.guardado.TemporizadorAutoguardado;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(JavaFXExtension.class)
class FabricaPantallasModalesTest {

    private final FabricaPantallasModales fabrica = new FabricaPantallasModales();

    @Test
    @DisplayName("Crea pantallas modales vinculadas al flujo de sesión")
    void testCrearPantallasModales() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        CamaraZoom camara = new CamaraZoom();
        GestorGuardadoPartida guardado = new GestorGuardadoPartida();
        TemporizadorAutoguardado autoguardado = new TemporizadorAutoguardado(guardado, sim, camara);
        CoordinadorSesionJuego coordinador = new CoordinadorSesionJuego(sim, camara, guardado, autoguardado);

        PantallaMenuPrincipal menu = fabrica.crearMenuPrincipal(coordinador, guardado);
        assertNotNull(menu);

        PantallaMenuPausa pausa = fabrica.crearMenuPausa(coordinador);
        assertNotNull(pausa);

        PantallaGameOver gameOver = fabrica.crearGameOver(coordinador);
        assertNotNull(gameOver);
    }
}
