package org.example.game.controlador;
import org.example.game.render.ControlZoom;
/**
 * Gestiona el gesto de mantener clic izquierdo y arrastrar para desplazar la camara
 * sin interferir con clicks simples de inspeccion ni con el modo de colocacion (SRP).
 */
public class ControlArrastreCamara {

    public static final double UMBRAL_ARRASTRE_PX = 3.0;

    private boolean activo;
    private boolean superoUmbralClick;
    private double inicioX;
    private double inicioY;
    private double ultimoX;
    private double ultimoY;

    public void iniciar(double pantallaX, double pantallaY) {
        this.activo = true;
        this.superoUmbralClick = false;
        this.inicioX = pantallaX;
        this.inicioY = pantallaY;
        this.ultimoX = pantallaX;
        this.ultimoY = pantallaY;
    }

    public boolean arrastrar(double pantallaX, double pantallaY, ControlZoom camara) {
        if (!activo || camara == null) {
            return false;
        }
        double deltaX = pantallaX - ultimoX;
        double deltaY = pantallaY - ultimoY;
        this.ultimoX = pantallaX;
        this.ultimoY = pantallaY;

        if (Math.hypot(pantallaX - inicioX, pantallaY - inicioY) >= UMBRAL_ARRASTRE_PX) {
            this.superoUmbralClick = true;
        }
        camara.desplazarPorPantalla(deltaX, deltaY);
        return true;
    }

    public boolean finalizarYEsClickSimple() {
        if (!activo) {
            return false;
        }
        boolean fueClickSimple = !superoUmbralClick;
        cancelar();
        return fueClickSimple;
    }

    public void cancelar() {
        this.activo = false;
        this.superoUmbralClick = false;
    }

    public boolean estaActivo() {
        return activo;
    }

    public boolean huboArrastreSignificativo() {
        return superoUmbralClick;
    }
}
