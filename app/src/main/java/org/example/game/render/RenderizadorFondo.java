package org.example.game.render;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * Responsable exclusivo de dibujar el fondo espacial, nebulosas procedimentales,
 * grilla opcional y estrellas titilantes.
 *
 * Principios SOLID:
 * - SRP: Encapsula exclusivamente la representacion grafica del fondo del cosmos.
 */
public class RenderizadorFondo {

    private static final Color COLOR_FONDO_ESPACIAL = Color.web("#0e1017");
    private static final Color COLOR_GRILLA_LINEA = Color.web("#2b2d3a");
    private static final Color COLOR_GRILLA_EJE = Color.web("#5be3ff");
    private static final Color COLOR_NEBULOSA_1 = Color.rgb(40, 20, 60, 0.22);
    private static final Color COLOR_NEBULOSA_2 = Color.rgb(15, 45, 65, 0.18);
    private static final double FACTOR_PARALLAX_POR_DEFECTO = 0.4;

    private Image imagenFondo;
    private final RepositorioSprites repositorioSprites;
    private ControlZoom camara;
    private double desplazamientoFondoX = 0.0;
    private double desplazamientoFondoY = 0.0;
    private double velocidadBaseFondo = 1.0;
    private double factorParallax = FACTOR_PARALLAX_POR_DEFECTO;
    private double anchoCanvas;
    private double altoCanvas;

    private final List<EstrellaFondo> estrellasFondo = new ArrayList<>();

    public static class EstrellaFondo {
        final double x, y, size;
        final double brilloBase;
        final double velocidadParpadeo;

