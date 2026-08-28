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
        return "Luna " + getNombre() + " (masa: " + String.format("%.2e", getMasa()) + " kg)";
    }

    @Override
    public double getRadio() {
        return 9.0;
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.LUNA.getColorHexString();
    }
}
