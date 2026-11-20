package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.efectos.GestorEfectos;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.Vector2D;
import org.example.game.render.capa.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * SRP / DIP: Agrupa y coordina el dibujo de las capas visuales superpuestas del mundo (zonas, estelas, naves, efectos, previews).
 */
public class CapasVisualesSimulacion {

    private static final FabricaRender FABRICA = new FabricaRender();

    private static class ConjuntoCapas {
        final GestorEstelas gestorEstelas;
        final ConversorCoordenadas conversor = FABRICA.crearConversorCoordenadas();
        final CapaZonasHabitables capaZonasHabitables;
        final CapaAreaAtraccion capaAreaAtraccion;
        final CapaSeleccion capaSeleccion;
        final CapaTrayectorias capaTrayectorias;
        final CapaGhostPreview capaGhostPreview;
        final CapaNavesColonizadoras capaNaves = new CapaNavesColonizadoras();
        final RepositorioSprites repositorioSprites;
        Supplier<List<NaveColonizadora>> proveedorNaves;
        GestorEfectos gestorEfectos;
        boolean mostrarGrilla = false;
        boolean mostrarEstelas = true;
        boolean mostrarZonasHabitables = true;

        ConjuntoCapas(GestorEstelas ge, RepositorioSprites sprites,
                      CapaZonasHabitables czh, CapaAreaAtraccion caa,
                      CapaSeleccion cs, CapaTrayectorias ct, CapaGhostPreview cgp) {
            this.repositorioSprites = sprites != null ? sprites : FABRICA.crearRepositorioSprites();
            this.gestorEstelas = ge != null ? ge : FABRICA.crearGestorEstelas();
            this.capaZonasHabitables = czh != null ? czh : FABRICA.crearCapaZonasHabitables();
            this.capaAreaAtraccion = caa != null ? caa : FABRICA.crearCapaAreaAtraccion();
            this.capaSeleccion = cs != null ? cs : FABRICA.crearCapaSeleccion();
            this.capaTrayectorias = ct != null ? ct : FABRICA.crearCapaTrayectorias();
            this.capaGhostPreview = cgp != null ? cgp : FABRICA.crearCapaGhostPreview(this.repositorioSprites);
        }

        void asociarEfectos(GestorEfectos efectos) {
            this.gestorEfectos = efectos;
            if (this.gestorEfectos != null) {
                this.gestorEfectos.setRepositorioSprites(this.repositorioSprites);
            }
        }
    }

    private final ConjuntoCapas capas;
    private final DespachadorRenderCuerpo despachadorCuerpos;

    public CapasVisualesSimulacion(GestorEstelas gestorEstelas, RepositorioSprites sprites,
                                   CapaZonasHabitables capaZonas, CapaAreaAtraccion capaAtraccion,
                                   CapaSeleccion capaSeleccion, CapaTrayectorias capaTrayectorias,
                                   CapaGhostPreview capaGhost) {
        this.capas = new ConjuntoCapas(gestorEstelas, sprites, capaZonas, capaAtraccion, capaSeleccion, capaTrayectorias, capaGhost);
        this.despachadorCuerpos = new DespachadorRenderCuerpo(this.capas.repositorioSprites);
    }

    public void registrarRenderizadorCuerpo(TipoCuerpo tipo, RenderizadorCuerpo renderizador) {
        despachadorCuerpos.registrar(tipo, renderizador);
    }

    public void setProveedorNaves(Supplier<List<NaveColonizadora>> proveedorNaves) {
        capas.proveedorNaves = proveedorNaves;
    }

    public void setGestorEfectos(GestorEfectos gestorEfectos) {
        capas.asociarEfectos(gestorEfectos);
    }

    public void dibujarCapasInferiores(GraphicsContext gc, List<CuerpoCeleste> cuerpos,
                                       RenderizadorFondo fondo, CamaraZoom camara,
                                       double anchoCanvas, double altoCanvas) {
        if (capas.mostrarGrilla) fondo.dibujarGrilla(gc, anchoCanvas, altoCanvas, camara);
        if (capas.mostrarZonasHabitables) capas.capaZonasHabitables.dibujar(gc, cuerpos, anchoCanvas, altoCanvas);
        capas.capaAreaAtraccion.dibujar(gc, cuerpos, anchoCanvas, altoCanvas);
        if (capas.mostrarEstelas) capas.gestorEstelas.dibujar(gc, anchoCanvas, altoCanvas);
    }

    public void dibujarCuerposYEntidades(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick,
                                         double tiempoAnimacion, double anchoCanvas, double altoCanvas) {
        despachadorCuerpos.dibujarTodos(gc, cuerpos, tick, tiempoAnimacion, capas.conversor, anchoCanvas, altoCanvas);
        capas.capaNaves.dibujar(gc, capas.proveedorNaves, capas.conversor, anchoCanvas, altoCanvas);
        if (capas.gestorEfectos != null) {
            capas.gestorEfectos.dibujar(gc, anchoCanvas, altoCanvas);
        }
    }

    public void dibujarOverlaysInteraccion(GraphicsContext gc, List<CuerpoCeleste> cuerpos,
                                           CuerpoCeleste seleccionado, List<Vector2D> trayectoriaPreview,
                                           Vector2D previewPos, TipoCuerpo previewTipo,
                                           double previewFactorMasa, String previewNombre,
                                           long tick, double tiempoAnimacion,
                                           double anchoCanvas, double altoCanvas) {
        if (seleccionado != null && cuerpos.contains(seleccionado)) {
            capas.capaSeleccion.dibujar(gc, seleccionado, anchoCanvas, altoCanvas);
        }
        if (trayectoriaPreview != null && !trayectoriaPreview.isEmpty()) {
            capas.capaTrayectorias.dibujar(gc, trayectoriaPreview, anchoCanvas, altoCanvas);
        }
        if (previewTipo != null && previewPos != null) {
            capas.capaGhostPreview.dibujar(gc, previewPos, previewTipo, previewFactorMasa, previewNombre,
                    tick, tiempoAnimacion, anchoCanvas, altoCanvas);
        }
    }

    public void actualizarEstelas(List<CuerpoCeleste> cuerpos) { capas.gestorEstelas.actualizar(cuerpos); }
    public void limpiarEstelas() { capas.gestorEstelas.limpiar(); }
    public void toggleGrilla() { capas.mostrarGrilla = !capas.mostrarGrilla; }
    public void toggleEstelas() { capas.mostrarEstelas = !capas.mostrarEstelas; }
    public void toggleZonasHabitables() { capas.mostrarZonasHabitables = !capas.mostrarZonasHabitables; }
    public RepositorioSprites getRepositorioSprites() { return capas.repositorioSprites; }
}
