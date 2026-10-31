package org.example.game.controlador.comando;
import org.example.game.render.RenderizadorPixelArt;
/**
 * Clase base para comandos que interactuan con el renderizador visual.
 *
 * Principios SOLID:
 * - SRP: Encapsula la referencia y comprobacion de nulidad del renderizador.
 * - DRY: Centraliza campos comunes entre comandos de alternancia visual.
 */
public abstract class ComandoRenderizador implements Comando {

    protected final RenderizadorPixelArt renderizador;

    public ComandoRenderizador(RenderizadorPixelArt renderizador) {
        this.renderizador = renderizador;
    }
}
