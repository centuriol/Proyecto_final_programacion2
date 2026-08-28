package org.example.game.motor;

/**
 * Vector 2D inmutable para cálculos físicos.
 * Usa double para precisión en simulaciones orbitales.
 */
public final class Vector2D {
    public final double x;
    public final double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2D cero() {
        return new Vector2D(0, 0);
    }

    public Vector2D sumar(Vector2D otro) {
        return new Vector2D(this.x + otro.x, this.y + otro.y);
    }

    public Vector2D restar(Vector2D otro) {
        return new Vector2D(this.x - otro.x, this.y - otro.y);
    }

    public Vector2D multiplicar(double escalar) {
        return new Vector2D(this.x * escalar, this.y * escalar);
    }

    public Vector2D dividir(double escalar) {
        return new Vector2D(this.x / escalar, this.y / escalar);
    }

    public double magnitud() {
        return Math.hypot(x, y);
    }

    public double magnitudCuadrado() {
        return x * x + y * y;
    }

    public Vector2D normalizar() {
        double mag = magnitud();
        if (mag == 0) return Vector2D.cero();
        return dividir(mag);
    }

    public double productoPunto(Vector2D otro) {
        return this.x * otro.x + this.y * otro.y;
    }

    public double distanciaA(Vector2D otro) {
        return this.restar(otro).magnitud();
    }

    public double distanciaCuadradoA(Vector2D otro) {
        return this.restar(otro).magnitudCuadrado();
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.3f, %.3f)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector2D)) return false;
        Vector2D otro = (Vector2D) obj;
        return Double.compare(otro.x, x) == 0 && Double.compare(otro.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}