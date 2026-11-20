package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.GestorAgujeroNegro;
import org.example.game.render.capa.CapaEscudoPlanetario;
import org.example.game.render.cuerpo.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * SRP: Despacha el renderizado individual de cada cuerpo celeste, su rotación axial, escudo y etiqueta.
 */
public class DespachadorRenderCuerpo {

    private final Map<TipoCuerpo, RenderizadorCuerpo> renderizadores = new EnumMap<>(TipoCuerpo.class);
    private final CapaEscudoPlanetario capaEscudo;

    public DespachadorRenderCuerpo(CatalogoSprites catalogoSprites) {
        this.capaEscudo = new CapaEscudoPlanetario(catalogoSprites);
        inicializarPorDefecto(catalogoSprites);
    }

    private void inicializarPorDefecto(CatalogoSprites sprites) {
        RenderizadorPlaneta planetaRenderer = new RenderizadorPlaneta(sprites);
        RenderizadorAgujeroNegro agujeroRenderer = new RenderizadorAgujeroNegro();
        registrar(TipoCuerpo.ESTRELLA, new RenderizadorEstrella(sprites));
        registrar(TipoCuerpo.PLANETA_ROCOSO, planetaRenderer);
        registrar(TipoCuerpo.PLANETA_AGUA, planetaRenderer);
        registrar(TipoCuerpo.PLANETA_LAVA, planetaRenderer);
        registrar(TipoCuerpo.PLANETA_GASEOSO, planetaRenderer);
        registrar(TipoCuerpo.PLANETA_HELADO, planetaRenderer);
        registrar(TipoCuerpo.LUNA, new RenderizadorLuna(sprites));
        registrar(TipoCuerpo.SATELITE, new RenderizadorSatelite());
        registrar(TipoCuerpo.METEORITO, new RenderizadorMeteorito());
        registrar(TipoCuerpo.AGUJERO_NEGRO, agujeroRenderer);
        registrar(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, agujeroRenderer);
    }

    public void registrar(TipoCuerpo tipo, RenderizadorCuerpo renderizador) {
        if (tipo != null && renderizador != null) {
            renderizadores.put(tipo, renderizador);
        }
    }

    public void dibujarTodos(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick,
                             double tiempoAnimacion, ConversorCoordenadas conversor,
                             double anchoCanvas, double altoCanvas) {
        for (CuerpoCeleste c : cuerpos) {
            dibujarCuerpo(gc, c, cuerpos, tick, tiempoAnimacion, conversor, anchoCanvas, altoCanvas);
        }
    }

    private void dibujarCuerpo(GraphicsContext gc, CuerpoCeleste c, List<CuerpoCeleste> cuerpos,
                               long tick, double tiempoAnimacion, ConversorCoordenadas conversor,
                               double anchoCanvas, double altoCanvas) {
        double x = Math.round(conversor.fisicaAXJavaFX(c.getPosicion().x(), anchoCanvas));
        double y = Math.round(conversor.fisicaAYJavaFX(c.getPosicion().y(), altoCanvas));
        double r = c.getRadio();

        dibujarSpriteTransformado(gc, c, cuerpos, tick, tiempoAnimacion, x, y, r);
        if (ServicioEscudo.getInstanciaPorDefecto().tieneEscudo(c)) {
            capaEscudo.dibujarCupula(gc, x, y, r, tick);
        }
        dibujarNombre(gc, c, x, y, r);
    }

    private void dibujarSpriteTransformado(GraphicsContext gc, CuerpoCeleste c, List<CuerpoCeleste> cuerpos,
                                           long tick, double tiempoAnimacion, double x, double y, double r) {
        double angulo = calcularAnguloRotacion(c, r, tiempoAnimacion);
        double escalaBH = GestorAgujeroNegro.calcularFactorEscala(c, cuerpos);
        double rotacionBH = GestorAgujeroNegro.calcularRotacionExtra(c, cuerpos, tick);

        gc.save();
        gc.translate(x, y);
        if (escalaBH < 1.0) gc.scale(escalaBH, escalaBH);
        if (angulo != 0.0 || rotacionBH != 0.0) gc.rotate(angulo + rotacionBH);

        TipoCuerpo tipo = c.getTipoCuerpo();
        RenderizadorCuerpo renderer = renderizadores.get(tipo);
        if (renderer != null) {
            renderer.dibujar(gc, c, tipo, r, tick);
        } else {
            UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb(c.getColorHex()));
        }
        gc.restore();
    }

    private double calcularAnguloRotacion(CuerpoCeleste c, double r, double tiempoAnimacion) {
        if (c.getMasa() >= 1e29) return 0.0;
        double velAngular = 57.2958 / Math.max(6.0, r);
        double faseInicial = (c.getNombre() != null) ? Math.abs(c.getNombre().hashCode() % 360) : 0;
        return (faseInicial + tiempoAnimacion * velAngular) % 360.0;
    }

    private void dibujarNombre(GraphicsContext gc, CuerpoCeleste c, double x, double y, double r) {
        gc.setFill(UtilidadRender.colorWeb("#e8e4d8"));
        gc.setGlobalAlpha(0.85);
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(c.getNombre(), x, y + r + 14);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setGlobalAlpha(1.0);
    }
}
