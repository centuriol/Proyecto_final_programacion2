package org.example.game.controlador;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.example.game.controlador.comando.Comando;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ControlTiempoWidget;
import java.util.HashMap;
import java.util.Map;
/**
 * Controlador que encapsula la recepcion y procesamiento de eventos de teclado.
 *
 * Principios SOLID:
 * - SRP: Encapsula exclusivamente el despacho de eventos de teclado a comandos.
 * - OCP: El mapeo soporta cualquier combinacion de teclas y modificadores via CombinacionTeclas.
 * - DIP: Depende de la interfaz Comando, permitiendo inyectar cualquier accion desacoplada.
 */
public class ControladorTeclado implements EventHandler<KeyEvent> {

    private final Map<CombinacionTeclas, Comando> mapaComandos = new HashMap<>();
    private final ConfiguradorAtajosTeclado configuradorAtajos;

    public ControladorTeclado() {
        this(new ConfiguradorAtajosTeclado());
    }

    public ControladorTeclado(ConfiguradorAtajosTeclado configuradorAtajos) {
        this.configuradorAtajos = configuradorAtajos != null ? configuradorAtajos : new ConfiguradorAtajosTeclado();
    }

    public void registrarComando(CombinacionTeclas combinacion, Comando comando) {
        if (combinacion != null && comando != null) {
            mapaComandos.put(combinacion, comando);
        }
    }

    public void registrarComando(KeyCode tecla, Comando comando) {
        if (tecla != null && comando != null) {
            registrarComando(CombinacionTeclas.de(tecla), comando);
        }
    }

    public void desregistrarComando(CombinacionTeclas combinacion) {
        if (combinacion != null) {
            mapaComandos.remove(combinacion);
        }
    }

    public void desregistrarComando(KeyCode tecla) {
        if (tecla != null) {
            desregistrarComando(CombinacionTeclas.de(tecla));
        }
    }

    public Comando obtenerComando(CombinacionTeclas combinacion) {
        return mapaComandos.get(combinacion);
    }

    public Comando obtenerComando(KeyCode tecla) {
        return tecla != null ? mapaComandos.get(CombinacionTeclas.de(tecla)) : null;
    }

    public boolean tieneComando(CombinacionTeclas combinacion) {
        return mapaComandos.containsKey(combinacion);
    }

    public boolean tieneComando(KeyCode tecla) {
        return tecla != null && mapaComandos.containsKey(CombinacionTeclas.de(tecla));
    }

    @Override
    public void handle(KeyEvent event) {
        if (event == null || event.getCode() == null) return;

        CombinacionTeclas combExacta = CombinacionTeclas.de(event);
        Comando comando = mapaComandos.get(combExacta);

        // Si no hay comando con modificador especifico, pero la tecla sola fue registrada
        if (comando == null && !event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && !event.isMetaDown()) {
            comando = mapaComandos.get(CombinacionTeclas.de(event.getCode()));
        }

        if (comando != null) {
            comando.ejecutar();
            event.consume();
        }
    }

    /**
     * Configura los comandos y atajos de teclado predeterminados del juego delegando a ConfiguradorAtajosTeclado (SRP).
     */
    public void configurarAtajosPorDefecto(
            SimulacionSolar sim, ControlTiempoWidget tiempo, BarraInventarioHotbar bar,
            RenderizadorPixelArt render, ControladorMouse mouse
    ) {
        configuradorAtajos.configurar(this, sim, tiempo, bar, render, mouse);
    }
}
