package org.example.game.render;
import org.example.game.motor.ConstantesFisicas; import org.example.game.motor.Vector2D;
/**
 * Encapsula el desplazamiento bidimensional (pan) de la camara en unidades del mundo,
 * acotado a los limites del area del mundo para evitar perderse en el espacio infinito.
 *
 * Principios SOLID:
 * - SRP: Gestiona exclusivamente las coordenadas de traslacion de vista, sus limites y su conversion segun el zoom.
 */
public class DesplazamientoCamara {

    public static final double LIMITE_MUNDO_POR_DEFECTO = ConstantesFisicas.RADIO_MUNDO_DEFAULT;

    private double limiteMundo;
    private double x;
    private double y;

    public DesplazamientoCamara() {
        this(0.0, 0.0, LIMITE_MUNDO_POR_DEFECTO);
    }

    public DesplazamientoCamara(double xInicial, double yInicial) {
        this(xInicial, yInicial, LIMITE_MUNDO_POR_DEFECTO);
    }

    public DesplazamientoCamara(double xInicial, double yInicial, double limiteMundo) {
        this.limiteMundo = esLimiteValido(limiteMundo) ? limiteMundo : LIMITE_MUNDO_POR_DEFECTO;
        setPosicion(xInicial, yInicial);
    }

    public void desplazarPorPantalla(double deltaPantallaX, double deltaPantallaY, double nivelZoom) {
        if (!Double.isFinite(deltaPantallaX) || !Double.isFinite(deltaPantallaY)) {
            return;
        }
        if (!Double.isFinite(nivelZoom) || nivelZoom <= 0.0) {
            return;
        }
        setPosicion(this.x + deltaPantallaX / nivelZoom, this.y + deltaPantallaY / nivelZoom);
    }

    public void setPosicion(double nuevoX, double nuevoY) {
        if (Double.isFinite(nuevoX) && Double.isFinite(nuevoY)) {
            this.x = acotarAlMundo(nuevoX);
            this.y = acotarAlMundo(nuevoY);
        }
    }

    public void enfocarPuntoFisico(double xFisica, double yFisica) {
        setPosicion(-xFisica, yFisica);
    }

    public void setLimiteMundo(double nuevoLimite) {
        if (esLimiteValido(nuevoLimite)) {
            this.limiteMundo = nuevoLimite;
            this.x = acotarAlMundo(this.x);
            this.y = acotarAlMundo(this.y);
        }
    }

    public double getLimiteMundo() {
        return limiteMundo;
    }

    public void centrar() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public boolean esCentrada() {
        return Math.abs(x) < 1e-4 && Math.abs(y) < 1e-4;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2D comoVector() {
        return new Vector2D(x, y);
    }

    private double acotarAlMundo(double valor) {
        return Math.max(-limiteMundo, Math.min(limiteMundo, valor));
    }

    private static boolean esLimiteValido(double valor) {
        return Double.isFinite(valor) && valor > 0.0;
    }
}
