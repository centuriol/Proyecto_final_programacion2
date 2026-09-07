package org.example.game.cuerpo;

import org.example.CuerpoCeleste;

/**
 * Luna craterizada que puede orbitar planetas o estrellas.
 * SRP: Representa las propiedades y comportamiento de un satélite natural.
 */
public class Luna extends CuerpoCeleste {

    public Luna(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.LUNA);
    }

    @Override
    public String describir() {
        return "Luna " + getNombre() + " (masa: " + TipoCuerpo.formatearMasa(getMasa()) + ")";
    }

    @Override
    public double getRadio() {
        double factor = getMasa() / TipoCuerpo.LUNA.masaBase;
        if (factor <= 0) return 2.0;
        return Math.max(2.0, 9.0 * Math.cbrt(factor));
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.LUNA.getColorHexString();
    }
}
