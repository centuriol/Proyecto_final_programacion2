package org.example.game.controlador.comando;

import javafx.stage.Window;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.VentanaProgresion;

/**
 * Comando para abrir la pantalla del árbol de investigación tecnológica por ciencia.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de invocar la acción de visualización de progresión.
 * - DIP: Desacoplado mediante abstracción Runnable y SimulacionSolar.
 */
public class ComandoAbrirProgresion implements Comando {

    private final SimulacionSolar simulacion;
    private Window owner;
    private Runnable accionAbrir;

    public ComandoAbrirProgresion(SimulacionSolar simulacion, Window owner) {
        this.simulacion = simulacion;
        this.owner = owner;
    }

    public ComandoAbrirProgresion(SimulacionSolar simulacion) {
        this(simulacion, (Window) null);
    }

    public ComandoAbrirProgresion(SimulacionSolar simulacion, Runnable accionAbrir) {
        this.simulacion = simulacion;
        this.accionAbrir = accionAbrir;
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }

    public void setAccionAbrir(Runnable accionAbrir) {
        this.accionAbrir = accionAbrir;
    }

    @Override
    public void ejecutar() {
        if (accionAbrir != null) {
            accionAbrir.run();
        } else {
            VentanaProgresion.mostrar(simulacion, owner);
        }
    }
}
