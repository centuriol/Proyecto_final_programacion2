package org.example.game.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;
import org.example.game.render.EscenaFondoMenu;

/**
 * SRP: Gestiona el canvas animado, bucle de animación y satélites interactivos del menú principal.
 */
public class ControladorFondoMenu {

    private static class EstadoFondoMenu {
        final EscenaFondoMenu escenaFondo = new EscenaFondoMenu();
        final CampoSatelitesMenu campoSatelites = new CampoSatelitesMenu();
        final ResizableCanvas canvasFondo = new ResizableCanvas(1280.0, 720.0);
        boolean animacionActiva = false;
        long ultimoPulsoNs = 0L;

        void dibujar() {
            var gc = canvasFondo.getGraphicsContext2D();
            double ancho = canvasFondo.getWidth();
            double alto = canvasFondo.getHeight();
            escenaFondo.dibujar(gc, ancho, alto);
            campoSatelites.dibujar(gc, ancho, alto);
        }

        void avanzar(double deltaSegundos) {
            escenaFondo.avanzar(deltaSegundos);
            campoSatelites.avanzar(deltaSegundos);
            dibujar();
        }
    }

    private final EstadoFondoMenu estado = new EstadoFondoMenu();
    private final AnimationTimer bucleFondo;

    public ControladorFondoMenu() {
        this.bucleFondo = crearBucleFondo();
        configurarCanvas();
    }

    private void configurarCanvas() {
        estado.canvasFondo.setMouseTransparent(false);
        estado.canvasFondo.setOnMousePressed(evento -> {
            if (evento.getButton() == MouseButton.PRIMARY) {
                manejarClicSatelite(evento.getX(), evento.getY());
            }
        });
        estado.canvasFondo.widthProperty().addListener((obs, ant, act) -> estado.dibujar());
        estado.canvasFondo.heightProperty().addListener((obs, ant, act) -> estado.dibujar());
        estado.dibujar();
    }

    private AnimationTimer crearBucleFondo() {
        return new AnimationTimer() {
            @Override
            public void handle(long ahoraNs) {
                if (estado.ultimoPulsoNs == 0L) {
                    estado.ultimoPulsoNs = ahoraNs;
                    estado.dibujar();
                    return;
                }
                double deltaSeg = (ahoraNs - estado.ultimoPulsoNs) / 1_000_000_000.0;
                estado.ultimoPulsoNs = ahoraNs;
                estado.avanzar(deltaSeg);
            }
        };
    }

    public void sincronizarAnimacion(boolean debeAnimar) {
        if (!Platform.isFxApplicationThread()) return;
        if (debeAnimar && !estado.animacionActiva) {
            estado.ultimoPulsoNs = 0L;
            estado.animacionActiva = true;
            bucleFondo.start();
        } else if (!debeAnimar && estado.animacionActiva) {
            estado.animacionActiva = false;
            estado.ultimoPulsoNs = 0L;
            bucleFondo.stop();
        }
    }

    public void avanzarYRenderizar(double deltaSegundos) { estado.avanzar(deltaSegundos); }

    public boolean manejarClicSatelite(double x, double y) {
        boolean activado = estado.campoSatelites.manejarClic(x, y, estado.canvasFondo.getWidth(), estado.canvasFondo.getHeight());
        if (activado) estado.dibujar();
        return activado;
    }

    public void reiniciarSatelites() {
        estado.campoSatelites.reiniciar();
        estado.dibujar();
    }

    public void renderizar() { estado.dibujar(); }
    public boolean isAnimacionActiva() { return estado.animacionActiva; }
    public EscenaFondoMenu getEscenaFondo() { return estado.escenaFondo; }
    public CampoSatelitesMenu getCampoSatelites() { return estado.campoSatelites; }
    public ResizableCanvas getCanvasFondo() { return estado.canvasFondo; }
}
