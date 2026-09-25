package org.example.game.controlador;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.example.game.controlador.comando.*;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ControlTiempoWidget;

import java.util.EnumMap;
import java.util.Map;

/**
 * Controlador que encapsula la recepción y procesamiento de eventos de teclado.
 *
 * Principios SOLID:
 * - SRP: Encapsula la gestión de atajos de teclado sin mezclar lógica de interfaz ni de física.
 * - OCP: El mapeo de teclas se basa en el patrón Command; se pueden asociar o modificar comandos sin editar esta clase.
 * - DIP: Depende de la interfaz Comando, permitiendo inyectar cualquier acción desacoplada.
 */
public class ControladorTeclado implements EventHandler<KeyEvent> {

    private final Map<KeyCode, Comando> mapaComandos = new EnumMap<>(KeyCode.class);

    public void registrarComando(KeyCode tecla, Comando comando) {
        if (tecla != null && comando != null) {
            mapaComandos.put(tecla, comando);
        }
    }

    public void desregistrarComando(KeyCode tecla) {
        if (tecla != null) {
            mapaComandos.remove(tecla);
        }
    }

    public Comando obtenerComando(KeyCode tecla) {
        return mapaComandos.get(tecla);
    }

    public boolean tieneComando(KeyCode tecla) {
        return mapaComandos.containsKey(tecla);
    }

    @Override
    public void handle(KeyEvent event) {
        if (event == null) return;
        Comando comando = mapaComandos.get(event.getCode());
        if (comando != null) {
            comando.ejecutar();
            event.consume();
        }
    }

    /**
     * Configura los comandos y atajos de teclado predeterminados del juego.
     */
    public void configurarAtajosPorDefecto(
            SimulacionSolar simulacion,
            ControlTiempoWidget controlTiempo,
            BarraInventarioHotbar hotbar,
            RenderizadorPixelArt renderizador,
            ControladorMouse controladorMouse
    ) {
        registrarComando(KeyCode.SPACE, new ComandoAlternarPausa(simulacion, controlTiempo));
        registrarComando(KeyCode.S, new ComandoAvanzarPaso(simulacion, controlTiempo));
        registrarComando(KeyCode.ESCAPE, new ComandoCancelar(simulacion, hotbar, controladorMouse));

        registrarComando(KeyCode.DIGIT1, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.ESTRELLA));
        registrarComando(KeyCode.DIGIT2, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.PLANETA_ROCOSO));
        registrarComando(KeyCode.DIGIT3, new ComandoSeleccionarTipoCuerpo(hotbar, TipoCuerpo.LUNA));

        registrarComando(KeyCode.G, new ComandoAlternarGrilla(renderizador));
        registrarComando(KeyCode.T, new ComandoAlternarEstelas(renderizador));
        registrarComando(KeyCode.H, new ComandoAlternarZonasHabitables(renderizador));

        registrarComando(KeyCode.PLUS, new ComandoAjustarFactorMasa(simulacion, 1.15));
        registrarComando(KeyCode.EQUALS, new ComandoAjustarFactorMasa(simulacion, 1.15));
        registrarComando(KeyCode.MINUS, new ComandoAjustarFactorMasa(simulacion, 1.0 / 1.15));
    }
}
