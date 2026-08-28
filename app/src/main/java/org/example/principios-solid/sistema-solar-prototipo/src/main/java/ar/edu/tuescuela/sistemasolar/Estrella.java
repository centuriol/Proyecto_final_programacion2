package org.example;

import org.example.game.cuerpo.TipoCuerpo;

public class Estrella extends CuerpoCeleste {
    public Estrella(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.ESTRELLA);
    }

    @Override
    public String describir() {
        return "Estrella " + getNombre() + " (masa: " + String.format("%.2e", getMasa()) + " kg)";
    }

    @Override
    public double getRadio() {
        return 35;
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.ESTRELLA.getColorHexString();
    }
}
