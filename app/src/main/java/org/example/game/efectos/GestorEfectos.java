package org.example.game.efectos;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.Vector2D;
import org.example.game.render.CatalogoSprites;
import org.example.game.render.FabricaRender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Gestor de efectos visuales de explosión animada por secuencia de sprites en colisiones (SRP / ISP / DIP).
 */
public class GestorEfectos {

    private static final FabricaRender FABRICA_RENDER = new FabricaRender();

    public static final List<String> SPRITES_EXPLOSION = List.of(
            "explosion1", "explosion2", "explosion3",
            "explosion4", "explosion5", "explosion6",
            "explosion7", "explosion8", "explosion9"
    );
    private static final int TICKS_POR_FRAME_GIF = 4;
    private static final int DURACION_FRAMES_EXPLOSION = SPRITES_EXPLOSION.size() * TICKS_POR_FRAME_GIF;

    private static class ContextoRenderEfectos {
        final ConversorCoordenadas conversor;
        CatalogoSprites repositorioSprites;

        ContextoRenderEfectos(ConversorCoordenadas conv, CatalogoSprites sprites) {
            this.conversor = conv != null ? conv : FABRICA_RENDER.crearConversorCoordenadas();
            this.repositorioSprites = sprites != null ? sprites : FABRICA_RENDER.crearRepositorioSprites();
        }

        void dibujarTodos(List<EfectoExplosion> explosiones, GraphicsContext gc, double ancho, double alto) {
            for (EfectoExplosion e : explosiones) {
                e.dibujar(gc, ancho, alto, conversor, repositorioSprites);
            }
        }
    }

    private final List<EfectoExplosion> explosiones = new ArrayList<>();
    private final ContextoRenderEfectos render;

    public GestorEfectos() {
        this(FABRICA_RENDER.crearConversorCoordenadas(), FABRICA_RENDER.crearRepositorioSprites());
    }

    public GestorEfectos(ConversorCoordenadas conversor) {
        this(conversor, FABRICA_RENDER.crearRepositorioSprites());
    }

    public GestorEfectos(ConversorCoordenadas conversor, CatalogoSprites repositorioSprites) {
        this.render = new ContextoRenderEfectos(conversor, repositorioSprites);
    }

    public void setRepositorioSprites(CatalogoSprites repositorioSprites) {
        if (repositorioSprites != null) render.repositorioSprites = repositorioSprites;
    }

    public void agregarExplosion(Vector2D posicionFisica, double radioInicial, Color color) {
        if (posicionFisica != null) {
            explosiones.add(new EfectoExplosion(posicionFisica, radioInicial, color, DURACION_FRAMES_EXPLOSION));
        }
    }

    public void agregarExplosion(Vector2D posicionFisica, double radioInicial) {
        agregarExplosion(posicionFisica, radioInicial, Color.ORANGE);
    }

    public void actualizar() {
        Iterator<EfectoExplosion> it = explosiones.iterator();
        while (it.hasNext()) {
            if (it.next().avanzar()) it.remove();
        }
    }

    public void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas) {
        render.dibujarTodos(explosiones, gc, anchoCanvas, altoCanvas);
    }

    public int getCantidadExplosionesActivas() { return explosiones.size(); }
    public void limpiar() { explosiones.clear(); }
}
