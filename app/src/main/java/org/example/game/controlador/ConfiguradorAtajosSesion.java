package org.example.game.controlador;

import javafx.scene.input.KeyCode;
import org.example.game.controlador.comando.*;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.PantallaArbolProgresion;

/**
 * Configurador de atajos de teclado para la sesión, menús, guardado y progreso (SRP).
 */
public class ConfiguradorAtajosSesion {

    public void configurarAtajos(
            ControladorTeclado controladorTeclado,
            SimulacionSolar simulacion,
            CamaraZoom camaraZoom,
            GestorGuardadoPartida gestorGuardado,
            CoordinadorSesionJuego coordinadorSesion,
            ControladorMouse controladorMouse,
            BarraInventarioHotbar hotbar,
            PantallaArbolProgresion arbol
    ) {
        Comando cmdArbol = new ComandoAbrirProgresion(simulacion, arbol::alternar);
        controladorTeclado.registrarComando(KeyCode.P, () -> {
            if (!coordinadorSesion.isEnMenuPausa()) {
                cmdArbol.ejecutar();
            }
        });
        hotbar.setAccionAbrirArbol(arbol::mostrar);

        ComandoCancelar comandoCancelar = new ComandoCancelar(simulacion, hotbar, controladorMouse);
        controladorTeclado.registrarComando(KeyCode.ESCAPE, () -> {
            if (arbol.isVisible()) {
                arbol.ocultar();
            } else if (coordinadorSesion.isEnMenuPausa()) {
                coordinadorSesion.reanudarDesdePausa();
            } else {
                comandoCancelar.ejecutar();
                coordinadorSesion.abrirMenuPausa();
            }
        });

        registrarAtajosGuardadoYCarga(controladorTeclado, gestorGuardado, simulacion, camaraZoom, coordinadorSesion);
    }

    private void registrarAtajosGuardadoYCarga(
            ControladorTeclado controladorTeclado,
            GestorGuardadoPartida gestorGuardado,
            SimulacionSolar simulacion,
            CamaraZoom camaraZoom,
            CoordinadorSesionJuego coordinadorSesion
    ) {
        Comando cmdGuardar = new ComandoGuardarPartida(gestorGuardado, simulacion, camaraZoom);
        Comando cmdCargar = new ComandoCargarPartida(gestorGuardado, simulacion, camaraZoom);

        Comando guardarContextual = () -> {
            if (coordinadorSesion.isEnMenuPausa()) {
                coordinadorSesion.guardarDesdePausa();
            } else {
                cmdGuardar.ejecutar();
            }
        };

        Comando cargarContextual = () -> {
            if (coordinadorSesion.isEnMenuPausa()) {
                coordinadorSesion.cargarDesdePausa();
            } else {
                cmdCargar.ejecutar();
            }
        };

        controladorTeclado.registrarComando(KeyCode.F5, guardarContextual);
        controladorTeclado.registrarComando(CombinacionTeclas.ctrl(KeyCode.S), guardarContextual);
        controladorTeclado.registrarComando(KeyCode.F9, cargarContextual);
        controladorTeclado.registrarComando(CombinacionTeclas.ctrl(KeyCode.L), cargarContextual);
    }
}
