package org.example.game.jugador;
/**
 * Cantidad de un recurso específico.
 */
public final class Recurso {
    private static final double TOLERANCIA_PRECISION = 1.0e-9;

    private final TipoRecurso tipo;
    private double cantidad;

    public Recurso(TipoRecurso tipo, double cantidadInicial) {
        this.tipo = tipo;
        this.cantidad = Math.max(0, cantidadInicial);
    }

    public Recurso(TipoRecurso tipo) {
        this(tipo, 0);
    }

    public TipoRecurso getTipo() { return tipo; }
    public double getCantidad() { return cantidad; }

    public void agregar(double cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad a agregar no puede ser negativa: " + cantidad);
        }
        this.cantidad += cantidad;
    }

    public boolean gastar(double cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad a gastar no puede ser negativa: " + cantidad);
        }
        if (this.cantidad + TOLERANCIA_PRECISION >= cantidad) {
            this.cantidad = Math.max(0.0, this.cantidad - cantidad);
            return true;
        }
        return false;
    }

    public boolean puedeGastar(double cantidad) {
        if (cantidad < 0) {
            return false;
        }
        return this.cantidad + TOLERANCIA_PRECISION >= cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = Math.max(0, cantidad);
    }

    @Override
    public String toString() {
        return String.format("%s %.1f", tipo.icono, cantidad);
    }
}
