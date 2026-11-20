package org.example.game.ui;

import org.example.game.render.FabricaRender;
import org.example.game.render.RepositorioSprites;

/**
 * Fábrica de composición para los colaboradores por defecto del subsistema de interfaz de usuario (DIP).
 */
public class FabricaUI {

    private static final FabricaRender FABRICA_RENDER = new FabricaRender();

    public ProveedorInfoCuerpo crearProveedorInfoCuerpo() {
        return new SeccionInfoCuerpo(crearRepositorioSprites());
    }

    public VisualizadorDescripcion crearVisualizadorDescripcion() {
        return new VentanaDescripcionCuerpo(crearProveedorInfoCuerpo());
    }

    public RepositorioSprites crearRepositorioSprites() {
        return FABRICA_RENDER.crearRepositorioSprites();
    }
}
