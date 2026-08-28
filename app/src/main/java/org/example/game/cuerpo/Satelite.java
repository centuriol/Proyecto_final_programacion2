package org.example.game.cuerpo;

import org.example.CuerpoCeleste;

/**
 * Satélite artificial que recopila datos científicos y transmite energía.
 * SRP: Representa un artefacto tecnológico creado por el jugador o civilizaciones.
 */
public class Satelite extends CuerpoCeleste {

    private double cienciaGenerada = 0;
    private double energiaGenerada = 0;

    public Satelite(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.SATELITE);
    }

    /** Genera recursos pasivos periódicamente */
    public void producirRecursos(double dt) {
        cienciaGenerada += 0.05 * dt;
        energiaGenerada += 0.1 * dt;
    }

    public double getCienciaGenerada() {
        return cienciaGenerada;
    }

    public double getEnergiaGenerada() {
        return energiaGenerada;
    }

    @Override
    public String describir() {
        return "Satélite " + getNombre() + " (Ciencia: +" + String.format("%.1f", cienciaGenerada) + ")";
    }

    @Override
    public double getRadio() {
        return 7.0;
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.SATELITE.getColorHexString();
    }
}
