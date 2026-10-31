package org.example.game.motor;
/**
 * Vector 2D inmutable para cálculos físicos.
 * Usa double para precisión en simulaciones orbitales.
 * Implementado como Java 17 record.
 */
public record Vector2D(double x, double y) {

    public static Vector2D cero() {
        return new Vector2D(0, 0);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
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

    public Vector2D interpolar(Vector2D destino, double t) {
        return new Vector2D(this.x + (destino.x - this.x) * t, this.y + (destino.y - this.y) * t);
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.3f, %.3f)", x, y);
    }
}