        EstrellaFondo(double x, double y, double size, double brilloBase, double vel) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.brilloBase = brilloBase;
            this.velocidadParpadeo = vel;
        }
    }

    public RenderizadorFondo(double ancho, double alto) {
        this(ancho, alto, new GestorImagenes());
    }

    public RenderizadorFondo(double ancho, double alto, RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites != null ? repositorioSprites : new GestorImagenes();
        this.imagenFondo = this.repositorioSprites.obtenerImagenFondo();
        this.anchoCanvas = Math.max(0.0, ancho);
        this.altoCanvas = Math.max(0.0, alto);
        generarEstrellasFondo(ancho, alto);
    }

    public void generarEstrellasFondo(double ancho, double alto) {
        if (ancho > 0) {
            this.anchoCanvas = ancho;
        }
        if (alto > 0) {
            this.altoCanvas = alto;
        }
        estrellasFondo.clear();
        Random rand = new Random(42);
        for (int i = 0; i < 180; i++) {
            double x = rand.nextDouble() * ancho;
            double y = rand.nextDouble() * alto;
            double size = rand.nextDouble() < 0.8 ? 1.5 : (rand.nextDouble() < 0.9 ? 2.5 : 3.5);
            double brillo = 0.2 + rand.nextDouble() * 0.6;
            double vel = 0.5 + rand.nextDouble() * 2.0;
            estrellasFondo.add(new EstrellaFondo(x, y, size, brillo, vel));
        }
    }

    public void dibujarFondo(GraphicsContext gc, double anchoCanvas, double altoCanvas, long tick) {
        dibujarFondo(gc, anchoCanvas, altoCanvas, tick, this.camara);
    }

    public void dibujarFondo(GraphicsContext gc, double anchoCanvas, double altoCanvas, long tick, ControlZoom camara) {
        if (camara != null) {
            this.camara = camara;
        }
        gc.setFill(COLOR_FONDO_ESPACIAL);
        gc.fillRect(0, 0, anchoCanvas, altoCanvas);

        Image fondo = getImagenFondo();
        if (fondo != null) {
            normalizarDesplazamiento(anchoCanvas, altoCanvas);
            dibujarImagenFondoConWrap(gc, fondo, anchoCanvas, altoCanvas);
        } else {
            normalizarDesplazamiento(anchoCanvas, altoCanvas);
            dibujarNebulosas(gc, anchoCanvas, altoCanvas, tick);
        }
    }

    private void dibujarImagenFondoConWrap(GraphicsContext gc, Image fondo, double ancho, double alto) {
        double x1 = getDesplazamientoFondoX();
        double x2 = x1 - ancho;
        double y1 = getDesplazamientoFondoY();

        if (y1 <= 1e-6) {
            gc.drawImage(fondo, x1, 0, ancho + 1, alto);
            gc.drawImage(fondo, x2, 0, ancho + 1, alto);
        } else {
            double y2 = y1 - alto;
            gc.drawImage(fondo, x1, y1, ancho + 1, alto + 1);
            gc.drawImage(fondo, x2, y1, ancho + 1, alto + 1);
            gc.drawImage(fondo, x1, y2, ancho + 1, alto + 1);
            gc.drawImage(fondo, x2, y2, ancho + 1, alto + 1);
        }
    }

    public void dibujarNebulosas(GraphicsContext gc, double anchoCanvas, double altoCanvas, long tick) {
        double despX = getDesplazamientoFondoX();
        double despY = getDesplazamientoFondoY();
        double x1 = normalizarValor(anchoCanvas * 0.1 + despX, anchoCanvas);
        double y1 = normalizarValor(altoCanvas * 0.15 + despY, altoCanvas);
        double x2 = normalizarValor(anchoCanvas * 0.65 + despX, anchoCanvas);
        double y2 = normalizarValor(altoCanvas * 0.55 + despY, altoCanvas);

        gc.setFill(COLOR_NEBULOSA_1);
        gc.fillOval(x1, y1, 450, 300);

        gc.setFill(COLOR_NEBULOSA_2);
        gc.fillOval(x2, y2, 550, 380);
    }

    public void dibujarGrilla(GraphicsContext gc, double anchoCanvas, double altoCanvas) {
        dibujarGrilla(gc, anchoCanvas, altoCanvas, 1.0);
    }

    public void dibujarGrilla(GraphicsContext gc, double anchoCanvas, double altoCanvas, double nivelZoom) {
        CamaraZoom temporal = new CamaraZoom(nivelZoom, CamaraZoom.INCREMENTO_POR_DEFECTO);
        dibujarGrilla(gc, anchoCanvas, altoCanvas, temporal);
    }

    public void dibujarGrilla(GraphicsContext gc, double anchoCanvas, double altoCanvas, CamaraZoom camara) {
        CamaraZoom cam = camara != null ? camara : new CamaraZoom();
        double minX = cam.pantallaAMundoX(0.0, anchoCanvas);
        double maxX = cam.pantallaAMundoX(anchoCanvas, anchoCanvas);
        double minY = cam.pantallaAMundoY(0.0, altoCanvas);
        double maxY = cam.pantallaAMundoY(altoCanvas, altoCanvas);

        gc.setStroke(COLOR_GRILLA_LINEA);
        gc.setLineWidth(1.0);
        gc.setGlobalAlpha(0.25);

        int tamanoCelda = 32;
        double inicioX = Math.floor(minX / tamanoCelda) * tamanoCelda;
        double inicioY = Math.floor(minY / tamanoCelda) * tamanoCelda;
        for (double x = inicioX; x <= maxX; x += tamanoCelda) {
            gc.strokeLine(x, minY, x, maxY);
        }
        for (double y = inicioY; y <= maxY; y += tamanoCelda) {
            gc.strokeLine(minX, y, maxX, y);
        }

        gc.setStroke(COLOR_GRILLA_EJE);
        gc.setGlobalAlpha(0.18);
        double ejeX = anchoCanvas / 2.0;
        double ejeY = altoCanvas / 2.0;
        if (ejeX >= minX && ejeX <= maxX) {
            gc.strokeLine(ejeX, minY, ejeX, maxY);
        }
        if (ejeY >= minY && ejeY <= maxY) {
            gc.strokeLine(minX, ejeY, maxX, ejeY);
        }

        gc.setGlobalAlpha(1.0);
    }

    public void dibujarEstrellasFondo(GraphicsContext gc, long tick) {
        double despX = getDesplazamientoFondoX();
        double despY = getDesplazamientoFondoY();
        for (EstrellaFondo ef : estrellasFondo) {
            double alpha = ef.brilloBase + 0.25 * Math.sin(tick * 0.05 * ef.velocidadParpadeo);
            alpha = Math.max(0.1, Math.min(1.0, alpha));
            double x = anchoCanvas > 0 ? normalizarValor(ef.x + despX, anchoCanvas) : ef.x + despX;
            double y = altoCanvas > 0 ? normalizarValor(ef.y + despY, altoCanvas) : ef.y + despY;

            gc.setFill(Color.color(0.91, 0.89, 0.85, alpha));
            gc.fillRect((int) x, (int) y, (int) ef.size, (int) ef.size);
        }
    }

    public void avanzarDesplazamiento(double deltaTime, double multiplicadorVelocidad, double anchoCanvas) {
        if (anchoCanvas <= 0 || deltaTime <= 0 || multiplicadorVelocidad <= 0) {
            return;
        }
        this.anchoCanvas = anchoCanvas;
        double deltaX = velocidadBaseFondo * multiplicadorVelocidad * deltaTime;
        desplazamientoFondoX += deltaX;
        normalizarDesplazamiento(anchoCanvas);
    }

    public void normalizarDesplazamiento(double ancho) {
        if (ancho > 0) {
            this.anchoCanvas = ancho;
            desplazamientoFondoX = normalizarValor(desplazamientoFondoX, ancho);
        }
    }

    public void normalizarDesplazamiento(double ancho, double alto) {
        normalizarDesplazamiento(ancho);
        if (alto > 0) {
            this.altoCanvas = alto;
            desplazamientoFondoY = normalizarValor(desplazamientoFondoY, alto);
        }
    }

    public void normalizarDesplazamiento() {
        normalizarDesplazamiento(this.anchoCanvas, this.altoCanvas);
    }

    public double calcularParallaxCamaraX() {
        if (camara == null || camara.getDesplazamiento() == null) {
            return 0.0;
        }
        return camara.getDesplazamiento().getX() * camara.getNivelZoom() * factorParallax;
    }

    public double calcularParallaxCamaraY() {
        if (camara == null || camara.getDesplazamiento() == null) {
            return 0.0;
        }
        return camara.getDesplazamiento().getY() * camara.getNivelZoom() * factorParallax;
    }

    public double getDesplazamientoFondoX() {
        return normalizarValor(desplazamientoFondoX + calcularParallaxCamaraX(), anchoCanvas);
    }

    public void setDesplazamientoFondoX(double desplazamiento) {
        this.desplazamientoFondoX = normalizarValor(desplazamiento, this.anchoCanvas);
    }

    public double getDesplazamientoFondoY() {
        return normalizarValor(desplazamientoFondoY + calcularParallaxCamaraY(), altoCanvas);
    }

    public double getFactorParallax() {
        return factorParallax;
    }

    public void setFactorParallax(double factorParallax) {
        if (Double.isFinite(factorParallax) && factorParallax >= 0.0 && factorParallax <= 1.0) {
            this.factorParallax = factorParallax;
        }
    }

    public void setCamara(ControlZoom camara) {
        this.camara = camara;
    }

    public double getVelocidadBaseFondo() { return velocidadBaseFondo; }
    public void setVelocidadBaseFondo(double vel) { this.velocidadBaseFondo = vel; }

    public Image getImagenFondo() {
        if (imagenFondo == null) {
            imagenFondo = repositorioSprites.obtenerImagenFondo();
        }
        return imagenFondo;
    }
    public void setImagenFondo(Image imagenFondo) { this.imagenFondo = imagenFondo; }

    private double normalizarValor(double valor, double dimension) {
        if (!Double.isFinite(valor)) {
            return 0.0;
        }
        if (!Double.isFinite(dimension) || dimension <= 0.0) {
            return valor;
        }
        return (valor % dimension + dimension) % dimension;
    }
}
