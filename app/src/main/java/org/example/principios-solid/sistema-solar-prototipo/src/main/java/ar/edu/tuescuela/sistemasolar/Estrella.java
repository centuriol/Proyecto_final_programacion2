package org.example;

import org.example.game.cuerpo.TipoCuerpo;

public class Estrella extends CuerpoCeleste {
    public Estrella(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.ESTRELLA);
    }

    @Override
    public String describir() {
        return "Estrella " + getNombre() + " (masa: " + TipoCuerpo.formatearMasa(getMasa()) + ")";
    }

    @Override
    public double getRadio() {
        double factor = getMasa() / TipoCuerpo.ESTRELLA.masaBase;
        if (factor <= 0) return 5.0;
        return Math.max(5.0, 35.0 * Math.cbrt(factor));
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.ESTRELLA.getColorHexString();
    }
}
