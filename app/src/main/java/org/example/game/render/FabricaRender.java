package org.example.game.render;

import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.render.capa.*;

/**
 * Fábrica de composición para los colaboradores por defecto del subsistema de renderizado (DIP).
 */
public class FabricaRender {

    public RepositorioSprites crearRepositorioSprites() {
        return new GestorImagenes();
    }

    public ConversorCoordenadas crearConversorCoordenadas() {
        return new TransformadorCoordenadas();
    }

    public CamaraZoom crearCamaraZoom() {
        return new CamaraZoom();
    }

    public ProyeccionCamara crearProyeccionCamara(double nivelZoom) {
        return new CamaraZoom(nivelZoom, CamaraZoom.INCREMENTO_POR_DEFECTO);
    }

    public RenderizadorFondo crearRenderizadorFondo(double ancho, double alto) {
        return new RenderizadorFondo(ancho, alto, crearRepositorioSprites());
    }

    public GestorEstelas crearGestorEstelas() {
        return new GestorEstelas(crearConversorCoordenadas());
    }

    public CapaZonasHabitables crearCapaZonasHabitables() {
        return new CapaZonasHabitables(crearConversorCoordenadas());
    }

    public CapaAreaAtraccion crearCapaAreaAtraccion() {
        return new CapaAreaAtraccion(crearConversorCoordenadas());
    }

    public CapaSeleccion crearCapaSeleccion() {
        return new CapaSeleccion(crearConversorCoordenadas());
    }

    public CapaTrayectorias crearCapaTrayectorias() {
        return new CapaTrayectorias(crearConversorCoordenadas());
    }

    public CapaGhostPreview crearCapaGhostPreview(CatalogoSprites sprites) {
        return new CapaGhostPreview(sprites, crearConversorCoordenadas());
    }
}
