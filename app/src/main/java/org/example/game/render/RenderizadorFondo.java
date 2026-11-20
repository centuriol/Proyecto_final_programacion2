package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Coordina el renderizado del fondo espacial delegando estrellas, grilla y parallax (SRP).
 */
public class RenderizadorFondo {

    public static final double FACTOR_PARALLAX_POR_DEFECTO = 0.4;

    private static final Color COLOR_FONDO_ESPACIAL = Color.web("#0e1017");
    private static final Color COLOR_NEBULOSA_1 = Color.rgb(40, 20, 60, 0.22);
    private static final Color COLOR_NEBULOSA_2 = Color.rgb(15, 45, 65, 0.18);

    private static final FabricaRender FABRICA = new FabricaRender();

    private static class RecursosFondo {
        final CatalogoSprites repositorioSprites;
        final CapaEstrellasFondo capaEstrellas = new CapaEstrellasFondo();
        final CapaGrillaEspacial capaGrilla = new CapaGrillaEspacial();
        Image imagenFondo;

        RecursosFondo(CatalogoSprites repo) {
            this.repositorioSprites = repo != null ? repo : FABRICA.crearRepositorioSprites();
            this.imagenFondo = this.repositorioSprites.obtenerImagenFondo();
        }

        Image resolverImagenFondo() {
            if (imagenFondo == null) {
                imagenFondo = repositorioSprites.obtenerImagenFondo();
            }
            return imagenFondo;
        }
    }

    private final CalculadorParallaxFondo parallax;
    private final RecursosFondo recursos;

    public RenderizadorFondo(double ancho, double alto) {
        this(ancho, alto, FABRICA.crearRepositorioSprites());
    }

    public RenderizadorFondo(double ancho, double alto, CatalogoSprites repositorioSprites) {
        this.recursos = new RecursosFondo(repositorioSprites);
        this.parallax = new CalculadorParallaxFondo(ancho, alto);
        generarEstrellasFondo(ancho, alto);
    }

    public void generarEstrellasFondo(double ancho, double alto) {
        parallax.actualizarDimensiones(ancho, alto);
        recursos.capaEstrellas.generar(ancho, alto);
    }

    public void dibujarFondo(GraphicsContext gc, double anchoCanvas, double altoCanvas, long tick) {
        gc.setFill(COLOR_FONDO_ESPACIAL);
        gc.fillRect(0, 0, anchoCanvas, altoCanvas);

        parallax.normalizar(anchoCanvas, altoCanvas);
        Image fondo = getImagenFondo();
        if (fondo != null) {
            dibujarImagenFondoEnMosaico(gc, fondo, anchoCanvas, altoCanvas);
        } else {
            dibujarNebulosas(gc, anchoCanvas, altoCanvas, tick);
        }
    }

    private void dibujarImagenFondoEnMosaico(GraphicsContext gc, Image fondo, double ancho, double alto) {
        double x1 = parallax.getDesplazamientoFondoX();
        double x2 = x1 - ancho;
        double y1 = parallax.getDesplazamientoFondoY();
        double y2 = y1 - alto;
        double anchoTile = ancho + 1;
        double altoTile = y1 > 0.0 ? alto + 1 : alto;

        gc.drawImage(fondo, x1, y1, anchoTile, altoTile);
        gc.drawImage(fondo, x2, y1, anchoTile, altoTile);
        if (y1 > 0.0) {
            gc.drawImage(fondo, x1, y2, anchoTile, altoTile);
            gc.drawImage(fondo, x2, y2, anchoTile, altoTile);
        }
    }

    public void dibujarNebulosas(GraphicsContext gc, double anchoCanvas, double altoCanvas, long tick) {
        double offsetX = parallax.getDesplazamientoFondoX();
        double offsetY = parallax.getDesplazamientoFondoY();
        gc.setFill(COLOR_NEBULOSA_1);
        gc.fillOval(parallax.normalizarEnRango(anchoCanvas * 0.1 + offsetX, anchoCanvas),
                parallax.normalizarEnRango(altoCanvas * 0.15 + offsetY, altoCanvas), 450, 300);
        gc.setFill(COLOR_NEBULOSA_2);
        gc.fillOval(parallax.normalizarEnRango(anchoCanvas * 0.65 + offsetX, anchoCanvas),
                parallax.normalizarEnRango(altoCanvas * 0.55 + offsetY, altoCanvas), 550, 380);
    }

    public void dibujarGrilla(GraphicsContext gc, double anchoCanvas, double altoCanvas) {
        dibujarGrilla(gc, anchoCanvas, altoCanvas, 1.0);
    }

    public void dibujarGrilla(GraphicsContext gc, double anchoCanvas, double altoCanvas, double nivelZoom) {
        dibujarGrilla(gc, anchoCanvas, altoCanvas, FABRICA.crearProyeccionCamara(nivelZoom));
    }

    public void dibujarGrilla(GraphicsContext gc, double anchoCanvas, double altoCanvas, ProyeccionCamara camara) {
        recursos.capaGrilla.dibujar(gc, anchoCanvas, altoCanvas, camara);
    }

    public void dibujarEstrellasFondo(GraphicsContext gc, long tick) {
        recursos.capaEstrellas.dibujar(gc, tick, parallax);
    }

    public void avanzarDesplazamiento(double deltaTime, double multiplicadorVelocidad, double anchoCanvas) {
        parallax.avanzarDesplazamiento(deltaTime, multiplicadorVelocidad, anchoCanvas);
    }

    public void actualizarDesplazamientoCamara(DesplazamientoCamara desplazamiento, double nivelZoom) {
        if (desplazamiento == null) {
            parallax.actualizarDesplazamientoCamara(0.0, 0.0, nivelZoom);
        } else {
            parallax.actualizarDesplazamientoCamara(desplazamiento.getX(), desplazamiento.getY(), nivelZoom);
        }
    }

    public void actualizarDesplazamientoCamara(double camaraX, double camaraY, double nivelZoom) {
        parallax.actualizarDesplazamientoCamara(camaraX, camaraY, nivelZoom);
    }

    public double calcularOffsetParallaxX() { return parallax.calcularOffsetParallaxX(); }
    public double calcularOffsetParallaxY() { return parallax.calcularOffsetParallaxY(); }
    public void normalizarDesplazamiento(double ancho, double alto) { parallax.normalizar(ancho, alto); }
    public void normalizarDesplazamiento(double ancho) { parallax.normalizar(ancho); }
    public void normalizarDesplazamiento() { parallax.normalizar(); }
    public double getDesplazamientoFondoX() { return parallax.getDesplazamientoFondoX(); }
    public void setDesplazamientoFondoX(double d) { parallax.setDesplazamientoFondoX(d); }
    public double getDesplazamientoFondoY() { return parallax.getDesplazamientoFondoY(); }
    public void setDesplazamientoFondoY(double d) { parallax.setDesplazamientoFondoY(d); }
    public double getFactorParallax() { return parallax.getFactorParallax(); }
    public void setFactorParallax(double f) { parallax.setFactorParallax(f); }
    public ControlZoom getCamaraZoom() { return parallax.getCamaraZoom(); }
    public void setCamaraZoom(ControlZoom c) { parallax.setCamaraZoom(c); }
    public double getVelocidadBaseFondo() { return parallax.getVelocidadBaseFondo(); }
    public void setVelocidadBaseFondo(double v) { parallax.setVelocidadBaseFondo(v); }

    public Image getImagenFondo() {
        return recursos.resolverImagenFondo();
    }

    public void setImagenFondo(Image imagenFondo) {
        recursos.imagenFondo = imagenFondo;
    }
}
