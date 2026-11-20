package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.example.game.render.cuerpo.RenderizadorEstrella;
import org.example.game.render.cuerpo.RenderizadorPlaneta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Escena astronómica animada permanente para el fondo del menú principal (SRP / DIP).
 */
public class EscenaFondoMenu {

    public static final int TOTAL_ESTRELLAS = 160;
    private static final double FRACCION_X_SOL = 0.16;
    private static final double FRACCION_X_PLANETA_PORRO = 0.84;
    private static final double FRACCION_Y_ASTROS = 0.50;
    private static final double VELOCIDAD_DERIVA_ESTELAR = 0.022;

    private record AstroEstelar(Vector2D posicionNormalizada, double tamano, double brilloBase, Vector2D dinamica) {}

    private static class EstadoEscenaMenu {
        final List<AstroEstelar> estrellas = new ArrayList<>(TOTAL_ESTRELLAS);
        final Estrella solIzquierdo;
        final Planeta planetaPorroDerecho;
        final RenderizadorEstrella renderizadorSol;
        final RenderizadorPlaneta renderizadorPlaneta;
        double tiempoAnimacion = 0.0;
        double desplazamientoEstrellas = 0.0;

        EstadoEscenaMenu(CatalogoSprites sprites) {
            this.renderizadorSol = new RenderizadorEstrella(sprites);
            this.renderizadorPlaneta = new RenderizadorPlaneta(sprites);
            this.solIzquierdo = new Estrella("Sol", TipoCuerpo.ESTRELLA.masaBase * 6.5, 0.0, 0.0);
            this.planetaPorroDerecho = new Planeta(
                    TipoCuerpo.PLANETA_ROCOSO.nombre, TipoCuerpo.PLANETA_ROCOSO.masaBase * 24.0,
                    0.0, 0.0, TipoCuerpo.PLANETA_ROCOSO
            );
        }

        void avanzarTiempo(double paso) {
            tiempoAnimacion += paso;
            desplazamientoEstrellas += VELOCIDAD_DERIVA_ESTELAR * paso;
        }
    }

    private static final FabricaRender FABRICA = new FabricaRender();
    private final EstadoEscenaMenu estado;

    public EscenaFondoMenu() {
        this(FABRICA.crearRepositorioSprites());
    }

    public EscenaFondoMenu(CatalogoSprites repositorioSprites) {
        CatalogoSprites sprites = repositorioSprites != null ? repositorioSprites : FABRICA.crearRepositorioSprites();
        this.estado = new EstadoEscenaMenu(sprites);
        inicializarCampoEstelar();
    }

    private void inicializarCampoEstelar() {
        Random generador = new Random(1337L);
        for (int i = 0; i < TOTAL_ESTRELLAS; i++) {
            Vector2D pos = new Vector2D(generador.nextDouble(), generador.nextDouble());
            double tam = 1.2 + (i % 3) * 0.9;
            double brillo = 0.28 + generador.nextDouble() * 0.52;
            Vector2D dinamica = new Vector2D(1.2 + generador.nextDouble() * 3.0, 0.4 + (i % 4) * 0.35);
            estado.estrellas.add(new AstroEstelar(pos, tam, brillo, dinamica));
        }
    }

    public void avanzar(double deltaSegundos) {
        if (!Double.isFinite(deltaSegundos) || deltaSegundos <= 0.0) return;
        estado.avanzarTiempo(Math.min(deltaSegundos, 0.25));
    }

    public void dibujar(GraphicsContext gc, double ancho, double alto) {
        if (gc == null || ancho <= 0.0 || alto <= 0.0) return;
        dibujarEspacioProfundo(gc, ancho, alto);
        dibujarCampoEstrellas(gc, ancho, alto);

        long tickVisual = Math.round(estado.tiempoAnimacion * 20.0);
        dibujarSolIzquierdo(gc, calcularPosicionSolX(ancho), calcularPosicionAstroY(alto), tickVisual);
        dibujarPlanetaPorroDerecho(gc, calcularPosicionPlanetaPorroX(ancho), calcularPosicionAstroY(alto), tickVisual);
    }

    private void dibujarEspacioProfundo(GraphicsContext gc, double ancho, double alto) {
        gc.setFill(Color.web("#080a12"));
        gc.fillRect(0.0, 0.0, ancho, alto);
        gc.setFill(Color.rgb(52, 34, 18, 0.16));
        gc.fillOval(ancho * 0.02, alto * 0.20, ancho * 0.32, alto * 0.60);
        gc.setFill(Color.rgb(20, 58, 48, 0.16));
        gc.fillOval(ancho * 0.66, alto * 0.22, ancho * 0.30, alto * 0.56);
    }

    private void dibujarCampoEstrellas(GraphicsContext gc, double ancho, double alto) {
        for (int i = 0; i < estado.estrellas.size(); i++) {
            AstroEstelar astro = estado.estrellas.get(i);
            double xNorm = (astro.posicionNormalizada().x() + estado.desplazamientoEstrellas * astro.dinamica().y()) % 1.0;
            if (xNorm < 0.0) xNorm += 1.0;
            double pulso = astro.brilloBase() + 0.28 * Math.sin(estado.tiempoAnimacion * astro.dinamica().x() + i);
            gc.setFill(Color.color(0.92, 0.94, 1.0, Math.max(0.12, Math.min(1.0, pulso))));
            gc.fillOval(xNorm * ancho, astro.posicionNormalizada().y() * alto, astro.tamano(), astro.tamano());
        }
    }

    private void dibujarSolIzquierdo(GraphicsContext gc, double cx, double cy, long tickVisual) {
        double radio = estado.solIzquierdo.getRadio();
        double resplandor = radio * (1.85 + 0.10 * Math.sin(estado.tiempoAnimacion * 2.2));
        gc.save();
        gc.translate(cx, cy);
        gc.setFill(Color.rgb(255, 190, 50, 0.14));
        gc.fillOval(-resplandor, -resplandor, resplandor * 2.0, resplandor * 2.0);
        estado.renderizadorSol.dibujar(gc, estado.solIzquierdo, TipoCuerpo.ESTRELLA, radio, tickVisual);
        gc.restore();
    }

    private void dibujarPlanetaPorroDerecho(GraphicsContext gc, double cx, double cy, long tickVisual) {
        double radio = estado.planetaPorroDerecho.getRadio();
        double atmosfera = radio * 1.42;
        gc.save();
        gc.translate(cx, cy);
        gc.setFill(Color.rgb(72, 187, 120, 0.16));
        gc.fillOval(-atmosfera, -atmosfera, atmosfera * 2.0, atmosfera * 2.0);
        gc.rotate((estado.tiempoAnimacion * 14.0) % 360.0);
        estado.renderizadorPlaneta.dibujar(gc, estado.planetaPorroDerecho, TipoCuerpo.PLANETA_ROCOSO, radio, tickVisual);
        gc.restore();
    }

    public double calcularPosicionSolX(double ancho) { return ancho * FRACCION_X_SOL; }
    public double calcularPosicionPlanetaPorroX(double ancho) { return ancho * FRACCION_X_PLANETA_PORRO; }
    public double calcularPosicionAstroY(double alto) { return alto * FRACCION_Y_ASTROS; }
    public Estrella getSolIzquierdo() { return estado.solIzquierdo; }
    public Planeta getPlanetaPorroDerecho() { return estado.planetaPorroDerecho; }
    public double getTiempoAnimacion() { return estado.tiempoAnimacion; }
    public double getDesplazamientoEstrellas() { return estado.desplazamientoEstrellas; }
    public int getCantidadEstrellas() { return estado.estrellas.size(); }
}
