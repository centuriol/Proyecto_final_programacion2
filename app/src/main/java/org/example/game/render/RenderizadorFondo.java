package org.example.game.render;
import javafx.scene.canvas.GraphicsContext; import javafx.scene.image.Image; import javafx.scene.paint.Color; import java.util.ArrayList; import java.util.List; import java.util.Random;
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

    private Image imagenFondo;
    private double desplazamientoFondoX = 0.0;
    private double velocidadBaseFondo = 1.0;

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
        this.imagenFondo = GestorImagenes.getImagenFondo();
        generarEstrellasFondo(ancho, alto);
    }

    public void generarEstrellasFondo(double ancho, double alto) {
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
        gc.setFill(COLOR_FONDO_ESPACIAL);
        gc.fillRect(0, 0, anchoCanvas, altoCanvas);

        Image fondo = getImagenFondo();
        if (fondo != null) {
            double x1 = desplazamientoFondoX;
            double x2 = desplazamientoFondoX - anchoCanvas;
            gc.drawImage(fondo, x1, 0, anchoCanvas + 1, altoCanvas);
            gc.drawImage(fondo, x2, 0, anchoCanvas + 1, altoCanvas);
        } else {
            dibujarNebulosas(gc, anchoCanvas, altoCanvas, tick);
        }
    }

    public void dibujarNebulosas(GraphicsContext gc, double anchoCanvas, double altoCanvas, long tick) {
        gc.setFill(COLOR_NEBULOSA_1);
        gc.fillOval(anchoCanvas * 0.1, altoCanvas * 0.15, 450, 300);

        gc.setFill(COLOR_NEBULOSA_2);
        gc.fillOval(anchoCanvas * 0.65, altoCanvas * 0.55, 550, 380);
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
        for (EstrellaFondo ef : estrellasFondo) {
            double alpha = ef.brilloBase + 0.25 * Math.sin(tick * 0.05 * ef.velocidadParpadeo);
            alpha = Math.max(0.1, Math.min(1.0, alpha));

            gc.setFill(Color.color(0.91, 0.89, 0.85, alpha));
            gc.fillRect((int) ef.x, (int) ef.y, (int) ef.size, (int) ef.size);
        }
    }

    public void avanzarDesplazamiento(double deltaTime, double multiplicadorVelocidad, double anchoCanvas) {
        if (anchoCanvas > 0) {
            double deltaX = velocidadBaseFondo * multiplicadorVelocidad * deltaTime;
            desplazamientoFondoX = (desplazamientoFondoX + deltaX) % anchoCanvas;
            if (desplazamientoFondoX < 0) {
                desplazamientoFondoX += anchoCanvas;
            }
        }
    }

    public double getDesplazamientoFondoX() { return desplazamientoFondoX; }
    public void setDesplazamientoFondoX(double desplazamiento) { this.desplazamientoFondoX = desplazamiento; }
    public double getVelocidadBaseFondo() { return velocidadBaseFondo; }
    public void setVelocidadBaseFondo(double vel) { this.velocidadBaseFondo = vel; }

    public Image getImagenFondo() {
        if (imagenFondo == null) {
            imagenFondo = GestorImagenes.getImagenFondo();
        }
        return imagenFondo;
    }
    public void setImagenFondo(Image imagenFondo) { this.imagenFondo = imagenFondo; }
}
