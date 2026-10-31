package org.example.game.controlador;
import javafx.scene.input.KeyCode; import org.example.game.controlador.comando.*; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.render.RenderizadorPixelArt; import org.example.game.simulacion.SimulacionSolar; import org.example.game.ui.BarraInventarioHotbar; import org.example.game.ui.ControlTiempoWidget;
/**
 * Factoria y configurador de atajos predeterminados del juego dividido por áreas funcionales (SRP).
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

        configurarAtajosSimulacion(controlador, simulacion, controlTiempo, hotbar, controladorMouse);
        configurarAtajosHotbar(controlador, hotbar);
        configurarAtajosVisuales(controlador, renderizador, simulacion);
        configurarAtajosMasa(controlador, simulacion);
    }

    private static void configurarAtajosSimulacion(ControladorTeclado controlador, SimulacionSolar simulacion,
                                                   ControlTiempoWidget controlTiempo, BarraInventarioHotbar hotbar,
                                                   ControladorMouse mouse) {
        controlador.registrarComando(KeyCode.SPACE, new ComandoAlternarPausa(simulacion, controlTiempo));
        controlador.registrarComando(KeyCode.S, new ComandoAvanzarPaso(simulacion, controlTiempo));
        controlador.registrarComando(KeyCode.ESCAPE, new ComandoCancelar(simulacion, hotbar, mouse));
    }

    private static void configurarAtajosHotbar(ControladorTeclado controlador, BarraInventarioHotbar hotbar) {
        controlador.registrarComando(KeyCode.DIGIT1, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.ESTRELLA));
        controlador.registrarComando(KeyCode.DIGIT2, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.PLANETA_ROCOSO));
        controlador.registrarComando(KeyCode.DIGIT3, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.LUNA));
        controlador.registrarComando(KeyCode.DIGIT4, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.PLANETA_AGUA));
        controlador.registrarComando(KeyCode.DIGIT5, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.SATELITE));
        controlador.registrarComando(KeyCode.DIGIT6, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.PLANETA_LAVA));
    }

    private static void configurarAtajosVisuales(ControladorTeclado controlador, RenderizadorPixelArt renderizador, SimulacionSolar simulacion) {
        controlador.registrarComando(KeyCode.G, new ComandoAlternarGrilla(renderizador));
        controlador.registrarComando(KeyCode.T, new ComandoAlternarEstelas(renderizador));
        controlador.registrarComando(KeyCode.H, new ComandoAlternarZonasHabitables(renderizador));
        controlador.registrarComando(KeyCode.P, new ComandoAbrirProgresion(simulacion));
    }

    private static void configurarAtajosMasa(ControladorTeclado controlador, SimulacionSolar simulacion) {
        controlador.registrarComando(KeyCode.PLUS, new ComandoAjustarFactorMasa(simulacion, 1.15));
        controlador.registrarComando(KeyCode.EQUALS, new ComandoAjustarFactorMasa(simulacion, 1.15));
        controlador.registrarComando(KeyCode.MINUS, new ComandoAjustarFactorMasa(simulacion, 1.0 / 1.15));
    }
}
