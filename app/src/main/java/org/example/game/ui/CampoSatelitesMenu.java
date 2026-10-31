package org.example.game.ui;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.example.game.motor.Vector2D;
import org.example.game.render.GestorImagenes;
import org.example.game.render.RepositorioSprites;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * SRP / DIP: Conjunto decorativo exclusivo del menu principal compuesto por cinco satelites
 * a la izquierda y cinco a la derecha. Renderiza cada satelite con su sprite pixel art girando
 * sobre su propio eje y gestiona el clic para iniciar su caida gravitatoria fuera de pantalla.
 */
public class CampoSatelitesMenu {

    public static final int SATELITES_POR_LADO = 5;
    public static final int TOTAL_SATELITES = SATELITES_POR_LADO * 2;
    public static final String NOMBRE_SPRITE_SATELITE = "satelite";

    private final RepositorioSprites repositorioSprites;
    private final List<SateliteDecorativoMenu> satelites = new ArrayList<>(TOTAL_SATELITES);

    public CampoSatelitesMenu() {
        this(new GestorImagenes());
    }

    public CampoSatelitesMenu(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites != null ? repositorioSprites : new GestorImagenes();
        double[][] izquierda = {
                {0.10, 0.16, 46.0}, {0.23, 0.30, -38.0}, {0.07, 0.50, 54.0}, {0.24, 0.70, -42.0}, {0.11, 0.84, 50.0}
        };
        double[][] derecha = {
                {0.90, 0.16, -46.0}, {0.77, 0.30, 38.0}, {0.93, 0.50, -54.0}, {0.76, 0.70, 42.0}, {0.89, 0.84, -50.0}
        };
        for (double[] s : izquierda) {
            satelites.add(new SateliteDecorativoMenu(SateliteDecorativoMenu.Lado.IZQUIERDA, s[0], s[1], s[2]));
        }
        for (double[] s : derecha) {
            satelites.add(new SateliteDecorativoMenu(SateliteDecorativoMenu.Lado.DERECHA, s[0], s[1], s[2]));
        }
    }

    public void avanzar(double deltaSegundos) {
        for (SateliteDecorativoMenu satelite : satelites) {
            satelite.avanzar(deltaSegundos);
        }
    }

    public boolean manejarClic(double x, double y, double anchoCanvas, double altoCanvas) {
        for (int i = satelites.size() - 1; i >= 0; i--) {
            SateliteDecorativoMenu satelite = satelites.get(i);
            if (!satelite.getEstado().cayendo() && satelite.contienePunto(x, y, anchoCanvas, altoCanvas)) {
                satelite.iniciarCaida();
                return true;
            }
        }
        return false;
    }

    public void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas) {
        if (gc == null || anchoCanvas <= 0.0 || altoCanvas <= 0.0) {
            return;
        }
        Image sprite = repositorioSprites.obtenerImagenPorNombre(NOMBRE_SPRITE_SATELITE);
        for (SateliteDecorativoMenu satelite : satelites) {
            if (!satelite.estaFueraDePantalla(altoCanvas)) {
                dibujarSatelite(gc, satelite, sprite, satelite.calcularCentro(anchoCanvas, altoCanvas));
            }
        }
    }

    private void dibujarSatelite(GraphicsContext gc,
                                 SateliteDecorativoMenu satelite,
                                 Image sprite,
                                 Vector2D centro) {
        double mitad = SateliteDecorativoMenu.TAMANO_VISUAL_PX * 0.5;
        gc.save();
        gc.translate(centro.x(), centro.y());
        gc.rotate(satelite.getEstado().anguloGrados());
        gc.setImageSmoothing(false);
        if (sprite != null && !sprite.isError()) {
            gc.drawImage(sprite, -mitad, -mitad, mitad * 2.0, mitad * 2.0);
        } else {
            gc.setFill(Color.web("#2b6cb0"));
            gc.fillRect(-mitad, -mitad * 0.35, mitad * 2.0, mitad * 0.70);
        }
        gc.restore();
    }

    public void reiniciar() {
        for (SateliteDecorativoMenu satelite : satelites) {
            satelite.reiniciar();
        }
    }

    public List<SateliteDecorativoMenu> obtenerSatelites(SateliteDecorativoMenu.Lado lado) {
        if (lado == null) {
            return Collections.unmodifiableList(satelites);
        }
        return satelites.stream().filter(s -> s.getEstado().lado() == lado).toList();
    }
}
