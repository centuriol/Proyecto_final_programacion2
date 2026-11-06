package org.example.game.render.cuerpo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.civilizacion.SistemaCivilizaciones;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.EstrategiaVisualTipo;
import org.example.game.render.RegistroEstrategiasVisuales;
import org.example.game.render.RenderizadorCuerpo;
import org.example.game.render.RepositorioSprites;

import java.util.Objects;

/**
 * Renderizador Pixel Art para planetas delegando en estrategias visuales por tipo (SRP / OCP).
 */
public class RenderizadorPlaneta implements RenderizadorCuerpo {

    public static final String[] SPRITES_PERSONAS = {
            "gordo", "gordo1", "gordo2", "gordo3", "gordo4",
            "gordo5", "gordo6", "gordo7", "gordo8", "gordo9", "gordo10"
    };

    public static final double[] UMBRALES_POBLACION_PERSONAS = {
            100.0, 1_000.0, 2_000.0, 3_500.0, 5_000.0, 7_500.0,
            10_000.0, 15_000.0, 22_000.0, 32_000.0, 45_000.0, 65_000.0,
            90_000.0, 125_000.0, 175_000.0, 230_000.0, 300_000.0, 380_000.0,
            470_000.0, 570_000.0, 670_000.0, 780_000.0, 890_000.0, 1_000_000.0
    };

    private final RepositorioSprites repositorioSprites;

    public RenderizadorPlaneta(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites;
    }

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste c, TipoCuerpo tipo, double r, long tick) {
        if (intentarDibujarSpritePlaneta(gc, c, tipo)) return;

        EstrategiaVisualTipo estrategia = RegistroEstrategiasVisuales.get(tipo);
        estrategia.dibujarSuperficie(gc, c, r);
        dibujarPersonasSobrePlaneta(gc, c, tipo, Math.max(22.0, r * 2.0));
    }

    private boolean intentarDibujarSpritePlaneta(GraphicsContext gc, CuerpoCeleste c, TipoCuerpo tipo) {
        if (repositorioSprites == null) return false;
        Image imgPlaneta = repositorioSprites.obtenerImagen(tipo);
        if (imgPlaneta == null) return false;

        double r = c != null ? c.getRadio() : 15.0;
        double tam = Math.max(22.0, r * 2.0);
        gc.drawImage(imgPlaneta, Math.round(-tam / 2.0), Math.round(-tam / 2.0), tam, tam);
        dibujarPersonasSobrePlaneta(gc, c, tipo, tam);
        return true;
    }

    private void dibujarPersonasSobrePlaneta(GraphicsContext gc, CuerpoCeleste c, TipoCuerpo tipo, double tamPlaneta) {
        if (gc == null || repositorioSprites == null || tipo == null || !tipo.esPlaneta()) return;

        double poblacion = obtenerPoblacionPlaneta(c);
        int cantidad = calcularCantidadPersonasVisibles(poblacion);
        if (cantidad <= 0) return;

        double tamPersona = Math.max(3.5, tamPlaneta * 0.14);
        double radioBorde = (tamPlaneta / 2.0) + (tamPersona * 0.25);
        double pasoAngular = (2.0 * Math.PI) / UMBRALES_POBLACION_PERSONAS.length;

        gc.save();
        gc.setImageSmoothing(false);
        for (int i = 0; i < cantidad; i++) {
            String nombreSprite = SPRITES_PERSONAS[i % SPRITES_PERSONAS.length];
            Image imgPersona = repositorioSprites.obtenerImagenPorNombre(nombreSprite);
            if (imgPersona == null) continue;

            double angulo = calcularAnguloBorde(i, pasoAngular);
            double px = Math.cos(angulo) * radioBorde;
            double py = Math.sin(angulo) * radioBorde;

            gc.save();
            gc.translate(px, py);
            gc.rotate(Math.toDegrees(angulo) + 90.0);
            gc.drawImage(imgPersona, -tamPersona / 2.0, -tamPersona / 2.0, tamPersona, tamPersona);
            gc.restore();
        }
        gc.restore();
    }

    private double calcularAnguloBorde(int indice, double pasoAngular) {
        if (indice == 0) return -Math.PI / 2.0;
        int par = (indice + 1) / 2;
        double signo = (indice % 2 == 1) ? 1.0 : -1.0;
        return (-Math.PI / 2.0) + signo * par * pasoAngular;
    }

    public static int calcularCantidadPersonasVisibles(double poblacion) {
        int cantidad = 0;
        for (double umbral : UMBRALES_POBLACION_PERSONAS) {
            if (poblacion >= umbral) {
                cantidad++;
            } else {
                break;
            }
        }
        return Math.min(cantidad, UMBRALES_POBLACION_PERSONAS.length);
    }

    private double obtenerPoblacionPlaneta(CuerpoCeleste c) {
        if (c == null) return 0.0;
        return SistemaCivilizaciones.getInstancia().obtenerCivilizable(c)
                .filter(Civilizable::tieneCivilizacion)
                .map(Civilizable::getCivilizacion)
                .filter(Objects::nonNull)
                .map(CivilizacionComponent::getPoblacion)
                .orElse(0.0);
    }
}
