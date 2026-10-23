package org.example.game.controlador;

import javafx.scene.input.KeyCode;
import org.example.game.controlador.comando.*;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ControlTiempoWidget;

/**
 * Factoria y configurador de atajos predeterminados del juego.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de asociar combinaciones de teclas con comandos de juego.
 *   Desacopla a ControladorTeclado del conocimiento de comandos concretos y subsistemas.
 * - OCP: Para nuevos esquemas de atajos (o atajos configurables por el usuario), se implementan
 *   sin modificar la logica de recepcion y despacho de eventos de ControladorTeclado.
 * - DIP: Opera sobre las interfaces y controladores inyectados.
 */
public class ConfiguradorAtajosTeclado {

    public static void configurar(
            ControladorTeclado controlador,
            SimulacionSolar simulacion,
            ControlTiempoWidget controlTiempo,
            BarraInventarioHotbar hotbar,
            RenderizadorPixelArt renderizador,
            ControladorMouse controladorMouse
    ) {
        if (controlador == null) return;

        controlador.registrarComando(KeyCode.SPACE, new ComandoAlternarPausa(simulacion, controlTiempo));
        controlador.registrarComando(KeyCode.S, new ComandoAvanzarPaso(simulacion, controlTiempo));
        controlador.registrarComando(KeyCode.ESCAPE, new ComandoCancelar(simulacion, hotbar, controladorMouse));

        controlador.registrarComando(KeyCode.DIGIT1, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.ESTRELLA));
        controlador.registrarComando(KeyCode.DIGIT2, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.PLANETA_ROCOSO));
        controlador.registrarComando(KeyCode.DIGIT3, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.LUNA));
        controlador.registrarComando(KeyCode.DIGIT4, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.PLANETA_AGUA));
        controlador.registrarComando(KeyCode.DIGIT5, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.SATELITE));
        controlador.registrarComando(KeyCode.DIGIT6, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.PLANETA_LAVA));

        controlador.registrarComando(KeyCode.G, new ComandoAlternarGrilla(renderizador));
        controlador.registrarComando(KeyCode.T, new ComandoAlternarEstelas(renderizador));
        controlador.registrarComando(KeyCode.H, new ComandoAlternarZonasHabitables(renderizador));
        controlador.registrarComando(KeyCode.P, new ComandoAbrirProgresion(simulacion));

        controlador.registrarComando(KeyCode.PLUS, new ComandoAjustarFactorMasa(simulacion, 1.15));
        controlador.registrarComando(KeyCode.EQUALS, new ComandoAjustarFactorMasa(simulacion, 1.15));
        controlador.registrarComando(KeyCode.MINUS, new ComandoAjustarFactorMasa(simulacion, 1.0 / 1.15));
    }
}
