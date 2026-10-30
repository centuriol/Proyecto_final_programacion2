package org.example.game.cuerpo;

import org.example.game.cuerpo.CuerpoCeleste;

/**
 * Satélite artificial que recopila datos científicos y transmite energía.
 * SRP: Representa un artefacto tecnológico creado por el jugador o civilizaciones.
 */
public class Satelite extends CuerpoCeleste implements Actualizable {

    private double cienciaGenerada = 0;
    private double energiaGenerada = 0;

    public Satelite(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.SATELITE);
    }

    @Override
    public void actualizarEntidad(double dt) {
        producirRecursos(dt);
    }

    /** Genera recursos pasivos periódicamente (ciencia y energía) */
    public void producirRecursos(double dt) {
        cienciaGenerada += org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA * dt;
        energiaGenerada += org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA * dt;
    }

    public double getCienciaGenerada() {
        return cienciaGenerada;
    }

    public double getEnergiaGenerada() {
        return energiaGenerada;
    }

    @Override
    public String describir() {
        return "Satélite " + getNombre() + " (Ciencia: +" + String.format("%.1f", cienciaGenerada) + ", Energía: +" + String.format("%.1f", energiaGenerada) + ")";
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
