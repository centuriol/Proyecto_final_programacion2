package org.example.game.cuerpo;

import org.example.CuerpoCeleste;

/**
 * Cúpula de escudo energético que protege sectores del espacio y desvía meteoritos.
 * SRP: Representa una estructura de defensa energética orbital.
 */
public class EscudoProtector extends CuerpoCeleste {

    private double energiaRestante = 100.0;
    private final double radioEscudo = 65.0; // Radio visual/efectivo de protección
    private double pulsoVisual = 0.0;

    public EscudoProtector(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.ESCUDO_DOME);
    }

    public boolean puedeAbsorberImpacto() {
        return energiaRestante > 0;
    }

    public void absorberDano(double dano) {
        this.energiaRestante = Math.max(0, this.energiaRestante - dano);
    }

    public void recargarEnergia(double cantidad) {
        this.energiaRestante = Math.min(100.0, this.energiaRestante + cantidad);
    }

    public double getEnergiaRestante() {
        return energiaRestante;
    }

    public double getRadioEscudo() {
        return radioEscudo;
    }

    public double getPulsoVisual() {
        return pulsoVisual;
    }

    public void avanzarPulso(double dt) {
        this.pulsoVisual = (this.pulsoVisual + dt * 2.0) % (Math.PI * 2);
    }

    @Override
    public String describir() {
        return "Escudo " + getNombre() + " (Energía: " + String.format("%.0f%%", energiaRestante) + ")";
    }

    @Override
    public double getRadio() {
        return 12.0;
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.ESCUDO_DOME.getColorHexString();
    }
}
