package org.example.game.render;

import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;

/**
 * Encapsula el desplazamiento bidimensional (pan) de la cámara acotado al área del mundo (SRP).
 */
public class DesplazamientoCamara {

    public static final double LIMITE_MUNDO_POR_DEFECTO = ConstantesFisicas.RADIO_MUNDO_DEFAULT;

    private double limiteMundo;
    private Vector2D posicion = Vector2D.cero();

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
        if (!Double.isFinite(deltaPantallaX) || !Double.isFinite(deltaPantallaY)) return;
        if (!Double.isFinite(nivelZoom) || nivelZoom <= 0.0) return;
        setPosicion(posicion.x() + deltaPantallaX / nivelZoom, posicion.y() + deltaPantallaY / nivelZoom);
    }

    public void setPosicion(double nuevoX, double nuevoY) {
        if (Double.isFinite(nuevoX) && Double.isFinite(nuevoY)) {
            this.posicion = new Vector2D(acotarAlMundo(nuevoX), acotarAlMundo(nuevoY));
        }
    }

    public void enfocarPuntoFisico(double xFisica, double yFisica) {
        setPosicion(-xFisica, yFisica);
    }

    public void setLimiteMundo(double nuevoLimite) {
        if (esLimiteValido(nuevoLimite)) {
            this.limiteMundo = nuevoLimite;
            this.posicion = new Vector2D(acotarAlMundo(posicion.x()), acotarAlMundo(posicion.y()));
        }
    }

    public double getLimiteMundo() { return limiteMundo; }
    public void centrar() { this.posicion = Vector2D.cero(); }
    public boolean esCentrada() { return Math.abs(posicion.x()) < 1e-4 && Math.abs(posicion.y()) < 1e-4; }
    public double getX() { return posicion.x(); }
    public double getY() { return posicion.y(); }
    public Vector2D comoVector() { return posicion; }

    private double acotarAlMundo(double valor) {
        return Math.max(-limiteMundo, Math.min(limiteMundo, valor));
    }

    private static boolean esLimiteValido(double valor) {
        return Double.isFinite(valor) && valor > 0.0;
    }
}
